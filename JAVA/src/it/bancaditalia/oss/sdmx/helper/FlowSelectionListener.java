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

import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class FlowSelectionListener implements ListSelectionListener{

	private String provider = null;
	private String dataflow = null;
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public FlowSelectionListener(String provider) {
		super();
		this.provider = provider;
	}

	public void valueChanged(ListSelectionEvent e) {
		dataflow = ((String)((JList)e.getSource()).getSelectedValue()).split(":")[0];
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {	
				try {
					DefaultListModel dimListModel = new DefaultListModel();
					List<Dimension> dims = SdmxClientHandler.getDimensions(provider, dataflow);
					int i=0;
					for (Iterator<Dimension> iterator = dims.iterator(); iterator.hasNext();) {
						Dimension dim = iterator.next();
						dimListModel.add(i++, dim.getId());
					}
					JList dimList = new JList(dimListModel);
					dimList.addListSelectionListener(new DimensionSelectionListener(provider, dataflow));
					QueryPanel.dimensionsPane.getViewport().add(dimList);
					initSelections(dataflow,dims);
				} catch (SdmxException ex) {
					logger.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
					logger.log(Level.FINER, "", ex);
				}
	        }
		});
	}

	private void initSelections(String dataflow, List<Dimension> dims){
		QueryPanel.dataflow = dataflow;
		QueryPanel.codeSelections = new LinkedHashMap<String, Object[]>();
		for (Iterator<Dimension> iterator = dims.iterator(); iterator.hasNext();) {
			Dimension d = (Dimension) iterator.next();
			QueryPanel.setSelection(d.getId(), null);
		}
	}
}
