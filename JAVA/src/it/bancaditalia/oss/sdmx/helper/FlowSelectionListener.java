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
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class FlowSelectionListener implements ListSelectionListener{

	protected static Logger logger = Configuration.getSdmxLogger();
	
	public void valueChanged(ListSelectionEvent e) {
		JTable flowsTable = (JTable)QueryPanel.flowsPane.getViewport().getComponent(0);
		
		int rowSelected =flowsTable.getSelectedRow();
		//if this is not a clearing
		if(rowSelected != -1){
			QueryPanel.clearDimensions();
			QueryPanel.clearCodes();
			QueryPanel.sdmxQuery.setText("");
			rowSelected =flowsTable.convertRowIndexToModel(rowSelected);
			final String dataflow = flowsTable.getModel().getValueAt(rowSelected, 0).toString();
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
		        public void run() {	
					try {
						DefaultListModel dimListModel = new DefaultListModel();
						List<Dimension> dims = SdmxClientHandler.getDimensions(QueryPanel.selectedProvider, dataflow);
						int i=0;
						for (Iterator<Dimension> iterator = dims.iterator(); iterator.hasNext();) {
							Dimension dim = iterator.next();
							dimListModel.add(i++, dim.getId());
						}
						JList dimList = new JList(dimListModel);
						dimList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						dimList.addListSelectionListener(new DimensionSelectionListener(QueryPanel.selectedProvider, dataflow));
						QueryPanel.dimensionsPane.getViewport().add(dimList);
						initSelections(dataflow,dims);
					} catch (SdmxException ex) {
						logger.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
						logger.log(Level.FINER, "", ex);
					}
		        }
			});
		}
	}

	private void initSelections(String dataflow, List<Dimension> dims){
		QueryPanel.selectedDataflow = dataflow;
		QueryPanel.codeSelections = new LinkedHashMap<String, Object[]>();
		for (Iterator<Dimension> iterator = dims.iterator(); iterator.hasNext();) {
			Dimension d = (Dimension) iterator.next();
			QueryPanel.setSelection(d.getId(), null);
		}
		//clear codes
		JTable codesTable = (JTable)QueryPanel.codesPane.getViewport().getComponent(0);
    	codesTable.setModel(new KeyValueTableModel("Code ID", "Code Description"));
    	codesTable.getSelectionModel().clearSelection();
	}
}
