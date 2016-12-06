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

	public SdmxIOException(XMLStreamException cause) {
		super("Connection problems while retrieving xml from endpoint: " + cause.getCause().getMessage(), cause);
	}

	public SdmxIOException(SdmxResponseException cause) {
		super("Endpoint currently has problems: " + cause.getMessage(), cause);
	}
}
