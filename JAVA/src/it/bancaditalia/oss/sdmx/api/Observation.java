package it.bancaditalia.oss.sdmx.api;

import java.io.Serializable;
import java.util.Map;

public class Observation implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String timeslot;
	private final String value;
	private final Map<String, String> obsAttributes;
	
	public Observation(String timeslot, String value, Map<String, String> obsAttributes)
	{
		this.timeslot = timeslot;
		this.value = value;
		this.obsAttributes = obsAttributes;
	}

	public String getTimeslot()
	{
		return timeslot;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public Map<String, String> getAttributes()
	{
		return obsAttributes;
	}

	public String getAttributeValue(String attrName)
	{
		return obsAttributes.get(attrName);
	} 
}
