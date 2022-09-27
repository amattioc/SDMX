/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or - as soon they
* will be approved by the European Commission - subsequent
* versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the
* Licence.
* You may obtain a copy of the Licence at:
*
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in
* writing, software distributed under the Licence is
* distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied.
* See the Licence for the specific language governing
* permissions and limitations under the Licence.
*/
/**
 * 
 */
package it.bancaditalia.oss.sdmx.parser.v21;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.DoubleObservation;
import it.bancaditalia.oss.sdmx.api.Message;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.api.SdmxAttribute;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LanguagePriorityList;
import it.bancaditalia.oss.sdmx.util.LocalizedText;

/**
 * @author Attilio Mattiocco
 *
 */
public class CompactDataParser implements Parser<DataParsingResult>
{
	private static final String		sourceClass	= CompactDataParser.class.getSimpleName();
	protected static final Logger	logger		= Configuration.getSdmxLogger();

	private static final String		DATASET		= "DataSet";
	private static final String		ACTION		= "action";
	private static final String		VALID_FROM	= "validFromDate";
	private static final String		VALID_TO	= "validToDate";
	private static final String		SERIES		= "Series";
	private static final String		OBS			= "Obs";

	private static final String		FOOTER		= "Footer";
	private static final String		MESSAGE		= "Message";
	private static final String		CODE		= "code";
	private static final String		SEVERITY	= "severity";
	private static final String		TEXT		= "Text";

	private DataFlowStructure		dsd;
	private Dataflow				dataflow;
	private boolean					data;

	public CompactDataParser(DataFlowStructure dsd, Dataflow dataflow, boolean data)
	{
		this.dsd = dsd;
		this.dataflow = dataflow;
		this.data = data;
	}

	@Override
	public DataParsingResult parse(XMLEventReader eventReader, LanguagePriorityList languages)
			throws XMLStreamException, SdmxException
	{
		final String sourceMethod = "parse";
		logger.entering(sourceClass, sourceMethod);

		LinkedHashMap<String, PortableTimeSeries<Double>> tsList = new LinkedHashMap<>();

		DataParsingResult result = new DataParsingResult();
		PortableTimeSeries<Double> ts = null;
		String currentAction = null;
		String currentValidFromDate = null;
		String currentValidToDate = null;

		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());

			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();

