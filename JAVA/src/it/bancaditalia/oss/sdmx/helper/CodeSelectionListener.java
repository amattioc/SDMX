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

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class CodeSelectionListener implements ListSelectionListener{
	
	private String dimension = null;
	
	public CodeSelectionListener(String dimension) {
		super();
		this.dimension = dimension;
	}

	public void valueChanged(ListSelectionEvent e) {
		Object[] codes = ((JList)e.getSource()).getSelectedValues();
		ArrayList<String> newCodes = new ArrayList<String>();
		for (int i = 0; i < codes.length; i++) {
			newCodes.add(((String)codes[i]).split(":")[0]);
		}
		QueryPanel.setSelection(dimension, newCodes.toArray());	
	}
}
