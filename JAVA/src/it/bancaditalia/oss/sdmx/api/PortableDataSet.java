/* Copyright 2010,2015 Bank Of Italy
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

import it.bancaditalia.oss.sdmx.exceptions.DataStructureException;
import it.bancaditalia.oss.sdmx.util.Configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.table.DefaultTableModel;

/**
 * Java container for a dataset/table. In the various statistical tools it will be transformed 
 * by a converter into a native dataset.
 * 
 * @author Attilio Mattiocco
 *
 */
public class PortableDataSet{
	
	public static final String TIME_LABEL = "TIME_PERIOD";
	public static final String OBS_LABEL = "OBS_VALUE";
	public static final String ID_LABEL = "ID";
	
	private static final String sourceClass = PortableDataSet.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();
	private  DefaultTableModel model = null;

	public PortableDataSet() {
		model = new DefaultTableModel();
	}

	public PortableDataSet(List<PortableTimeSeries> tslist) throws DataStructureException {
		this();
		putTimeSeries(tslist);
	}

	public int getColumnIndex(String name) throws DataStructureException{
		int n = model.getColumnCount();
		for(int i = 0; i < n; i++){
			if(model.getColumnName(i).equals(name)){
				return(i);
			}
		}
		throw new DataStructureException("Error: column " + name + " does not exist.");
	}
	
	public int getRowCount(){
		return model.getRowCount();
	}
	
	public int getColumnCount(){
		return model.getColumnCount();
	}
	
	public String getColumnName(int idx) throws DataStructureException{
		if(idx < getColumnCount()){
			return model.getColumnName(idx);
		}
		else {
			throw new DataStructureException("Error: index exceeds number of actual columns");
		}
	}
	
	public Object getValueAt(int row, int column) throws DataStructureException {
		if(row < getRowCount() && column < getColumnCount()){
			return model.getValueAt(row, column);
		}
		else{
			throw new DataStructureException("Error: index exceeds number of actual rows or columns");
		}
	}
	
	public String[] getTimeStamps() throws DataStructureException {
		int rows = getRowCount();
		String[] result = new String[rows];
		int timeCol = getColumnIndex(TIME_LABEL);
		for(int i = 0; i < rows; i++){
			result[i] = (String) getValueAt(i, timeCol);
		}
		return(result);
	}

	public Double[] getObservations() throws DataStructureException {
		int rows = getRowCount();
		Double[] result = new Double[rows];
		int obsCol = getColumnIndex(OBS_LABEL);
		for(int i = 0; i < rows; i++){
			result[i] = (Double) getValueAt(i, obsCol);
		}
		return(result);
	}

	public String[] getMetadata(String name){
		int rows = getRowCount();
		String[] result = new String[rows];
		try {
			int obsCol = getColumnIndex(name);
			for(int i = 0; i < rows; i++){
				result[i] = (String) getValueAt(i, obsCol);
			}
		} catch (DataStructureException e) {
			result = new String[0];
		}
		return(result);
	}
	
	public String[] getMetadataNames() throws DataStructureException{
		int cols = getColumnCount();
		List<String> result = new ArrayList<String>();
		for(int i = 0; i < cols; i++){
			String colName = getColumnName(i);
			if(!colName.equals(OBS_LABEL) && !colName.equals(TIME_LABEL)){
				result.add(colName);
			}
		}
		return result.toArray(new String[0]);
	}
	
	public void putTimeSeries(List<PortableTimeSeries> tslist) throws DataStructureException{
		final String sourceMethod = "putTimeSeries";
		logger.entering(sourceClass, sourceMethod);
		for (Iterator<PortableTimeSeries> iterator = tslist.iterator(); iterator.hasNext();) {
			PortableTimeSeries ts = (PortableTimeSeries) iterator.next();
			putTimeSeries(ts);
		}
		logger.exiting(sourceClass, sourceMethod);
	}
	
	public void putTimeSeries(PortableTimeSeries ts) throws DataStructureException{
		final String sourceMethod = "putTimeSeries";
		logger.entering(sourceClass, sourceMethod);
		int row = model.getRowCount();
		
		List<String> dims = ts.getDimensions();
		List<String> attrs = ts.getAttributes();
		List<Double> values = ts.getObservations();
		List<String> times = ts.getTimeSlots();
		List<String> obsAttrs = ts.getObsLevelAttributesNames();
		String tsName = ts.getName();
		
		int n = values.size();
		//model.setRowCount(row + n);
		for (int index = 0; index < n; index++) {
			Double val = (Double) values.get(index);
			if(times != null){
				String time = times.get(index);
				if(time != null)
					addValue(row, TIME_LABEL, time);
			}
			addValue(row, OBS_LABEL, val);
			if(tsName != null && !tsName.isEmpty()){
				addValue(row, ID_LABEL, tsName);	
			}
			
			// set obs level attributes
			for (Iterator<String> iterator = obsAttrs.iterator(); iterator.hasNext();) {
				String name = (String) iterator.next();
				String value = ts.getObsLevelAttributes(name).get(index);
				addValue(row, name, value);
			}
		
			
			// set dimensions
			String delims = "=";
			for (Iterator<String> iterator = dims.iterator(); iterator.hasNext();) {
				String dim = (String) iterator.next();
				String[] tokens = dim.split(delims);
				String name = tokens[0];
				String value = tokens[1];
				addValue(row, name, value);
			}
			// set attributes
			for (Iterator<String> iterator = attrs.iterator(); iterator.hasNext();) {
				String attr = (String) iterator.next();
				String[] tokens = attr.split(delims);
				String name = tokens[0];
				String value = tokens[1];
				addValue(row, name, value);
			}

			
			row++;
		}
		
		logger.exiting(sourceClass, sourceMethod);
	}
	
	public void addValue(int row, String columnName, Object value) throws DataStructureException{
		if(row >= model.getRowCount()){
			model.setRowCount(row + 1);
		}
		int idx = -1;
		try {
			idx = getColumnIndex(columnName);
		} catch (DataStructureException e) {
			model.addColumn(columnName);
			try {
				idx = getColumnIndex(columnName);
			} catch (DataStructureException e1) {
				logger.severe(e1.getMessage());
				throw new DataStructureException("Unexpected error while adding column: " + columnName);
			}
		}
		model.setValueAt(value, row, idx);
	}
	
	@Override
	public String toString(){
		int rows = model.getRowCount();
		int cols = model.getColumnCount();
		String buffer = "";
		for(int j = 0; j < cols; j++){
			if(j != 0){
				buffer += ";";
			}
			buffer += model.getColumnName(j);
		}
		buffer += "\n";
		for(int i = 0; i < rows; i++){
			if(i != 0){
				buffer += "\n";
			}
			for(int j = 0; j < cols; j++){
				if(j != 0){
					buffer += ";";
				}
				buffer += model.getValueAt(i,j);
			}			
		}
		return buffer;
	}
}
