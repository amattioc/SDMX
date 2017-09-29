package it.bancaditalia.oss.sdmx.client;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.LanguagePriorityList;

public interface Parser<T> 
{
	public T parse(XMLEventReader eventReader, LanguagePriorityList languages) throws XMLStreamException, SdmxException;
}
