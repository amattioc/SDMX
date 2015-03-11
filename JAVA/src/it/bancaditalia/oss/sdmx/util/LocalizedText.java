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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

public class LocalizedText {
	private String preferredLang = "en";
	private String preferredText = "";
	private String altText = "";
	private final String LANG = "lang";

	public LocalizedText(String preferredLang) {
		super();
		this.preferredLang = preferredLang;
	}

	public void setPreferredText(String englishText) {
		this.preferredText = englishText;
	}
	public String getEnglishText() {
		return preferredText;
	}
	public void setAltText(String altText) {
		this.altText = altText;
	}
	public String getAltText() {
		return altText;
	}
	public String getText() {
		return (!preferredText.isEmpty() ? preferredText : altText);
	}
	public void setText(StartElement startElement, XMLEventReader eventReader) throws XMLStreamException{
		String text = null;
		boolean preferred = false;
		@SuppressWarnings("unchecked")
		Iterator<Attribute> attributes = startElement.getAttributes();
		while (attributes.hasNext()) {
			Attribute attribute = attributes.next();
			if (attribute.getName().getLocalPart().equals(this.LANG)) {
				String lang = attribute.getValue();
				if(lang.equals(preferredLang)){
					preferred = true;
				}
			}
		}
		text = eventReader.getElementText();
		if(preferred){
			setPreferredText(text);
		}
		else{
			if(altText == null || altText.isEmpty()){
				setAltText(text);
			}
		}
	}
	
	public void clear(){
		preferredText = "";
		altText = "";
	}

}
