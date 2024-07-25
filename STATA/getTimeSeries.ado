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

/*
 *      Execute getTimeSeries call. Parameters:
 *
 *      provider: the id of the data provider
 *      tskey: the SDMX identifier of the resource to be retrieved
 *      start: start time
 *      end: end time
 *      meta: handle or not metadata
 *              0: only data (default)
 *              1: data and metadata (can be very resource consuming)
 *              2: only metadata
 *      force: if set to 1, eventual data in memory will be cleared
 *
 */

program getTimeSeries
        version 17
        args provider tskey start end meta force

        if _N > 0 {
                if "`force'" != "1" {
                        display "Data would be lost. Please clear the dataset and retry."
                        exit
                }
                else {
                        clear
                }
        }

        java, shared(BItools): SgetTimeSeries("`provider'", "`tskey'", "`start'", "`end'", "`meta'");
end

quietly initSDMX

java, shared(BItools):
        import it.bancaditalia.oss.sdmx.api.BaseObservation;
        import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
        import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
		import it.bancaditalia.oss.sdmx.util.Configuration;

        import java.util.Map.Entry;
        import java.util.logging.Logger;

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
                                        logger.info("The query returned " + dataLength + " observations.");
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
                                                                Data.storeNum(val, rowOffset + j + 1, Double.isNaN(obs.getValueAsDouble()) 
                                                                	? Data.getMissingValue() : obs.getValueAsDouble());
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
                                logger.warning("The query did not return any observations.");
                }
                catch (Exception e)
                {
                        e.printStackTrace();
                }
        }
end
