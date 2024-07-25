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
package it.bancaditalia.oss.sdmx.parser.v30;

import java.util.ArrayList;
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

import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * @author Attilio Mattiocco
 *
 */
public class AvailabilityParser implements Parser<Map<String, List<String>>>
{
	private static final String	sourceClass	= AvailabilityParser.class.getSimpleName();
	protected static Logger		logger		= Configuration.getSdmxLogger();

	// valid in V.3.0
	static final String			KEYVAL		= "KeyValue";
	static final String			ID			= "id";
	static final String			VALUE	= "Value";

	@Override
	public Map<String, List<String>> parse(XMLEventReader eventReader, List<LanguageRange> languages)
			throws XMLStreamException, SdmxException
	{
		return parse(eventReader, languages, KEYVAL, ID, VALUE);
	}

	public static Map<String, List<String>> parse(XMLEventReader eventReader, List<LanguageRange> languages, String keyval, String id, String value) throws XMLStreamException, SdmxException
	{
		final String sourceMethod = "parse";
		logger.entering(sourceClass, sourceMethod);

		Map<String, List<String>> dimensions = new LinkedHashMap<>();
		List<String> codes = null;
		String dimension = null;
		
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();
				if (keyval.equals(startElement.getName().getLocalPart()))
				{
					codes = new ArrayList<String>();

					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attr = attributes.next();
						if (id.equals(attr.getName().getLocalPart()))
							dimension = attr.getValue();
					}
				}
				else if (value.equals(startElement.getName().getLocalPart()))
					codes.add(eventReader.getElementText());
			}

			if (event.isEndElement())
			{
				String eventName = event.asEndElement().getName().getLocalPart();
				if (keyval.equals(eventName))
					if (dimension != null) {
						if(!dimension.equals("TIME_PERIOD")){ //TODO handle time period when available
							if(codes.size() > 0){
								logger.finer("Got dimension " + dimension);
								dimensions.put(dimension, codes);
							}
							else{
								throw new SdmxInvalidParameterException("The selection identifies an empty cube region");
							}
						}
					}
					else
						throw new SdmxXmlContentException("Error during Codelist Parsing. Invalid dimension");
			}
		}
		
		return dimensions;
	}
}
