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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.SDMXReference;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LocalizedText;

/**
 * @author Attilio Mattiocco
 *
 */
public class CodelistParser implements Parser<Codelist>
{
	protected static Logger		logger		= Configuration.getSdmxLogger();

	// valid in V.2.1
	static final String			CODELIST	= "Codelist";
	static final String			CODE_ID		= "Code";
	static final String			ID			= "id";
	static final String			CODE_DESCRIPTION	= "Name";
	static final String			PARENT		= "Parent";
	static final String			REF			= "Ref";
	static final String 		AGENCY 		= "agencyID";
	static final String 		VERSION 	= "version";


	@Override
	public Codelist parse(XMLEventReader eventReader, List<LanguageRange> languages)
			throws XMLStreamException, SdmxException
	{
		return getCodeList(eventReader, languages);
	}

	public static Codelist getCodeList(XMLEventReader eventReader, List<LanguageRange> languages)
			throws XMLStreamException, SdmxException
	{
		String cl_id = null;
		String agency = null;
		String version = null;
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();
				if (CODELIST.equalsIgnoreCase(startElement.getName().getLocalPart()))
				{
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attr = attributes.next();
						if (attr.getName().toString().equals(ID))
						{
							cl_id = attr.getValue();
						}
						else if (attr.getName().toString().equals(AGENCY))
						{
							agency = attr.getValue();
						}
						else if (attr.getName().toString().equals(VERSION))
						{
							version = attr.getValue();
						}
					}
					logger.finest("Got codelist: " + cl_id);
					break;
				}
			}
		}
		return getCodes(new SDMXReference(cl_id, agency, version), eventReader, languages);
	}

	public static Codelist getCodes(SDMXReference coordinates, XMLEventReader eventReader, List<LanguageRange> languages) throws XMLStreamException, SdmxException
	{
		return getCodes(coordinates, eventReader, languages, ID, CODE_DESCRIPTION);
	}
	
	public static Codelist getCodes(SDMXReference coordinates, XMLEventReader eventReader, List<LanguageRange> languages,
									String valueID, String codeDescription) throws XMLStreamException, SdmxException
	{
		Map<String, LocalizedText> codes = new LinkedHashMap<>();
		Map<String, String> parents = new HashMap<>();
		Codelist result = null;
		
		String key = null;
		LocalizedText value = new LocalizedText(languages);
		boolean parent = false;
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();
				if (CODE_ID.equals(startElement.getName().getLocalPart()))
				{
					value = new LocalizedText(languages);
					key = null;

					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attr = attributes.next();
						if (valueID.equals(attr.getName().getLocalPart()))
							key = attr.getValue();
					}
				}
				else if (codeDescription.equals(startElement.getName().getLocalPart()))
					value.setText(startElement, eventReader);
				else if (PARENT.equals(startElement.getName().getLocalPart()))
					parent = true;
				else if (parent && REF.equals(startElement.getName().getLocalPart()))
				{
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attr = attributes.next();
						if (valueID.equals(attr.getName().getLocalPart()))
						{
							parents.put(key, attr.getValue());
							logger.finest("PARENT: " + key + " = " + attr.getValue());
						}
					}
				}
			}

			if (event.isEndElement())
			{
				String eventName = event.asEndElement().getName().getLocalPart();
				if (CODE_ID.equals(eventName))
					if (key != null)
					{
						logger.finest("Got code " + key + ", " + value.getText());
						codes.put(key, value);
					}
					else
						throw new SdmxXmlContentException("Error during Codelist Parsing. Invalid code id: " + key);
				else if (PARENT.equals(eventName))
					parent = false;
				else if (CODELIST.equalsIgnoreCase(eventName)){
					result = new Codelist(coordinates, codes, parents);
					break;
				}
			}
		}
		return result;
	}
}
