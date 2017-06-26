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
package it.bancaditalia.oss.sdmx.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import it.bancaditalia.oss.sdmx.exceptions.DataStructureException;
import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * This is a Java container for a Time Series. It will be transformed by a converter in the various 
 * statistical packages into a native time series object.
 * 
 * @author Attilio Mattiocco
 *
 */

public class PortableTimeSeries {
	
	protected static Logger logger = Configuration.getSdmxLogger();

	private String frequency = null;
	private String dataflow = null;
	
	private Map<String, String> attributes = new HashMap<String, String>();
	//note that the dimensions have to be ordered as prescribed by the DSD
	private Map<String, String> dimensions = new LinkedHashMap<String, String>();
	private List<String> timeSlots = new ArrayList<String>();
	private List<String> observations = new ArrayList<String>();
	//left here for backward compatibility
	private List<String> status = new ArrayList<String>();
	private Map<String, List<String>> obsLevelAttributes = new HashMap<String, List<String>>();

	private String name;
	private boolean errorFlag = false;
	private boolean numeric = true;
	private String errorMessage = null;
	
	public PortableTimeSeries() {
		super();
	}

	//quick constructor for typical basic time series
	public PortableTimeSeries(String name, String[] timeSlots, Double[] observations, String statusName, String[] status) throws DataStructureException {
		super();
		this.name = name;
		if(timeSlots != null && observations != null && observations.length == timeSlots.length){
			for (int i = 0; i < observations.length; i++) {
				this.observations.add(observations[i].toString());
			}
			this.timeSlots = Arrays.asList(timeSlots);
		}
		else{
			throw new DataStructureException("Time slots and data have to be not null and of the same length");
		}
		if(statusName != null && status != null && status.length == observations.length){
			this.obsLevelAttributes.put(statusName, Arrays.asList(status));
		}
	}

	public Map<String, String> getAttributesMap() {
		return attributes;
	}
	
	public String[] getAttributeNamesArray() {
		return attributes.keySet().toArray(new String[0]);
	}
	
