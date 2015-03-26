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

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class CodeSelectionListener implements ListSelectionListener{
	
	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting()){
			JTable codesTable = (JTable)QueryPanel.codesPane.getViewport().getComponent(0);
			ArrayList<String> newCodes = new ArrayList<String>();
			int[] rowSelected =codesTable.getSelectedRows();
			//if this is not a clearing
			if(rowSelected.length != 0){
				for (int i = 0; i < rowSelected.length; i++) {
					rowSelected[i] =codesTable.convertRowIndexToModel(rowSelected[i]);
					newCodes.add(codesTable.getModel().getValueAt(rowSelected[i], 0).toString());
				}
			}
			QueryPanel.setSelection(QueryPanel.selectedDimension, newCodes.toArray());
		}
	}

}
