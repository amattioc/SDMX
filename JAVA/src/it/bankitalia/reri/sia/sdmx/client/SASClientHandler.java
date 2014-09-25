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

package it.bankitalia.reri.sia.sdmx.client;

import it.bankitalia.reri.sia.sdmx.api.Dimension;
import it.bankitalia.reri.sia.sdmx.api.PortableTimeSeries;
import it.bankitalia.reri.sia.util.Configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * <p>Java class for optimizing interactions with the SdmxClients in SAS. 
 * It provides a sort of 'session', storing the clients that are created 
 * and reusing them. It also provides caching of last time series queried and of all 
 * key families retrieved.
 * 
 * @author Attilio Mattiocco
 *
 */
public class SASClientHandler extends SdmxClientHandler{
	
	protected static Logger logger = Configuration.getSdmxLogger();
	private static List<String> metadataCache = new ArrayList<String>();
	private static List<Double> dataCache = new ArrayList<Double>();
	private static List<String> timeCache = new ArrayList<String>();
	private static List<String> nameCache = new ArrayList<String>();
	private static List<String> statusCache = new ArrayList<String>();
	
	public static String makeGetDimensions(String provider, String dataflow){
		StringBuilder result = new StringBuilder();
		try {
			List<Dimension> dims = SdmxClientHandler.getDimensions(provider, dataflow);
		    for(Dimension dim : dims) {
		        result.append(dim.getId());
		        result.append(",");
		    }
		} catch (Exception e) {
			logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
		}
	    return result.length() > 0 ? result.substring(0, result.length() - 1): "";
	}

	public static int makeGetTimeSeries(String provider, String tsKey, String startTime, String endTime){
		int returnCode = 0;
		resetCache();
		
		try {
			List<PortableTimeSeries> result = SdmxClientHandler.getTimeSeries(provider, tsKey, startTime, endTime);
			if(!result.isEmpty()){
				for (Iterator<PortableTimeSeries> iterator = result.iterator(); iterator.hasNext();) {
					PortableTimeSeries ts = (PortableTimeSeries) iterator.next();
					String name = ts.getName();
					// setting metadata
					List<String> dimensions = ts.getDimensions();
					for (Iterator<String> iterator2 = dimensions.iterator(); iterator2.hasNext();) {
						String dim = (String) iterator2.next();
						metadataCache.add(name + "," + dim + "," + "Dimension");
					}
					List<String> attributes = ts.getAttributes();
					for (Iterator<String> iterator2 = attributes.iterator(); iterator2.hasNext();) {
						String attr = (String) iterator2.next();
						metadataCache.add(name + "," + attr + "," + "Attribute");
					}
					
					//setting data cache
					boolean skipStatus = false;
					String[] timeSlots = ts.getTimeSlotsArray();
					Double[] observations = ts.getObservationsArray();
					String[] status = ts.getStatusArray();
					if(timeSlots.length != observations.length){
						logger.warning("The time series " + name + " is not well formed. Skip it.");
						break;
					}
					if(timeSlots.length != status.length){
						logger.info("The time series " + name + " is missing the status attributes. It will not be set.");
						skipStatus = true;
					}
					for (int i = 0; i < timeSlots.length; i++) {
						String time = (String) timeSlots[i];
						Double obs = (Double) observations[i];
						String stat = "''";
						if(!skipStatus){
							stat = (String) status[i];
						}
						nameCache.add(name);
						timeCache.add(time);
						dataCache.add(obs);
						statusCache.add(stat);
					}
				}
				returnCode = result.size();
			}
		} catch (Exception e) {
			logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
			resetCache();
			returnCode = -1;
		}
				
		return returnCode;
		
	}
	
	public static String getMeta(double index) {
		if(index >= metadataCache.size()){
			return null;
		}
		return(metadataCache.get((int)index));
	}
	public static double getData(double index) {
		if(index >= dataCache.size()){
			return 0;
		}
		return(dataCache.get((int)index));
	}
	public static String getTimestamp(double index) {
		if(index >= timeCache.size()){
			return null;
		}
		return(timeCache.get((int)index));
	}
	public static String getStatus(double index) {
		if(index >= statusCache.size()){
			return null;
		}
		return(statusCache.get((int)index));
	}
	public static String getName(double index) {
		if(index >= nameCache.size()){
			return null;
		}
		return(nameCache.get((int)index));
	}
	public static int getNumberOfMeta() {
		return(metadataCache.size());
	}
	public static int getNumberOfData() {
		return(dataCache.size());
	}

	public static void resetCache() {
		metadataCache.clear();
		dataCache.clear();
		timeCache.clear();
		statusCache.clear();
		nameCache.clear();
	}

}
