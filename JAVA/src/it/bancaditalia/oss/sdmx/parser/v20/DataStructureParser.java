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
import java.io.Reader;
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

import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.SdmxAttribute;
import it.bancaditalia.oss.sdmx.api.SdmxMetaElement;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LanguagePriorityList;
import it.bancaditalia.oss.sdmx.util.LocalizedText;

/**
 * @author Attilio Mattiocco
 *
 */
public class DataStructureParser implements Parser<List<DataFlowStructure>> {
	private static final String sourceClass = DataStructureParser.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();

	static final String DATASTRUCTURE = "KeyFamily";
	
	static final String CODELISTS = "CodeLists";
	static final String CODELIST = "CodeList";
	static final String CODELIST2 = "codelist";
	static final String CODELISTAGENCY = "codelistAgency";
        
	static final String CONCEPTS = "Concepts";
	static final String CONCEPTSCHEME = "ConceptScheme";
	static final String CONCEPT = "Concept";

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

	public List<DataFlowStructure> parse(Reader xmlBuffer, LanguagePriorityList languages) throws XMLStreamException, SdmxException {
		final String sourceMethod = "parse";
		logger.entering(sourceClass, sourceMethod);
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		BufferedReader br = GenericDataParser.skipBOM(xmlBuffer);
		XMLEventReader eventReader = inputFactory.createXMLEventReader(br);
		
		List<DataFlowStructure> result = new ArrayList<DataFlowStructure>();
		Map<String, Map<String,String>> codelists = null;
		Map<String, String> concepts = null;
		DataFlowStructure currentStructure = null;

		LocalizedText currentName = new LocalizedText(languages);
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());

			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				
				if (startElement.getName().getLocalPart() == (CODELISTS)) {
					codelists = getCodelists(eventReader, languages);
				}
                                else if (startElement.getName().getLocalPart() == (CONCEPTS)) {
					concepts = getConcepts(eventReader, languages);
				}
				else if (startElement.getName().getLocalPart() == (DATASTRUCTURE)) {
					currentStructure = new DataFlowStructure();
					currentName.clear();
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
						setStructureDimensionsAndAttributes(currentStructure, eventReader, codelists, concepts);
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
			DataFlowStructure currentStructure, XMLEventReader eventReader, Map<String, Map<String,String>> codelists, Map<String, String> concepts) throws XMLStreamException {
		final String sourceMethod = "setStructureDimensions";
		logger.entering(sourceClass, sourceMethod);
		
		String agency = currentStructure.getAgency();
		SdmxMetaElement currentElement = null;
		int position = 0;
		
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals(DIMENSION) || startElement.getName().getLocalPart().equals(ATTRIBUTE)) {
					boolean isDimension = startElement.getName().getLocalPart().equals(DIMENSION);
					if(isDimension){
						logger.finer("Got dimension");
						currentElement = new Dimension();
						// in sdmx2.0 this position is not set, we rely on the order of the DSD
						position++;
						((Dimension)currentElement).setPosition(position);
					}
					else{
						logger.finer("Got attribute");
						currentElement = new SdmxAttribute();
					}
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					String id = null;
					String codelistID = null;
					String codelistAgency = null;
					
					while (attributes.hasNext()) {
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equals(CONCEPT_REF)) {
							id=attribute.getValue();
						}
						else if (attribute.getName().toString().equals(CODELIST2)) {
							codelistID=attribute.getValue();
						}
						else if (attribute.getName().toString().equals(CODELISTAGENCY)) {
							codelistAgency=attribute.getValue();
						}
					}
					
					if(id!= null && !id.isEmpty()){
						currentElement.setId(id);
						if (concepts != null) {
							currentElement.setName(concepts.get(agency + "/" + id));
						}
					}
					else{
						throw new RuntimeException("Error during Structure Parsing. Invalid id: " + id);
					}
					if(codelistID!= null && !codelistID.isEmpty()){
						Codelist cl = new  Codelist(codelistID, codelistAgency != null ? codelistAgency : agency, null);
						if(codelists != null){
						Map<String, String> codes = codelists.get(cl.getFullIdentifier());
							cl.setCodes(codes);
						}
						currentElement.setCodeList(cl);					
					}
					else{
						if(isDimension)
							throw new RuntimeException("Error during Structure Parsing. Invalid CODELIST: " + codelistID);
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
					if(currentStructure != null && currentElement != null){
						logger.finer("Adding dimension: " + currentElement);
						currentStructure.setDimension((Dimension)currentElement);
					}
					else{
						throw new RuntimeException("Error during Structure Parsing. Null current structure or dimension.");
					}	
				}
				else if (event.asEndElement().getName().getLocalPart().equals(ATTRIBUTE)) {
					if(currentStructure != null && currentElement != null){
						logger.finer("Adding attribute: " + currentElement);
						currentStructure.setAttribute((SdmxAttribute)currentElement);
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
	
	private static Map<String, Map<String, String>> getCodelists(XMLEventReader eventReader, LanguagePriorityList languages) throws XMLStreamException, SdmxException{
		Map<String, Map<String, String>> codelists = new Hashtable<String, Map<String,String>>();
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
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
					Map<String, String> codes = CodelistParser.getCodes(eventReader, languages);
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
	
	private static Map<String, String> getConcepts(XMLEventReader eventReader, LanguagePriorityList languages) throws XMLStreamException, SdmxException{
		Map<String, String> concepts = new Hashtable<String, String>();
		String conceptSchemeAgency = null;
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();

				if (startElement.getName().getLocalPart().equals(CONCEPTSCHEME)) {
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					conceptSchemeAgency = null;
					while (attributes.hasNext()) {
						Attribute attr = attributes.next();
						if (attr.getName().toString().equals(AGENCYID)) {
							conceptSchemeAgency = attr.getValue();
						}
					}
					logger.finer("Got conceptSchemeAgency: " + conceptSchemeAgency);
				} else if (startElement.getName().getLocalPart().equals(CONCEPT)) {
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					String id = null;
					String agency = null;
					String conceptName = "";
					while (attributes.hasNext()) {
						Attribute attr = attributes.next();
						if (attr.getName().toString().equals(ID)) {
							id = attr.getValue();
						}
						else if (attr.getName().toString().equals(AGENCYID)) {
							agency = attr.getValue();
						}
					}
					if (agency == null && conceptSchemeAgency != null) {
						agency = conceptSchemeAgency;
                    }
					conceptName = agency + "/" + id;
					logger.finer("Got concept: " + conceptName);
					concepts.put(conceptName, getConceptName(eventReader, languages));	
				}
			}
			if (event.isEndElement()) {
				if (event.asEndElement().getName().getLocalPart().equals(CONCEPTS)) {
					break;
				}
			}
		}
		return(concepts);
	}

	private static String getConceptName(XMLEventReader eventReader, LanguagePriorityList languages) throws XMLStreamException, SdmxException{
		LocalizedText value = new LocalizedText(languages);
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart() == ("Name")) {
					value.setText(startElement, eventReader);
				}
				
			}
			
			if (event.isEndElement()) {
				String eventName=event.asEndElement().getName().getLocalPart();
				if (eventName.equals(CONCEPT)) {
                                    break;
				}
			}
		}
		return value.getText();
        }
	
} 
