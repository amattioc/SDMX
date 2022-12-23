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

import java.util.Iterator;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * @author Attilio Mattiocco
 *
 */
public class SeriesCountParser implements Parser<Integer>
{
	private static final String	sourceClass	= SeriesCountParser.class.getSimpleName();
	protected static Logger		logger		= Configuration.getSdmxLogger();

	// valid in V.3.0
	static final String			ANNOTATION		= "Annotation";
	static final String			ID			= "id";
	static final String			ANNOTATION_TITLE	= "AnnotationTitle";
	static final String			SERIES_COUNT	= "series_count";

	@Override
	public Integer parse(XMLEventReader eventReader, List<LanguageRange> languages)
			throws XMLStreamException, SdmxException
	{
		final String sourceMethod = "parse";
		logger.entering(sourceClass, sourceMethod);

		boolean isTSNumber = false;
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();
				if (ANNOTATION.equals(startElement.getName().getLocalPart()))
				{
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attr = attributes.next();
						if (ID.equals(attr.getName().getLocalPart()) && attr.getValue().equals(SERIES_COUNT))
							isTSNumber=true;
					}
				}
				else if (ANNOTATION_TITLE.equals(startElement.getName().getLocalPart()) && isTSNumber)
					return Integer.parseInt(eventReader.getElementText());
				
			}
		}
		
		return null;
	}
}
