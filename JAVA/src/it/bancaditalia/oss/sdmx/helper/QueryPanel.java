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

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

/**
 * @author Attilio Mattiocco
 *
 */
public class QueryPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	static JTextArea sdmxQuery = new JTextArea();
	static JScrollPane flowsPane = new JScrollPane();
	static JScrollPane dimensionsPane = new JScrollPane();
	static JScrollPane codesPane = new JScrollPane();
	
	static String selectedProvider = null;
	static String selectedDataflow = null;
	static String selectedDimension = null;
	static LinkedHashMap<String, Object[]> codeSelections = new LinkedHashMap<String, Object[]>();


	public QueryPanel() {
		super(new BorderLayout());
		
		JLabel queryLab = new JLabel("Your query:");
		sdmxQuery.setLineWrap(true);
		sdmxQuery.setFont(new Font(null, Font.BOLD, 16));
		sdmxQuery.setEditable(false);
		JScrollPane queryPane = new JScrollPane(sdmxQuery);
		
		JTable flowsTable = new JTable(new KeyValueTableModel("Flow ID", "Flow Description"));
		flowsTable.setAutoCreateRowSorter(true);
		flowsTable.getSelectionModel().addListSelectionListener(new FlowSelectionListener());
		flowsPane.getViewport().add(flowsTable);
		
		JTable codesTable = new JTable(new KeyValueTableModel("Code ID", "Code Description"));
		codesTable.setAutoCreateRowSorter(true);
		codesTable.getSelectionModel().addListSelectionListener(new CodeSelectionListener());
		codesPane.getViewport().add(codesTable);
		
		JSplitPane querySplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, queryLab, queryPane);
		querySplit.setResizeWeight(.2d);
		querySplit.setEnabled(false);
		
		JLabel dimLab = new JLabel("Select Dimensions");
		JSplitPane dimensionsSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dimLab, dimensionsPane);
		dimensionsSplit.setEnabled(false);

		JLabel codesLab = new JLabel("Select Codes");
		JSplitPane codesSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codesLab, codesPane);
		codesSplit.setEnabled(false);

		JSplitPane innerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dimensionsSplit, codesSplit);
		innerSplit.setResizeWeight(.2d);

		JSplitPane dataSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, flowsPane, innerSplit);
		dataSplit.setResizeWeight(.5d);
		
		JSplitPane mainQuerySplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, querySplit, dataSplit);
		add(mainQuerySplit);
	}
	
    public static void clearViews(){
    	JTable flowsTable = (JTable)QueryPanel.flowsPane.getViewport().getComponent(0);
		flowsTable.setModel(new KeyValueTableModel("Flow ID", "Flow Description"));
		flowsTable.getSelectionModel().clearSelection();
    	dimensionsPane.getViewport().add(new JList());
    	JTable codesTable = (JTable)QueryPanel.codesPane.getViewport().getComponent(0);
    	codesTable.setModel(new KeyValueTableModel("Code ID", "Code Description"));
    	codesTable.getSelectionModel().clearSelection();
    	sdmxQuery.setText("");
    }
    
	private static String getSDMXQuery(){
		StringBuffer buf = new StringBuffer(selectedDataflow + "/");
		Set<String> dimensions = codeSelections.keySet();
		int i = 0;
		for (Iterator<String> iterator = dimensions.iterator(); iterator.hasNext(); i++) {
			if(i != 0)
				buf.append(".");
			String dim = iterator.next();
			Object[] codes = (Object[]) codeSelections.get(dim);
			if(codes != null){
				for (int j = 0; j < codes.length; j++) {
					if(j != 0)
						buf.append("+");
					buf.append((String)codes[j]);
				}
			}
		}
		return buf.toString();
	}
	
    public static void setSelection(String dimension, Object[] codes){
    	codeSelections.put(dimension, codes);
    	QueryPanel.sdmxQuery.setText(getSDMXQuery());
    }


}
