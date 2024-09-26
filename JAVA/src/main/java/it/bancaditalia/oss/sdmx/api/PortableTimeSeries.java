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

import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.util.Configuration;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * This is a Java container for a Time Series. It will be transformed by a converter in the various statistical packages
 * into a native time series object.
 * 
 * @author Attilio Mattiocco
 * 
 * @param <T> The type to store in this {@link PortableTimeSeries}
 */
public class PortableTimeSeries<T> implements List<BaseObservation<? extends T>>, RandomAccess, Serializable
{

	private static final long							serialVersionUID		= 1L;

	/**
	 * The default name for an attribute that stores the generated name for unnamed series.
	 */
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

	public PortableTimeSeries(Dataflow dataflow, Map<String, Entry<String, String>> dimensions, Map<String, String> attributes, List<? extends BaseObservation<? extends T>> obs)
	{
		this.dataflow = dataflow;
		this.attributes.putAll(attributes);
		this.dimensions.putAll(dimensions);

		if (dimensions.containsKey("FREQ"))
			this.frequency = dimensions.get("FREQ").getKey();
		else if (dimensions.containsKey("freq"))
			this.frequency = dimensions.get("freq").getKey();
		else if (dimensions.containsKey("FREQUENCY"))
			this.frequency = dimensions.get("FREQUENCY").getKey();
		
		useGeneratedName = true;
		observations.addAll(obs);
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

			for (Entry<String, String> code : dimensions.values()) {
				if (code != null) {
					nameBuilder.append(", " + code.getKey() + "(" + (code.getValue() != null ? code.getValue() : "") + ")");
				}
			}
			
			attributes.put(GENERATEDNAME_ATTR_NAME, nameBuilder.toString());
		}

