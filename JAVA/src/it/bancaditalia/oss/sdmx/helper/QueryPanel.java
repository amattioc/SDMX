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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableRowSorter;

/**
 * @author Attilio Mattiocco
 *
 */
public class QueryPanel extends JPanel implements ActionListener{
	private static Logger logger = Configuration.getSdmxLogger();
	private static final long serialVersionUID = 1L;
	
	static JLabel queryLab = new JLabel();
	static JTextArea sdmxQuery = new JTextArea();
	static JScrollPane flowsPane = new JScrollPane();
	static JScrollPane dimensionsPane = new JScrollPane();
	static JScrollPane codesPane = new JScrollPane();
	static TableRowSorter<KeyValueTableModel> sorter = null;
	
	static JTextField flowFilter = new JTextField("");
	
	static String selectedProvider = null;
	static String selectedDataflow = null;
	static String selectedDimension = null;
	
	static LinkedHashMap<String, Object[]> codeSelections = new LinkedHashMap<String, Object[]>();


	public QueryPanel() {
		super(new BorderLayout());
		
		sdmxQuery.setLineWrap(true);
		sdmxQuery.setFont(new Font(null, Font.BOLD, 16));
		sdmxQuery.setEditable(false);
		JScrollPane queryPane = new JScrollPane(sdmxQuery);
		
		JButton btn = new JButton("Check Query");		
		btn.addActionListener(this);
				
		KeyValueTableModel m = new KeyValueTableModel("Flow ID", "Flow Description");
		JTable flowsTable = new JTable(m);
		sorter = new TableRowSorter<KeyValueTableModel>(m);
		flowsTable.setRowSorter(sorter);
		flowsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		flowsTable.getSelectionModel().addListSelectionListener(new FlowSelectionListener());
		flowsPane.getViewport().add(flowsTable);
		
		JLabel filterLab = new JLabel("Filter flows:", SwingConstants.TRAILING);
		flowFilter.getDocument().addDocumentListener(new FlowFilterListener());
		flowFilter.setFont(new Font(null, Font.BOLD, 16));
		flowFilter.setForeground(Color.RED);
		JSplitPane filterSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filterLab, flowFilter);
		filterSplit.setResizeWeight(.05d);
		filterSplit.setEnabled(false);
		
		JSplitPane flowSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, flowsPane, filterSplit);
		flowSplit.setResizeWeight(.95d);
		
		JTable codesTable = new JTable(new KeyValueTableModel("Code ID", "Code Description"));
		codesTable.setAutoCreateRowSorter(true);
		codesTable.getSelectionModel().addListSelectionListener(new CodeSelectionListener());
		codesPane.getViewport().add(codesTable);
		
		JSplitPane querySplit1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, queryLab, queryPane);
		querySplit1.setResizeWeight(.4d);
		//querySplit1.setEnabled(false);
		JSplitPane querySplit2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, querySplit1, btn);
		querySplit2.setResizeWeight(.8d);
		//querySplit2.setEnabled(false);
		
		JLabel dimLab = new JLabel("Select Dimensions");
		JSplitPane dimensionsSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dimLab, dimensionsPane);
		dimensionsSplit.setEnabled(false);

		JLabel codesLab = new JLabel("Select Codes");
		JSplitPane codesSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codesLab, codesPane);
		codesSplit.setEnabled(false);

		JSplitPane innerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dimensionsSplit, codesSplit);
		innerSplit.setResizeWeight(.2d);

		JSplitPane dataSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, flowSplit, innerSplit);
		dataSplit.setResizeWeight(.5d);
		
		JSplitPane mainQuerySplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, querySplit2, dataSplit);
		add(mainQuerySplit);
	}
	
    public void actionPerformed(ActionEvent e) {	
		try {
			List<String> result = SdmxClientHandler.getTimeSeriesNames(selectedProvider, sdmxQuery.getText());
			logger.severe("The query identified: " +  result.size() + " time series.");
			logger.severe(result.toString());
		} catch (SdmxException ex) {
			logger.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
			logger.log(Level.FINER, "", ex);
		}
    }
    
    public static void clearAllViews(){
    	clearFlows();
    	clearDimensions();
    	clearCodes();
    	sdmxQuery.setText("");
    }
    
    public static void clearFlows(){
    	JTable flowsTable = (JTable)QueryPanel.flowsPane.getViewport().getComponent(0);
    	KeyValueTableModel m = new KeyValueTableModel("Flow ID", "Flow Description");
		flowsTable.setModel(m);
		flowsTable.getSelectionModel().clearSelection();
		sorter = new TableRowSorter<KeyValueTableModel>(m);
		flowsTable.setRowSorter(sorter);
    }

    public static void clearDimensions(){
    	dimensionsPane.getViewport().add(new JList());
    }
    
    public static void clearCodes(){
    	JTable codesTable = (JTable)QueryPanel.codesPane.getViewport().getComponent(0);
    	codesTable.setModel(new KeyValueTableModel("Code ID", "Code Description"));
    	codesTable.getSelectionModel().clearSelection();
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
