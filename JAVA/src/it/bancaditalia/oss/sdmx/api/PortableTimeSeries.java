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

import java.io.Serializable;
import java.util.AbstractMap;
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

public class PortableTimeSeries implements Serializable, Iterable<Observation> {
	
	private static final long serialVersionUID = 1L;

	public static final String GENERATEDNAME_ATTR_NAME = "CONNECTORS_AUTONAME";
	protected static final Logger logger = Configuration.getSdmxLogger();

	private String frequency = null;
	private Dataflow dataflow = null;
	
	private final Map<String, String> attributes = new HashMap<String, String>();
	//note that the dimensions have to be ordered as prescribed by the DSD
	// Map each dimension to its content representation (list of codes) in this timeseries
	// TODO: implement a Code class and use Map<Dimension, List<Code>>.
	private final Map<String, Entry<String, String>> dimensions = new LinkedHashMap<String, Entry<String, String>>();
	private List<String> timeSlots = new ArrayList<String>();
	private List<String> observations = new ArrayList<String>();
	//left here for backward compatibility
	final List<String> status = new ArrayList<String>();
	final Map<String, List<String>> obsLevelAttributes = new HashMap<String, List<String>>();

	private boolean errorFlag = false;
	private boolean numeric = true;
	private String errorMessage = null;
	private String name;
	private boolean useGeneratedName = true;
	
	/**
	 * @return
	 */
	public Map<String, String> getAttributesMap() 
	{
		if (!attributes.containsKey(GENERATEDNAME_ATTR_NAME) && dataflow != null && dataflow.getName() != null)
		{
			StringBuilder nameBuilder = new StringBuilder();
			
			if(dataflow != null && dataflow.getName() != null && !dataflow.getName().isEmpty())
				nameBuilder.append(dataflow.getName());
			
			for (Entry<String, String> code: dimensions.values())
				nameBuilder.append(", " + code.getKey() + "(" + (code.getValue() != null ? code.getValue() : "") + ")");
			
			attributes.put(GENERATEDNAME_ATTR_NAME, nameBuilder.toString());
		}
		
		return attributes;
	}
	
	/**
	 * @return
	 */
	public String[] getAttributeNamesArray() {
		return getAttributesMap().keySet().toArray(new String[0]);
	}
	
	/**
	 * @param code
	 * @return
	 */
	public String getAttribute(String code){
		return getAttributesMap().get(code);
	}

	/**
	 * @param attributes
	 */
	public void setAttributes(Map<String, String> attributes) {
		getAttributesMap().clear();
		getAttributesMap().putAll(attributes);
	}

	/**
	 * @param key
	 * @param value
	 */
	public void addAttribute(String key, String value) {
		this.getAttributesMap().put(key, value);
	}
	
	/**
	 * @return
	 */
	public Map<String, String> getDimensionsMap() {
		Map<String, String> result = new LinkedHashMap<String, String>();
		for (Entry<String, Entry<String, String>> dimension: dimensions.entrySet())
			result.put(dimension.getKey(), Configuration.getCodesPolicy().equalsIgnoreCase(Configuration.SDMX_CODES_POLICY_DESC) ? 
					dimension.getValue().getValue() : dimension.getValue().getKey());
			
		return result;
	}

	/**
	 * @return
	 */
	public String[] getDimensionNamesArray() {
		return dimensions.keySet().toArray(new String[0]);
	}

	/**
	 * @param code
	 * @return
	 */
	public String getDimension(String code){
		return dimensions.containsKey(code) ? dimensions.get(code).getKey() : null;
	}

	/**
	 * @param dimensions
	 */
	public void setDimensions(Map<String, Entry<String, String>> dimensions) {
		this.dimensions.clear();
		this.dimensions.putAll(dimensions);
		attributes.remove(GENERATEDNAME_ATTR_NAME);
		if (useGeneratedName)
			name = null;
	}

	/**
	 * @param key
	 * @param value
	 */
	public void addDimension(String key, String value) {
		this.dimensions.put(key, new AbstractMap.SimpleEntry<String, String>(value, null));
		attributes.remove(GENERATEDNAME_ATTR_NAME);
		if (useGeneratedName)
			name = null;
	}
	
	/**
	 * @param observation
	 * @param timeSlot
	 * @param attributes
	 */
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
				String key = iterator.next();
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

	/**
	 * @param obs
	 * @throws DataStructureException
	 */
	public void setObservations(List<String> obs) throws DataStructureException{
		if(obs.size() == this.timeSlots.size()){
			this.observations = obs;
		}
		else{
			throw new DataStructureException("Error setting data in time series. Wrong observation number.");
		}
	}

	/**
	 * @return
	 */
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

	/**
	 * @return
	 * @throws DataStructureException
	 */
	public Object[] getObservationsArray() throws DataStructureException {
		if(isNumeric()){
			return getObservations().toArray(new Double[0]);
		}
		else{
			return getObservations().toArray(new String[0]);
		}		
	}

	/**
	 * @param dates
	 * @throws DataStructureException
	 */
	public void setTimeSlots(List<String> dates) throws DataStructureException{
		if(dates.size() == this.observations.size()){
			this.timeSlots = dates;
		}
		else{
			throw new DataStructureException("Error setting dates in time series. Wrong dates number.");
		}
	}

	/**
	 * @return
	 */
	public List<String> getTimeSlots() {
		return timeSlots;
	}

	/**
	 * @return
	 */
	public String[] getTimeSlotsArray() {
		return timeSlots.toArray(new String[0]);
	}
	
