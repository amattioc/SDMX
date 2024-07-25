package it.bancaditalia.oss.sdmx.api;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A specialized implementation of {link {@link BaseObservation} for {@code double} type.
 * It provides faster method executions for the majority of SDMX time series.
 * 
 * @author Valentino Pinna
 *
 */
public class DoubleObservation extends BaseObservation<Double>
{
	private static final long serialVersionUID = 1L;
	
	protected final double					value;

	/**
	 * Creates an immutable observation from given values.
	 * 
	 * @param timeslot The timestamp of the observation.
	 * @param value The value of the observation
	 * @param obsAttributes A map of observation-level attributes.
	 */
	public DoubleObservation(String timeslot, double value, Map<String, String> obsAttributes)
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
	public DoubleObservation(BaseObservation<?> other, double value)
	{
		super(other);
		this.value = value;
	}

	@Override
	public Double getValue()
	{
		return value;
	}

	@Override
	public double getValueAsDouble()
	{
		return value;
	}

	@Override
	public DoubleObservation mapTimeslot(Function<String, String> mapper)
	{
		return new DoubleObservation(mapper.apply(timeslot), value, obsAttributes);
	}

	@Override
	public <U> BaseObservation<U> mapValue(Function<? super Double, U> mapper)
	{
		return new Observation<>(this, mapper.apply(value));
	}

	@Override
	public <U, R> BaseObservation<R> combine(BaseObservation<U> other, BiFunction<? super Double, ? super U, R> combiner)
	{
		return new Observation<>(this, combiner.apply(value, other.getValue()));
	}
}
