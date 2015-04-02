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

import it.bancaditalia.oss.sdmx.util.Configuration;

import java.awt.Component;
import java.util.logging.Logger;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class DimensionSelectionListener implements ListSelectionListener{
	
	protected static Logger logger = Configuration.getSdmxLogger();
	private Component parent = null;
	
	public DimensionSelectionListener(Component parent) {
		super();
		this.parent = parent;
	}

	public void valueChanged(ListSelectionEvent e) {
		String dimension = (String)((JList)e.getSource()).getSelectedValue();
		QueryPanel.selectedDimension = dimension;
		if(!e.getValueIsAdjusting()){
			QueryPanel.clearCodes();
			JTable table = QueryPanel.codeTables.get(dimension);
			if(table == null){
				final ProgressViewer progress = new ProgressViewer(parent);
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
			        public void run() {
			        	GetCodesTask task = new GetCodesTask(progress);
			        	task.execute();
			        }
			    });  
		    	progress.setVisible(true);
		    	progress.setAlwaysOnTop(true);
			}
			else{
				QueryPanel.codesPane.getViewport().add(table);
			}
		}
     }
}