	/**
	 * @return
	 */
	public List<String> getObsLevelAttributesNames() {
		return new ArrayList<String>(obsLevelAttributes.keySet());
	}

	/**
	 * @return
	 */
	public String[] getObsLevelAttributesNamesArray() {
		return getObsLevelAttributesNames().toArray(new String[0]);
	}

	/**
	 * @param attributeName
	 * @return
	 */
	public List<String> getObsLevelAttributes(String attributeName) {
		return obsLevelAttributes.get(attributeName);
	}
	
	/**
	 * @param attributeName
	 * @return
	 */
	public String[] getObsLevelAttributesArray(String attributeName) {
		return getObsLevelAttributes(attributeName).toArray(new String[0]);
	}
	
	/**
	 * @return
	 */
	public String getName() {
		if (useGeneratedName && name == null)
		{
			// we determine the name in the SDMX way
			StringBuilder nameBuilder = new StringBuilder();
			
			if(dataflow != null && dataflow.getId() != null && !dataflow.getId().isEmpty())
				nameBuilder.append(dataflow.getId() + ".");
			
			for (Entry<String, String> code: dimensions.values())
				nameBuilder.append((Configuration.getCodesPolicy().equalsIgnoreCase(Configuration.SDMX_CODES_POLICY_DESC) ? 
						code.getValue() : code.getKey()) + ".");
			
			// remove last dot
			nameBuilder.setLength(nameBuilder.length() - 1);
			name = nameBuilder.toString();
		}
		
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) 
	{
		useGeneratedName = false;
		this.name = name;
	}

	/**
	 * @return
	 */
	public String getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	/**
	 * @return
	 */
	public String getDataflow() {
		return dataflow.getId();
	}

	/**
	 * @return
	 */
	public Dataflow getDataflowObject() {
		return dataflow;
	}

	/**
	 * @param dataflow
	 */
	public void setDataflow(Dataflow dataflow) {
		this.dataflow = dataflow;
	}
	
	/**
	 * @return
	 */
	public boolean isErrorFlag() {
		return errorFlag;
	}

	/**
	 * @param errorFlag
	 */
	public void setErrorFlag(boolean errorFlag) {
		this.errorFlag = errorFlag;
	}

	/**
	 * @return
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return an Iterator over Observations
	 */
	@Override
	public Iterator<Observation> iterator()
	{
		return new ObservationIterator(this);
	}
	
	/**
	 * 
	 */
	public void reverse(){
		Collections.reverse(this.observations);
		Collections.reverse(this.timeSlots);
		for (Iterator<String> iterator = obsLevelAttributes.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			List<String> attrs = obsLevelAttributes.get(key);
			Collections.reverse(attrs);
			obsLevelAttributes.put(key, attrs);
		}
	}
	
	/**
	 * @return
	 */
	public boolean isNumeric() {
		return numeric;
	}

	/**
	 * @param numeric
	 */
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
		buffer += "\nAttributes: " + getAttributesMap();
		buffer += "\nDimensions: " + getDimensionsMap();
		buffer += "\nVALUES: ";
		buffer += observations;
		buffer += "\nTIMES:";
		buffer += timeSlots;
		buffer += "\nOBSERVATION ATTRIBUTES:";
		buffer += obsLevelAttributes;
		
		return buffer;
	}
	
	/**
	 * @return
	 */
	@Deprecated
	public List<String> getAttributes() {
		return Arrays.asList(getAttributesArray());
	}

	/**
	 * @param code
	 * @return
	 */
	@Deprecated
	public String getAttributeValue(String code){
		return getAttribute(code);
	}

	/**
	 * @return
	 */
	@Deprecated 
	public String[] getAttributesArray() {
		String[] result = new String[getAttributesMap().size()];
		int i = 0;
		for (Entry<String, String> x: getAttributesMap().entrySet())
			result[i++] = x.getKey() + "=" + x.getValue();
		return result;
	}

	/**
	 * @param attributes
	 */
	@Deprecated
	public void setAttributes(List<String> attributes) {
		this.getAttributesMap().clear();
		for (String pair: attributes)
			this.getAttributesMap().put(pair.split("=")[0], pair.split("=")[1]);
	}

	/**
	 * @param attribute
	 */
	@Deprecated
	public void addAttribute(String attribute) {
		addAttribute(attribute.split("=")[0], attribute.split("=")[1]);
	}

	/**
	 * @return
	 */
	@Deprecated
	public List<String> getDimensions() {
		return Arrays.asList(getDimensionsArray());
	}

	/**
	 * @param code
	 * @return
	 */
	@Deprecated
	public String getDimensionValue(String code){
		return getDimension(code);
	}

	/**
	 * @return
	 */
	@Deprecated
	public String[] getDimensionsArray() {
		String[] result = new String[dimensions.size()];
		int i = 0;
		for (Entry<String, Entry<String, String>> code: dimensions.entrySet())
			result[i++] = code.getKey() + "=" + code.getValue().getKey();
		return result;
	}

	/**
	 * @param dimension
	 */
	@Deprecated
	public void addDimension(String dimension) {
		addDimension(dimension.split("=")[0], dimension.split("=")[1]);
	}

	/**
	 * @return
	 */
	@Deprecated
	public List<String> getStatus() {
		return this.status;
	}

	/**
	 * @return
	 */
	@Deprecated
	public String[] getStatusArray() {
		return this.status.toArray(new String[0]);
	}
}
