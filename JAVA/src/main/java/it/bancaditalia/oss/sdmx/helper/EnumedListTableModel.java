package it.bancaditalia.oss.sdmx.helper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.DefaultTableModel;

public class EnumedListTableModel<T> extends DefaultTableModel {

	private static final long serialVersionUID = 3265022631397431923L;
	
	public EnumedListTableModel(String keyName, String valueName) {
		super();
		
		setColumnCount(3);
		setColumnIdentifiers(new String[] { "", keyName, valueName });
		setRowCount(0);
	}
	
	public void setItems(Map<String, String> items)
	{
		setRowCount(0);
		
		int index = 1;
		if (items != null && items.size() > 0)
			for (Entry<String, String> item: items.entrySet())
				addRow(new Object[] { index++, item.getKey(), item.getValue() });
	}

	public void setItems(List<T> items, Mapper<T> mapper)
	{
		setRowCount(0);
		
		int index = 1;
		if (items != null && items.size() > 0)
			for (T item: items)
			{
				Object row[] = new Object[] { index++, null, null };
				System.arraycopy(mapper.toMapEntry(item), 0, row, 1, 2);
				addRow(row);
			}
	}

	@Override
	public boolean isCellEditable(int row, int column) {  
        return false;  
    }

	@Override
	public Class<?> getColumnClass(int column) {
		return column == 0 ? Integer.class : String.class;
	}
}
