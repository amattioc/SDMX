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
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

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
import javax.swing.table.TableRowSorter;

/**
 * @author Attilio Mattiocco
 *
 */
public class QueryPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	static JLabel queryLab = new JLabel();
	static JTextArea sdmxQuery = new JTextArea();
	static JScrollPane flowsPane = new JScrollPane();
	static JScrollPane dimensionsPane = new JScrollPane();
	static JScrollPane codesPane = new JScrollPane();
	static TableRowSorter<KeyValueTableModel> sorter = null;
	static Hashtable<String, JTable> codeTables = new Hashtable<String, JTable>();
	
	static JTextField flowFilter = new JTextField("");
	
	static String selectedProvider = null;
	static String selectedDataflow = null;
	static String selectedDimension = null;
	
	public QueryPanel() {
		super(new BorderLayout());
		
		sdmxQuery.setLineWrap(true);
		sdmxQuery.setFont(new Font(null, Font.BOLD, 16));
		sdmxQuery.setEditable(false);
		JScrollPane queryPane = new JScrollPane(sdmxQuery);
		
		JButton btn = new JButton("Check Query");		
		btn.addActionListener(this);
				
		KeyValueTableModel m = new KeyValueTableModel("Flow ID", "Flow Description", null);
		JTable flowsTable = new JTable(m);
		sorter = new TableRowSorter<KeyValueTableModel>(m);
		flowsTable.setRowSorter(sorter);
		flowsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		flowsTable.getSelectionModel().addListSelectionListener(new FlowSelectionListener(this));
		flowsPane.getViewport().add(flowsTable);
		
		JLabel filterLab = new JLabel("Filter flows:");
		flowFilter.getDocument().addDocumentListener(new FlowFilterListener());
		flowFilter.setFont(new Font(null, Font.BOLD, 16));
		flowFilter.setForeground(Color.RED);
		JSplitPane filterSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filterLab, flowFilter);
		filterSplit.setResizeWeight(.05d);
		filterSplit.setEnabled(false);
		
		JSplitPane flowSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, flowsPane, filterSplit);
		flowSplit.setResizeWeight(.95d);
		
		JTable codesTable = new JTable(new KeyValueTableModel("Code ID", "Code Description", null));
		codesTable.setAutoCreateRowSorter(true);
		codesTable.getSelectionModel().addListSelectionListener(new CodeSelectionListener());
		codesPane.getViewport().add(codesTable);
		
		JSplitPane querySplit1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, queryLab, queryPane);
		querySplit1.setResizeWeight(.4d);
		JSplitPane querySplit2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, querySplit1, btn);
		querySplit2.setResizeWeight(.8d);
		
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
    	final ProgressViewer progress = new ProgressViewer(this);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            GetQueryContentTask task = new GetQueryContentTask(progress);
	        	task.execute();
	        }
	    });  
    	progress.setVisible(true);
    	progress.setAlwaysOnTop(true);

	}
    
    public static void clearAllViews(){
    	clearFlows();
    	clearDimensions();
    	clearCodes();
    	sdmxQuery.setText("");
    }
    
    public static void clearFlows(){
    	JTable flowsTable = (JTable)QueryPanel.flowsPane.getViewport().getComponent(0);
    	KeyValueTableModel m = new KeyValueTableModel("Flow ID", "Flow Description", null);
		flowsTable.setModel(m);
		flowsTable.getSelectionModel().clearSelection();
		sorter = new TableRowSorter<KeyValueTableModel>(m);
		flowsTable.setRowSorter(sorter);
    }

    public static void clearDimensions(){
    	codeTables = new Hashtable<String, JTable>();
    	dimensionsPane.getViewport().add(new JList());
    }
    
    public static void clearCodes(){
    	codesPane.getViewport().removeAll();
    	QueryPanel.codesPane.getViewport().repaint();
    }
    
	public static String getSDMXQuery() throws SdmxException{
		StringBuffer buf = new StringBuffer(QueryPanel.selectedDataflow + "/");
		List<Dimension> dims = SdmxClientHandler.getDimensions(QueryPanel.selectedProvider, QueryPanel.selectedDataflow);
		int i = 0;
		for (Iterator<Dimension> iterator = dims.iterator(); iterator.hasNext(); i++) {
			if(i != 0){
				buf.append(".");
			}
			Dimension dim = (Dimension) iterator.next();
			JTable table = QueryPanel.codeTables.get(dim.getId());
			if(table != null){
				int[] rowSelected =table.getSelectedRows();
				for (int j = 0; j < rowSelected.length; j++) {
					if(j != 0){
						buf.append("+");
					}
					int convertedIndex = table.convertRowIndexToModel(rowSelected[j]);
					String code = table.getModel().getValueAt(convertedIndex, 0).toString();
					buf.append(code);
				}
			}
		}
		return buf.toString();
	}

}
