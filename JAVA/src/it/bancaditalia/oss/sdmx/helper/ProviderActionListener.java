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
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;

public class ProviderActionListener implements ActionListener{
	
	private String provider = null;
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public void actionPerformed(ActionEvent ae) {
		provider = ((JMenuItem)ae.getSource()).getText().split(":")[0];
		QueryPanel.clearViews();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {		
				QueryPanel.provider = provider;
				try {
					DefaultListModel flowListModel = new DefaultListModel();
					Map<String, String> flows = SdmxClientHandler.getFlows(provider , null);
					int i=0;
					for (Iterator<String> iterator = flows.keySet().iterator(); iterator.hasNext();) {
						String flow = iterator.next();
						flowListModel.add(i++, flow + ":    " + flows.get(flow));
					}
					final JList flowList = new JList(flowListModel);
					flowList.addListSelectionListener(new FlowSelectionListener(provider));
					QueryPanel.flowsPane.getViewport().add(flowList);
				} catch (SdmxException ex) {
					logger.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
					logger.log(Level.FINER, "", ex);
				}
			}
		});
    }
}
