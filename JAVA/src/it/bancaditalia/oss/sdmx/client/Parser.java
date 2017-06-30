package it.bancaditalia.oss.sdmx.client;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.LanguagePriorityList;

public interface Parser<T> 
{
	public T parse(Reader xmlReader, LanguagePriorityList languages) throws XMLStreamException, SdmxException;
}
