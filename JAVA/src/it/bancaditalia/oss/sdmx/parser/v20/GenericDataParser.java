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
package it.bancaditalia.oss.sdmx.parser.v20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;
import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * @author Attilio Mattiocco
 *
 */
public class GenericDataParser implements Parser<DataParsingResult> {
	protected static Logger logger = Configuration.getSdmxLogger();

	private static final String SERIES = "Series";
	private static final String SERIES_KEY = "SeriesKey";
	private static final String VALUE = "value";
	private static final String CONCEPT = "concept";
	private static final String OBS = "Obs";
	private static final String OBS_TIME = "Time";
	private static final String OBS_VALUE = "ObsValue";
	private static final String ATTRIBUTES = "Attributes";
	private static final String ATTRIBUTEVALUE = "Value";

	private DataFlowStructure dsd;
	private String dataflow;
	private boolean data;

	public GenericDataParser(DataFlowStructure dsd, String dataflow, boolean data)
	{
		this.dsd = dsd;
		this.dataflow = dataflow;
		this.data = data;
	}
	
	public DataParsingResult parse(Reader xmlBuffer) throws XMLStreamException, SdmxException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		BufferedReader br = skipBOM(xmlBuffer);
		XMLEventReader eventReader = inputFactory.createXMLEventReader(br);

		DataParsingResult result = new DataParsingResult();
		List<PortableTimeSeries> tsList = new ArrayList<PortableTimeSeries>();
		PortableTimeSeries ts = null;

		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();

				if (startElement.getName().getLocalPart() == (SERIES)) {
					ts = new PortableTimeSeries();
					ts.setDataflow(dataflow);
				}

				if (startElement.getName().getLocalPart() == (SERIES_KEY)) {
					setSeriesKey(ts, eventReader);
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

	private void setSeriesKey(PortableTimeSeries ts, XMLEventReader eventReader) throws XMLStreamException {
		String id = null;
		String val = null;
		String[] names = new String[dsd.getDimensions().size()];
		String[] values = new String[dsd.getDimensions().size()];
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
						if (attribute.getName().toString().equalsIgnoreCase(CONCEPT)) {
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
					if(dsd.isDimension(id)){
						names[dsd.getDimensionPosition(id)-1] = id;
						values[dsd.getDimensionPosition(id)-1] = val;
					}
					if(id.equalsIgnoreCase("FREQ") || id.equalsIgnoreCase("FREQUENCY")){
						ts.setFrequency(val);
					}
				}
			}
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart() == (SERIES_KEY)) {
					Map<String, String> dimensions = new LinkedHashMap<String, String>(names.length);
					for (int i = 0; i < names.length; i++)
						dimensions.put(names[i], values[i]);
					ts.setDimensions(dimensions);
					break;
				}
			}
		}
	}
	
	private static void setSeriesAttributes(PortableTimeSeries ts, XMLEventReader eventReader) throws XMLStreamException {
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
						if (attribute.getName().toString().equalsIgnoreCase(CONCEPT)) {
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

	private static void setSeriesSingleObs(PortableTimeSeries ts, XMLEventReader eventReader) throws XMLStreamException, SdmxException {
		String time = null;
		String val = null;
		Hashtable<String, String> obs_attr = new Hashtable<String, String>();
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart() == (OBS_TIME)) {
					time = eventReader.getElementText();
				}
				if (startElement.getName().getLocalPart() == (OBS_VALUE)) {
					val = startElement.getAttributeByName(new QName(VALUE)).getValue();
				}
				if (startElement.getName().getLocalPart() == (ATTRIBUTEVALUE)) {
					String name = startElement.getAttributeByName(new QName(CONCEPT)).getValue();
					String value = startElement.getAttributeByName(new QName(VALUE)).getValue();
					obs_attr.put(name, value);
				}
			}
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart() == (OBS)) {
					ts.addObservation(val, time,  obs_attr);
					break;
				}
			}
		}
	}
	
	
	// some 2.0 providers are apparently adding a BOM
	public static BufferedReader skipBOM(Reader xmlBuffer) throws SdmxException{
		BufferedReader br = new BufferedReader(xmlBuffer) 
		{ 
			@Override public void close() throws IOException 
			{  
				logger.fine("GenericDataParser::skipBOM: closing stream.");
				super.close();
			} 
		};
		try {
			// java uses Unicode big endian
			char[] cbuf = new char[1];
			// TODO: Source of problems here
			br.mark(1);
			br.read(cbuf, 0, 1);
			logger.fine(String.format("0x%2s", Integer.toHexString(cbuf[0])));
			if(		(byte)cbuf[0] == (byte)0xfeff) 
			{
				logger.fine("BOM found and skipped");
			}
			else{
				// TODO: Source of problems here
				logger.fine("GenericDataParser::skipBOM: Resetting stream.");
				br.reset();
			}
		} catch (IOException e) {
			throw SdmxExceptionFactory.wrap(e);
		}
		return br;
	}
} 