	public String getAttribute(String code){
		return attributes.get(code);
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public void addAttribute(String key, String value) {
		this.attributes.put(key, value);
	}
	
	public Map<String, String> getDimensionsMap() {
		return dimensions;
	}

	public String[] getDimensionNamesArray() {
		return dimensions.keySet().toArray(new String[0]);
	}

	public String getDimension(String code){
		return dimensions.get(code);
	}

	public void setDimensions(Map<String, String> dimensions) {
		this.dimensions.clear();
		this.dimensions.putAll(dimensions);
	}

	public void addDimension(String key, String value) {
		this.dimensions.put(key, value);
	}
	
	public void addObservation(String observation, String timeSlot, Map<String, String> attributes){
		if(observation == null || observation.isEmpty()){
			logger.info(getName() + ": missing observation for time slot: " + timeSlot + ", I'll set a NaN.");
			this.observations.add("NaN");
		}
		else{
			this.observations.add(observation);
		}
		
		if(timeSlot == null || timeSlot.isEmpty()){
			logger.info(getName() + ": a time slot is missing. This is not a well formed time series. I'll set a blank character.");
			timeSlot = "";
		}
		this.timeSlots.add(timeSlot);
		
		if(attributes != null){
			for (Iterator<String> iterator = attributes.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				//backward compatibility, to be removed in a couple of versions
				if(key.equals("OBS_STATUS")){
					this.status.add(attributes.get(key));
				}
				if(obsLevelAttributes.containsKey(key)){
					obsLevelAttributes.get(key).add(attributes.get(key));
				}
				else{
					//new attribute
					ArrayList<String> newattr = new ArrayList<String>();
					for (int i = 0; i < timeSlots.size() - 1; i++) {
						newattr.add("");
					}
					newattr.add(attributes.get(key));
					obsLevelAttributes.put(key, newattr);
				}
			}
		}
		//now add empty slots for all attributes that are not present
		if(obsLevelAttributes != null){
			for (Iterator<List<String>> iterator = obsLevelAttributes.values().iterator(); iterator.hasNext();) {
				ArrayList<String> tmp = (ArrayList<String>) iterator.next();
				if(tmp.size() < timeSlots.size()){
					tmp.add("");
				}
			}
		}
	}
	public void setObservations(List<String> obs) throws DataStructureException{
		if(obs.size() == this.timeSlots.size()){
			this.observations = obs;
		}
		else{
			throw new DataStructureException("Error setting data in time series. Wrong observation number.");
		}
	}
	public List<Object> getObservations() {
		List<Object> result = new ArrayList<Object>();
		if(isNumeric()){
			for (String obs : observations) {
				try {
					result.add(Double.valueOf(obs));		
				} catch (NumberFormatException  e) {
					logger.info(getName() + ": found invalid observation for time series: " + this.getName() + ", I'll set a NaN.");
					result.add(new Double("NaN"));
				}
					
			}
		}
		else{
			for (String obs : observations) {
				result.add(obs);			
			}
		}		
		return result;
	}
	public Object[] getObservationsArray() throws DataStructureException {
		if(isNumeric()){
			return getObservations().toArray(new Double[0]);
		}
		else{
			return getObservations().toArray(new String[0]);
		}		
	}
	public void setTimeSlots(List<String> dates) throws DataStructureException{
		if(dates.size() == this.observations.size()){
			this.timeSlots = dates;
		}
		else{
			throw new DataStructureException("Error setting dates in time series. Wrong dates number.");
		}
	}
	public List<String> getTimeSlots() {
		return timeSlots;
	}
	public String[] getTimeSlotsArray() {
		return timeSlots.toArray(new String[0]);
	}
	public List<String> getObsLevelAttributesNames() {
		return new ArrayList<String>(obsLevelAttributes.keySet());
	}

	public String[] getObsLevelAttributesNamesArray() {
		return getObsLevelAttributesNames().toArray(new String[0]);
	}

	public List<String> getObsLevelAttributes(String attributeName) {
		return obsLevelAttributes.get(attributeName);
	}
	
	public String[] getObsLevelAttributesArray(String attributeName) {
		return getObsLevelAttributes(attributeName).toArray(new String[0]);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		String name = this.name;
		if(name == null || name.isEmpty()){
			// we determine the name in the SDMX way
			if(dimensions.size() > 0){
				if(dataflow != null && !dataflow.isEmpty()){
					name = this.dataflow + ".";
				}
				else{
					name = "";
				}
				ArrayList<String> dimValues = new ArrayList<String>(dimensions.values());
				// dimensions.size() > 0
				name += dimValues.get(0);
				for (String dimValue: dimValues.subList(1, dimValues.size()))
					name += "." + dimValue;
			}
		}
		return name;
	}
	
	public String getFrequency() {
		return frequency;
	}
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	public String getDataflow() {
		return dataflow;
	}
	public void setDataflow(String dataflow) {
		this.dataflow = dataflow;
	}
	
	public boolean isErrorFlag() {
		return errorFlag;
	}

	public void setErrorFlag(boolean errorFlag) {
		this.errorFlag = errorFlag;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void reverse(){
		Collections.reverse(this.observations);
		Collections.reverse(this.timeSlots);
		for (Iterator<String> iterator = obsLevelAttributes.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			List<String> attrs = obsLevelAttributes.get(key);
			Collections.reverse(attrs);
			obsLevelAttributes.put(key, attrs);
		}
	}
	
	public boolean isNumeric() {
		return numeric;
	}

	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}

	public String toString(){
		String buffer = "";
		buffer += "\nName: " + getName();
		buffer += "\nFrequency: " + frequency;
		buffer += "\nnumeric: " + numeric;
		buffer += "\nerror: " + isErrorFlag();
		buffer += "\nerror_msg: " + getErrorMessage();
		buffer += "\nAttributes: " + attributes;
		buffer += "\nDimensions: " + dimensions;
		buffer += "\nVALUES: ";
		buffer += observations;
		buffer += "\nTIMES:";
		buffer += timeSlots;
		buffer += "\nOBSERVATION ATTRIBUTES:";
		buffer += obsLevelAttributes;
		
		return buffer;
	}
	
	@Deprecated
	public List<String> getAttributes() {
		return Arrays.asList(getAttributesArray());
	}

	@Deprecated
	public String getAttributeValue(String code){
		return getAttribute(code);
	}

	@Deprecated 
	public String[] getAttributesArray() {
		String[] result = new String[attributes.size()];
		int i = 0;
		for (Entry<String, String> x: attributes.entrySet())
			result[i++] = x.getKey() + "=" + x.getValue();
		return result;
	}

	@Deprecated
	public void setAttributes(List<String> attributes) {
		this.attributes.clear();
		for (String pair: attributes)
			this.attributes.put(pair.split("=")[0], pair.split("=")[1]);
	}

	@Deprecated
	public void addAttribute(String attribute) {
		addAttribute(attribute.split("=")[0], attribute.split("=")[1]);
	}

	@Deprecated
	public List<String> getDimensions() {
		return Arrays.asList(getDimensionsArray());
	}

	@Deprecated
	public String getDimensionValue(String code){
		return getDimension(code);
	}

	@Deprecated
	public String[] getDimensionsArray() {
		String[] result = new String[dimensions.size()];
		int i = 0;
		for (Entry<String, String> x: dimensions.entrySet())
			result[i++] = x.getKey() + "=" + x.getValue();
		return result;
	}

	@Deprecated
	public void setDimensions(List<String> dimensions) {
		this.dimensions.clear();
		for (String pair: dimensions)
		{
			System.out.println(Arrays.asList(Thread.getAllStackTraces().get(Thread.currentThread())));
			System.out.println(pair);
			System.out.println(Arrays.asList(pair.split("=")));
			this.dimensions.put(pair.split("=")[0], pair.split("=")[1]);
		}
	}

	@Deprecated
	public void addDimension(String dimension) {
		addDimension(dimension.split("=")[0], dimension.split("=")[1]);
	}

	@Deprecated
	public List<String> getStatus() {
		return this.status;
	}

	@Deprecated
	public String[] getStatusArray() {
		return this.status.toArray(new String[0]);
	}

}
