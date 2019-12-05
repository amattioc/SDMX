package it.bancaditalia.oss.sdmx.helper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import it.bancaditalia.oss.sdmx.util.Utils.Function;

public class EnumedListTableModel<T> extends AbstractTableModel
{

	private static final long serialVersionUID = 3265022631397431923L;

	private final String columnIdentifiers[];
	private Object       items[][] = new Object[0][];

	public EnumedListTableModel(String keyName, String valueName)
	{
		columnIdentifiers = new String[] { "", keyName, valueName };
	}
	
	public void clear()
	{
		items = new Object[0][];
		
		fireTableDataChanged();
	}

	public void setItems(Map<String, String> itemMap)
	{
		Object[][] items = new Object[itemMap.size()][];

		int i = 0;
		if (itemMap != null)
			for (Entry<String, String> item : itemMap.entrySet())
				items[i] = new Object[] { i++, item.getKey(), item.getValue() };

		this.items = items;

		fireTableDataChanged();
	}

	public void setItems(List<T> itemMap, Function<? super T, String[]> mapper)
	{
		Object[][] items = new Object[itemMap.size()][];

		int i = 0;
		if (itemMap != null)
			for (T item : itemMap)
			{
				Object row[] = new Object[] { i, null, null };
				System.arraycopy(mapper.apply(item), 0, row, 1, 2);
				items[i++] = row;
			}

		this.items = items;
		
		fireTableDataChanged();
	}

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return false;
	}

	@Override
	public Class<?> getColumnClass(int column)
	{
		return column == 0 ? Integer.class : String.class;
	}

	@Override
	public String getColumnName(int column)
	{
		return columnIdentifiers[column];
	}

	@Override
	public int getColumnCount()
	{
		return 3;
	}

	@Override
	public int getRowCount()
	{
		return items.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		return items[rowIndex][columnIndex];
	}
}
