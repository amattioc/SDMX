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
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.RandomAccess;
import java.util.Set;
import java.util.logging.Logger;

import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.Utils;
import it.bancaditalia.oss.sdmx.util.Utils.BiFunction;
import it.bancaditalia.oss.sdmx.util.Utils.Function;

/**
 * This is a Java container for a Time Series. It will be transformed by a converter in the various statistical packages
 * into a native time series object.
 * 
 * @author Attilio Mattiocco
 *
 */

public class PortableTimeSeries<T> implements List<BaseObservation<? extends T>>, RandomAccess, Serializable
{

	private static final long							serialVersionUID		= 1L;

	public static final String							GENERATEDNAME_ATTR_NAME	= "CONNECTORS_AUTONAME";
	protected static final Logger						logger					= Configuration.getSdmxLogger();

	// note that the dimensions have to be ordered as prescribed by the DSD
	// Map each dimension to its content representation (list of codes)
	// TODO: implement a Code class and use Map<Dimension, List<Code>>.
	private final Map<String, Entry<String, String>>	dimensions				= new LinkedHashMap<>();
	private final List<BaseObservation<? extends T>>	observations			= new ArrayList<>();
	private final Map<String, String>					attributes				= new HashMap<>();

	private boolean										errorFlag				= false;
	private boolean										numeric					= true;
	private String										errorMessage			= null;
	private String										name;
	private boolean										useGeneratedName		= true;
	private String										frequency				= null;
	private Dataflow									dataflow				= null;

	private class ListWrapper<T1> extends AbstractList<T1>
	{
		final Function<BaseObservation<? extends T>, T1> func;

		public ListWrapper(Function<BaseObservation<? extends T>, T1> f)
		{
			func = f;
		}

		@Override
		public int size()
		{
			return PortableTimeSeries.this.size();
		}

		@Override
		public T1 get(int index)
		{
			return func.apply(PortableTimeSeries.this.get(index));
		}
	}

	/**
	 * Creates an empty time series, ready to be populated
	 */
	public PortableTimeSeries()
	{

	}

	/**
	 * Build an empty series which has the same attributes and dimensions as another non-null series
	 * 
	 * @param other The original series to copy
	 */
	public PortableTimeSeries(PortableTimeSeries<?> other)
	{
		errorFlag = other.errorFlag;
		numeric = other.numeric;
		errorMessage = other.errorMessage;
		name = other.name;
		useGeneratedName = other.useGeneratedName;
		frequency = other.frequency;
		dataflow = other.dataflow;

		attributes.putAll(other.attributes);
		dimensions.putAll(other.dimensions);

		name = other.name;
	}

	/**
	 * Compare two series metadata to determine if they match.
	 * 
	 * @param other a series to compare to this series.
	 * @return true if other is not null, both series aren't error series and the series match frequency and size.
	 */
	public boolean matchesMetadata(PortableTimeSeries<?> other)
	{
		return !errorFlag && other != null && !other.errorFlag && size() == other.size()
				&& frequency.equals(other.frequency);
	}

	/**
	 * @return The map of series' attributes.
	 */
	public Map<String, String> getAttributesMap()
	{
		if (!attributes.containsKey(GENERATEDNAME_ATTR_NAME) && dataflow != null && dataflow.getName() != null)
		{
			StringBuilder nameBuilder = new StringBuilder();

			if (dataflow != null && dataflow.getName() != null && !dataflow.getName().isEmpty())
				nameBuilder.append(dataflow.getName());

			for (Entry<String, String> code : dimensions.values())
				nameBuilder.append(", " + code.getKey() + "(" + (code.getValue() != null ? code.getValue() : "") + ")");

			attributes.put(GENERATEDNAME_ATTR_NAME, nameBuilder.toString());
		}

		return attributes;
	}

	/**
	 * Creates a new PortableTimeSeries over the same time slots of this series with each of its values being set equal
	 * to the result of respectively applying a function over each of this series' values.
	 * 
	 * @param mapper The function that maps values.
	 * @return The new PortableTimeSeries
	 * 
	 * @throws NullPointerException if mapper is null.
	 */
	public <R> PortableTimeSeries<R> mapValues(Function<T, R> mapper)
	{
		PortableTimeSeries<R> newSeries = new PortableTimeSeries<>(this);

		for (BaseObservation<? extends T> obs : this)
			newSeries.add(obs.mapValue(mapper));

		return newSeries;
	}

