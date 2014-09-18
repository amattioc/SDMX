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
package it.bankitalia.reri.sia.sdmx.parser.v20;

import it.bankitalia.reri.sia.util.Configuration;
import it.bankitalia.reri.sia.util.LocalizedText;
import it.bankitalia.reri.sia.util.SdmxException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

/**
 * @author Attilio Mattiocco
 *
 */
public class CodelistParser {
	private static final String sourceClass = CodelistParser.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();

	static final String CODELIST = "CodeList";

	static final String VALUE = "value";
	static final String KEY = "Code";
	static final String DESCRIPTION = "Description";

	public static Map<String,String> parse(String xmlBuffer) throws XMLStreamException, SdmxException, UnsupportedEncodingException {
		final String sourceMethod = "parse";
		logger.entering(sourceClass, sourceMethod);

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		InputStream in = new ByteArrayInputStream(xmlBuffer.getBytes("UTF-8"));
		XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
		Map<String,String> codes = getCodes(eventReader);
		
		
		
		logger.exiting(sourceClass, sourceMethod);
		return codes;
	}
	
	public static Map<String, String> getCodes(XMLEventReader eventReader) throws XMLStreamException, SdmxException{
		Map<String,String> codes = new Hashtable<String,String>();

		String key = null;
		LocalizedText value = new LocalizedText();

		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart() == (KEY)) {
					key = null;
					value = new LocalizedText();
					logger.finer("Got code element.");
					
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext()) {
						Attribute attr = attributes.next();
						if (attr.getName().toString().equals(VALUE)) {
							key = attr.getValue();
						}
					}
				}
				else if (startElement.getName().getLocalPart() == (DESCRIPTION)) {
					value.setText(startElement, eventReader);
				}
				
			}
			else if (event.isEndElement()) {
				String eventName=event.asEndElement().getName().getLocalPart();
				if (eventName.equals(KEY)) {
					if(key != null){
						codes.put(key, value.getText());
					}
					else{
						throw new SdmxException("Error during Codelist Parsing. Invalid code id: " + key);
					}
				}
				else{
					//stop after first codelist
					if (eventName.equals(CODELIST)) {
						break;
					}
				}
			}
		}
		return codes;
	}
} 
