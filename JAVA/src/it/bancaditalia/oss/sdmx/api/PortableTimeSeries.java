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
package it.bankitalia.reri.sia.sdmx.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This is a Java container for a Time Series. It will be transformed by a converter in the various 
 * statistical packages into a native time series object.
 * 
 * @author Attilio Mattiocco
 *
 */

public class PortableTimeSeries {
	
	private String frequency = null;
	private String dataflow = null;
	
	private List<String> attributes = null;
	//note that the mimensions have to be ordered as prescribed by the DSD
	private List<String> dimensions = null;
	private List<String> timeSlots = null;
	private List<Double> observations = null;
	private List<String> status = null;
	
	public PortableTimeSeries() {
		super();
		this.attributes = new ArrayList<String>();
		this.dimensions = new ArrayList<String>();
		this.timeSlots = new ArrayList<String>();
		this.observations = new ArrayList<Double>();
		this.status = new ArrayList<String>();
	}
	public List<String> getAttributes() {
		return attributes;
	}
	public String[] getAttributesArray() {
		return attributes.toArray(new String[0]);
	}
	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}
	public void addAttribute(String attribute) {
		this.attributes.add(attribute);
	}
	public List<String> getDimensions() {
		return dimensions;
	}
	public String[] getDimensionsArray() {
		return dimensions.toArray(new String[0]);
	}
	public void setDimensions(List<String> dimensions) {
		this.dimensions = dimensions;
	}
	public void addDimension(String dimension) {
		this.dimensions.add(dimension);
	}
	public void addObservation(Double observation, String timeSlot, String status) {
		this.observations.add(observation);
		this.timeSlots.add(timeSlot);
		this.status.add(status);
	}
	public List<Double> getObservations() {
		return observations;
	}
	public Double[] getObservationsArray() {
		return observations.toArray(new Double[0]);
	}
	public List<String> getTimeSlots() {
		return timeSlots;
	}
	public String[] getTimeSlotsArray() {
		return timeSlots.toArray(new String[0]);
	}
	public List<String> getStatus() {
		return this.status;
	}
	public String[] getStatusArray() {
		return this.status.toArray(new String[0]);
	}
	
	public String getName() {
		String name = this.dataflow + ".";
		String delims = "[ =]";
		for (Iterator<String> iterator = dimensions.iterator(); iterator.hasNext();) {
			String dim = (String) iterator.next();
			String[] tokens = dim.split(delims);
			String value = tokens[1];
			name += value;
			if(iterator.hasNext()){
				name += ".";
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


	
	public void reverse(){
		Collections.reverse(this.observations);
		Collections.reverse(this.timeSlots);
	}
	
	public String toString(){
		String buffer = "";
		buffer += "\nName: " + getName();
		buffer += "\nFrequency: " + frequency;
		buffer += "\nAttributes: " + attributes;
		buffer += "\nDimensions: " + dimensions;
		buffer += "\nVALUES: ";
		buffer += observations;
		buffer += "\nTIMES:";
		buffer += timeSlots;
		buffer += "\nSTATUS:";
		buffer += status;
		
		return buffer;
	}


}
