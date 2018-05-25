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
package it.bancaditalia.oss.sdmx.parser.v21;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.DoubleObservation;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LanguagePriorityList;

/**
 * @author Attilio Mattiocco
 *
 */
public class GenericDataParser implements Parser<DataParsingResult>{
	protected static Logger logger = Configuration.getSdmxLogger();

	private static final String SERIES = "Series";
	private static final String SERIES_KEY = "SeriesKey";
	private static final String VALUE = "value";
	private static final String ID = "id";
	private static final String OBS = "Obs";
	private static final String OBS_TIME = "ObsDimension";
	private static final String OBS_VALUE = "ObsValue";
	private static final String ATTRIBUTES = "Attributes";
	private static final String ATTRIBUTEVALUE = "Value";

	private DataFlowStructure		dsd;
	private Dataflow				dataflow;
	private boolean					data;

	public GenericDataParser(DataFlowStructure dsd, Dataflow dataflow, boolean data)
	{
		this.dsd = dsd;
		this.dataflow = dataflow;
		this.data = data;
	}
	
	public DataParsingResult parse(XMLEventReader eventReader, LanguagePriorityList languages) throws XMLStreamException, SdmxException {
		DataParsingResult result = new DataParsingResult();
		List<PortableTimeSeries<Double>> tsList = new ArrayList<>();
		PortableTimeSeries<Double> ts = null;

		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();

				if (startElement.getName().getLocalPart() == (SERIES)) {
					ts = new PortableTimeSeries<>();
					ts.setDataflow(dataflow);
				}

				if (startElement.getName().getLocalPart() == (SERIES_KEY)) {
					setSeriesKey(ts, eventReader, dsd);
				}

				if (startElement.getName().getLocalPart() == (ATTRIBUTES)) {
					setSeriesAttributes(ts, eventReader);
				}

				if (startElement.getName().getLocalPart() == (OBS)  && data) {
					setSeriesSingleObs(ts, eventReader);
				}
				
			}
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart() == (SERIES)) {
					tsList.add(ts);
				}
			}
		}
		result.setData(tsList);
		return result;
	}

	private void setSeriesKey(PortableTimeSeries<Double> ts, XMLEventReader eventReader, DataFlowStructure dsd) throws XMLStreamException {
		String id = null;
		int size = dsd.getDimensions().size();
		@SuppressWarnings("unchecked")
		Entry<String, String> values[] = new Entry[size];
		String[] names = new String[size];
		Map<String, Entry<String, String>> dimensions = new LinkedHashMap<>();
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equalsIgnoreCase(VALUE)) {
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext()) {
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase(ID)) {
							id=attribute.getValue();
						}
						else if (attribute.getName().toString().equalsIgnoreCase(VALUE)) {
							String val = attribute.getValue();
							names[dsd.getDimensionPosition(id) - 1] = id;
							values[dsd.getDimensionPosition(id) - 1] = new SimpleEntry<>(val, "");
							if(id.equalsIgnoreCase("FREQ") || id.equalsIgnoreCase("FREQUENCY")){
								ts.setFrequency(val);
							}
						}
					}
				}
			}
			
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart() == (SERIES_KEY)) {
					for (int i = 0; i < size; i++){
						dimensions.put(names[i], values[i]);
					}
					ts.setDimensions(dimensions);
					break;
				}
			}
		}
	}
	
	private static void setSeriesAttributes(PortableTimeSeries<Double> ts, XMLEventReader eventReader) throws XMLStreamException {
		String id = null;
		String val = null;
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equalsIgnoreCase(VALUE)) {
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext()) {
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase(ID)) {
							id=attribute.getValue();
						}
						else if (attribute.getName().toString().equalsIgnoreCase(VALUE)) {
							val=attribute.getValue();
						}
					}
				}
			}
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart().equalsIgnoreCase(VALUE)) {
					ts.addAttribute(id, val);
				}
			}
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart() == (ATTRIBUTES)) {
					break;
				}
			}
		}
	}

	private static void setSeriesSingleObs(PortableTimeSeries<Double> ts, XMLEventReader eventReader) throws XMLStreamException, SdmxException {
		String time = null;
		String val = null;
		Hashtable<String, String> obs_attr = new Hashtable<String, String>();
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart() == (OBS_TIME)) {
					time = startElement.getAttributeByName(new QName(VALUE)).getValue();
				}
				if (startElement.getName().getLocalPart() == (OBS_VALUE)) {
					val = startElement.getAttributeByName(new QName(VALUE)).getValue();
				}
				if (startElement.getName().getLocalPart() == (ATTRIBUTEVALUE)) {
					String name = startElement.getAttributeByName(new QName(ID)).getValue();
					String value = startElement.getAttributeByName(new QName(VALUE)).getValue();
					obs_attr.put(name, value);
				}
			}
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart() == (OBS)) {
					try {
						ts.add(new DoubleObservation(time, Double.valueOf(val), obs_attr));
					} catch (NumberFormatException e) {
						ts.add(new DoubleObservation(time, Double.NaN, obs_attr));
					}
					break;
				}
			}
		}
	}

} 
