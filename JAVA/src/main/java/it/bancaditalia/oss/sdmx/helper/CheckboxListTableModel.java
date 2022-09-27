package it.bancaditalia.oss.sdmx.helper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

public final class CheckboxListTableModel<T> extends AbstractTableModel
{
	private static final Logger LOGGER = Logger.getLogger(CheckboxListTableModel.class.getName());
	private static final long serialVersionUID = 1L;
	
	private static volatile int counter = 0;
	
	private final String columnIdentifiers[]; 
	private Object items[][] = new Object[0][]; 
	private final int num = counter++;

	public CheckboxListTableModel(String keyName, String valueName)
	{
		columnIdentifiers = new String[] { "", keyName, valueName };
	}

	public void setItems(Map<String, String> itemMap)
	{
		//LOGGER.info(num + " - " + new Object() {}.getClass().getEnclosingMethod().getName());
		Object[][] items = new Object[itemMap.size()][];
//		if(this.items.length == 0){
			int i = 0;
			if (itemMap != null)
				for (Entry<String, String> item : itemMap.entrySet()){
					items[i++] = new Object[] { wasSelectedItem(item.getKey()), item.getKey(), item.getValue() };
				}
//		}
//		else{
//			for (int i = 0, j = 0; i < this.items.length; i++) {
//				Object[] item = this.items[i];
//				if(itemMap.containsKey(item[1])){
//					items[j++] = new Object[] { item[0], item[1], item[2] };
//				}
//			}
//		}
		this.items = items;
		
		fireTableStructureChanged();
	}

	private Boolean wasSelectedItem(String key) {
		Boolean selected = Boolean.FALSE;
		for (int i = 0; i < this.items.length; i++) {
			if(key.equals(this.items[i][1])){
				return (Boolean) this.items[i][0];
			}
		}
		return selected;
	}

	public void setItems(List<T> itemMap, Function<? super T, String[]> mapper)
	{
		//LOGGER.info(num + " - " + new Object() {}.getClass().getEnclosingMethod().getName());
		Object[][] items = new Object[itemMap.size()][];

		int i = 0;
		if (itemMap != null)
			for (T item : itemMap)
			{
				Object row[] = new Object[] { Boolean.FALSE, null, null };
				System.arraycopy(mapper.apply(item), 0, row, 1, 2);
				items[i++] = row;
			}
		
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
	
	@Override
	public String getColumnName(int column)
	{
		return columnIdentifiers[column];
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

	public void uncheckAll()
	{
		LOGGER.info(num + " - " + new Object() {}.getClass().getEnclosingMethod().getName());

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
		LOGGER.info(num + " - " + new Object() {}.getClass().getEnclosingMethod().getName());
		
		items[rowIndex][columnIndex] = aValue;
		
		fireTableCellUpdated(rowIndex, columnIndex);
	}
}
