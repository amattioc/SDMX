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

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.helper.SDMXHelper;
import it.bancaditalia.oss.sdmx.util.Configuration;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.stata.sfi.Data;
import com.stata.sfi.SFIToolkit;
/**
 * <p>Java class for optimizing interactions with the SdmxClientHandler in STATA. It uses the
 *  
 * 
 * @author Attilio Mattiocco
 *
 */
public class StataClientHandler {
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
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
				if(processData){
					SFIToolkit.displayln("The query returned " + dataLength + " observations.");
					Data.addVarStr("DATE", 5);
					Data.addVarDouble("VALUE");
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
						List<Double> tsobs = ts.getObservations();
						List<String> tsdates = ts.getTimeSlots();
						int j = 0; // observation counter
						for (Iterator<Double> iterator2 = tsobs.iterator(); iterator2.hasNext();) {
							Data.storeStr(name, rowOffset+j+1, tsname);
							Data.storeNum(val, rowOffset+j+1, iterator2.next());
							Data.storeStr(date, rowOffset+j+1, tsdates.get(j));
							if(processMeta){
								List<String> dimensions = ts.getDimensions();
								List<String> attributes = ts.getAttributes();
								attributes.addAll(dimensions);
								for (Iterator<String> iterator3 = attributes.iterator(); iterator3.hasNext();) {
									String attr = (String) iterator3.next();
									String[] tokens = attr.split("\\s*=\\s*");
									String key = tokens[0];
									String value = tokens[1];
									if(key != null && !key.isEmpty() && value != null && ! value.isEmpty()){
										int attrPos = Data.getVarIndex(key) ;
										if(attrPos > lastPos){
											lastPos = attrPos;
											//not set yet
											Data.addVarStr(key, value.length());
										}
										Data.storeStr(attrPos, rowOffset+j+1, value);
									}
								}
								if(processMeta){
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
							}
							j++;
						}
						rowOffset = j;
					}
					else{
						Data.storeStr(name, i+1, tsname);
						List<String> dimensions = ts.getDimensions();
						List<String> attributes = ts.getAttributes();
						attributes.addAll(dimensions);
						for (Iterator<String> iterator3 = attributes.iterator(); iterator3.hasNext();) {
							String attr = (String) iterator3.next();
							String[] tokens = attr.split("\\s*=\\s*");
							String key = tokens[0];
							String value = tokens[1];
							if(key != null && !key.isEmpty() && value != null && ! value.isEmpty()){
								int attrPos = Data.getVarIndex(key) ;
								if(attrPos > lastPos){
									lastPos = attrPos;
									//not set yet
									Data.addVarStr(key, 10);
								}
								Data.storeStr(attrPos, i+1, value);
							}
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
}




