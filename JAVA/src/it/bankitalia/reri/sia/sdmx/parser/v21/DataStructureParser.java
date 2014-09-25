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
package it.bankitalia.reri.sia.sdmx.parser.v21;

import it.bankitalia.reri.sia.sdmx.api.DataFlowStructure;
import it.bankitalia.reri.sia.sdmx.api.Dimension;
import it.bankitalia.reri.sia.sdmx.client.SDMXClientFactory;
import it.bankitalia.reri.sia.util.Configuration;
import it.bankitalia.reri.sia.util.SdmxException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @author Attilio Mattiocco
 *
 */
public class DataStructureParser {
	private static final String sourceClass = SDMXClientFactory.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();

	static final String DATASTRUCTURE = "DataStructure";
	
	static final String CODELISTS = "Codelists";
	static final String CODELIST = "Codelist";

	static final String DIMENSIONLIST = "DimensionList";
	static final String GROUP = "Group";
	static final String ATTRIBUTELIST = "AttributeList";
	static final String MEASURELIST = "MeasureList";
	
	static final String DIMENSION = "Dimension";
	static final String TIMEDIMENSION = "TimeDimension";

	static final String POSITION = "position";
	static final String ID = "id";
	static final String AGENCYID = "agencyID";
	static final String VERSION = "version";

	static final String LOCAL_REPRESENTATION = "LocalRepresentation";
	static final String REF = "Ref";

	public static List<DataFlowStructure> parse(String xmlBuffer) throws XMLStreamException, SdmxException, UnsupportedEncodingException {
		final String sourceMethod = "parse";
		logger.entering(sourceClass, sourceMethod);
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		InputStream in = new ByteArrayInputStream(xmlBuffer.getBytes("UTF-8"));
		XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
		
		List<DataFlowStructure> result = new ArrayList<DataFlowStructure>();
		Map<String, Map<String,String>> codelists = null;
		DataFlowStructure currentStructure = null;

		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();

			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				
				if (startElement.getName().getLocalPart() == (CODELISTS)) {
					codelists = getCodelists(eventReader);
				}

				if (startElement.getName().getLocalPart() == (DATASTRUCTURE)) {
					
					currentStructure = new DataFlowStructure();
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext()) {
						Attribute attr = attributes.next();
						String id = null;
						String agency = null;
						String version = null;
						if (attr.getName().toString().equals(ID)) {
							id = attr.getValue();
							currentStructure.setId(id);
						}
						else if (attr.getName().toString().equals(AGENCYID)) {
							agency = attr.getValue();
							currentStructure.setAgency(agency);
						}
						else if (attr.getName().toString().equals(VERSION)) {
							version = attr.getValue();
							currentStructure.setVersion(version);
						}
					}
					logger.finer("Got data structure.");
				}
				
				if (startElement.getName().getLocalPart().equals(DIMENSIONLIST)) {
					if(currentStructure != null){
						setStructureDimensions(currentStructure, eventReader, codelists);
					}
					else{
						throw new RuntimeException("Error during Structure Parsing. Null current structure.");
					}	
				}
				
