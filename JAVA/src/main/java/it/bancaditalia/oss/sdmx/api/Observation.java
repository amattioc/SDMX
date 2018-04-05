package it.bancaditalia.oss.sdmx.api;

import java.util.Map;

/**
 * Generic specialization for a {@link BaseObservation}.
 * 
 * @author m027907
 *
 * @param <T> The type of value managed by this Observation.
 */
public class Observation<T> extends BaseObservation<T>
{
	private static final long serialVersionUID = 1L;
	
	protected final T					value;

	/**
	 * Creates an immutable observation from given values.
	 * 
	 * @param timeslot The timestamp of the observation.
	 * @param value The value of the observation
	 * @param obsAttributes A map of observation-level attributes.
	 */
	public Observation(String timeslot, T value, Map<String, String> obsAttributes)
	{
		super(timeslot, obsAttributes);
		this.value = value;
	}

	/**
	 * Creates a new observation that is a copy of this Observation, changing the value.
	 * 
	 * @param other The Observation to copy.
	 * @param value the new value to assign to this Observation
	 */
	public Observation(BaseObservation<?> other, T value)
	{
		super(other);
		this.value = value;
	}

	@Override
	public T getValue()
	{
		return value;
	}
}
