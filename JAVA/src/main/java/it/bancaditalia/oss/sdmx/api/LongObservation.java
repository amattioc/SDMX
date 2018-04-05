package it.bancaditalia.oss.sdmx.api;

import java.util.Map;

/**
 * A specialized implementation of {link {@link BaseObservation} for {@code long} type.
 *
 * @author Valentino Pinna
 *
 */
public class LongObservation extends BaseObservation<Long>
{
	private static final long serialVersionUID = 1L;
	
	protected final long					value;

	/**
	 * Creates an immutable observation from given values.
	 * 
	 * @param timeslot The timestamp of the observation.
	 * @param value The value of the observation
	 * @param obsAttributes A map of observation-level attributes.
	 * @param frequency The frequency of the series.
	 */
	public LongObservation(String timeslot, long value, Map<String, String> obsAttributes)
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
	public LongObservation(BaseObservation<?> other, long value)
	{
		super(other);
		this.value = value;
	}

	@Override
	public Long getValue()
	{
		return value;
	}

	@Override
	public double getValueAsDouble()
	{
		return value;
	}
}
