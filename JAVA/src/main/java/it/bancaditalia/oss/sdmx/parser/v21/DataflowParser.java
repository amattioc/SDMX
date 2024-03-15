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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.SDMXReference;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LocalizedText;

/**
 * @author Attilio Mattiocco
 *
 */
public class DataflowParser implements Parser<List<Dataflow>> {
	protected static Logger logger = Configuration.getSdmxLogger();
	
	private static final String DATAFLOW = "Dataflow";
	private static final String ID = "id";
	private static final String AGENCY = "agencyID";
	private static final String VERSION = "version";
	private static final String EXTERNAL = "isExternalReference";
	private static final String NAME = "Name";
	private static final String REF = "Ref";

	@Override
	public List<Dataflow> parse(XMLEventReader eventReader, List<LanguageRange> languages) throws XMLStreamException {
		List<Dataflow> dfList = new ArrayList<>();

		Dataflow df = null;

		LocalizedText currentName = new LocalizedText(languages);
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
						
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();

				if (startElement.getName().getLocalPart() == (DATAFLOW)) 
				{
					currentName = new LocalizedText(languages);
					String id = null, agency = null, version = null;
					boolean isExternalReference = false;
					for (Attribute attr: (Iterable<Attribute>) startElement::getAttributes)
						switch (attr.getName().toString())
						{
							case ID: id = attr.getValue(); break;
							case AGENCY: agency = attr.getValue(); break;
							case VERSION: version = attr.getValue(); break;
							case EXTERNAL: isExternalReference = attr.getValue().equalsIgnoreCase("true"); break;
						}
						//TODO: check what to do with these cases
						if(isExternalReference){
							logger.fine("Dataflow: " + agency + "," + id + "," + version + " has an externa reference");
							continue;
						}
						df = new Dataflow(id, agency, version, currentName);
				}
				if (startElement.getName().getLocalPart() == (NAME))
					currentName.setText(startElement, eventReader);
				if (startElement.getName().getLocalPart() == (REF))
				{
					String id = null, agency = null, version = null;
					for (Attribute attr: (Iterable<Attribute>) startElement::getAttributes)
						switch (attr.getName().toString())
						{
							case ID: id = attr.getValue(); break;
							case AGENCY: agency = attr.getValue(); break;
							case VERSION: version = attr.getValue(); break;
						}
					
					df.setDsdIdentifier(new SDMXReference(id, agency, version));
				}
			}
			else if (event.isEndElement() && DATAFLOW.equals(event.asEndElement().getName().getLocalPart())){ 
				dfList.add(df);
			}
		}
		
		return dfList;
	}

} 
