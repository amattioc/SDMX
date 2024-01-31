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

import static it.bancaditalia.oss.sdmx.api.SDMXVersion.V2;
import static it.bancaditalia.oss.sdmx.api.SDMXVersion.V3;

import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.stata.sfi.Data;
import com.stata.sfi.SFIToolkit;

import it.bancaditalia.oss.sdmx.api.BaseObservation;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.api.SDMXVersion;
import it.bancaditalia.oss.sdmx.helper.SDMXHelper;
import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * <p>
 * Java class for optimizing interactions with the SdmxClientHandler in STATA.
 * It uses the
 * 
 * 
 * @author Attilio Mattiocco
 *
 */
public class StataClientHandler
{

	protected static Logger logger = Configuration.getSdmxLogger();

	static
	{
		logger.addHandler(new StataLogHandler());
	}

	@SuppressWarnings("javadoc")
	public static int getTimeSeries(String[] args)
	{
		List<PortableTimeSeries<Double>> tslist = null;
		int returnCode = 0;
		if (args.length < 2)
		{
			SFIToolkit.displayln("The provider name and time series key are required.");
			return -1;
		}
		String provider = args[0];
		String tsKey = args[1];
		String start = "";
		String end = "";

		String meta = "0"; // only data
		boolean processMeta = false;
		boolean processData = true;

		if (args.length >= 3 && !args[2].isEmpty())
		{
			start = args[2];
		}
		if (args.length >= 4 && !args[3].isEmpty())
		{
			end = args[3];
		}
		if (args.length >= 5)
		{
			meta = args[4];
			if (meta.equalsIgnoreCase(""))
			{
				meta = "0";
			}

			if (meta.equalsIgnoreCase("0"))
			{
				SFIToolkit.displayln("METADATA is disabled.");
			}
			else if (meta.equalsIgnoreCase("1"))
			{
				SFIToolkit.displayln("METADATA is enabled.");
				processMeta = true;
			}
			else if (meta.equalsIgnoreCase("2"))
			{
				SFIToolkit.displayln("Only METADATA is enabled.");
				processMeta = true;
				processData = false;
			}
			else
			{
				SFIToolkit.displayln("Metadata parameter not valid: " + meta);
			}
		}
		try
		{
			int dataLength = 0;
			tslist = SdmxClientHandler.getTimeSeries(provider, tsKey, start, end, false, null, false);
			if (tslist == null)
			{
				SFIToolkit.displayln("The query did not complete correctly. Check java traces for details.");
				return -1;
			}
			else
				SFIToolkit.displayln("The query returned " + tslist.size() + " time series.");

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
				int lastPos = name;
				boolean allNumeric = true;
				for (PortableTimeSeries<?> ts: tslist)
					if (!ts.isNumeric())
					{
						allNumeric = false;
						break;
					}
				if (processData)
				{
					SFIToolkit.displayln("The query returned " + dataLength + " observations.");
					Data.addVarStr("DATE", 5);
					if (allNumeric)
						Data.addVarDouble("VALUE");
					else
						Data.addVarStr("VALUE", 40);
					date = Data.getVarIndex("DATE");
					val = Data.getVarIndex("VALUE");
					lastPos = val;
				}

				long i = 0; // time series counter
				long rowOffset = 0; // row counter
				for (PortableTimeSeries<?> ts: tslist)
				{
					String tsname = ts.getName();
					if (processData)
					{
						int j = 0; // observation counter
						for (BaseObservation<?> obs : ts)
						{
							Data.storeStr(name, rowOffset + j + 1, tsname);
							if (allNumeric)
								Data.storeNum(val, rowOffset + j + 1,
										obs.getValueAsDouble() == Double.NaN ? Data.getMissingValue()
												: obs.getValueAsDouble());
							else
								Data.storeStr(val, rowOffset + j + 1, obs.getValueAsString());
							
							Data.storeStr(date, rowOffset + j + 1, obs.getTimeslot());
							if (processMeta)
							{
								for (Entry<String, String> dim : ts.getDimensionsMap().entrySet())
								{
									String key = dim.getKey();
									String value = dim.getValue();
									int attrPos = Data.getVarIndex(key);
									if (attrPos > lastPos)
									{
										lastPos = attrPos;
										// not set yet
										Data.addVarStr(key, value.length());
									}
									Data.storeStr(attrPos, rowOffset + j + 1, value);
								}
								
								for (Entry<String, String> attr : ts.getAttributesMap().entrySet())
								{
									String key = attr.getKey();
									String value = attr.getValue();
									int attrPos = Data.getVarIndex(key);
									if (attrPos > lastPos)
									{
										lastPos = attrPos;
										// not set yet
										Data.addVarStr(key, value.length());
									}
									Data.storeStr(attrPos, rowOffset + j + 1, value);
								}

								// Set obs-level attribute values 
								for (String attrName: ts.getObsLevelAttributesNames())
								{
									int attrPos = Data.getVarIndex(attrName);
									if (attrPos > lastPos)
									{
										lastPos = attrPos;
										// not set yet
										Data.addVarStr(attrName, 1);
									}
								}
							}
							j++;
						}
						rowOffset += j;
					}
					else
					{
						Data.storeStr(name, i + 1, tsname);
						for (Entry<String, String> dim : ts.getDimensionsMap().entrySet())
						{
							String key = dim.getKey();
							String value = dim.getValue();
							int attrPos = Data.getVarIndex(key);
							if (attrPos > lastPos)
							{
								lastPos = attrPos;
								// not set yet
								Data.addVarStr(key, value.length());
							}
							Data.storeStr(attrPos, rowOffset + i + 1, value);
						}
						for (Entry<String, String> attr : ts.getAttributesMap().entrySet())
						{
							String key = attr.getKey();
							String value = attr.getValue();
							int attrPos = Data.getVarIndex(key);
							if (attrPos > lastPos)
							{
								lastPos = attrPos;
								// not set yet
								Data.addVarStr(key, value.length());
							}
							Data.storeStr(attrPos, rowOffset + i + 1, value);
						}
					}
					i++;
				}
			}
			else
			{
				SFIToolkit.displayln("The query did not return any observations.");
			}
		}
		catch (Exception e)
		{
			SFIToolkit.displayln("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
			returnCode = -1;
		}

		return returnCode;

	}

