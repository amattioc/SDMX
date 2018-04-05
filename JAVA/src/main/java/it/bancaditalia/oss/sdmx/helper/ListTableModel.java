package it.bancaditalia.oss.sdmx.helper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.DefaultTableModel;

public class ListTableModel<T> extends DefaultTableModel {

	private static final long serialVersionUID = 3265022631397431923L;
	
	public ListTableModel(String keyName, String valueName) {
		super();
		
		setColumnCount(2);
		setColumnIdentifiers(new String[] { keyName, valueName });
		setRowCount(0);
	}
	
	public void setItems(Map<String, String> items)
	{
		setRowCount(0);
		
		if (items != null && items.size() > 0)
			for (Entry<String, String> item: items.entrySet())
				addRow(new String[] { item.getKey(), item.getValue() });
	}

	public void setItems(List<T> items, Mapper<T> mapper)
	{
		setRowCount(0);
		
		if (items != null && items.size() > 0)
			for (T item: items)
				addRow(mapper.toMapEntry(item));
	}

	@Override
	public boolean isCellEditable(int row, int column) {  
        return false;  
    }
}
