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

import java.util.Iterator;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

/**
 * @author Attilio Mattiocco
 *
 */
public class KeyValueTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 3265022631397431923L;
	
	public KeyValueTableModel(String keyName, String valueName, Map<String, String> items) {
		super();
		super.setColumnCount(2);
		super.setColumnIdentifiers(new Object[] {keyName, valueName});
		if(items != null && items.size() > 0){
			super.setNumRows(items.size());
			int row = 0;
			for (Iterator<String> iterator = items.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				String value = items.get(key);
				super.setValueAt(key, row, 0);
				super.setValueAt(value, row, 1);
				row++;
			}
		}
		else{
			super.setRowCount(0);
		}
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
