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

package it.bancaditalia.oss.sdmx.client;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.bancaditalia.oss.sdmx.api.BaseObservation;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * <p>
 * Java class for optimizing interactions with the SdmxClients in SAS. It provides a sort of 'session', storing the
 * clients that are created and reusing them. It also provides caching of last time series queried and of all key
 * families retrieved.
 * 
 * @author Attilio Mattiocco
 *
 */
public class SASClientHandler
{
	private static final Method GET_TIME_SERIES;
	
	static
	{
		String clsName = System.getenv().getOrDefault("SAS_SDMX_CLIENT_HANDLER", "it.bancaditalia.oss.sdmx.client.SdmxClientHandler");
		try
		{
			Class<?> chClass = Class.forName(clsName);
			GET_TIME_SERIES = chClass.getMethod("getTimeSeries", String.class, String.class, String.class, String.class, boolean.class, String.class, boolean.class);
		}
		catch (ClassNotFoundException | NoSuchMethodException | SecurityException e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}
	
	private static class SdmxSASException extends SdmxException
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public SdmxSASException(String message)
		{
			super("SAS connector error: " + message, null);
		}

	}

	private static class DataCache
	{
		private static final int	NAME_COL	= 0;
		private static final int	TIME_COL	= 1;
		private static final int	OBS_COL		= 2;

		Object[][]					data		= null;

		DataCache(int size)
		{
			super();
			this.data = new Object[size][3];
		}

		void setRow(int rowIndex, String name, String time, double obs) throws SdmxException
		{
			if (data != null && rowIndex < data.length)
			{
				data[rowIndex][NAME_COL] = name;
				data[rowIndex][TIME_COL] = time;
				data[rowIndex][OBS_COL] = new Double(obs);
			}
			else
			{
				throw new SASClientHandler.SdmxSASException("Row index exceeds data size");
			}
		}

		double getObservation(int rowIndex) throws SASClientHandler.SdmxSASException
		{
			if (data != null && rowIndex < data.length)
			{
				return ((Double) data[rowIndex][OBS_COL]).doubleValue();
			}
			else
			{
				throw new SASClientHandler.SdmxSASException("Data cache error: cache is null or index exceeds size.");
			}
		}

		String getName(int rowIndex) throws SASClientHandler.SdmxSASException
		{
			if (data != null && rowIndex < data.length)
			{
				return (String) data[rowIndex][NAME_COL];
			}
			else
			{
				throw new SASClientHandler.SdmxSASException("Data cache error: cache is null or index exceeds size.");
			}
		}

		String getTimestamp(int rowIndex) throws SASClientHandler.SdmxSASException
		{
			if (data != null && rowIndex < data.length)
			{
				return (String) data[rowIndex][TIME_COL];
			}
			else
			{
				throw new SASClientHandler.SdmxSASException("Data cache error: cache is null or index exceeds size.");
			}
		}

		int size()
		{
			if (data != null)
				return data.length;
			else
				return -1;
		}
	}

	private static class MetadataCache
	{
		private static final int	NAME_COL	= 0;
		private static final int	KEY_COL		= 1;
		private static final int	VALUE_COL	= 2;
		private static final int	TYPE_COL	= 3;

		String[][]					data		= null;

		MetadataCache(int size)
		{
			super();
			this.data = new String[size][4];
		}

		void setRow(int rowIndex, String name, String key, String value, String type)
				throws SASClientHandler.SdmxSASException
		{
			if (data != null && rowIndex < data.length)
			{
				data[rowIndex][NAME_COL] = name;
				data[rowIndex][KEY_COL] = key;
				data[rowIndex][VALUE_COL] = value;
				data[rowIndex][TYPE_COL] = type;
			}
			else
			{
				throw new SASClientHandler.SdmxSASException("Row index exceeds metadata size");
			}
		}

		String getName(int rowIndex) throws SASClientHandler.SdmxSASException
		{
			if (data != null && rowIndex < data.length)
			{
				return (String) data[rowIndex][NAME_COL];
			}
			else
			{
				throw new SASClientHandler.SdmxSASException(
						"Metadata cache error: cache is null or index exceeds size.");
			}
		}

		String getKey(int rowIndex) throws SASClientHandler.SdmxSASException
		{
			if (data != null && rowIndex < data.length)
			{
				return (String) data[rowIndex][KEY_COL];
			}
			else
			{
				throw new SASClientHandler.SdmxSASException(
						"Metadata cache error: cache is null or index exceeds size.");
			}
		}

		String getValue(int rowIndex) throws SASClientHandler.SdmxSASException
		{
			if (data != null && rowIndex < data.length)
			{
				return (String) data[rowIndex][VALUE_COL];
			}
			else
			{
				throw new SASClientHandler.SdmxSASException(
						"Metadata cache error: cache is null or index exceeds size.");
			}
		}

