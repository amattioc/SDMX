package it.bancaditalia.oss.sdmx.helper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class CheckboxListTableModel<T> extends DefaultTableModel
{

	private static final long serialVersionUID = 1L;

	public CheckboxListTableModel(String keyName, String valueName)
	{
		super();

		setColumnCount(3);
		setColumnIdentifiers(new String[] { "", keyName, valueName });
		setRowCount(0);
	}

	public void setItems(Map<String, String> items)
	{
		setRowCount(0);

		if (items != null && items.size() > 0)
			for (Entry<String, String> item : items.entrySet())
				addRow(new Object[] { Boolean.FALSE, item.getKey(), item.getValue() });
	}

	public void setItems(List<T> items, Mapper<T> mapper)
	{
		setRowCount(0);

		if (items != null && items.size() > 0)
			for (T item : items)
			{
				Object row[] = new Object[] { Boolean.FALSE, null, null };
				System.arraycopy(mapper.toMapEntry(item), 0, row, 1, 2);
				addRow(row);
			}
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

	@SuppressWarnings("unchecked")
	public Collection<String> getCheckedCodes()
	{
		List<String> codes = new LinkedList<>();

		for (Vector<?> row : (Vector<Vector<?>>) getDataVector())
			// 0 => checkbox column
			if ((Boolean) row.get(0))
				// 1 => key column
				codes.add((String) row.get(1));
		return codes;
	}

	public void uncheckAll()
	{
		for (int i = 0; i < getRowCount(); i++)
			setValueAt(false, i, 0);
	}

}
