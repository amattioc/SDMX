package it.bancaditalia.oss.sdmx.helper;

import javax.swing.table.AbstractTableModel;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public final class CheckboxListTableModel<T> extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(CheckboxListTableModel.class.getName());
	
	private Object[][] items = new Object[0][];

	public void setItems(Map<String, String> itemMap)
	{
		Object[][] items = new Object[itemMap.size()][];
		int i = 0;
		for (Entry<String, String> item : itemMap.entrySet())
			items[i++] = new Object[] { false, item.getKey(), item.getValue() };
		
		this.items = items;
		fireTableStructureChanged();
	}

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return column == 0;
	}

	@Override
	public Class<?> getColumnClass(int column)
	{
		return column == 0 ? Boolean.class : String.class;
	}

	public Collection<String> getCheckedCodes()
	{
		List<String> codes = new LinkedList<>();

		for (int i = 0; i < items.length; i++)
			// 0 => checkbox column
			if ((Boolean) items[i][0])
				// 1 => key column
				codes.add((String) items[i][1]);
		
		return codes;
	}

	public void updateCheckedCodes(Collection<String> codes)
	{
		for (int i = 0; i < items.length; i++)
			// 1 => key column
			if (codes.contains(((String) items[i][1])))
				// 0 => checkbox column
				items[i][0] = new Boolean(true);
	}

	public int getCheckedCodesCount()
	{
		int c = 0;

		for (int i = 0; i < items.length; i++)
			// 0 => checkbox column
			if ((Boolean) items[i][0])
				c++;
		
		return c;
	}

	public void uncheckAll()
	{
		for (int i = 0; i < getRowCount(); i++)
			setValueAt(false, i, 0);
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
		try
		{
			return items[rowIndex][columnIndex];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			return columnIndex == 0 ? Boolean.FALSE : "";
		}
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		items[rowIndex][columnIndex] = aValue;
		
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	/**
	 * clear() will uncheck then remove all items from the CheckBoxListTableModel.
	 * it's intended purpose is to set the CheckBoxListTableModel Object to an empty state.
	 */
	public void clear() 
	{
		uncheckAll();
		this.items = new Object[0][];
		fireTableStructureChanged();
	}
}