	/**
	 * Creates a new PortableTimeSeries over the same time slots of this series with each of its values being set equal
	 * to the result of respectively applying a function over each corresponding pair of values of this and another
	 * PortableTimeSeries.
	 * 
	 * @param other The other series to combine with this series
	 * @param combiner The function that combines two values.
	 * @return The new PortableTimeSeries
	 * 
	 * @throws UnsupportedOperationException if the two series metadata do not match, as defined by
	 *             {@link #matchesMetadata(PortableTimeSeries)}.
	 */
	public <U, R> PortableTimeSeries<R> combineValues(PortableTimeSeries<U> other, BiFunction<T, U, R> combiner)
	{
		if (size() != other.size())
			throw new UnsupportedOperationException("The two series do not have the same size.");

		PortableTimeSeries<R> newSeries = new PortableTimeSeries<>(this);
		Iterator<BaseObservation<? extends U>> iter = other.iterator();

		for (BaseObservation<? extends T> obs : this)
			newSeries.add(obs.combine(iter.next(), combiner));

		return newSeries;
	}

	/**
	 * @return An array containing the names of series' attributes.
	 */
	public String[] getAttributeNamesArray()
	{
		return getAttributesMap().keySet().toArray(new String[0]);
	}

	/**
	 * @param code A series' attribute name.
	 * @return The value of the attribute, or null if the attribute is not defined.
	 */
	public String getAttribute(String code)
	{
		return getAttributesMap().get(code);
	}

	/**
	 * Set the series' attributes names and values. Any previous attribute, if defined, will be cleared.
	 * 
	 * @param attributes A non-null map containing attribute bindings.
	 */
	public void setAttributes(Map<String, String> attributes)
	{
		getAttributesMap().clear();
		getAttributesMap().putAll(attributes);
	}

	/**
	 * Adds or update an attribute value.
	 * 
	 * @param key The name of series' attribute. Must be non-null.
	 * @param value The value of the attribute.
	 */
	public void addAttribute(String key, String value)
	{
		this.getAttributesMap().put(key, value);
	}

	/**
	 * @return a map of codes for each defined dimension, with dimensions as keys and codes as values.
	 */
	public Map<String, String> getDimensionsMap()
	{
		Map<String, String> result = new LinkedHashMap<>();
		for (Entry<String, Entry<String, String>> dimension : dimensions.entrySet())
			result.put(dimension.getKey(),
					Configuration.getCodesPolicy().equalsIgnoreCase(Configuration.SDMX_CODES_POLICY_DESC)
							? dimension.getValue().getValue()
							: dimension.getValue().getKey());

		return result;
	}

	/**
	 * @return an array of defined dimensions names.
	 */
	public String[] getDimensionNamesArray()
	{
		return dimensions.keySet().toArray(new String[0]);
	}

	/**
	 * @param code The name of the dimension.
	 * @return The code associated to given dimension, or null if the dimension is not defined.
	 */
	public String getDimension(String code)
	{
		return dimensions.containsKey(code) ? dimensions.get(code).getKey() : null;
	}

	/**
	 * @param dimensions
	 */
	public void setDimensions(Map<String, Entry<String, String>> dimensions)
	{
		this.dimensions.clear();
		this.dimensions.putAll(dimensions);
		attributes.remove(GENERATEDNAME_ATTR_NAME);
		if (useGeneratedName)
			name = null;
	}

	/**
	 * @param key Dimension key
	 * @param value Dimension value
	 */
	public void addDimension(String key, String value)
	{
		this.dimensions.put(key, new AbstractMap.SimpleEntry<String, String>(value, null));
		attributes.remove(GENERATEDNAME_ATTR_NAME);
		if (useGeneratedName)
			name = null;
	}

	/**
	 * @return A list of series' observation values, in the same order as they were added to this series.
	 * @deprecated Use the series natural iterator to obtain values instead
	 */
	@Deprecated
	public List<T> getObservations()
	{
		return new ListWrapper<>(Utils.<T>obsExtractor());
	}

	/**
	 * @return An array containing this series' observation values, in the same order as they were added.
	 * 
	 * @deprecated Use the series natural iterator to obtain values instead
	 */
	@Deprecated
	public Object[] getObservationsArray()
	{
		if (isNumeric())
		{
			return getObservations().toArray(new Double[0]);
		}
		else
		{
			return getObservations().toArray(new String[0]);
		}
	}

	/**
	 * Returns the same value as {@link #mapValues(Function) mapValues(Utils.TIMESLOTEXTRACTOR)}
	 * 
	 * @return A collection of series' observation timestamps, in the same order as they were added to this series.
	 * 
	 * @deprecated Use the series natural iterator to obtain timeslots instead
	 */
	@Deprecated
	public List<String> getTimeSlots()
	{
		return new ListWrapper<>(Utils.<T>timeslotExtractor());
	}

