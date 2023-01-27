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

import static java.util.Collections.singletonList;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

public class LocalizedText
{
	private static final String LANG = "lang";
	
	private final List<LanguageRange> languages;
	private final Map<String, String> data = new LinkedHashMap<>();

	public LocalizedText(String universalName)
	{
		languages = singletonList(new LanguageRange("*"));
		put("en", universalName);
	}
	
	public LocalizedText(List<LanguageRange> languages)
	{
		this.languages = languages;
	}

//	public LocalizedText(List<LanguageRange> languages, List<TextTypeWrapper> texts)
//	{
//		this.languages = languages;
//		for (TextTypeWrapper text: texts)
//			put(text.getLocale(), text.getValue());
//	}

	private void put(String lang, String text)
	{
		if (text != null)
			data.put(lang, text);
	}
	
	/**
	 * Gets a localized text.
	 * @return a text by using the best matching language if available
	 * or the first parsed text otherwise or null if nothing is available
	 */
	public String getText()
	{
		if (data.isEmpty())
			return null;

		String lang = Locale.lookupTag(languages, data.keySet());
		return lang != null ? data.get(lang) : data.values().iterator().next();
	}

	/**
	 * Parse a SDMX message snippet to retrieve a localized text
	 * 
	 * @param startElement
	 * @param eventReader
	 * @throws XMLStreamException
	 */
	public void setText(StartElement startElement, XMLEventReader eventReader) throws XMLStreamException
	{
		@SuppressWarnings("unchecked")
		Iterator<Attribute> attributes = startElement.getAttributes();
		while (attributes.hasNext())
		{
			Attribute attribute = attributes.next();
			if (attribute.getName().getLocalPart().equals(LANG))
				put(attribute.getValue(), eventReader.getElementText());
		}
	}

	@Override
	public String toString() {
		return "LocalizedText [" + data.entrySet()
        .stream()
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining(", ")) + "]";
	}
}
