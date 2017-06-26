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

import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

/**
 * @author Attilio Mattiocco
 *
 */
public class QueryContentFrame extends JFrame{
	private static final long serialVersionUID = 1L;
	
	public QueryContentFrame(List<PortableTimeSeries> tslist) throws SdmxException {
		setSize(800, 600);
		this.setLocationRelativeTo(this.getContentPane());
		JScrollPane tsPane = new JScrollPane();
		int nRows = tslist.size();
		if(nRows > 0 ){

			Vector<String> columnNames = new Vector<String>();
			columnNames.add("Time Series");
			PortableTimeSeries ts = tslist.get(0);
			for (String dim: ts.getDimensionsMap().keySet())
				columnNames.add(dim);

			Vector<Vector<String>> table = new Vector<Vector<String>>();

			for (PortableTimeSeries series: tslist) 
			{
				Vector<String> row = new Vector<String>();
				table.add(row);
				row.add(series.getName());
				for (Entry<String, String> dim: series.getDimensionsMap().entrySet()) 
				{
					String name = dim.getKey();
					String code = dim.getValue();
					String descr = SdmxClientHandler.getCodes(QueryPanel.selectedProvider, QueryPanel.selectedDataflow, name).get(code);
					row.add(code + " (" + descr + ")");
				}
			}
			JTable tsTable = new JTable(table, columnNames);
			tsTable.setAutoCreateRowSorter(true);
			tsPane.getViewport().add(tsTable);
			add(tsPane);
		}
	}
}
