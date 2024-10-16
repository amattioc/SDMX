import it.bancaditalia.oss.sdmx.api.BaseObservation;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.util.Configuration;

import java.util.Map.Entry;
import java.util.logging.Logger;

void S__createAndSet(Map<String, String> values, long rowIndex)
{
	for (String key: values.keySet())
	{
		int varIndex = Data.getVarIndex(key);
		if (varIndex == 0)
		{
			Data.addVarStr(key, 1);
			varIndex = Data.getVarIndex(key);
		}
		
		Data.storeStr(varIndex, rowIndex, values.get(key));
	}
}

void SgetTimeSeries(String provider, String tsKey, String start, String end, String meta)
{
	Logger logger = Configuration.getSdmxLogger();
	
	List<PortableTimeSeries<Double>> tslist = null;
	if (provider.trim().isEmpty() || tsKey.trim().isEmpty())
	{
		logger.info("The provider name and time series key are required.");
		return;
	}

	boolean processMeta = false;
	boolean processData = true;

	switch (meta)
	{
		case "1":
			logger.info("METADATA is enabled.");
			processMeta = true;
			break;
		case "2":
			logger.info("Only METADATA is enabled.");
			processMeta = true;
			processData = false;
			break;
		default:
			logger.info("METADATA is disabled.");
	}

	try
	{
		int dataLength = 0;
		tslist = SdmxClientHandler.getTimeSeries(provider, tsKey, start, end, false, null, false);
		if (tslist == null)
		{
			logger.warning("The query did not complete correctly. Check java traces for details.");
			return;
		}
		else
			logger.info("The query returned " + tslist.size() + " time series.");

		if (processData)
			for (PortableTimeSeries<?> ts: tslist)
				dataLength += ts.size();
		else
			dataLength = tslist.size();

		if (dataLength > 0)
		{
			int name = 0;
			int date = 0;
			int val = 0;
			Data.setObsTotal(dataLength);
			Data.addVarStr("TSNAME", 10);
			name = Data.getVarIndex("TSNAME");
			boolean allNumeric = true;
			for (PortableTimeSeries<?> ts: tslist)
				if (!ts.isNumeric())
				{
					allNumeric = false;
					break;
				}

			if (processData)
			{
				logger.info("The query returned " + dataLength + " observations.");
				Data.addVarStr("DATE", 5);
				if (allNumeric)
					Data.addVarDouble("VALUE");
				else
					Data.addVarStr("VALUE", 40);
				date = Data.getVarIndex("DATE");
				val = Data.getVarIndex("VALUE");
			}

			long rowIndex = 1;
			for (PortableTimeSeries<?> ts: tslist)
			{
				String tsname = ts.getName();
				
				if (processData)
				{
					for (BaseObservation<?> obs : ts)
					{
						Data.storeStr(name, rowIndex, tsname);
						
						if (allNumeric)
							Data.storeNum(val, rowIndex, Double.isNaN(obs.getValueAsDouble()) 
								? Data.getMissingValue() : obs.getValueAsDouble());
						else
							Data.storeStr(val, rowIndex, obs.getValueAsString());
						
						Data.storeStr(date, rowIndex, obs.getTimeslot());
						if (processMeta)
						{
							S__createAndSet(ts.getDimensionsMap(), rowIndex);
							S__createAndSet(ts.getAttributesMap(), rowIndex);
							S__createAndSet(obs.getAttributes(), rowIndex);
						}
						
						rowIndex++;
					}
				}
				else
				{
					Data.storeStr(name, rowIndex, tsname);

					S__createAndSet(ts.getDimensionsMap(), rowIndex);
					S__createAndSet(ts.getAttributesMap(), rowIndex);
					
					rowIndex++;
				}
			}
		}
		else
			logger.warning("The query did not return any observations.");
	}
	catch (Exception e)
	{
		e.printStackTrace();
	}
}
