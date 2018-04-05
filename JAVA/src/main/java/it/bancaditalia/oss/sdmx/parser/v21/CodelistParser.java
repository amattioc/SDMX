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
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LanguagePriorityList;
import it.bancaditalia.oss.sdmx.util.LocalizedText;

/**
 * @author Attilio Mattiocco
 *
 */
public class CodelistParser implements Parser<Codelist>
{
	private static final String	sourceClass	= CodelistParser.class.getSimpleName();
	protected static Logger		logger		= Configuration.getSdmxLogger();

	// valid in V.2.1
	static final String			CODELIST	= "Codelist";
	static final String			CODE		= "Code";
	static final String			ID			= "id";
	static final String			DESCRIPTION	= "Name";
	static final String			PARENT		= "Parent";
	static final String			REF			= "Ref";

	@Override
	public Codelist parse(XMLEventReader eventReader, LanguagePriorityList languages)
			throws XMLStreamException, SdmxException
	{
		return parse(eventReader, languages, CODELIST, CODE, ID, DESCRIPTION);
	}

	public static Codelist parse(XMLEventReader eventReader, LanguagePriorityList languages, String codelist,
			String code, String id, String description) throws XMLStreamException, SdmxException
	{
		final String sourceMethod = "parse";
		logger.entering(sourceClass, sourceMethod);

		Codelist codes = getCodes(eventReader, languages, codelist, code, id, description);

		logger.exiting(sourceClass, sourceMethod);
		return codes;
	}

	public static Codelist getCodes(XMLEventReader eventReader, LanguagePriorityList languages)
			throws XMLStreamException, SdmxException
	{
		return getCodes(eventReader, languages, CODELIST, CODE, ID, DESCRIPTION);
	}

	public static Codelist getCodes(XMLEventReader eventReader, LanguagePriorityList languages,
			String codelist, String code, String id, String description) throws XMLStreamException, SdmxException
	{
		Map<String, String> codes = new LinkedHashMap<>();
		Map<String, String> parents = new HashMap<>();

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
				if (code.equals(startElement.getName().getLocalPart()))
				{
					value.clear();
					key = null;

					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attr = attributes.next();
						if (id.equals(attr.getName().getLocalPart()))
							key = attr.getValue();
					}
				}
				else if (description.equals(startElement.getName().getLocalPart()))
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
						if (id.equals(attr.getName().getLocalPart()))
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
				if (code.equals(eventName))
					if (key != null)
					{
						logger.finer("Got code " + key + ", " + value.getText());
						codes.put(key, value.getText());
					}
					else
						throw new SdmxXmlContentException("Error during Codelist Parsing. Invalid code id: " + key);
				else if (eventName.equals(codelist))
					// stop after first codelist
					break;
				else if (PARENT.equals(eventName))
					parent = false;
			}
		}
		
		return new Codelist(codes, parents);
	}
}
