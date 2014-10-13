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
import it.bankitalia.reri.sia.util.SdmxException;

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
	private static DataCache data = null; 
	private static MetadataCache metadata = null; 
	
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
		data = null; 
		metadata = null;
		try {
			List<PortableTimeSeries> result = SdmxClientHandler.getTimeSeries(provider, tsKey, startTime, endTime);
			if(!result.isEmpty()){
				//check size of full result as a table
				int datasize = 0;
				int metasize = 0;
				for (Iterator<PortableTimeSeries> iterator = result.iterator(); iterator.hasNext();) {
					PortableTimeSeries ts = (PortableTimeSeries) iterator.next();
					datasize += ts.getObservations().size();
					metasize += ts.getDimensions().size();
					metasize += ts.getAttributes().size();
				}
				
				//init data cache
				data = new DataCache(datasize);
				metadata = new MetadataCache(metasize);
				int dataRowIndex = 0;
				int metaRowIndex = 0;
				for (Iterator<PortableTimeSeries> iterator = result.iterator(); iterator.hasNext();) {
					PortableTimeSeries ts = (PortableTimeSeries) iterator.next();
					String name = ts.getName();
					// setting metadata
					List<String> dimensions = ts.getDimensions();
					for (Iterator<String> iterator2 = dimensions.iterator(); iterator2.hasNext();) {
						String dim = (String) iterator2.next();
						//deparse dimension (KEY=VALUE)
						String[] tokens = dim.split("\\s*=\\s*");
						String key = tokens[0];
						String value = tokens[1];
						metadata.setRow(metaRowIndex, name, key, value, "DIMENSION");
						metaRowIndex++;
					}
					List<String> attributes = ts.getAttributes();
					for (Iterator<String> iterator2 = attributes.iterator(); iterator2.hasNext();) {
						String attr = (String) iterator2.next();
						//deparse dimension (KEY=VALUE)
						String[] tokens = attr.split("\\s*=\\s*");
						String key = tokens[0];
						String value = tokens[1];
						metadata.setRow(metaRowIndex, name, key, value, "ATTRIBUTE");
						metaRowIndex++;
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
						if(dataRowIndex <= data.size()){
							data.setRow(dataRowIndex, name, time, obs, stat);
						}
						else{
							throw new SdmxException("Unexpected error during Time Series pocessing in SasHandler.");
						}
						dataRowIndex++;
					}
				}
				returnCode = result.size();
			}
		} catch (Exception e) {
			logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
			data = null;
			metadata = null;
			returnCode = -1;
		}
				
		return returnCode;
		
	}
	
	public static String getMetaName(double index) throws SdmxException {
		if(metadata != null && index <= metadata.size()){
			return metadata.getName((int)index);
		}
		else{
			throw new SdmxException("Metadata cache error: cache is null or index exceeds size.");
		}
	}
	public static String getMetaKey(double index) throws SdmxException {
		if(metadata != null && index <= metadata.size()){
			return metadata.getKey((int)index);
		}
		else{
			throw new SdmxException("Metadata cache error: cache is null or index exceeds size.");
		}
	}
	public static String getMetaValue(double index) throws SdmxException {
		if(metadata != null && index <= metadata.size()){
			return metadata.getValue((int)index);
		}
		else{
			throw new SdmxException("Metadata cache error: cache is null or index exceeds size.");
		}
	}
	public static String getMetaType(double index) throws SdmxException {
		if(metadata != null && index <= metadata.size()){
			return metadata.getType((int)index);
		}
		else{
			throw new SdmxException("Metadata cache error: cache is null or index exceeds size.");
		}
	}
	
	public static double getDataObservation(double index) throws SdmxException {
		if(data != null && index <= data.size()){
			return data.getObservation((int)index);
		}
		else{
			throw new SdmxException("Data cache error: cache is null or index exceeds size.");
		}
	}
	public static String getDataTimestamp(double index) throws SdmxException {
		if(data != null && index <= data.size()){
			return data.getTimestamp((int)index);
		}
		else{
			throw new SdmxException("Data cache error: cache is null or index exceeds size.");
		}
	}
	public static String getDataStatus(double index) throws SdmxException {
		if(data != null && index <= data.size()){
			return data.getStatus((int)index);
		}
		else{
			throw new SdmxException("Data cache error: cache is null or index exceeds size.");
		}
	}
	public static String getDataName(double index) throws SdmxException {
		if(data != null && index <= data.size()){
			return data.getName((int)index);
		}
		else{
			throw new SdmxException("Data cache error: cache is null or index exceeds size.");
		}
	}
	public static int getNumberOfMeta() {
		if(metadata != null)
			return metadata.size();
		else
			return 0;
	}
	public static int getNumberOfData() {
		if(data != null)
			return data.size();
		else
			return 0;
	}
	
}