	/**
	 * @return An array containing this series' observation timestamps, in the same order as they were added.
	 * @deprecated Use the series natural iterator to obtain timeslots instead
	 */
	@Deprecated
	public String[] getTimeSlotsArray()
	{
		return getTimeSlots().toArray(new String[0]);
	}

	/**
	 * @return A collection of this series' observation-level attribute names.
	 */
	public Set<String> getObsLevelAttributesNames()
	{
		Set<String> result = new HashSet<>();

		for (BaseObservation<? extends T> obs : this)
			result.addAll(obs.getAttributes().keySet());

		return result;
	}

	/**
	 * @return An collection this series' observation-level attribute names.
	 */
	public String[] getObsLevelAttributesNamesArray()
	{
		return getObsLevelAttributesNames().toArray(new String[0]);
	}

	/**
	 * @param attributeName A name of an observation-level attribute.
	 * @return A list of series' observation-level attribute values for given name, in the same order as each
	 *         observation was added to this series.
	 */
	public List<String> getObsLevelAttributes(String attributeName)
	{
		return new ListWrapper<>(Utils.<T>obsLevelAttrsExtractor(attributeName));
	}

	/**
	 * @param attributeName A name of an observation-level attribute.
	 * @return A list of series' observation-level attribute values for given name, in the same order as each
	 *         observation was added to this series.
	 */
	public String[] getObsLevelAttributesArray(String attributeName)
	{
		return getObsLevelAttributes(attributeName).toArray(new String[0]);
	}

	/**
	 * Returns a sensible name for this series, eventually generating a default name from codes.
	 * 
	 * @return The name of this series
	 */
	public String getName()
	{
		if (useGeneratedName && name == null)
		{
			// we determine the name in the SDMX way
			StringBuilder nameBuilder = new StringBuilder();

			if (dataflow != null && dataflow.getId() != null && !dataflow.getId().isEmpty())
				nameBuilder.append(dataflow.getId() + ".");

			for (Entry<String, String> code : dimensions.values())
				nameBuilder
						.append((Configuration.getCodesPolicy().equalsIgnoreCase(Configuration.SDMX_CODES_POLICY_DESC)
								? code.getValue()
								: code.getKey()) + ".");

			// remove last dot
			if (nameBuilder.length() > 0)
				nameBuilder.setLength(nameBuilder.length() - 1);
			name = nameBuilder.toString();
		}

		return name;
	}

	/**
	 * @param name Sets a proper name for this series
	 */
	public void setName(String name)
	{
		useGeneratedName = false;
		this.name = name;
	}

	/**
	 * @return The frequency of this series
	 */
	public String getFrequency()
	{
		return frequency;
	}

	/**
	 * @param frequency
	 */
	public void setFrequency(String frequency)
	{
		this.frequency = frequency;
	}

	/**
	 * @return The identifier of the dataflow containing this series, or null if it is not available.
	 */
	public String getDataflow()
	{
		return dataflow.getId();
	}

	/**
	 * @return The dataflow containing this series, or null if it is not available.
	 */
	public Dataflow getDataflowObject()
	{
		return dataflow;
	}

	/**
	 * @param dataflow
	 */
	public void setDataflow(Dataflow dataflow)
	{
		this.dataflow = dataflow;
	}

	/**
	 * @return True if there was an error creating or updating this series.
	 */
	public boolean isErrorFlag()
	{
		return errorFlag;
	}

	/**
	 * @param errorFlag
	 */
	public void setErrorFlag(boolean errorFlag)
	{
		this.errorFlag = errorFlag;
	}

	/**
	 * @return The error message.
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 */
	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	/**
	 * Reverses this series' observation order.
	 */
	public void reverse()
	{
		Collections.reverse(this);
	}

	/**
	 * @return True if this series contains numeric observation values, false otherwise.
	 */
	public boolean isNumeric()
	{
		return numeric;
	}

	/**
	 * @param numeric
	 */
	public void setNumeric(boolean numeric)
	{
		this.numeric = numeric;
	}

	/**
	 * @return a default representation of this series.
	 */
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder(50000);
		buffer.append("\nName: " + getName());
		buffer.append("\nFrequency: " + frequency);
		buffer.append("\nnumeric: " + isNumeric());
		buffer.append("\nerror: " + isErrorFlag());
		buffer.append("\nerror_msg: " + getErrorMessage());
		buffer.append("\nAttributes: " + getAttributesMap());
		buffer.append("\nDimensions: " + getDimensionsMap());
		buffer.append("\nVALUES: ");
		buffer.append(getObservations());
		buffer.append("\nTIMES:");
		buffer.append(getTimeSlots());
		buffer.append("\nOBSERVATION ATTRIBUTES:");