				if (startElement.getName().getLocalPart().equals(GROUP)) {
					setStructureGroups(currentStructure, eventReader);
				}
				if (startElement.getName().getLocalPart().equals(ATTRIBUTELIST)) {
					setStructureAttributes(currentStructure, eventReader);
				}
				if (startElement.getName().getLocalPart().equals(MEASURELIST)) {
					setStructureMeasures(currentStructure, eventReader);	
				}
				
			}
			
			if (event.isEndElement()) {
				if (event.asEndElement().getName().getLocalPart().equals(DATASTRUCTURE)) {
					logger.finer("Adding data structure. " + currentStructure);
					result.add(currentStructure);
				}
			}
		}
		
		logger.exiting(sourceClass, sourceMethod);
		return result;
	}

	private static void setStructureDimensions(
			DataFlowStructure currentStructure, XMLEventReader eventReader, Map<String, Map<String,String>> codelists) throws XMLStreamException {
		final String sourceMethod = "setStructureDimensions";
		logger.entering(sourceClass, sourceMethod);
		
		Dimension currentDimension = null;
		int position = 0;
		
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals(DIMENSION)) {
					logger.finer("Got dimension");
					currentDimension = new Dimension();
					/*
					 * Uhm, we could rely on the position attribute but it seems not a good idea:
					 * From the SDMX2.1 specs:
					 * "The position attribute specifies the position of the dimension in the data 
					 * structure definition. It is optional an the position of the dimension in the 
					 * key descriptor (DimensionList element) always takes precedence over the value 
					 * supplied here. This is strictly for informational purposes only."
					 * 
					 * So we rely on the position in the key descriptor.
					 */
					position++;
					currentDimension.setPosition(position);
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					String id = null;
					while (attributes.hasNext()) {
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equals(ID)) {
							id=attribute.getValue();
						}
						// see above for the position
//						if (attribute.getName().toString().equals(POSITION)) {
//							position=Integer.parseInt(attribute.getValue());
//						}
					}
					
					if(id!= null && !id.isEmpty()){
						currentDimension.setId(id);
					}
					else{
						throw new RuntimeException("Error during Structure Parsing. Invalid id: " + id);
					}
				}
				else if (startElement.getName().getLocalPart().equals((TIMEDIMENSION))) {
					logger.finer("Got time dimension");
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					String id = null;
					while (attributes.hasNext()) {
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equals(ID)) {
							id=attribute.getValue();
						}
					}
					if(id!= null && !id.isEmpty()){
						if(currentStructure != null){
							logger.finer("Adding time dimension: " + id);
							currentStructure.setTimeDimension(id);
						}
						else{
							throw new RuntimeException("Error during Structure Parsing. Null current Structure.");
						}	
					}
					else{
						throw new RuntimeException("Error during Structure Parsing. Invalid time dimension: " + id);
					}
					continue;
				}
				else if (startElement.getName().getLocalPart().equals((LOCAL_REPRESENTATION))) {
					logger.finer("Got time codelist");
					setCodelistName(currentDimension, eventReader);
					//now set codes
					if(codelists != null){
						currentDimension.setCodes(codelists.get(currentDimension.getCodeList()));
					}
				}
				
			}
			
			if (event.isEndElement()) {
				
				if (event.asEndElement().getName().getLocalPart().equals(DIMENSION)) {
					if(currentStructure != null && currentDimension != null){
						logger.finer("Adding dimension: " + currentDimension);
						currentStructure.setDimension(currentDimension);
					}
					else{
						throw new RuntimeException("Error during Structure Parsing. Null current structure or dimension.");
					}	
				}
				else if (event.asEndElement().getName().getLocalPart().equals(DIMENSIONLIST)) {
					break;	
				}
			}
		}
		logger.exiting(sourceClass, sourceMethod);
	}
	
	private static void setCodelistName(Dimension dim, XMLEventReader eventReader) throws XMLStreamException {
		final String sourceMethod = "setCodelist";
		logger.entering(sourceClass, sourceMethod);
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals(REF)) {
					logger.finer("Got codelist");
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					String id = null;
					String version = "";
					String agency = "";
					while (attributes.hasNext()) {
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equals(ID)) {
							id=attribute.getValue();
						}
						else if (attribute.getName().toString().equals(AGENCYID)) {
							agency=attribute.getValue();
						}
						else if (attribute.getName().toString().equals(VERSION)) {
							version=attribute.getValue();
						}
					}
					if(id!= null && !id.isEmpty()){
						if(dim != null){
							String key = agency + "/" + id + "/" + version;
							logger.finer("Adding codelist: " + agency + "/" + id + "/" + version);
							dim.setCodeList(key);
						}
						else{
							throw new RuntimeException("Error during Structure Parsing. Null current Structure.");
						}	
					}
					else{
						throw new RuntimeException("Error during Structure Parsing. Invalid time dimension: " + id);
					}
					continue;
				}
			
			}
			if (event.isEndElement()) {
				if (event.asEndElement().getName().getLocalPart().equals(LOCAL_REPRESENTATION)) {
					break;
				}
			}
		}
		logger.exiting(sourceClass, sourceMethod);
	}
	
	
	private static void setStructureGroups(
			DataFlowStructure currentStructure, XMLEventReader eventReader) throws XMLStreamException {
		final String sourceMethod = "setStructureGroups";
		logger.entering(sourceClass, sourceMethod);
		while (eventReader.hasNext()) {
			// TODO skip for now
			
			XMLEvent event = eventReader.nextEvent();
			if (event.isEndElement()) {
				if (event.asEndElement().getName().getLocalPart().equals(GROUP)) {
					break;
				}
			}
		}
		logger.exiting(sourceClass, sourceMethod);
	}
	

	private static void setStructureMeasures(
			DataFlowStructure currentStructure, XMLEventReader eventReader) throws XMLStreamException {
		final String sourceMethod = "setStructureMeasures";
		logger.entering(sourceClass, sourceMethod);
		while (eventReader.hasNext()) {
			// TODO skip for now
			
			XMLEvent event = eventReader.nextEvent();
			if (event.isEndElement()) {
				if (event.asEndElement().getName().getLocalPart().equals(MEASURELIST)) {
					break;
				}
			}
		}
		logger.exiting(sourceClass, sourceMethod);
	}
	
	private static void setStructureAttributes(
			DataFlowStructure currentStructure, XMLEventReader eventReader) throws XMLStreamException {
		final String sourceMethod = "setStructureAttributes";
		logger.entering(sourceClass, sourceMethod);
		while (eventReader.hasNext()) {
			// TODO skip for now
			
			XMLEvent event = eventReader.nextEvent();
			if (event.isEndElement()) {
				if (event.asEndElement().getName().getLocalPart().equals(ATTRIBUTELIST)) {
					break;
				}
			}
		}
		logger.exiting(sourceClass, sourceMethod);
	}
	
	private static Map<String, Map<String, String>> getCodelists(XMLEventReader eventReader) throws XMLStreamException, SdmxException{
		Map<String, Map<String, String>> codelists = new Hashtable<String, Map<String,String>>();
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();

				if (startElement.getName().getLocalPart().equals(CODELIST)) {
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					String id = null;
					String agency = null;
					String version = null;
					String codelistName = "";
					while (attributes.hasNext()) {
						Attribute attr = attributes.next();
						if (attr.getName().toString().equals(ID)) {
							id = attr.getValue();
						}
						else if (attr.getName().toString().equals(AGENCYID)) {
							agency = attr.getValue();
						}
						else if (attr.getName().toString().equals(VERSION)) {
							version = attr.getValue();
						}
					}
					codelistName = agency + "/" + id+ "/" + version;
					logger.finer("Got codelist: " + codelistName);
					Map<String, String> codes = CodelistParser.getCodes(eventReader);
					codelists.put(codelistName, codes);	
				}
			}
			if (event.isEndElement()) {
				if (event.asEndElement().getName().getLocalPart().equals(CODELISTS)) {
					break;
				}
			}
		}
		return(codelists);
	}
	
} 
