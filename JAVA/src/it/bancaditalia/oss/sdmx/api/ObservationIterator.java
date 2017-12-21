package it.bancaditalia.oss.sdmx.api;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

final class ObservationIterator implements Iterator<Observation>
{
	private final PortableTimeSeries series;
	private final int size;

	private int position = -1;

	/**
	 * @param portableTimeSeries
	 */
	ObservationIterator(PortableTimeSeries portableTimeSeries)
	{
		series = portableTimeSeries;
		size = series.getObservations().size();
	}

	private void checkConcurrentModificationException()
	{
		if (series.getObservations().size() != size)
			throw new ConcurrentModificationException("PortableTimeSeries modified while iterating over it.");
	}

	@Override
	public boolean hasNext()
	{
		checkConcurrentModificationException();
		return position + 1 < size;
	}

	@Override
	public Observation next()
	{
		checkConcurrentModificationException();
		if (!hasNext())
			throw new NoSuchElementException("Time series has " + size + " elements, but " + position + "th was requested.");

		position++;
		
		Map<String, String> obsAttributes = new HashMap<String, String>();
		for (Entry<String, List<String>> entry: series.obsLevelAttributes.entrySet())
			obsAttributes.put(entry.getKey(), entry.getValue().get(position));
		
		Observation result = new Observation(series.getTimeSlots().get(position), 
				series.getObservations().get(position).toString(), obsAttributes);
		
		return result;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("Delete operation not permitted");
	}
}