		buffer.append("{");
		boolean first = true;

		for (String attrName : getObsLevelAttributesNames())
		{
			buffer.append((first ? "" : ", ") + attrName + "=");
			buffer.append(getObsLevelAttributes(attrName));
			first = false;
		}

		return buffer.toString();
	}

	/**
	 * @return
	 */
	@Deprecated
	public List<String> getAttributes()
	{
		return Arrays.asList(getAttributesArray());
	}

	/**
	 * @param code
	 * @return
	 */
	@Deprecated
	public String getAttributeValue(String code)
	{
		return getAttribute(code);
	}

	/**
	 * @return
	 */
	@Deprecated
	public String[] getAttributesArray()
	{
		String[] result = new String[getAttributesMap().size()];
		int i = 0;
		for (Entry<String, String> x : getAttributesMap().entrySet())
			result[i++] = x.getKey() + "=" + x.getValue();
		return result;
	}

	/**
	 * @param attributes
	 */
	@Deprecated
	public void setAttributes(List<String> attributes)
	{
		this.getAttributesMap().clear();
		for (String pair : attributes)
			this.getAttributesMap().put(pair.split("=")[0], pair.split("=")[1]);
	}

	/**
	 * @param attribute
	 */
	@Deprecated
	public void addAttribute(String attribute)
	{
		addAttribute(attribute.split("=")[0], attribute.split("=")[1]);
	}

	/**
	 * @return
	 */
	@Deprecated
	public List<String> getDimensions()
	{
		return Arrays.asList(getDimensionsArray());
	}

	/**
	 * @param code
	 * @return
	 */
	@Deprecated
	public String getDimensionValue(String code)
	{
		return getDimension(code);
	}

	/**
	 * @return
	 */
	@Deprecated
	public String[] getDimensionsArray()
	{
		String[] result = new String[dimensions.size()];
		int i = 0;
		for (Entry<String, Entry<String, String>> code : dimensions.entrySet())
			result[i++] = code.getKey() + "=" + code.getValue().getKey();
		return result;
	}

	/**
	 * @param dimension
	 */
	@Deprecated
	public void addDimension(String dimension)
	{
		addDimension(dimension.split("=")[0], dimension.split("=")[1]);
	}

	/**
	 * @return
	 */
	@Deprecated
	public List<String> getStatus()
	{
		return getObsLevelAttributes("OBS_STATUS");
	}

	/**
	 * @return
	 */
	@Deprecated
	public String[] getStatusArray()
	{
		return getStatus().toArray(new String[0]);
	}

	@Override
	public boolean add(BaseObservation<? extends T> e)
	{
		return observations.add(e);
	}

	@Override
	public void add(int index, BaseObservation<? extends T> element)
	{
		observations.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends BaseObservation<? extends T>> c)
	{
		return observations.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends BaseObservation<? extends T>> c)
	{
		return observations.addAll(index, c);
	}

	@Override
	public void clear()
	{
		observations.clear();
	}

	@Override
	public boolean contains(Object o)
	{
		return observations.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return observations.containsAll(c);
	}

	@Override
	public boolean equals(Object o)
	{
		return observations.equals(o);
	}

	@Override
	public BaseObservation<? extends T> get(int index)
	{
		return observations.get(index);
	}

	@Override
	public int hashCode()
	{
		return observations.hashCode();
	}

	@Override
	public int indexOf(Object o)
	{
		return observations.indexOf(o);
	}

	@Override
	public boolean isEmpty()
	{
		return observations.isEmpty();
	}

	@Override
	public Iterator<BaseObservation<? extends T>> iterator()
	{
		return observations.iterator();
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return observations.lastIndexOf(o);
	}

	@Override
	public ListIterator<BaseObservation<? extends T>> listIterator()
	{
		return observations.listIterator();
	}

	@Override
	public ListIterator<BaseObservation<? extends T>> listIterator(int index)
	{
		return observations.listIterator(index);
	}

	@Override
	public boolean remove(Object o)
	{
		return observations.remove(o);
	}

	@Override
	public BaseObservation<? extends T> remove(int index)
	{
		return observations.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return observations.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return observations.retainAll(c);
	}

	@Override
	public BaseObservation<? extends T> set(int index, BaseObservation<? extends T> element)
	{
		return observations.set(index, element);
	}

	@Override
	public int size()
	{
		return observations.size();
	}

	@Override
	public List<BaseObservation<? extends T>> subList(int fromIndex, int toIndex)
	{
		return observations.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray()
	{
		return observations.toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a)
	{
		return observations.toArray(a);
	}
}
