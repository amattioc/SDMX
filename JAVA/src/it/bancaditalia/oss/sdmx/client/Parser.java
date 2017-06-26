package it.bancaditalia.oss.sdmx.client;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

public interface Parser<T> 
{
	public T parse(Reader xmlReader) throws XMLStreamException, SdmxException;
}
