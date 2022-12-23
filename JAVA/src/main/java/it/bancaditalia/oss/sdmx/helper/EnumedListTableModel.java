package it.bancaditalia.oss.sdmx.helper;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.swing.table.AbstractTableModel;

public class EnumedListTableModel<T> extends AbstractTableModel
{

	private static final long serialVersionUID = 3265022631397431923L;

	private Object       items[][] = new Object[0][];

	public void clear()
	{
		items = new Object[0][];
		
		fireTableDataChanged();
	}

	@SuppressWarnings("unchecked")
	public void setItems(List<T> itemMap, Function<? super T, String[]> mapper)
	{
		Object[][] items = new Object[itemMap.size()][];

		if (itemMap != null)
		{
			final Object[] array = itemMap.toArray();
			for (int i = 0; i < array.length; i++)
			{
				items[i] = new Object[] { i, null, null, array[i] };
				System.arraycopy(mapper.apply((T) array[i]), 0, items[i], 1, 2);
			}
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
	
	@SuppressWarnings("unchecked")
	public List<T> getSource()
	{
		return Arrays.stream(items)
			.map(item -> item[3])
			.map(item -> (T) item)
			.collect(toList());
	}
}
