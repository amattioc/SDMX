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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.SwingWorker;

import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * @author Attilio Mattiocco
 *
 */
class GetCodesTask extends SwingWorker<Void, Void> {
	private static Logger logger = Configuration.getSdmxLogger();
	private ProgressViewer progress = null;
	
	public GetCodesTask(ProgressViewer progress) {
		super();
		this.progress = progress;
	}

	@Override
	protected Void doInBackground() throws Exception {
        try {
 			Map<String, String> codes = SdmxClientHandler.getCodes(QueryPanel.selectedProvider, QueryPanel.selectedDataflow, QueryPanel.selectedDimension);
 			JTable codesTable = new JTable(new KeyValueTableModel("Code ID", "Code Description", codes));
 			codesTable.setAutoCreateRowSorter(true);
 			codesTable.getSelectionModel().addListSelectionListener(new CodeSelectionListener());
 			QueryPanel.codesPane.getViewport().add(codesTable);
 			QueryPanel.codeTables.put(QueryPanel.selectedDimension, codesTable);
		} catch (Exception ex) {
			logger.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
			logger.log(Level.FINER, "", ex);
		} finally {
			progress.setVisible(false);
		}
        return null;
	}	
}
