/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or – as soon they
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
package it.bankitalia.reri.sia.sdmx.parser.v21;

import it.bankitalia.reri.sia.sdmx.api.DataFlowStructure;
import it.bankitalia.reri.sia.sdmx.api.PortableTimeSeries;
import it.bankitalia.reri.sia.sdmx.client.SDMXClientFactory;
import it.bankitalia.reri.sia.util.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @author Attilio Mattiocco
 *
 */
public class CompactDataParser {
	private static final String sourceClass = CompactDataParser.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();
	
	private static final String SERIES = "Series";
	private static final String OBS = "Obs";

	public static List<PortableTimeSeries> parse(String xmlBuffer, DataFlowStructure dsd, String dataflow) throws XMLStreamException, UnsupportedEncodingException {
		final String sourceMethod = "parse";
		logger.entering(sourceClass, sourceMethod);
		
		List<PortableTimeSeries> tsList = new ArrayList<PortableTimeSeries>();
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		InputStream in = new ByteArrayInputStream(xmlBuffer.getBytes("UTF-8"));
		XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

		PortableTimeSeries ts = null;

		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();

			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();

				if (startElement.getName().getLocalPart() == (SERIES)) {
					logger.finest("Got time series");
					ts = new PortableTimeSeries();
					ts.setDataflow(dataflow);
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					setMetadata(ts, dsd, attributes);
				}

				if (startElement.getName().getLocalPart().equals(OBS)) {
					event = eventReader.nextEvent();
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					String time = null;
					String obs_val = null;
					String obs_stat = "";
					while (attributes.hasNext()) {
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equals(dsd.getTimeDimension())) {
							time=attribute.getValue();
						}
						else if (attribute.getName().toString().equals(dsd.getObsStatus())) {
							obs_stat=attribute.getValue();
						}
						else if (attribute.getName().toString().equals(dsd.getMeasure())) {
							obs_val=attribute.getValue();
						}
					}
					if(time!= null && !time.isEmpty() && obs_val!= null && !obs_val.isEmpty() ){
						ts.addObservation(new Double(obs_val), time, obs_stat);
					}
					else{
						throw new RuntimeException("Error during CompactData Parsing. Invalid Observation Time: " + time + " or value: " + obs_val);
					}
					continue;
				}
			}
		
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart() == (SERIES) && ts.getObservations().size() > 0) {
					logger.finest("Adding time series " + ts);
					tsList.add(ts);
				}
			}

		}
		logger.exiting(sourceClass, sourceMethod);
		return tsList;
	}

	private static void setMetadata(PortableTimeSeries ts, DataFlowStructure dsd, Iterator<Attribute> attributes) {
		final String sourceMethod = "setMetadata";
		logger.entering(sourceClass, sourceMethod);
		String[] dimensions = new String[dsd.getDimensions().size()];
		while (attributes.hasNext()) {
			Attribute attr = attributes.next();
			String id = attr.getName().toString();
			String value = attr.getValue();
			if(dsd.isDimension(id)){
				dimensions[dsd.getDimensionPosition(id)-1] = id+"="+value;
				if(id.equalsIgnoreCase("FREQ")){
					ts.setFrequency(value);
				}
			}
			else{
				ts.addAttribute(attr.toString());
			}
			ts.setDimensions(Arrays.asList(dimensions));
		}
		logger.exiting(sourceClass, sourceMethod);
	}

} 