		String getType(int rowIndex) throws SASClientHandler.SdmxSASException
		{
			if (data != null && rowIndex < data.length)
			{
				return (String) data[rowIndex][TYPE_COL];
			}
			else
			{
				throw new SASClientHandler.SdmxSASException(
						"Metadata cache error: cache is null or index exceeds size.");
			}
		}

		int size()
		{
			if (data != null)
				return data.length;
			else
				return -1;
		}

	}

	private static class ObservationMetadataCache
	{
		private static final int	NAME_COL	= 0;
		private static final int	KEY_COL		= 1;
		private static final int	VALUE_COL	= 2;
		private static final int	DATE_COL	= 3;

		String[][]					data		= null;

		ObservationMetadataCache(int size)
		{
			super();
			this.data = new String[size][4];
		}

		void setRow(int rowIndex, String name, String key, String value, String date)
				throws SASClientHandler.SdmxSASException
		{
			if (data != null && rowIndex < data.length)
			{
				data[rowIndex][NAME_COL] = name;
				data[rowIndex][KEY_COL] = key;
				data[rowIndex][VALUE_COL] = value;
				data[rowIndex][DATE_COL] = date;
			}
			else
			{
				throw new SASClientHandler.SdmxSASException("Row index exceeds metadata size");
			}
		}

		String getName(int rowIndex) throws SASClientHandler.SdmxSASException
		{
			if (data != null && rowIndex < data.length)
			{
				return (String) data[rowIndex][NAME_COL];
			}
			else
			{
				throw new SASClientHandler.SdmxSASException(
						"Metadata cache error: cache is null or index exceeds size.");
			}
		}

		String getKey(int rowIndex) throws SASClientHandler.SdmxSASException
		{
			if (data != null && rowIndex < data.length)
			{
				return (String) data[rowIndex][KEY_COL];
			}
			else
			{
				throw new SASClientHandler.SdmxSASException(
						"Metadata cache error: cache is null or index exceeds size.");
			}
		}

		String getValue(int rowIndex) throws SASClientHandler.SdmxSASException
		{
			if (data != null && rowIndex < data.length)
			{
				return (String) data[rowIndex][VALUE_COL];
			}
			else
			{
				throw new SASClientHandler.SdmxSASException(
						"Metadata cache error: cache is null or index exceeds size.");
			}
		}

		String getDate(int rowIndex) throws SASClientHandler.SdmxSASException
		{
			if (data != null && rowIndex < data.length)
			{
				return (String) data[rowIndex][DATE_COL];
			}
			else
			{
				throw new SASClientHandler.SdmxSASException(
						"Metadata cache error: cache is null or index exceeds size.");
			}
		}

		int size()
		{
			if (data != null)
				return data.length;
			else
				return -1;
		}
	}

	protected static Logger					logger		= Configuration.getSdmxLogger();
	private static DataCache				data		= null;
	private static MetadataCache			metadata	= null;
	private static ObservationMetadataCache	obsmetadata	= null;

	public static String makeGetDimensions(String provider, String dataflow)
	{
		StringBuilder result = new StringBuilder();
		try
		{
			List<Dimension> dims = SdmxClientHandler.getDimensions(provider, dataflow);
			for (Dimension dim : dims)
			{
				result.append(dim.getId());
				result.append(",");
			}
		}
		catch (Exception e)
		{
			logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
		}
		return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
	}

	public static int makeGetTimeSeries(String provider, String tsKey, String startTime, String endTime)
	{
		int returnCode = 0;
		data = null;
		metadata = null;
		obsmetadata = null;
		try
		{
			@SuppressWarnings("unchecked")
			List<PortableTimeSeries<Double>> result = (List<PortableTimeSeries<Double>>) GET_TIME_SERIES.invoke(null, provider, tsKey, startTime, endTime, false, null, false);
			if (!result.isEmpty())
			{
				// check size of full result as a table
				int datasize = 0;
				int metasize = 0;
				int obsmetasize = 0;
				for (PortableTimeSeries<Double> ts: result)
				{
					int obsnum = ts.size();
					datasize += obsnum;
					metasize += ts.getDimensionsMap().size();
					metasize += ts.getAttributesMap().size();
					obsmetasize += ts.getObsLevelAttributesNames().size() * obsnum;
				}

				// init data cache
				data = new DataCache(datasize);
				metadata = new MetadataCache(metasize);
				obsmetadata = new ObservationMetadataCache(obsmetasize);
				int dataRowIndex = 0;
				int metaRowIndex = 0;
				int obsMetaRowIndex = 0;
				for (PortableTimeSeries<?> ts : result)
				{
					if (!ts.isNumeric())
					{
						logger.warning("Time Series " + ts.getName() + "is not numeric and will be skipped in SAS.");
						continue;
					}
					String name = ts.getName();
					// setting ts level metadata
					for (Entry<String, String> dimension : ts.getDimensionsMap().entrySet())
						metadata.setRow(metaRowIndex++, name, dimension.getKey(), dimension.getValue(), "DIMENSION");
					for (Entry<String, String> attribute : ts.getAttributesMap().entrySet())
						metadata.setRow(metaRowIndex++, name, attribute.getKey(), attribute.getValue(), "ATTRIBUTE");

					for (BaseObservation<?> obs : ts)
					{
						String time = obs.getTimeslot();
						double value = obs.getValueAsDouble();

						if (dataRowIndex > data.size())
							throw new SASClientHandler.SdmxSASException("Trying to process more data than available");

						data.setRow(dataRowIndex, name, time, value);

						// now set obs level metadata
						for (String obsAttrName : ts.getObsLevelAttributesNames())
						{
							obsmetadata.setRow(obsMetaRowIndex, name, obsAttrName, obs.getAttributeValue(obsAttrName),
									time);
							obsMetaRowIndex++;
						}

						dataRowIndex++;
					}
				}
				returnCode = result.size();
			}
		}
		catch (Exception e)
		{
			logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
			data = null;
			metadata = null;
			returnCode = -1;
		}

		return returnCode;

	}

