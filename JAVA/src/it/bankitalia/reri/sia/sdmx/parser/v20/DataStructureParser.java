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
package it.bankitalia.reri.sia.sdmx.parser.v20;

import it.bankitalia.reri.sia.sdmx.api.DataFlowStructure;
import it.bankitalia.reri.sia.sdmx.api.Dimension;
import it.bankitalia.reri.sia.util.Configuration;
import it.bankitalia.reri.sia.util.LocalizedText;
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
	private static final String sourceClass = DataStructureParser.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();

	static final String DATASTRUCTURE = "KeyFamily";
	
	static final String CODELISTS = "CodeLists";
	static final String CODELIST = "CodeList";
	static final String CODELIST2 = "codelist";

	static final String COMPONENTS = "Components";
	static final String NAME = "Name";
	
	static final String DIMENSION = "Dimension";
	static final String ATTRIBUTE = "Attribute";
	static final String TIMEDIMENSION = "TimeDimension";
	static final String CONCEPT_REF = "conceptRef";

	static final String ID = "id";
	static final String AGENCYID = "agencyID";

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
		LocalizedText currentName = new LocalizedText();

		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();

			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				
				if (startElement.getName().getLocalPart() == (CODELISTS)) {
					codelists = getCodelists(eventReader);
				}
				else if (startElement.getName().getLocalPart() == (DATASTRUCTURE)) {
					
					currentStructure = new DataFlowStructure();
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext()) {
						Attribute attr = attributes.next();
						String id = null;
						String agency = null;
						if (attr.getName().toString().equals(ID)) {
							id = attr.getValue();
							currentStructure.setId(id);
						}
						else if (attr.getName().toString().equals(AGENCYID)) {
							agency = attr.getValue();
							currentStructure.setAgency(agency);
						}
					}					
				}
				else if (startElement.getName().getLocalPart().equals(NAME)) {
					//this has to be checked better
					if(currentStructure != null){
						currentName.setText(startElement, eventReader);
					}
				}				
				else if (startElement.getName().getLocalPart().equals(COMPONENTS)) {
					if(currentStructure != null){
						setStructureDimensionsAndAttributes(currentStructure, eventReader, codelists);
					}
					else{
						throw new RuntimeException("Error during Structure Parsing. Null current structure.");
					}	
				}				
			}
			
			if (event.isEndElement()) {
				if (event.asEndElement().getName().getLocalPart().equals(DATASTRUCTURE)) {
					logger.finer("Adding data structure. " + currentStructure);
					currentStructure.setName(currentName.getText());
					result.add(currentStructure);
				}
			}
		}
		logger.exiting(sourceClass, sourceMethod);
		return result;
	}

	private static void setStructureDimensionsAndAttributes(
			DataFlowStructure currentStructure, XMLEventReader eventReader, Map<String, Map<String,String>> codelists) throws XMLStreamException {
		final String sourceMethod = "setStructureDimensions";
		logger.entering(sourceClass, sourceMethod);
		
		String agency = currentStructure.getAgency();
		Dimension currentDimension = null;
		int position = 0;
		
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals(DIMENSION)) {
					logger.finer("Got dimension");
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					String id = null;
					String codelist = null;
					currentDimension = new Dimension();
					// in sdmx2.0 this position is not set, we rely on the order of the DSD
					position++;
					currentDimension.setPosition(position);
					while (attributes.hasNext()) {
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equals(CONCEPT_REF)) {
							id=attribute.getValue();
						}
						else if (attribute.getName().toString().equals(CODELIST2)) {
							codelist=attribute.getValue();
						}
					}
					
					if(id!= null && !id.isEmpty()){
						currentDimension.setId(id);
					}
					else{
						throw new RuntimeException("Error during Structure Parsing. Invalid id: " + id);
					}
					if(codelist!= null && !codelist.isEmpty()){
						currentDimension.setCodeList(agency + "/" + codelist);
						if(codelists != null){
							Map<String, String> codes = codelists.get(currentDimension.getCodeList());
							currentDimension.setCodes(codes);
						}
					}
					else{
						throw new RuntimeException("Error during Structure Parsing. Invalid CODELIST: " + codelist);
					}
				}
				else if (startElement.getName().getLocalPart().equals((TIMEDIMENSION))) {
					logger.finer("Got time dimension");
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					String id = null;
					while (attributes.hasNext()) {
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equals(CONCEPT_REF)) {
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
				else if (event.asEndElement().getName().getLocalPart().equals(COMPONENTS)) {
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
					String codelistName = "";
					while (attributes.hasNext()) {
						Attribute attr = attributes.next();
						if (attr.getName().toString().equals(ID)) {
							id = attr.getValue();
						}
						else if (attr.getName().toString().equals(AGENCYID)) {
							agency = attr.getValue();
						}
					}
					codelistName = agency + "/" + id;
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
