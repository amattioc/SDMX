package it.bancaditalia.oss.sdmx.helper;

import java.util.Collection;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class ComparatorModel extends DefaultTableModel {

	private static final long serialVersionUID = 3265022631397431923L;
	
	public ComparatorModel(Collection<String> names) {
		super();
		
		Vector<String> vector = new Vector<>();
		vector.insertElementAt("Time", 0);
		vector.addAll(names);
		setColumnCount(vector.size());
		setColumnIdentifiers(vector);
		setRowCount(0);
	}

	@Override
	public boolean isCellEditable(int row, int column) {  
        return false;  
    }
}
