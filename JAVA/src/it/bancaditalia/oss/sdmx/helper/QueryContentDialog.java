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

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * @author Attilio Mattiocco
 *
 */
public class QueryContentDialog extends JFrame{
	private static final long serialVersionUID = 1L;
	private static Logger logger = Configuration.getSdmxLogger();
	//private DefaultListModel tsListModel = new DefaultListModel();
	
	public QueryContentDialog(List<PortableTimeSeries> tslist) {
		setSize(400, 300);
		this.setLocationRelativeTo(this.getContentPane());
        //this.setModal(true);
		JScrollPane tsPane = new JScrollPane();
		//JList tsList = new JList(tsListModel);
		//tsPane.getViewport().add(tsList);
		int nTs = tslist.size();
		if(nTs > 0 ){
			ArrayList<String> colNames = new ArrayList<String>();
			colNames.add("Time Series");
			PortableTimeSeries ts = tslist.get(0);
			List<String> dimensions = ts.getDimensions();
			String delims = "[ =]";
			for (Iterator<String> iterator = dimensions.iterator(); iterator.hasNext();) {
				String dim = (String) iterator.next();
				String[] tokens = dim.split(delims);
				String dimName = tokens[0] + " (" + tokens[1] + ")";
				colNames.add(dimName);
			}
			int ncols = colNames.size();
			if(ncols > 0){
				DefaultTableModel m = new DefaultTableModel();
				m.setColumnCount(ncols);
				m.setColumnIdentifiers(colNames.toArray());
				m.setNumRows(nTs);
				int i=0, j=1;
				for (Iterator<PortableTimeSeries> iterator = tslist.iterator(); iterator.hasNext();) {
					PortableTimeSeries item = (PortableTimeSeries) iterator.next();
					List<String> dims = item.getDimensions();
					m.setValueAt(item.getName(), i, 0);
					j = 1;
					for (Iterator<String> iterator2 = dims.iterator(); iterator2.hasNext();) {
						String dim = (String) iterator2.next();
						String[] tokens = dim.split(delims);
						String name = tokens[0];
						String code = tokens[1];
						try {
							String descr = SdmxClientHandler.getCodes(QueryPanel.selectedProvider, QueryPanel.selectedDataflow, name).get(code);
							m.setValueAt(code + " (" + descr + ")", i, j);
						} catch (SdmxException ex) {
							logger.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
							logger.log(Level.FINER, "", ex);
						}
						j++;
					}
					i++;
				}
				JTable tsTable = new JTable(m);
				tsTable.setAutoCreateRowSorter(true);
				tsPane.getViewport().add(tsTable);
				add(tsPane);
			}
		}
	}
	
//	public void addList(){
//		int i=0;
//		for (Iterator<String> iterator = tslist.iterator(); iterator.hasNext(); i++) {
//			String ts = (String) iterator.next();
//			tsListModel.add(i, ts);
//		}
//	}
	
//	public static void main(String[] args) {
//		List<PortableTimeSeries> a;
//		try {
//			a = SdmxClientHandler.getTimeSeries("ECB", "EXR.A|M.USD.EUR.SP00.A", null, null);
//			new QueryContentDialog(a);
//		} catch (SdmxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