	@SuppressWarnings("javadoc")
	public static int sdmxHelper(String[] args)
	{
		SDMXHelper.start();
		return 0;
	}

	@SuppressWarnings("javadoc")
	public static int addProvider(String[] args)
	{
		int returnCode = 0;
		if (args.length < 2)
		{
			SFIToolkit.displayln("The provider name and endpoint are required.");
			return -1;
		}
		String name = args[0];
		String endpoint = args[1];

		boolean needsCredentials = false;
		boolean needsURLEncoding = false;
		boolean supportsCompression = false;
		String description = "";
		SDMXVersion sdmxVersion = V2;

		if (args.length >= 3 && !args[2].isEmpty())
		{
			needsCredentials = args[2].equalsIgnoreCase("1") ? true : false;
		}
		if (args.length >= 4 && !args[3].isEmpty())
		{
			needsURLEncoding = args[3].equalsIgnoreCase("1") ? true : false;
		}
		if (args.length >= 5 && !args[4].isEmpty())
		{
			supportsCompression = args[4].equalsIgnoreCase("1") ? true : false;
		}
		if (args.length >= 6 && !args[5].isEmpty())
		{
			description = args[5];
		}
		if (args.length >= 7 && !args[6].isEmpty())
		{
			sdmxVersion = args[5] == "V2" ? V2 : V3;
		}

		try
		{
			SdmxClientHandler.addProvider(name, endpoint, needsCredentials, needsURLEncoding, supportsCompression, description, sdmxVersion);
		}
		catch (Exception e)
		{
			SFIToolkit.displayln("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
			returnCode = -1;
		}

		return returnCode;
	}
}
