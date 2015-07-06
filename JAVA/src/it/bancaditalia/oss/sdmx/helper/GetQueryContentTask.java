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

import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

/**
 * @author Attilio Mattiocco
 *
 */
class GetQueryContentTask extends SwingWorker<Void, Void> {
	private static Logger logger = Configuration.getSdmxLogger();
	private ProgressViewer progress = null;
	
	public GetQueryContentTask(ProgressViewer progress) {
		super();
		this.progress = progress;
	}

	@Override
	protected Void doInBackground() throws Exception {
		String query = QueryPanel.sdmxQuery.getText();
		try {
			if(query == null || query.isEmpty()){
				throw new SdmxException("The sdmx query is not valid yet: '" + query + "'");
			}
			List<PortableTimeSeries> result = SdmxClientHandler.getTimeSeriesNames(QueryPanel.selectedProvider, query);
			Dataflow df = SdmxClientHandler.getFlow(QueryPanel.selectedProvider, QueryPanel.selectedDataflow);
			QueryContentFrame wnd = new QueryContentFrame(result);
			wnd.setTitle(result.size() + " results" + " - " + df.getDescription());
			progress.setVisible(false);
		    wnd.setVisible( true );
		} catch (SdmxException ex) {
			logger.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
			logger.log(Level.FINER, "", ex);
		} finally {
			progress.setVisible(false);
		}

        return null;
	}

}
