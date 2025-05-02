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

import it.bancaditalia.oss.sdmx.api.*;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LocalizedText;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Locale.LanguageRange;
import java.util.Map.Entry;
import java.util.logging.Logger;

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
	private boolean 				revisions;

	public CompactDataParser(DataFlowStructure dsd, Dataflow dataflow, boolean data, boolean revisions)
	{
		this.dsd = dsd;
		this.dataflow = dataflow;
		this.data = data;
		this.revisions = revisions;
	}

	@Override
	public DataParsingResult parse(XMLEventReader eventReader, List<LanguageRange> languages) throws XMLStreamException, SdmxException
	{
		final String sourceMethod = "parse";
		logger.entering(sourceClass, sourceMethod);

		LinkedHashMap<String, PortableTimeSeries<Double>> tsList = new LinkedHashMap<>();

		DataParsingResult result = new DataParsingResult();
		String currentAction = null;
		String currentValidFromDate = null;
		String currentValidToDate = null;
		Entry<Map<String, Entry<String, String>>, Map<String, String>> metadata = null;
		List<DoubleObservation> obs = new ArrayList<>();
		Message message = null;
		
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());

			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();

				if (startElement.getName().getLocalPart() == (DATASET))
				{
					currentAction = null;
					currentValidFromDate = null;
					currentValidToDate = null;

					logger.finer("Got new dataset");
					for (Attribute attribute: (Iterable<Attribute>) startElement::getAttributes)
					{
						String id = attribute.getName().getLocalPart().toString();
						
						if (ACTION.equalsIgnoreCase(id))
							currentAction = attribute.getValue();
						else if (VALID_FROM.equalsIgnoreCase(id))
							currentValidFromDate = attribute.getValue();
						else if (VALID_TO.equalsIgnoreCase(id))
							currentValidToDate = attribute.getValue();
					}
				}
				else if (startElement.getName().getLocalPart() == (SERIES))
				{
					logger.finer("Got new time series");
					metadata = getMetadata(startElement::getAttributes);
				}
				else if (startElement.getName().getLocalPart() == (FOOTER))
					message = getMessage(eventReader, languages);
				else if (startElement.getName().getLocalPart().equals(OBS) && data)
					obs.add(getObservation(eventReader, currentAction, currentValidFromDate, currentValidToDate, startElement::getAttributes));
			}
			else if (event.isEndElement() && event.asEndElement().getName().getLocalPart() == (SERIES))
			{
				PortableTimeSeries<Double> ts = new PortableTimeSeries<>(dataflow, metadata.getKey(), metadata.getValue(), obs);
				String validity =  (currentValidFromDate!= null ? currentValidFromDate : "NA") 
									+ "-" 
									+ (currentValidToDate!= null ? currentValidToDate : "NA");
				String key = ts.getName() + validity;
				if(revisions || !tsList.containsKey(key)){
					Collections.sort(ts);
					tsList.put(key, ts);
				}
				else{
					try {
						//in some cases time series are split in different chunks, 
						// we have to merge attributes and obs, but only if we are not in 
						// history mode
						PortableTimeSeries<Double> previous = tsList.get(key);
						previous.merge(ts);
					} catch (SdmxInvalidParameterException e) {
						throw new SdmxXmlContentException(e.getMessage());
					}
				}
				obs = new ArrayList<>();
			}
		}
	
		if (message != null)
			result.setMessage(message);
		result.setData(new ArrayList<PortableTimeSeries<Double>>(tsList.values()));
		logger.exiting(sourceClass, sourceMethod);
		return result;
	}

	private DoubleObservation getObservation(XMLEventReader eventReader, String currentAction, String currentValidFromDate, 
			String currentValidToDate, Iterable<Attribute> attributes) throws XMLStreamException, SdmxInvalidParameterException
	{
		XMLEvent event = eventReader.nextEvent();
		logger.finest(event.toString());

		String time = null;
		String obs_val = null;
		Map<String, String> obs_attr = new HashMap<>();
		for (Attribute attribute: attributes)
		{
			String name = attribute.getName().toString();
			if (name.equals(dsd.getTimeDimension()))
				time = attribute.getValue();
			// workaround for some flows (e.g. in OECD) that do not respect the declared
			// time dimension
			else if (name.equals("TIME") && time == null)
				time = attribute.getValue();
			else if (name.equals(dsd.getMeasure()))
				obs_val = attribute.getValue();
			else
			{
				String value = attribute.getValue();
				String desc = null;
				if (!Configuration.getCodesPolicy().equalsIgnoreCase(Configuration.SDMX_CODES_POLICY_ID))
				{
					SdmxAttribute sdmxattr = dsd.getAttribute(name);
					if (sdmxattr != null)
					{
						Codelist cl = (Codelist) sdmxattr.getCodeList();
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
		// set validity and action at obs level (for multiple datasets and revisions)
		if (currentAction != null)
			obs_attr.put(ACTION, currentAction);
		if (currentValidFromDate != null)
			obs_attr.put(VALID_FROM, currentValidFromDate);
		if (currentValidToDate != null)
			obs_attr.put(VALID_TO, currentValidToDate);

		try
		{
			return new DoubleObservation(time, Double.valueOf(obs_val != null ? obs_val : ""), obs_attr);
		}
		catch (NumberFormatException e)
		{
			logger.fine("The date: " + time + "has an obs value that is not parseable to a numer: " + obs_val + ". A NaN will be set.");
			return new DoubleObservation(time, Double.NaN, obs_attr);
		}
	}

	private Entry<Map<String, Entry<String, String>>, Map<String, String>> getMetadata(Iterable<Attribute> attributes)
	{
		final String sourceMethod = "setMetadata";
		logger.entering(sourceClass, sourceMethod);
		int size = dsd.getDimensions().size();
		String[] names = new String[size];
		Map<String, String> attrValues = new HashMap<>();

		List<Entry<String, String>> values = new ArrayList<>(size);
		for (int i = 0; i < size; i++)
			values.add(null);

		for (Attribute attribute: attributes)
		{
			String id = attribute.getName().toString();
			String value = attribute.getValue();
			if (dsd.isDimension(id))
			{
				String desc = null;
				names[dsd.getDimensionPosition(id) - 1] = id;

				Dimension dim = dsd.getDimension(id);
				if (dim != null)
				{
					Codelist cl = (Codelist) dim.getCodeList();
					if (cl != null)
						desc = cl.get(value);
				}

				values.set(dsd.getDimensionPosition(id) - 1, new SimpleEntry<>(value, desc));
			}
			else
			{
				String desc = null;
				if (!Configuration.getCodesPolicy().equalsIgnoreCase(Configuration.SDMX_CODES_POLICY_ID))
				{
					SdmxAttribute sdmxattr = dsd.getAttribute(id);
					if (sdmxattr != null)
					{
						Codelist cl = (Codelist) sdmxattr.getCodeList();
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

				attrValues.put(id, value);
			}
		}

		Map<String, Entry<String, String>> dimensions = new LinkedHashMap<>();
		for (int i = 0; i < size; i++)
			dimensions.put(names[i], values.get(i));

		logger.exiting(sourceClass, sourceMethod);
		return new SimpleEntry<>(dimensions, attrValues);
	}

	private Message getMessage(XMLEventReader eventReader, List<LanguageRange> languages) throws XMLStreamException
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
					for (Attribute attribute: (Iterable<Attribute>) startElement::getAttributes)
					{
						String id = attribute.getName().toString();
						String value = attribute.getValue();
						if (id.equalsIgnoreCase(CODE))
							msg.setCode(value);
						else if (id.equalsIgnoreCase(SEVERITY))
							msg.setSeverity(value);
					}
				}
				else if (startElement.getName().getLocalPart() == (TEXT))
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
			else if (event.isEndElement() && event.asEndElement().getName().getLocalPart() == (MESSAGE))
			{
				logger.finer("Adding footer message");
				return msg;
			}
		}

		return null;
	}
}
