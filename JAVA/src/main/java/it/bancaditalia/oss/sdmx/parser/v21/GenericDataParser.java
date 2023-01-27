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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.DoubleObservation;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.Configuration;

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
	
	public DataParsingResult parse(XMLEventReader eventReader, List<LanguageRange> languages) throws XMLStreamException, SdmxException
	{
		DataParsingResult result = new DataParsingResult();
		List<PortableTimeSeries<Double>> tsList = new ArrayList<>();
		Map<String, Entry<String, String>> dimKeys = null;
		Map<String, String> attrValues = null;
		List<DoubleObservation> obs = new ArrayList<>();
		
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
	
			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();

				switch (startElement.getName().getLocalPart())
				{
					case SERIES_KEY: dimKeys = getSeriesKey(eventReader, dsd); break;
					case ATTRIBUTES: attrValues = getSeriesAttributes(eventReader); break;
					case OBS: if (data) obs.add(getObservation(eventReader)); break;
				}
			}
			else if (event.isEndElement() && event.asEndElement().getName().getLocalPart() == (SERIES))
				tsList.add(new PortableTimeSeries<>(dataflow, dimKeys, attrValues, obs));
		}
		
		result.setData(tsList);
		return result;
	}

	private Map<String, Entry<String, String>> getSeriesKey(XMLEventReader eventReader, DataFlowStructure dsd) throws XMLStreamException
	{
		String id = null;
		int size = dsd.getDimensions().size();
		@SuppressWarnings("unchecked")
		Entry<String, String> values[] = new Entry[size];
		String[] names = new String[size];
		Map<String, Entry<String, String>> dimensions = new LinkedHashMap<>();

		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			
			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();
				if (VALUE.equalsIgnoreCase(startElement.getName().getLocalPart()))
					for (Attribute attribute: (Iterable<Attribute>) startElement::getAttributes)
						if (ID.equalsIgnoreCase(attribute.getName().toString()))
							id = attribute.getValue();
						else if (VALUE.equalsIgnoreCase(attribute.getName().toString()))
						{
							String val = attribute.getValue();
							names[dsd.getDimensionPosition(id) - 1] = id;
							values[dsd.getDimensionPosition(id) - 1] = new SimpleEntry<>(val, "");
						}
			} 
			else if (event.isEndElement() && SERIES_KEY.equals(event.asEndElement().getName().getLocalPart()))
			{
				for (int i = 0; i < size; i++)
					dimensions.put(names[i], values[i]);
				return dimensions;
			}
		}
		
		throw new XMLStreamException("EOF while reading dimensions."); 
	}
	
	private static Map<String, String> getSeriesAttributes(XMLEventReader eventReader) throws XMLStreamException
	{
		Map<String, String> attributes = new HashMap<>();
	
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			
			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equalsIgnoreCase(VALUE))
				{
					String id = null, val = null;
					for (Attribute attribute: (Iterable<Attribute>) startElement::getAttributes)
					{
						if (attribute.getName().toString().equalsIgnoreCase(ID))
							id = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase(VALUE))
							val = attribute.getValue();
					}
					
					attributes.put(id, val);
				}
			}
			else if (event.isEndElement() && event.asEndElement().getName().getLocalPart() == (ATTRIBUTES))
				return attributes;
		}

		throw new XMLStreamException("EOF while reading attributes."); 
	}

	private static DoubleObservation getObservation(XMLEventReader eventReader) throws XMLStreamException, SdmxException
	{
		String time = null;
		String val = "";
		Map<String, String> obs_attr = new HashMap<>();
		
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart() == (OBS_TIME))
				{
					for (Attribute attribute: (Iterable<Attribute>) startElement::getAttributes)
						if (attribute.getName().toString().equals(VALUE))
							time = attribute.getValue();
				}
				else if (startElement.getName().getLocalPart() == (OBS_VALUE))
				{
					for (Attribute attribute: (Iterable<Attribute>) startElement::getAttributes)
						if (attribute.getName().toString().equals(VALUE))
							val = attribute.getValue();
				}
				else if (startElement.getName().getLocalPart() == (ATTRIBUTEVALUE))
				{
					String name = startElement.getAttributeByName(new QName(ID)).getValue();
					String value = startElement.getAttributeByName(new QName(VALUE)).getValue();
					obs_attr.put(name, value);
				}
			}
			else if (event.isEndElement() && event.asEndElement().getName().getLocalPart() == (OBS))
				try
				{
					return new DoubleObservation(time, Double.valueOf(val), obs_attr);
				}
				catch (NumberFormatException e)
				{
					logger.fine("Non-numeric value for observation at date " + time + ". Using NaN instead.");
					return new DoubleObservation(time, Double.NaN, obs_attr);
				}
		}

		throw new XMLStreamException("EOF while reading an observation."); 
	}
} 