				if (startElement.getName().getLocalPart() == (DATASET))
				{
					logger.finer("Got new dataset");
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attr = attributes.next();
						String id = attr.getName().getLocalPart().toString();
						String value = attr.getValue();
						if (id.equalsIgnoreCase(ACTION))
						{
							logger.finer("action: " + value);
							currentAction = value;
						}
						else if (id.equalsIgnoreCase(VALID_FROM))
						{
							logger.finer("VALID_FROM: " + value);
							currentValidFromDate = value;
						}
						else if (id.equalsIgnoreCase(VALID_TO))
						{
							logger.finer("VALID_TO: " + value);
							currentValidToDate = value;
						}
					}
				}

				if (startElement.getName().getLocalPart() == (SERIES))
				{
					logger.finer("Got new time series");
					ts = new PortableTimeSeries<>();
					ts.setDataflow(dataflow);

					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					setMetadata(ts, attributes, currentAction, currentValidFromDate, currentValidToDate);
				}

				if (startElement.getName().getLocalPart() == (FOOTER))
				{
					setFooter(eventReader, languages, result);
				}

				if (startElement.getName().getLocalPart().equals(OBS) && data)
				{
					
					//now the metadata has been set and we know the series key. We use it to check
					//if the ts is already present in the result set. (This check has been introduced 
					//in recent times because some providers started returning the same time series in 
					//different chunks)
					
					ts = tsList.get(ts.getName()) != null ? tsList.get(ts.getName()) : ts;
					
					event = eventReader.nextEvent();
					logger.finest(event.toString());
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					String time = null;
					String obs_val = null;
					Map<String, String> obs_attr = new HashMap<>();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						String name = attribute.getName().toString();
						if (name.equals(dsd.getTimeDimension()))
						{
							time = attribute.getValue();
						}
						// workaround for some flows (e.g. in OECD) that do not respect the declared
						// time dimension
						else if (name.equals("TIME") && time == null)
						{
							time = attribute.getValue();
						}
						else if (name.equals(dsd.getMeasure()))
						{
							obs_val = attribute.getValue();
						}
						else
						{
							String value = attribute.getValue();
							String desc = null;
							if (!Configuration.getCodesPolicy().equalsIgnoreCase(Configuration.SDMX_CODES_POLICY_ID))
							{
								SdmxAttribute sdmxattr = dsd.getAttribute(name);
								if (sdmxattr != null)
								{
									Codelist cl = sdmxattr.getCodeList();
									if (cl != null)
									{
										desc = cl.get(value);
										if (desc != null)
										{
											value = Configuration.getCodesPolicy()
													.equalsIgnoreCase(Configuration.SDMX_CODES_POLICY_DESC) ? desc
															: value + "(" + desc + ")";
										}
									}
								}
							}
							obs_attr.put(name, value);
						}
					}
					try {
						ts.add(new DoubleObservation(time, Double.valueOf(obs_val != null ? obs_val : ""), obs_attr));
					} catch (NumberFormatException e) {
						logger.fine("The date: " + time + "has an obs value that is not parseable to a numer: " + obs_val + ". A NaN will be set.");
						ts.add(new DoubleObservation(time, Double.NaN, obs_attr));
					}
					continue;
				}
			}

			if (event.isEndElement())
			{
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart() == (SERIES))
				{
					logger.finer("Adding time series " + ts);
					//add empty series only if it is not in the list already
					if(!tsList.containsKey(ts.getName())){
						tsList.put(ts.getName(), ts);
					}
				}
			}

		}
		//make sure the time series is ordered by time
		for (PortableTimeSeries<Double> tts: result) Collections.sort(tts);
		
		result.setData(new ArrayList<PortableTimeSeries<Double>>(tsList.values()));
		logger.exiting(sourceClass, sourceMethod);
		return result;
	}

	private void setMetadata(PortableTimeSeries<?> ts, Iterator<Attribute> attributes, String action, String validFrom,
			String validTo)
	{
		final String sourceMethod = "setMetadata";
		logger.entering(sourceClass, sourceMethod);
		if (action != null)
		{
			ts.addAttribute(ACTION, action);
		}
		if (validFrom != null)
		{
			ts.addAttribute(VALID_FROM, validFrom);
		}
		if (validTo != null)
		{
			ts.addAttribute(VALID_TO, validTo);
		}
		int size = dsd.getDimensions().size();
		String[] names = new String[size];

		@SuppressWarnings("unchecked")
		Entry<String, String> values[] = new Entry[size];

		while (attributes.hasNext())
		{
			Attribute attr = attributes.next();
			String id = attr.getName().toString();
			String value = attr.getValue();
			if (dsd.isDimension(id))
			{
				String desc = null;
				names[dsd.getDimensionPosition(id) - 1] = id;
				if (id.equalsIgnoreCase("FREQ") || id.equalsIgnoreCase("FREQUENCY"))
				{
					ts.setFrequency(value);
				}

				Dimension dim = dsd.getDimension(id);
				if (dim != null)
				{
					Codelist cl = dim.getCodeList();
					if (cl != null)
						desc = cl.get(value);
				}

				values[dsd.getDimensionPosition(id) - 1] = new SimpleEntry<>(value, desc);
			}
			else
			{
				String desc = null;
				if (!Configuration.getCodesPolicy().equalsIgnoreCase(Configuration.SDMX_CODES_POLICY_ID))
				{
					SdmxAttribute sdmxattr = dsd.getAttribute(id);
					if (sdmxattr != null)
					{
						Codelist cl = sdmxattr.getCodeList();
						if (cl != null)
						{
							desc = cl.get(value);
							if (desc != null)
							{
								// TODO: Double-check this line. Unwanted side effects, for example in
								// PortableTimeSeries.getName()
								value = Configuration.getCodesPolicy().equalsIgnoreCase(
										Configuration.SDMX_CODES_POLICY_DESC) ? desc : value + " (" + desc + ")";
							}
						}
					}
				}

				ts.addAttribute(id, value);
			}
		}

		Map<String, Entry<String, String>> dimensions = new LinkedHashMap<>();
		for (int i = 0; i < size; i++)
			dimensions.put(names[i], values[i]);

		ts.setDimensions(dimensions);
		logger.exiting(sourceClass, sourceMethod);
	}

	private void setFooter(XMLEventReader eventReader, LanguagePriorityList languages, DataParsingResult parsingResult)
			throws XMLStreamException
	{
		final String sourceMethod = "setFooter";
		logger.entering(sourceClass, sourceMethod);
		Message msg = null;
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());

			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart() == (MESSAGE))
				{
					msg = new Message();
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attr = attributes.next();
						String id = attr.getName().toString();
						String value = attr.getValue();
						if (id.equalsIgnoreCase(CODE))
						{
							msg.setCode(value);
						}
						else if (id.equalsIgnoreCase(SEVERITY))
						{
							msg.setSeverity(value);
						}
					}
				}
				if (startElement.getName().getLocalPart() == (TEXT))
				{
					String item = null;
					LocalizedText text = new LocalizedText(languages);
					text.setText(startElement, eventReader);
					item = text.getText();
					msg.addText(item);
					try
					{
						@SuppressWarnings("unused")
						URL url = new URL(item);
						msg.setUrl(item);
					}
					catch (MalformedURLException e)
					{
					}
				}
			}

			if (event.isEndElement())
			{
				EndElement endElement = event.asEndElement();
				// just get the first message for now
				if (endElement.getName().getLocalPart() == (MESSAGE))
				{
					logger.finer("Adding footer message");
					parsingResult.setMessage(msg);
					break;
				}
			}
		}
		logger.exiting(sourceClass, sourceMethod);
	}

}
