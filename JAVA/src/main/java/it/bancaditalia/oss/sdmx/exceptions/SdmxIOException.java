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
package it.bancaditalia.oss.sdmx.exceptions;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

public class SdmxIOException extends SdmxException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	SdmxIOException(IOException cause) {
		super("Connection problems while talking to endpoint: " + cause.getMessage(), cause);
	}

	public SdmxIOException(String message, IOException cause) {
		super("Connection problems while talking to endpoint: " + message, cause);
	}

	public SdmxIOException(XMLStreamException cause) {
		super("Connection problems while retrieving xml from endpoint: " + cause.getCause().getMessage(), cause);
	}

	public SdmxIOException(SdmxResponseException cause) {
		super("Endpoint currently has problems: " + cause.getMessage(), cause);
	}
}
