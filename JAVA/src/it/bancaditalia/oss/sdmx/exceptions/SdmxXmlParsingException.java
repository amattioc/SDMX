package it.bancaditalia.oss.sdmx.exceptions;

import javax.xml.stream.XMLStreamException;

public class SdmxXmlParsingException extends SdmxException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	SdmxXmlParsingException(XMLStreamException cause) {
		super("Error detected while parsing SDMX response: " + cause.getMessage(), cause);
	}

}