	public static String getMetaName(double index) throws SASClientHandler.SdmxSASException
	{
		if (metadata != null && index <= metadata.size())
		{
			return metadata.getName((int) index);
		}
		else
		{
			throw new SASClientHandler.SdmxSASException("Metadata cache error: cache is null or index exceeds size.");
		}
	}

	public static String getMetaKey(double index) throws SASClientHandler.SdmxSASException
	{
		if (metadata != null && index <= metadata.size())
		{
			return metadata.getKey((int) index);
		}
		else
		{
			throw new SASClientHandler.SdmxSASException("Metadata cache error: cache is null or index exceeds size.");
		}
	}

	public static String getMetaValue(double index) throws SASClientHandler.SdmxSASException
	{
		if (metadata != null && index <= metadata.size())
		{
			return metadata.getValue((int) index);
		}
		else
		{
			throw new SASClientHandler.SdmxSASException("Metadata cache error: cache is null or index exceeds size.");
		}
	}

	public static String getMetaType(double index) throws SASClientHandler.SdmxSASException
	{
		if (metadata != null && index <= metadata.size())
		{
			return metadata.getType((int) index);
		}
		else
		{
			throw new SASClientHandler.SdmxSASException("Metadata cache error: cache is null or index exceeds size.");
		}
	}

	public static double getDataObservation(double index) throws SASClientHandler.SdmxSASException
	{
		if (data != null && index <= data.size())
		{
			return data.getObservation((int) index);
		}
		else
		{
			throw new SASClientHandler.SdmxSASException("Data cache error: cache is null or index exceeds size.");
		}
	}

	public static String getDataTimestamp(double index) throws SASClientHandler.SdmxSASException
	{
		if (data != null && index <= data.size())
		{
			return data.getTimestamp((int) index);
		}
		else
		{
			throw new SASClientHandler.SdmxSASException("Data cache error: cache is null or index exceeds size.");
		}
	}

	public static String getDataName(double index) throws SASClientHandler.SdmxSASException
	{
		if (data != null && index <= data.size())
		{
			return data.getName((int) index);
		}
		else
		{
			throw new SASClientHandler.SdmxSASException("Data cache error: cache is null or index exceeds size.");
		}
	}

	public static int getNumberOfMeta()
	{
		if (metadata != null)
			return metadata.size();
		else
			return 0;
	}

	public static int getNumberOfObsMeta()
	{
		if (obsmetadata != null)
			return obsmetadata.size();
		else
			return 0;
	}

	public static int getNumberOfData()
	{
		if (data != null)
			return data.size();
		else
			return 0;
	}

	public static String getObsMetaName(double index) throws SASClientHandler.SdmxSASException
	{
		if (obsmetadata != null && index <= obsmetadata.size())
		{
			return obsmetadata.getName((int) index);
		}
		else
		{
			throw new SASClientHandler.SdmxSASException("Observation level Metadata cache error: cache is null or index exceeds size.");
		}
	}

	public static String getObsMetaKey(double index) throws SASClientHandler.SdmxSASException
	{
		if (obsmetadata != null && index <= obsmetadata.size())
		{
			return obsmetadata.getKey((int) index);
		}
		else
		{
			throw new SASClientHandler.SdmxSASException(
					"Observation level  cache error: cache is null or index exceeds size.");
		}
	}

	public static String getObsMetaValue(double index) throws SASClientHandler.SdmxSASException
	{
		if (obsmetadata != null && index <= obsmetadata.size())
		{
			return obsmetadata.getValue((int) index);
		}
		else
		{
			throw new SASClientHandler.SdmxSASException(
					"Observation level  cache error: cache is null or index exceeds size.");
		}
	}

	public static String getObsMetaDate(double index) throws SASClientHandler.SdmxSASException
	{
		if (obsmetadata != null && index <= obsmetadata.size())
		{
			return obsmetadata.getDate((int) index);
		}
		else
		{
			throw new SASClientHandler.SdmxSASException(
					"Observation level  cache error: cache is null or index exceeds size.");
		}
	}

}
