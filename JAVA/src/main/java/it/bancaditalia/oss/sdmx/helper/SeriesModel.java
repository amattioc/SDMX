package it.bancaditalia.oss.sdmx.helper;

import java.util.Collection;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class SeriesModel extends DefaultTableModel {

	private static final long serialVersionUID = 3265022631397431923L;
	
	public SeriesModel(Collection<String> columns) {
		super();
		
		Vector<String> vector = new Vector<>(columns);
		vector.insertElementAt("Time", 0);
		vector.insertElementAt("Value", 1);
		setColumnCount(vector.size());
		setColumnIdentifiers(vector);
		setRowCount(0);
	}

	@Override
	public boolean isCellEditable(int row, int column) {  
        return false;  
    }
}