		return attributes;
	}

	/**
	 * Creates a new PortableTimeSeries by changing each timelot of this PortableTimeSeries  
	 * to the result of respectively applying a function over each of this series' timeslots.
	 * 
	 * @param mapper The function that maps timeslots.
	 * @return The new PortableTimeSeries
	 * 
	 * @throws NullPointerException if mapper is null.
	 */
	public PortableTimeSeries<T> mapTimeslots(Function<String, String> mapper)
	{
		return mapTimeslots(mapper, false);
	}

	/**
	 * Creates a new PortableTimeSeries by changing each timelot of this PortableTimeSeries  
	 * to the result of respectively applying a function over each of this series' timeslots,
	 * and optionally sort this PortableTimeSeries after timeslots have been mapped.
	 * 
	 * @param mapper The function that maps timeslots.
	 * @param sort True if this PortableTimeSeries has to be mapped.
	 * @return The new PortableTimeSeries
	 * 
	 * @throws NullPointerException if mapper is null.
	 */
	public PortableTimeSeries<T> mapTimeslots(Function<String, String> mapper, boolean sort)
	{
		PortableTimeSeries<T> newSeries = new PortableTimeSeries<>(this);

		for (BaseObservation<? extends T> obs : this)
			newSeries.add(obs.mapTimeslot(mapper));
		
		if (sort)
			Collections.sort(newSeries);

		return newSeries;
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
	 * @return a map of codes for each defined dimension, with dimensions as keys and codes as values.
	 */
	public Map<String, String> getDimensionsMap()
	{
		Map<String, String> result = new LinkedHashMap<>();
		for (Entry<String, Entry<String, String>> dimension : dimensions.entrySet())
		{
			if (dimension != null && dimension.getValue() != null)
			{
				result.put(dimension.getKey(),
				Configuration.getCodesPolicy().equalsIgnoreCase(Configuration.SDMX_CODES_POLICY_DESC)
					? dimension.getValue().getValue()
					: dimension.getValue().getKey());
			}
		}
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
	@Deprecated
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
		return new ListWrapper<T>(BaseObservation::getValue);
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
		return new ListWrapper<>(BaseObservation::getTimeslot);
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
		return new ListWrapper<>(obs -> obs.getAttributeValue(attributeName));
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
	@Deprecated
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
			buffer.append("\n");
			first = false;
		}

		return buffer.toString();
	}

	/**
	 * Get a list of Strings in the format "key=value" for each defined attribute in this {@link PortableTimeSeries}.
	 * 
	 * @return The list.
	 * @deprecated Use {@link #getAttributesMap()} instead.
	 */
	@Deprecated
	public List<String> getAttributes()
	{
		return Arrays.asList(getAttributesArray());
	}

	/**
	 * Get a value for an attribute.
	 * 
	 * @param code
	 * @return The list.
	 * @deprecated Use {@link #getAttributesMap()}.{@link Map#get(Object) get(String)} instead.
	 */
	@Deprecated
	public String getAttributeValue(String code)
	{
		return getAttribute(code);
	}

	/**
	 * Add an attribute to this time series.
	 * 
	 * @deprecated Use {@link #getAttributesMap()} instead.
	 */
	@Deprecated
	public void addAttribute(String code, String value)
	{
		attributes.put(code, value);
	}

	/**
	 * Get a String[] in the format "key=value" for each defined attribute in this {@link PortableTimeSeries}.
	 * 
	 * @return The array.
	 * @deprecated Use {@link #getAttributesMap()} instead.
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
	 * Sets attributes for this series.
	 * 
	 * @param attributes a List of String in the format "key=value" for each attribute to set.
	 * @deprecated Use {@link #getAttributesMap()}.{@link Map#putAll(Map) putAll(Map)} instead.
	 */
	@Deprecated
	public void setAttributes(List<String> attributes)
	{
		this.getAttributesMap().clear();
		for (String pair : attributes)
			this.getAttributesMap().put(pair.split("=")[0], pair.split("=")[1]);
	}

	/**
	 * Get a list of Strings in the format "key=value" for each defined dimension in this {@link PortableTimeSeries}.
	 * 
	 * @return The list.
	 * @deprecated Use {@link #getDimensionsMap()} instead.
	 */
	@Deprecated
	public List<String> getDimensions()
	{
		return Arrays.asList(getDimensionsArray());
	}

	/**
	 * Get a value for a dimension.
	 * 
	 * @param code
	 * @return The list.
	 * @deprecated Use {@link #getDimensionsMap()}.{@link Map#get(Object) get(String)} instead.
	 */
	@Deprecated
	public String getDimensionValue(String code)
	{
		return getDimension(code);
	}

	/**
	 * Get a String[] in the format "key=value" for each defined dimension in this {@link PortableTimeSeries}.
	 * 
	 * @return The array.
	 * @deprecated Use {@link #getDimensionsMap()} instead.
	 */
	@Deprecated
	public String[] getDimensionsArray()
	{
		String[] result = new String[dimensions.size()];
		int i = 0;
		for (Entry<String, Entry<String, String>> code : dimensions.entrySet())
		{
			if (code != null && code.getValue() != null)
			{
				result[i++] = code.getKey() + "=" + code.getValue().getKey();
			}
		}
		return result;
	}

	/**
	 * Gets a list of statuses for each observation in this {@link PortableTimeSeries}.
	 * 
	 * @return The "status" attribute value of each observation.
	 * @deprecated Use this series iterator to extract the status from each
	 *             {@link BaseObservation#getAttributeValue(String) BaseObservation}.
	 * 
	 * @see BaseObservation#getAttributeValue(String) BaseObservation.getAttributeValue("OBS_STATUS")
	 */
	@Deprecated
	public List<String> getStatus()
	{
		return getObsLevelAttributes("OBS_STATUS");
	}

	/**
	 * Gets an array of statuses for each observation in this {@link PortableTimeSeries}.
	 * 
	 * @return The "status" attribute value of each observation.
	 * @deprecated Use this series iterator to extract the status from each
	 *             {@link BaseObservation#getAttributeValue(String) BaseObservation}.
	 * 
	 * @see BaseObservation#getAttributeValue(String) BaseObservation.getAttributeValue("OBS_STATUS")
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


	/**
	 * getTimeSlotSet - return a set of all timeslots present in observations list.
	 * @return a Set containing all timeslots presents in ts.
	 */
	protected Set<String> getTimeSlotSet() {
		Set<String> dates = new HashSet<>();
		for (BaseObservation<? extends T> obs : observations) {
			dates.add(obs.getTimeslot());
		}
		return dates;
	}

	/**
	 * getTimeSeriesMap - return a map association the date of the observation with the observation itself.
	 * @return a map with date as key and observation as value.
	 */
	protected Map<String, BaseObservation<? extends T>> getTimeSeriesMap() {
		Map<String, BaseObservation<? extends T>> map = new ConcurrentHashMap<>();
		for (BaseObservation<? extends T> obs : this.observations) {
			map.put(obs.getTimeslot(), obs);
		}
		return map;
	}

	/**
	 *
	 * @param s1 first set
	 * @param s2 second set
	 * @return the intersection set
	 * @param <SetType>
	 */
	private <SetType> Set<SetType> getIntersection(Set<SetType> s1, Set<SetType> s2) {
		// retainAll modify the set in place, so we need to create a new set.
		Set<SetType> intersectionSet = new HashSet<>(s1);
		intersectionSet.retainAll(s2);
		return intersectionSet;
	}

	public void merge(PortableTimeSeries<T> other) throws SdmxInvalidParameterException {
		if (!name.equals(other.getName())) {
			throw new SdmxInvalidParameterException(String.format("The two time series have different names: '%s' and '%s'", name, other.getName()));
		}
		Set<String> datesIntersection = getIntersection(getTimeSlotSet(), other.getTimeSlotSet());
		if (datesIntersection.isEmpty()) {
			this.attributes.putAll(other.attributes);
			this.observations.addAll(other.observations);
			this.sort(BaseObservation::compareTo);
		} else {
			throw new SdmxInvalidParameterException(String.format("Repeated observations found the two time series. Common dates: {%s}", String.join(", ", datesIntersection)));
		}
	}

}