class DataCache {
	private static final int NAME_COL = 0;
	private static final int TIME_COL = 1;
	private static final int OBS_COL = 2;
	private static final int STATUS_COL = 3;
	
	Object [][] data = null;
	
	DataCache(int size) {
		super();
		this.data = new Object[size][4];
	}
	
	void setRow(int rowIndex, String name, String time, double obs, String status) throws SdmxException{
		if( data!= null && rowIndex < data.length ){
			data[rowIndex][NAME_COL] = name;
			data[rowIndex][TIME_COL] = time;
			data[rowIndex][OBS_COL] = new Double(obs);
			data[rowIndex][STATUS_COL] = status;
			
		}
		else{
			throw new SdmxException("Row index exceeds data size");
		}
	}
	double getObservation(int rowIndex) throws SdmxException{
		if( data!= null && rowIndex < data.length ){
			return ((Double)data[rowIndex][OBS_COL]).doubleValue();
		}
		else{
			throw new SdmxException("Data cache error: cache is null or index exceeds size.");
		}
	}
	String getName(int rowIndex) throws SdmxException{
		if( data!= null && rowIndex < data.length ){
			return (String) data[rowIndex][NAME_COL];
		}
		else{
			throw new SdmxException("Data cache error: cache is null or index exceeds size.");
		}
	}
	String getStatus(int rowIndex) throws SdmxException{
		if( data!= null && rowIndex < data.length ){
			return (String) data[rowIndex][STATUS_COL];
		}
		else{
			throw new SdmxException("Data cache error: cache is null or index exceeds size.");
		}
	}
	String getTimestamp(int rowIndex) throws SdmxException{
		if( data!= null && rowIndex < data.length ){
			return (String) data[rowIndex][TIME_COL];
		}
		else{
			throw new SdmxException("Data cache error: cache is null or index exceeds size.");
		}
	}
	
	int size(){
		if(data != null)
			return data.length; 
		else
			return -1;
	}

}

class MetadataCache {
	private static final int NAME_COL = 0;
	private static final int KEY_COL = 1;
	private static final int VALUE_COL = 2;
	private static final int TYPE_COL = 3;
	
	String [][] data = null;
	
	MetadataCache(int size) {
		super();
		this.data = new String[size][4];
	}
	
	void setRow(int rowIndex, String name, String key, String value, String type) throws SdmxException{
		if( data!= null && rowIndex < data.length ){
			data[rowIndex][NAME_COL] = name;
			data[rowIndex][KEY_COL] = key;
			data[rowIndex][VALUE_COL] = value;
			data[rowIndex][TYPE_COL] = type;
		}
		else{
			throw new SdmxException("Row index exceeds metadata size");
		}
	}
	String getName(int rowIndex) throws SdmxException{
		if( data!= null && rowIndex < data.length ){
			return (String) data[rowIndex][NAME_COL];
		}
		else{
			throw new SdmxException("Metadata cache error: cache is null or index exceeds size.");
		}
	}
	String getKey(int rowIndex) throws SdmxException{
		if( data!= null && rowIndex < data.length ){
			return (String) data[rowIndex][KEY_COL];
		}
		else{
			throw new SdmxException("Metadata cache error: cache is null or index exceeds size.");
		}
	}
	String getValue(int rowIndex) throws SdmxException{
		if( data!= null && rowIndex < data.length ){
			return (String) data[rowIndex][VALUE_COL];
		}
		else{
			throw new SdmxException("Metadata cache error: cache is null or index exceeds size.");
		}
	}
	String getType(int rowIndex) throws SdmxException{
		if( data!= null && rowIndex < data.length ){
			return (String) data[rowIndex][TYPE_COL];
		}
		else{
			throw new SdmxException("Metadata cache error: cache is null or index exceeds size.");
		}
	}
	
	int size(){
		if(data != null)
			return data.length; 
		else
			return -1;
	}


}


