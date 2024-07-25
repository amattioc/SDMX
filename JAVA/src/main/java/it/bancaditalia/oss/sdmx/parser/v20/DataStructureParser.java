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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.SDMXReference;
import it.bancaditalia.oss.sdmx.api.SdmxAttribute;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.parser.v21.CodelistParser;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LocalizedText;

/**
 * @author Attilio Mattiocco
 *
 */
public class DataStructureParser implements Parser<List<DataFlowStructure>>
{
	private static final String	sourceClass				= DataStructureParser.class.getSimpleName();
	protected static Logger		logger					= Configuration.getSdmxLogger();

	static final String			DATASTRUCTURE			= "KeyFamily";

	static final String			CODELISTS				= "CodeLists";
	static final String			CODELIST				= "CodeList";
	static final String			CODELISTAGENCY			= "codelistAgency";
	static final String 		CODE 					= "Code";
	static final String 		CODE_ID 				= "value";
	static final String 		CODE_DESCRIPTION 		= "Description";

	static final String			CONCEPTS				= "Concepts";
	static final String			CONCEPTSCHEME			= "ConceptScheme";
	static final String			CONCEPT					= "Concept";

	static final String			COMPONENTS				= "Components";
	static final String			NAME					= "Name";

	static final String			DIMENSION				= "Dimension";
	static final String			ATTRIBUTE				= "Attribute";
	static final String			TIMEDIMENSION			= "TimeDimension";
	static final String			PRIMARYMEASURE			= "PrimaryMeasure";
	static final String			CONCEPT_REF				= "conceptRef";

	static final String			ID						= "id";
	static final String			AGENCYID				= "agencyID";

	static final String			LOCAL_REPRESENTATION	= "LocalRepresentation";
	static final String			REF						= "Ref";

	@Override
	public List<DataFlowStructure> parse(XMLEventReader eventReader, List<LanguageRange> languages)
			throws XMLStreamException, SdmxException
	{
		final String sourceMethod = "parse";
		logger.entering(sourceClass, sourceMethod);

		List<DataFlowStructure> result = new ArrayList<>();
		Map<String, Codelist> codelists = null;
		Map<String, String> concepts = null;
		DataFlowStructure currentStructure = null;

		LocalizedText currentName = new LocalizedText(languages);
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());

			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();

