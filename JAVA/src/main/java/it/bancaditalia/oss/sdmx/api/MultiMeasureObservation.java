package it.bancaditalia.oss.sdmx.api;

import java.util.Map;

/**
 * A specialized implementation of {link {@link BaseObservation} for {@code double} type.
 * It provides faster method executions for the majority of SDMX time series.
 * 
 * @author Valentino Pinna
 *
 */
public class MultiMeasureObservation extends Observation<Map<Measure, Double>>
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates an immutable observation from given values.
	 * 
	 * @param timeslot The timestamp of the observation.
	 * @param value The value of the observation
	 * @param obsAttributes A map of observation-level attributes.
	 */
	public MultiMeasureObservation(String timeslot, Map<Measure, Double> value, Map<String, String> obsAttributes)
	{
		super(timeslot, value, obsAttributes);
	}
}
