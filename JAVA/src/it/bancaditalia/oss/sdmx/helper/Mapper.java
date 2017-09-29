package it.bancaditalia.oss.sdmx.helper;

public interface Mapper<T> {
	public String[] toMapEntry(T item);
}
