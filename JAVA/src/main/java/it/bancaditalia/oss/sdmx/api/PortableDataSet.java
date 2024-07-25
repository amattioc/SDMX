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

import java.io.Serializable;
import java.time.Year;
import java.time.YearMonth;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Logger;

import javax.swing.table.DefaultTableModel;

import it.bancaditalia.oss.sdmx.exceptions.DataStructureException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.WeekConverter;

/**
 * Java container for a dataset/table. In the various statistical tools it will be transformed by a converter into a
 * native dataset.
 * 
 * @author Attilio Mattiocco
 *
 */
public class PortableDataSet<T> implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final String	TIME_LABEL		= "TIME_PERIOD";
	public static final String	FREQ_LABEL		= "FREQ";
	public static final String	OBS_LABEL		= "OBS_VALUE";
	public static final String	ID_LABEL		= "ID";

	private static final String	sourceClass		= PortableDataSet.class.getSimpleName();
	protected static Logger		logger			= Configuration.getSdmxLogger();

	private boolean				errorFlag		= false;
	private boolean				numeric			= false;
	private StringJoiner		errorObjects	= new StringJoiner(", ");
	private String				dataflow		= null;

	private DefaultTableModel	model			= null;

	/**
	 * Creates an empty dataset.
	 */
	public PortableDataSet()
	{
		model = new DefaultTableModel();
	}

	/**
	 * Creates a dataset from a list of series.
	 * 
	 * @param tslist A list of series to add to this dataset.
	 * @throws DataStructureException if there is an error adding series.
	 */
	public <U extends T> PortableDataSet(List<PortableTimeSeries<U>> tslist) throws DataStructureException
	{
		this();
		setTimeSeries(tslist);
	}

	public String getDataflow() {
		return dataflow;
	}

	public void setDataflow(String dataflow) {
		this.dataflow = dataflow;
	}

	/**
	 * Returns the index of a column in this dataset given its name.
	 * 
	 * @param name A column name
	 * @return The index of a column with given name in this dataset.
	 * @throws DataStructureException if the column doesn't exists.
	 */
	public int getColumnIndex(String name) throws DataStructureException
	{
		int n = model.getColumnCount();
		for (int i = 0; i < n; i++)
		{
			if (model.getColumnName(i).equals(name))
			{
				return (i);
			}
		}
		throw new DataStructureException("Error: column " + name + " does not exist.");
	}

	/**
	 * @return The number of rows in this dataset.
	 */
	public int getRowCount()
	{
		return model.getRowCount();
	}

	/**
	 * @return The number of columns in this dataset.
	 */
	public int getColumnCount()
	{
		return model.getColumnCount();
	}

	/**
	 * Returns the name of a column in this dataset given its index.
	 * 
	 * @param idx A column index.
	 * @return The name of the column with given index.
	 * @throws DataStructureException If the column index is out of bounds.
	 */
	public String getColumnName(int idx) throws DataStructureException
	{
		if (idx >= 0 && idx < getColumnCount())
		{
			return model.getColumnName(idx);
		}
		else
		{
			throw new DataStructureException("Error: index exceeds number of actual columns");
		}
	}

	/**
	 * Gets the value of a dataset cell with specified coordinates.
	 * 
	 * @param row The row index
	 * @param column The column index
	 * @return An object corresponding to a cell in this dataset with given coordinates.
	 * @throws DataStructureException If row or column indexes are out of bounds.
	 */
	public Object getValueAt(int row, int column) throws DataStructureException
	{
		if (row >= 0 && column >= 0 && row < getRowCount() && column < getColumnCount())
		{
			return model.getValueAt(row, column);
		}
		else
		{
			throw new DataStructureException("Error: index exceeds number of actual rows or columns");
		}
	}

	/**
	 * @return A flattened array containing all observations timestamps for all series in this dataset.
	 * @throws DataStructureException If any error occurs
	 */
	public String[] getTimeStamps() throws DataStructureException
	{
		int rows = getRowCount();
		String[] result = new String[rows];
		int timeCol = getColumnIndex(TIME_LABEL);
		for (int i = 0; i < rows; i++)
		{
			result[i] = (String) getValueAt(i, timeCol);
		}
		return (result);
	}

	/**
	 * @return A flattened array containing all observations timestamps in gregorian format (end of period).
	 * @throws DataStructureException If any error occurs
	 * @throws SdmxInvalidParameterException 
	 */
	public String[] getGregorianTimeStamps() throws DataStructureException, SdmxInvalidParameterException
	{
		int rows = getRowCount();
		String[] result = new String[rows];
		String freq = "D";
		int freqCol = -1;
		int timeCol = getColumnIndex(TIME_LABEL);
		try{
			freqCol = getColumnIndex(FREQ_LABEL);
		}
		catch(DataStructureException dse){
			// set daily freq - do nothing and cross fingers
		}
		for (int i = 0; i < rows; i++)
		{
			String time = (String) getValueAt(i, timeCol);
			if(freqCol != -1){
				freq = (String) getValueAt(i, freqCol);
			}
			
			if(freq.equalsIgnoreCase("M")){
				result[i] = YearMonth.parse(time).atEndOfMonth().toString();
			}			
			else if(freq.equalsIgnoreCase("Q")){
				result[i] = time.replace("Q1", "03-31").replace("Q2", "06-30").replace("Q3", "09-30").replace("Q4", "12-31");
			}			
			else if(freq.equalsIgnoreCase("A")){
				result[i] = Year.parse(time).atMonth(12).atEndOfMonth().toString();
			}
			else if(freq.equalsIgnoreCase("H")){
				result[i] = ((String) getValueAt(i, timeCol)).replace("S1", "06-30").replace("S2", "12-31");   
			}
			else if(freq.equalsIgnoreCase("W")){
				result[i] = WeekConverter.convert((String) getValueAt(i, timeCol));   
			}
			else{
				result[i] = ((String) getValueAt(i, timeCol));
			}
		}
		return (result);
	}

	/**
	 * @return A flattened array containing all observations values for all series in this dataset.
	 * @throws DataStructureException If any error occurs
	 */
	public Object[] getObservations() throws DataStructureException
	{
		int rows = getRowCount();
		Object[] result = new Object[rows];
		int obsCol = getColumnIndex(OBS_LABEL);
		for (int i = 0; i < rows; i++)
		{
			result[i] = getValueAt(i, obsCol);
		}
		return result;
	}

	/**
	 * @param name The name of a metadata attribute.
	 * @return A flattened array containing all metadata with given name for all series in this dataset, or an empty
	 *         array if the attribute is not found.
	 */
	public String[] getMetadata(String name)
	{
		int rows = getRowCount();
		String[] result = new String[rows];
		try
		{
			int obsCol = getColumnIndex(name);
			for (int i = 0; i < rows; i++)
			{
				result[i] = (String) getValueAt(i, obsCol);
			}
		}
		catch (DataStructureException e)
		{
			result = new String[0];
		}
		return (result);
	}

	/**
	 * @return An array containing all metadata attribute names.
	 * @throws DataStructureException if an error occurs
	 */
	public String[] getMetadataNames() throws DataStructureException
	{
		int cols = getColumnCount();
		List<String> result = new ArrayList<>();
		for (int i = 0; i < cols; i++)
		{
			String colName = getColumnName(i);
			if (!colName.equals(OBS_LABEL) && !colName.equals(TIME_LABEL))
				result.add(colName);
		}
		return result.toArray(new String[0]);
	}

	/**
	 * Adds a list of series to this dataset.
	 * 
	 * @param tslist The list of series to add to this dataset.
	 * @throws DataStructureException if an error occurs adding the series.
	 */
	public <U> void setTimeSeries(List<PortableTimeSeries<U>> tslist) throws DataStructureException
	{
		final String sourceMethod = "putTimeSeries";
		logger.entering(sourceClass, sourceMethod);
		// check if all time series are numeric. Otherwise, convert everything to string
		boolean allNumeric = true;
		for (PortableTimeSeries<?> series : tslist)
			if (allNumeric && !series.isNumeric())
				allNumeric = false;

		for (PortableTimeSeries<?> ts : tslist)
			putTimeSeries(ts, allNumeric);

		logger.exiting(sourceClass, sourceMethod);
	}

	/**
	 * Sets a value for an element in this dataset with given coordinates, expanding the dataset as needed.
	 * 
	 * @param row The row index of the element which is to receive the value.
	 * @param columnName The name of the column of the element which is to receive the value.
	 * @param value The value to set.
	 * @throws DataStructureException if an error occurs
	 */
	public void addValue(int row, String columnName, Object value) throws DataStructureException
	{
		if (row >= model.getRowCount())
		{
			model.setRowCount(row + 1);
		}
		int idx = -1;
		try
		{
			idx = getColumnIndex(columnName);
		}
		catch (DataStructureException e)
		{
			model.addColumn(columnName);
			try
			{
				idx = getColumnIndex(columnName);
			}
			catch (DataStructureException e1)
			{
				logger.severe(e1.getMessage());
				throw new DataStructureException("Unexpected error while adding column: " + columnName);
			}
		}
		model.setValueAt(value, row, idx);
	}

	/**
	 * @return if any series in this dataset contain an error.
	 */
	public boolean isErrorFlag()
	{
		return errorFlag;
	}

	/**
	 * @param errorFlag the new error flag status
	 */
	public void setErrorFlag(boolean errorFlag)
	{
		this.errorFlag = errorFlag;
	}

	/**
	 * @return The concatenated error messages.
	 */
	public String getErrorObjects()
	{
		return errorObjects.toString();
	}

	/**
	 * @param text a text to concatenate to the error message of this dataset.
	 */
	public void addErrorObjects(String text)
	{
		errorObjects.add(text);
	}

	/**
	 * @return true if all the series in this dataset are numeric.
	 */
	public boolean isNumeric()
	{
		return numeric;
	}

	/**
	 * @param numeric the new numeric status
	 */
	public void setNumeric(boolean numeric)
	{
		this.numeric = numeric;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		int rows = model.getRowCount();
		int cols = model.getColumnCount();
		String buffer = "";
		for (int j = 0; j < cols; j++)
		{
			if (j != 0)
			{
				buffer += ";";
			}
			buffer += model.getColumnName(j);
		}
		buffer += "\n";
		for (int i = 0; i < rows; i++)
		{
			if (i != 0)
			{
				buffer += "\n";
			}
			for (int j = 0; j < cols; j++)
			{
				if (j != 0)
				{
					buffer += ";";
				}
				buffer += model.getValueAt(i, j);
			}
		}
		return buffer;
	}

	private void putTimeSeries(PortableTimeSeries<?> ts, boolean allNumeric) throws DataStructureException
	{
		final String sourceMethod = "putTimeSeries";
		logger.entering(sourceClass, sourceMethod);
		int row = model.getRowCount();
		setNumeric(allNumeric);

		Set<String> attrNames = ts.getObsLevelAttributesNames();
		String tsName = ts.getName();

		// check errors
		if (ts.isErrorFlag())
		{
			errorFlag = true;
			addErrorObjects(tsName);
		}

		// model.setRowCount(row + n);
		for (BaseObservation<?> obs : ts)
		{
			Object val = allNumeric && ts.isNumeric() ? obs.getValueAsDouble() : obs.getValue();

			addValue(row, TIME_LABEL, obs.getTimeslot());
			addValue(row, OBS_LABEL, val);
			if (tsName != null && !tsName.isEmpty())
				addValue(row, ID_LABEL, tsName);

			// set obs level attributes
			for (String attrName : attrNames)
				addValue(row, attrName, obs.getAttributeValue(attrName));

			// set dimensions
			for (Entry<String, String> dim : ts.getDimensionsMap().entrySet())
				addValue(row, dim.getKey(), dim.getValue());

			// set attributes
			for (Entry<String, String> dim : ts.getAttributesMap().entrySet())
				addValue(row, dim.getKey(), dim.getValue());

			row++;
		}

		logger.exiting(sourceClass, sourceMethod);
	}
	
	/**
	 * @return a view of this table as a list of columns
	 */
	public List<List<Object>> columnsView()
	{
		return new AbstractList<List<Object>>() {

			@Override
			public List<Object> get(final int col)
			{
				return new AbstractList<Object>() 
				{
					@Override
					public Object get(final int row)
					{
						return (Object) model.getValueAt(row, col);
					}

					@Override
					public int size()
					{
						return model.getRowCount();
					}
				};
			}

			@Override
			public int size()
			{
				return model.getColumnCount();
			}
		};
	}

	/**
	 * @return a view of this table as a list of rows
	 */
	public List<List<Object>> rowsView()
	{
		return new AbstractList<List<Object>>() {

			@Override
			public List<Object> get(final int row)
			{
				return new AbstractList<Object>() 
				{
					@Override
					public Object get(final int col)
					{
						return model.getValueAt(row, col);
					}

					@Override
					public int size()
					{
						return model.getColumnCount();
					}
				};
			}

			@Override
			public int size()
			{
				return model.getRowCount();
			}
		};
	}
}
