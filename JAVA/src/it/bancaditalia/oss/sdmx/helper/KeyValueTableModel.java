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
package it.bancaditalia.oss.sdmx.helper;

import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * @author Attilio Mattiocco
 *
 */
public class KeyValueTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 3265022631397431923L;
//	private Object[][] data = new Object[2][0];
	private Object[] keys = null;
	private Object[] values = null;
	private String keyName = null; 
	private String valueName = null;
	
	public KeyValueTableModel(String keyName, String valueName) {
		super();
		this.keyName = keyName;
		this.valueName = valueName;
	}
	public KeyValueTableModel(String keyName, String valueName, Map<String, String> items) {
		super();
		this.keyName = keyName;
		this.valueName = valueName;
		keys = items.keySet().toArray(); //ids
		values = items.values().toArray(); //descriptions
	}
	
	@Override
	public int getRowCount() {
		return (keys == null ? 0 : keys.length);
	}

	@Override
	public int getColumnCount() {
		return 2;
	}
	
	public String getColumnName(int col) {
		String name = "";
        if(col == 0){
        	name = keyName;
        }
        if(col == 1){
        	name = valueName;
        }
        return name;
    }

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;
//		if(rowIndex >= 0 && rowIndex <= getRowCount()){
//			result = data[columnIndex][rowIndex];
//		}
		if(rowIndex >= 0 && rowIndex <= getRowCount()){
			if(columnIndex == 0){
				result = keys[rowIndex];
			}
			else if(columnIndex == 1){
				result = values[rowIndex];
			}
		}
		return result;
	}

	

}