				if (startElement.getName().getLocalPart() == (CODELISTS))
				{
					codelists = getCodelists(eventReader, languages);
				}
				else if (startElement.getName().getLocalPart() == (CONCEPTS))
				{
					concepts = getConcepts(eventReader, languages);
				}
				else if (startElement.getName().getLocalPart() == (DATASTRUCTURE))
				{
					currentName = new LocalizedText(languages);
					String id = null, agency = null;
					for (Attribute attr: (Iterable<Attribute>) startElement::getAttributes)
						switch (attr.getName().toString())
						{
							case ID: id = attr.getValue(); break;
							case AGENCYID: agency = attr.getValue(); break;
						}
					// TODO: No version?
					currentStructure = new DataFlowStructure(id, agency, null);
				}
				else if (startElement.getName().getLocalPart().equals(NAME))
				{
					// this has to be checked better
					if (currentStructure != null)
					{
						currentName.setText(startElement, eventReader);
					}
				}
				else if (startElement.getName().getLocalPart().equals(COMPONENTS))
				{
					if (currentStructure != null)
					{
						setStructureDimensionsAndAttributes(currentStructure, eventReader, codelists, concepts);
					}
					else
					{
						throw new RuntimeException("Error during Structure Parsing. Null current structure.");
					}
				}
			}

			if (event.isEndElement())
			{
				if (event.asEndElement().getName().getLocalPart().equals(DATASTRUCTURE))
				{
					logger.finer("Adding data structure. " + currentStructure);
					currentStructure.setName(currentName.getText());
					result.add(currentStructure);
				}
			}
		}
		logger.exiting(sourceClass, sourceMethod);
		return result;
	}

	private static void setStructureDimensionsAndAttributes(DataFlowStructure currentStructure,
			XMLEventReader eventReader, Map<String, Codelist> codelists, Map<String, String> concepts)
			throws XMLStreamException
	{
		final String sourceMethod = "setStructureDimensions";
		logger.entering(sourceClass, sourceMethod);

		String agency = currentStructure.getAgency();
		int position = 0;

		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals(DIMENSION)
						|| startElement.getName().getLocalPart().equals(ATTRIBUTE))
				{
					boolean isDimension = startElement.getName().getLocalPart().equals(DIMENSION);
					logger.finer(isDimension ? "Got dimension" : "Got attribute");
					
					String id = null;
					String name = null;
					String codelistID = null;
					String codelistAgency = null;
					Codelist codeList = null;

					for (Attribute attribute: (Iterable<Attribute>) startElement::getAttributes)
						if (attribute.getName().toString().equals(CONCEPT_REF))
							id = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase(CODELIST))
							codelistID = attribute.getValue();
						else if (attribute.getName().toString().equals(CODELISTAGENCY))
							codelistAgency = attribute.getValue();

					if (id == null || id.isEmpty())
						throw new RuntimeException("Error during Structure Parsing. Invalid id: " + id);
					else if (concepts != null)
						name = concepts.get(agency + "/" + id);
					
					if (codelistID != null && !codelistID.isEmpty())
					{
						SDMXReference cl = new SDMXReference(codelistID, codelistAgency != null ? codelistAgency : agency, null);
						if (codelists != null)
							codeList = codelists.get(cl.getFullIdentifier());
					}
					else if (isDimension)
						throw new RuntimeException("Error during Structure Parsing. Invalid CODELIST: " + codelistID);
					
					if (isDimension)
					{
						Dimension dimension = new Dimension(id, name, codeList, ++position);
						logger.finer("Adding dimension: " + dimension);
						currentStructure.addDimension(dimension);
					}
					else
					{
						SdmxAttribute attribute = new SdmxAttribute(id, name, codeList);
						logger.finer("Adding attribute: " + attribute);
						currentStructure.addAttribute(attribute);
					}
				}
				else if (startElement.getName().getLocalPart().equals((TIMEDIMENSION)))
				{
					logger.finer("Got time dimension");
					String id = null;
					
					for (Attribute attribute: (Iterable<Attribute>) startElement::getAttributes)
						if (attribute.getName().toString().equals(CONCEPT_REF))
							id = attribute.getValue();
					
					if (id != null && !id.isEmpty())
						if (currentStructure != null)
						{
							logger.finer("Adding time dimension: " + id);
							currentStructure.setTimeDimension(id);
						}
						else
						{
							throw new RuntimeException("Error during Structure Parsing. Null current Structure.");
						}
					else
						throw new RuntimeException("Error during Structure Parsing. Invalid time dimension: " + id);
				}
				else if (startElement.getName().getLocalPart().equals((PRIMARYMEASURE)))
				{
					logger.finer("Got primary measure");
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					String id = null;
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equals(CONCEPT_REF))
						{
							id = attribute.getValue();
						}
					}
					if (id != null && !id.isEmpty())
					{
						if (currentStructure != null)
						{
							logger.finer("Adding primary measure: " + id);
							currentStructure.setMeasure(id);
						}
						else
						{
							throw new RuntimeException("Error during Structure Parsing. Null current Structure.");
						}
					}
					else
					{
						throw new RuntimeException("Error during Structure Parsing. Invalid primary measure: " + id);
					}
					continue;
				}
			}

			if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(COMPONENTS))
				break;
		}

		logger.exiting(sourceClass, sourceMethod);
	}

	private static Map<String, Codelist> getCodelists(XMLEventReader eventReader,
			List<LanguageRange> languages) throws XMLStreamException, SdmxException
	{
		Map<String, Codelist> codelists = new HashMap<>();
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();

				if (startElement.getName().getLocalPart().equalsIgnoreCase(CODELIST))
				{
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					String id = null;
					String agency = null;
					while (attributes.hasNext())
					{
						Attribute attr = attributes.next();
						if (attr.getName().toString().equals(ID))
							id = attr.getValue();
						else if (attr.getName().toString().equals(AGENCYID))
							agency = attr.getValue();
					}

					logger.finer("Got codelist: " + id);
					Codelist codes = CodelistParser.getCodes(new SDMXReference(id, agency, null), eventReader, languages, 
							CODE_ID, CODE_DESCRIPTION);
					codelists.put(codes.getFullIdentifier(), codes);
				}
			}
			else if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(CODELISTS))
				break;
		}

		return codelists;
	}

	private static Map<String, String> getConcepts(XMLEventReader eventReader, List<LanguageRange> languages)
			throws XMLStreamException, SdmxException
	{
		Map<String, String> concepts = new HashMap<>();
		String conceptSchemeAgency = null;
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();

				if (startElement.getName().getLocalPart().equals(CONCEPTSCHEME))
				{
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					conceptSchemeAgency = null;
					while (attributes.hasNext())
					{
						Attribute attr = attributes.next();
						if (attr.getName().toString().equals(AGENCYID))
							conceptSchemeAgency = attr.getValue();
					}
					logger.finer("Got conceptSchemeAgency: " + conceptSchemeAgency);
				}
				else if (startElement.getName().getLocalPart().equals(CONCEPT))
				{
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					String id = null;
					String agency = null;
					String conceptName = "";
					while (attributes.hasNext())
					{
						Attribute attr = attributes.next();
						if (attr.getName().toString().equals(ID))
							id = attr.getValue();
						else if (attr.getName().toString().equals(AGENCYID))
							agency = attr.getValue();
					}

					if (agency == null && conceptSchemeAgency != null)
						agency = conceptSchemeAgency;

					conceptName = agency + "/" + id;
					logger.finer("Got concept: " + conceptName);
					concepts.put(conceptName, getConceptName(eventReader, languages));
				}
			}
			else if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(CONCEPTS))
				break;
		}

		return (concepts);
	}

	private static String getConceptName(XMLEventReader eventReader, List<LanguageRange> languages)
			throws XMLStreamException, SdmxException
	{
		LocalizedText value = new LocalizedText(languages);
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement())
			{
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart() == ("Name"))
				{
					value.setText(startElement, eventReader);
				}

			}
			else if (event.isEndElement() && CONCEPT.equals(event.asEndElement().getName().getLocalPart()))
				break;
		}

		return value.getText();
	}

}
