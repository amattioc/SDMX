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

import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JTable;

public class ProviderActionListener implements ActionListener{
	
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public void actionPerformed(ActionEvent ae) {
		final String provider = ((JMenuItem)ae.getSource()).getText().split(":")[0];
		QueryPanel.clearViews();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {		
				QueryPanel.selectedProvider = provider;
				try {
					Map<String, String> flows = SdmxClientHandler.getFlows(provider , null);
					JTable flowsTable = (JTable)QueryPanel.flowsPane.getViewport().getComponent(0);
					flowsTable.setModel(new KeyValueTableModel("Code ID", "Code Description", flows));
					QueryPanel.queryLab.setText("Provider: " + QueryPanel.selectedProvider);
				} catch (SdmxException ex) {
					logger.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
					logger.log(Level.FINER, "", ex);
				}
			}
		});
    }
}
