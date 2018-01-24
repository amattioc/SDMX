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

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.stata.sfi.Data;
import com.stata.sfi.SFIToolkit;

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.helper.SDMXHelper;
import it.bancaditalia.oss.sdmx.util.Configuration;
/**
 * <p>Java class for optimizing interactions with the SdmxClientHandler in STATA. It uses the
 *  
 * 
 * @author Attilio Mattiocco
 *
 */
public class StataClientHandler {
		
	protected static Logger logger = Configuration.getSdmxLogger();

	static {
		logger.addHandler(new StataLogHandler());
	}
	
	public static int getTimeSeries(String[] args){
		List<PortableTimeSeries> tslist = null;
		int returnCode = 0;
		if(args.length < 2){
			SFIToolkit.displayln("The provider name and time series key are required.");
			return -1;
		}
		String provider = args[0];
		String tsKey = args[1];
		String start = "";
		String end = "";
		
		String meta = "0"; //only data
		boolean processMeta = false;
		boolean processData = true;
		
		if(args.length >= 3 && !args[2].isEmpty()){
			start = args[2];
		}
		if(args.length >= 4 && !args[3].isEmpty()){
			end = args[3];
		}
		if(args.length >= 5){
			meta = args[4];
			if(meta.equalsIgnoreCase("")){
				meta = "0";
			}
			
			if(meta.equalsIgnoreCase("0")){
				SFIToolkit.displayln("METADATA is disabled.");	
			}
			else if(meta.equalsIgnoreCase("1")){
				SFIToolkit.displayln("METADATA is enabled.");	
				processMeta = true;
			}
			else if(meta.equalsIgnoreCase("2")){
				SFIToolkit.displayln("Only METADATA is enabled.");
				processMeta = true;
				processData = false;
			}
			else{
				SFIToolkit.displayln("Metadata parameter not valid: " + meta);
			}
		}
		try {
			int dataLength = 0;
			tslist = SdmxClientHandler.getTimeSeries(provider, tsKey, start, end);
			if(tslist == null){
				SFIToolkit.displayln("The query did not complete correctly. Check java traces for details.");
				return -1;
			} 
			else{
				SFIToolkit.displayln("The query returned " + tslist.size() + " time series.");
			}
			if(processData){
				for (Iterator<PortableTimeSeries> iterator = tslist.iterator(); iterator.hasNext();) {
					PortableTimeSeries ts = (PortableTimeSeries) iterator.next();
					dataLength += ts.getObservations().size();
				}
			}
			else{
				dataLength = tslist.size();
			}
			
			if(dataLength > 0){
				int name = 0;
				int date = 0;
				int val = 0;
				Data.setObsCount(dataLength);
				Data.addVarStr("TSNAME", 10);
				name = Data.getVarIndex("TSNAME") ;
				int lastPos = name;
				boolean allNumeric = true;
				for (Iterator<PortableTimeSeries> iterator = tslist.iterator(); iterator.hasNext();) {
					if(!iterator.next().isNumeric()){
						allNumeric = false;
						break;
					}
				}				
				if(processData){
					SFIToolkit.displayln("The query returned " + dataLength + " observations.");
					Data.addVarStr("DATE", 5);
					if(allNumeric)
						Data.addVarDouble("VALUE");
					else
						Data.addVarStr("VALUE", 40);
					date = Data.getVarIndex("DATE") ;
					val = Data.getVarIndex("VALUE") ;
					lastPos = val;
				}
				
				int i = 0; // time series counter
				int rowOffset = 0; //row counter
				for (Iterator<PortableTimeSeries> iterator = tslist.iterator(); iterator.hasNext();) {
					PortableTimeSeries ts = (PortableTimeSeries) iterator.next();
					String tsname = ts.getName();
					if(processData){
						List<Object> tsobs = ts.getObservations();
						List<String> tsdates = ts.getTimeSlots();
						int j = 0; // observation counter
						for (Iterator<Object> iterator2 = tsobs.iterator(); iterator2.hasNext();) {
							Data.storeStr(name, rowOffset+j+1, tsname);
							if(allNumeric){
								Double tmpValue = (Double)iterator2.next();
								if(!tmpValue.equals(Double.NaN))
									Data.storeNum(val, rowOffset+j+1, tmpValue);
								else
									Data.storeNum(val, rowOffset+j+1, Data.getMissingValue());
							}
							else{
								Data.storeStr(val, rowOffset+j+1, iterator2.next().toString());
							}
							Data.storeStr(date, rowOffset+j+1, tsdates.get(j));
							if(processMeta){
								for (Entry<String, String> dim: ts.getDimensionsMap().entrySet()) {
									String key = dim.getKey();
									String value = dim.getValue();
									int attrPos = Data.getVarIndex(key) ;
									if(attrPos > lastPos){
										lastPos = attrPos;
										//not set yet
										Data.addVarStr(key, value.length());
									}
									Data.storeStr(attrPos, rowOffset+j+1, value);
								}
								for (Entry<String, String> attr: ts.getAttributesMap().entrySet()) {
									String key = attr.getKey();
									String value = attr.getValue();
									int attrPos = Data.getVarIndex(key) ;
									if(attrPos > lastPos){
										lastPos = attrPos;
										//not set yet
										Data.addVarStr(key, value.length());
									}
									Data.storeStr(attrPos, rowOffset+j+1, value);
								}
								List<String> obsAttrNames = ts.getObsLevelAttributesNames();
								for (Iterator<String> iterator3 = obsAttrNames.iterator(); iterator3.hasNext();) {
									String attrName = (String) iterator3.next();
									List<String> obsAttr = ts.getObsLevelAttributes(attrName);
									if(obsAttr != null && !obsAttr.isEmpty()){
										int attrPos = Data.getVarIndex(attrName) ;
										if(attrPos > lastPos){
											lastPos = attrPos;
											//not set yet
											Data.addVarStr(attrName, 1);
										}
										Data.storeStr(attrPos, rowOffset+j+1, obsAttr.get(j));
									}
								}
							}
							j++;
						}
						rowOffset += j;
					}
					else{
						Data.storeStr(name, i+1, tsname);
						for (Entry<String, String> dim: ts.getDimensionsMap().entrySet()) {
							String key = dim.getKey();
							String value = dim.getValue();
							int attrPos = Data.getVarIndex(key) ;
							if(attrPos > lastPos){
								lastPos = attrPos;
								//not set yet
								Data.addVarStr(key, value.length());
							}
							Data.storeStr(attrPos, rowOffset+i+1, value);
						}
						for (Entry<String, String> attr: ts.getAttributesMap().entrySet()) {
							String key = attr.getKey();
							String value = attr.getValue();
							int attrPos = Data.getVarIndex(key) ;
							if(attrPos > lastPos){
								lastPos = attrPos;
								//not set yet
								Data.addVarStr(key, value.length());
							}
							Data.storeStr(attrPos, rowOffset+i+1, value);
						}
					}
					i++;
				}
			}
			else{
				SFIToolkit.displayln("The query did not return any observations.");
			}
		} catch (Exception e) {
			SFIToolkit.displayln("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
			returnCode = -1;
		}
				
		return returnCode;
		
	}
	
	public static int sdmxHelper(String[] args){
		SDMXHelper.start();
		return 0;
	}
	
	public static int addProvider(String[] args){
		int returnCode = 0;
		if(args.length < 2){
			SFIToolkit.displayln("The provider name and endpoint are required.");
			return -1;
		}
		String name = args[0];
		String endpoint = args[1];
		
		boolean needsCredentials = false;
		boolean needsURLEncoding = false;
		boolean supportsCompression = false;
		String description = "";
		
		if(args.length >= 3 && !args[2].isEmpty()){
			needsCredentials = args[2].equalsIgnoreCase("1") ? true : false;
		}
		if(args.length >= 4 && !args[3].isEmpty()){
			needsURLEncoding = args[3].equalsIgnoreCase("1") ? true : false;
		}
		if(args.length >= 5 && !args[4].isEmpty()){
			supportsCompression = args[4].equalsIgnoreCase("1") ? true : false;
		}
		if(args.length >= 6 && !args[5].isEmpty()){
			description = args[5];
		}
		
		try{
			SdmxClientHandler.addProvider(name, endpoint, needsCredentials, needsURLEncoding, supportsCompression, description);
		} catch (Exception e) {
			SFIToolkit.displayln("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
			returnCode = -1;
		}
	
		return returnCode;
	}
}




