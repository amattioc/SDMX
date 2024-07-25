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
package it.bancaditalia.oss.sdmx.api;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import it.bancaditalia.oss.sdmx.util.LocalizedText;

/**
 * @author Valentino Pinna
 *
 */
public class Codelist extends SDMXReference implements Iterable<String>, Map<String, String>, Serializable
{
	/**
	 * 
	 */
	private static final long			serialVersionUID	= 1L;

	private final Map<String, LocalizedText>	codes	= new HashMap<>();
	private final Map<String, String>			parents	= new HashMap<>();

	/**
	 * Creates a codelist with givem id, agency and version.
	 * 
	 * @param id The id of the codelist
	 * @param agency The agency of the codelist
	 * @param version The version of the codelist
	 */
	public Codelist(String id, String agency, String version)
	{
		super(id, agency, version);
	}

	public Codelist(SDMXReference coordinates, Map<String, LocalizedText> codes, Map<String, String> parents)
	{
		super(coordinates);
		if (codes != null)
			this.codes.putAll(codes);
		if (parents != null)
			this.parents.putAll(parents);
	}

	public void importFrom(Codelist other)
	{
		this.codes.putAll(other.codes);
		this.parents.putAll(other.parents);
	}

	@Override
	public Iterator<String> iterator()
	{
		return keySet().iterator();
	}

	/**
	 * Returns the parent of a code in a code hierarchy.
	 * 
	 * @param code The code
	 * @return The parent of a code if it exists, null otherwise
	 */
	public String getParent(String code)
	{
		return parents.get(code);
	}

	/**
	 * @param code 
	 * @return The description of a code if it exists, null otherwise

	 * @deprecated Use this object directly.
	 */
	@Deprecated
	public String getDescription(String code)
	{
		return get(code);
	}

	@Override
	public String toString()
	{
		return String.format("Codelist [id=%s, codes=%s]", getFullIdentifier(), codes);
	}

	@Override
	public void clear()
	{
		codes.clear();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return codes.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return codes.containsValue(value);
	}

	@Override
	public Set<Entry<String, String>> entrySet()
	{
		return codes.keySet().stream().map(c -> new SimpleEntry<>(c, codes.get(c).getText())).collect(toSet());
	}

	@Override
	public boolean equals(Object o)
	{
		return codes.equals(o);
	}

	@Override
	public String get(Object key)
	{
		return codes.get(key).getText();
	}

	@Override
	public int hashCode()
	{
		return codes.hashCode();
	}

	@Override
	public boolean isEmpty()
	{
		return codes.isEmpty();
	}

	@Override
	public Set<String> keySet()
	{
		return codes.keySet();
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m)
	{
		throw new UnsupportedOperationException("putAll");
	}

	@Override
	public String remove(Object key)
	{
		throw new UnsupportedOperationException("remove");
	}

	@Override
	public int size()
	{
		return codes.size();
	}

	@Override
	public Collection<String> values()
	{
		return codes.values().stream().map(LocalizedText::getText).collect(toList());
	}

	@Override
	public String put(String key, String value)
	{
		throw new UnsupportedOperationException("put");
	}

	public Map<String, LocalizedText> localizedCodes()
	{
		return codes;
	}
}
