package it.bancaditalia.oss.sdmx.client;

import java.util.List;
import java.util.Locale.LanguageRange;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

public interface Parser<T> 
{
	public T parse(XMLEventReader eventReader, List<LanguageRange> languages) throws XMLStreamException, SdmxException;
}
