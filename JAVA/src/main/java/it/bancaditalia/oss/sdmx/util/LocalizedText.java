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
package it.bancaditalia.oss.sdmx.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

public class LocalizedText {
	private static final String LANG = "lang";
	
	private final LanguagePriorityList languages;
	private final Map<String, String> data;

	public LocalizedText(LanguagePriorityList languages) {
		super();
		this.languages = languages;
		this.data = new LinkedHashMap<>();
	}

	private void put(String lang, String text) {
		if (text != null) {
			data.put(lang, text);
		}
	}
	
	/**
	 * Gets a localized text.
	 * @return a text by using the best matching language if available
	 * or the first parsed text otherwise or null if nothing is available
	 */
	public String getText() {
		if (data.isEmpty()) {
			return null;
		}
		String lang = languages.lookupTag(data.keySet());
		return lang != null ? data.get(lang) : data.values().iterator().next();
	}

	public void setText(StartElement startElement, XMLEventReader eventReader) throws XMLStreamException{
		@SuppressWarnings("unchecked")
		Iterator<Attribute> attributes = startElement.getAttributes();
		while (attributes.hasNext()) {
			Attribute attribute = attributes.next();
			if (attribute.getName().getLocalPart().equals(LANG)) {
				put(attribute.getValue(), eventReader.getElementText());
			}
		}
	}
	
	public void clear(){
		data.clear();
	}
}
