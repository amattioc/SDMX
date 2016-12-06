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

import java.io.Reader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LocalizedText;

/**
 * @author Attilio Mattiocco
 *
 */
public class CodelistParser {
	private static final String sourceClass = CodelistParser.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();

	// valid in V.2.1
	static final String CODELIST = "Codelist";
	static final String CODE = "Code";
	static final String ID = "id";
	static final String DESCRIPTION = "Name";

	public static Map<String,String> parse(Reader xmlBuffer) throws XMLStreamException, SdmxException {
		return parse(xmlBuffer, CODELIST, CODE, ID, DESCRIPTION);
	}
	public static Map<String,String> parse(Reader xmlBuffer, String codelist, String code, String id, String description) throws XMLStreamException, SdmxException {
		final String sourceMethod = "parse";
		logger.entering(sourceClass, sourceMethod);

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(xmlBuffer);
		Map<String,String> codes = getCodes(eventReader, codelist, code, id, description);
		
		logger.exiting(sourceClass, sourceMethod);
		return codes;
	}

	public static Map<String, String> getCodes(XMLEventReader eventReader) throws XMLStreamException, SdmxException{
		return getCodes(eventReader, CODELIST, CODE, ID, DESCRIPTION);
	}
	public static Map<String, String> getCodes(XMLEventReader eventReader, String codelist, String code, String id, String description) throws XMLStreamException, SdmxException{
		Map<String,String> codes = new Hashtable<String,String>();

		String key = null;
		LocalizedText value = new LocalizedText(Configuration.getLang());
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart() == (code)) {
					value.clear();
					key = null;
					
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext()) {
						Attribute attr = attributes.next();
						if (attr.getName().toString().equals(id)) {
							key = attr.getValue();
						}
					}
				}
				else if (startElement.getName().getLocalPart() == (description)) {
					value.setText(startElement, eventReader);
				}
				
			}
			
			if (event.isEndElement()) {
				String eventName=event.asEndElement().getName().getLocalPart();
				if (eventName.equals(code)) {
					if(key != null){
						logger.finer("Got code " + key + ", " + value.getText());
						codes.put(key, value.getText());
					}
					else{
						throw new SdmxXmlContentException("Error during Codelist Parsing. Invalid code id: " + key);
					}
				}
				else{
					//stop after first codelist
					if (eventName.equals(codelist)) {
						break;
					}
				}
			}
		}
		return codes;
	}
} 
