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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Valentino Pinna
 *
 */
public class Codelist implements Iterable<String>, Map<String, String>, Serializable
{
	/**
	 * 
	 */
	private static final long			serialVersionUID	= 1L;

	private String						id					= null;
	private String						agency				= null;
	private String						version				= null;

	private final Map<String, String>	codes				= new HashMap<>();
	private final Map<String, String>	parents				= new HashMap<>();

	/**
	 * Creates a codelist with givem id, agency and version.
	 * 
	 * @param id The id of the codelist
	 * @param agency The agency of the codelist
	 * @param version The version of the codelist
	 */
	public Codelist(String id, String agency, String version)
	{
		this.id = id;
		this.agency = agency;
		this.version = version;
	}

	/**
	 * Creates an empty codelist.
	 */
	public Codelist()
	{
		this.id = null;
		this.agency = null;
		this.version = null;
	}

	public Codelist(Map<String, String> codes, Map<String, String> parents)
	{
		this();
		putAll(codes);
		this.parents.putAll(parents);
	}

	@Override
	public Iterator<String> iterator()
	{
		return keySet().iterator();
	}

	/**
	 * @return A depth-first view over the hierarchy of this codelist
	 */
	/*
	 * public Iterator<String> depthFirstIterator() { final Map<String, Collection<String>> children = new HashMap<>();
	 * final Set<String> roots = new HashSet<>(codes.keySet());
	 * 
	 * for (Entry<String, String> child : parents.entrySet()) { if (!children.containsKey(child.getValue()))
	 * children.put(child.getValue(), new ArrayList<String>());
	 * 
	 * children.get(child.getValue()).add(child.getKey()); roots.remove(child.getKey()); }
	 * 
	 * final Stack<Iterator<String>> stack = new Stack<>(); stack.push(roots.iterator());
	 * 
	 * return new Iterator<String>() { String current;
	 * 
	 * @Override public boolean hasNext() { if (stack.peek().hasNext()) return true;
	 * 
	 * while (!stack.isEmpty() && !stack.peek().hasNext()) stack.pop();
	 * 
	 * return !stack.isEmpty() && stack.peek().hasNext(); }
	 * 
	 * @Override public String next() { current = stack.peek().next(); if (children.containsKey(current))
	 * stack.push(children.get(current).iterator());
	 * 
	 * return current; }
	 * 
	 * @Override public void remove() { throw new UnsupportedOperationException("Not implemented"); } }; }
	 */

	/**
	 * @param codes
	 */
	public void setCodes(Map<String, String> codes)
	{
		if (codes != null)
		{
			clear();
			parents.clear();
			putAll(codes);
		}
	}

	/**
	 * @deprecated Use this object directly
	 * @return A map of codes names and descriptions in this codelist
	 */
	@Deprecated
	public Map<String, String> getCodes()
	{
		return this;
	}

	/**
	 * @return The parent of a code if it exists, null otherwise
	 */
	public String getParent(String code)
	{
		return parents.get(code);
	}

	/**
	 * @deprecated Use this object directly
	 * @return The description of a code if it exists, null otherwise
	 */
	@Deprecated
	public String getDescription(String code)
	{
		return get(code);
	}

	/**
	 * @return The codelist id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * @return The codelist agency
	 */
	public String getAgency()
	{
		return agency;
	}

	/**
	 * @param agency
	 */
	public void setAgency(String agency)
	{
		this.agency = agency;
	}

	/**
	 * @return The codelist version
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * @param version
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}

	/**
	 * @return The full identifier of this codelist in the form "agency/id/version".
	 */
	public String getFullIdentifier()
	{
		String codelist = id;
		if (agency != null)
		{
			codelist = agency + "/" + codelist;
		}

		if (version != null)
		{
			codelist = codelist + "/" + version;
		}
		return codelist;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Codelist [id=").append(getFullIdentifier()).append(", codes=").append(codes.toString())
				.append("]");
		return builder.toString();
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
		return codes.entrySet();
	}

	@Override
	public boolean equals(Object o)
	{
		return codes.equals(o);
	}

	@Override
	public String get(Object key)
	{
		return codes.get(key);
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
	public String put(String key, String value)
	{
		return codes.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m)
	{
		codes.putAll(m);
	}

	@Override
	public String remove(Object key)
	{
		return codes.remove(key);
	}

	@Override
	public int size()
	{
		return codes.size();
	}

	@Override
	public Collection<String> values()
	{
		return codes.values();
	}
}
