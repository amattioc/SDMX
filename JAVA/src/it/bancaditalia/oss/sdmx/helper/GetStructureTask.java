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

import java.awt.Component;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

/**
 * @author Attilio Mattiocco
 *
 */
class GetStructureTask extends SwingWorker<Void, Void> {
	private static Logger logger = Configuration.getSdmxLogger();
	private ProgressViewer progress = null;
	private Component parent = null;
	
	public GetStructureTask(ProgressViewer progress, Component parent) {
		super();
		this.progress = progress;
		this.parent = parent;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			DefaultListModel dimListModel = new DefaultListModel();
			List<Dimension> dims = SdmxClientHandler.getDimensions(QueryPanel.selectedProvider, QueryPanel.selectedDataflow);
			int i=0;
			for (Iterator<Dimension> iterator = dims.iterator(); iterator.hasNext();) {
				Dimension dim = iterator.next();
				dimListModel.add(i++, dim.getId());
			}
			JList dimList = new JList(dimListModel);
			dimList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			dimList.addListSelectionListener(new DimensionSelectionListener(parent));
			QueryPanel.dimensionsPane.getViewport().add(dimList);
			String query = QueryPanel.getSDMXQuery();
			QueryPanel.sdmxQuery.setText(query);
			//initSelections(QueryPanel.selectedDataflow,dims);
		} catch (Exception ex) {
			logger.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
			logger.log(Level.FINER, "", ex);
		} finally {
			progress.setVisible(false);
		}
        return null;
	}
	
//	private void initSelections(String dataflow, List<Dimension> dims){
//		QueryPanel.selectedDataflow = dataflow;
//		QueryPanel.codeSelections = new LinkedHashMap<String, Object[]>();
//		for (Iterator<Dimension> iterator = dims.iterator(); iterator.hasNext();) {
//			Dimension d = (Dimension) iterator.next();
//			QueryPanel.codeSelections.put(d.getId(), new Object[0]);
//		}
//		QueryPanel.sdmxQuery.setText(QueryPanel.getSDMXQuery());
//	}
}
