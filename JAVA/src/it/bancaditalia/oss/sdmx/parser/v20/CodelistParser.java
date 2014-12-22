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

import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * @author Attilio Mattiocco
 *
 */
public class CodelistParser {
	// valid in V.2.0
	static final String CODELIST = "CodeList";
	static final String CODE = "Code";
	static final String ID = "value";
	static final String DESCRIPTION = "Description";

	public static Map<String,String> parse(InputStreamReader xmlBuffer) throws XMLStreamException, SdmxException, UnsupportedEncodingException {
		return it.bancaditalia.oss.sdmx.parser.v21.CodelistParser.parse(xmlBuffer, CODELIST, CODE, ID, DESCRIPTION);
	}
	
	public static Map<String, String> getCodes(XMLEventReader eventReader) throws XMLStreamException, SdmxException{
		return it.bancaditalia.oss.sdmx.parser.v21.CodelistParser.getCodes(eventReader, CODELIST, CODE, ID, DESCRIPTION);
	}
} 
