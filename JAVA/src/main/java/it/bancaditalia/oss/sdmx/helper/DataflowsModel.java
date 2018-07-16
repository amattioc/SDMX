package it.bancaditalia.oss.sdmx.helper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.DefaultTableModel;

import it.bancaditalia.oss.sdmx.api.Dataflow;

public class DataflowsModel extends DefaultTableModel {

	private static final long serialVersionUID = 3265022631397431923L;
	
	public DataflowsModel() {
		super();
		
		setColumnCount(3);
		setColumnIdentifiers(new String[] { "Dataflow", "Version", "Description" });
		setRowCount(0);
	}
	
	public void setItems(Map<String, String> items)
	{
		setRowCount(0);
		
		if (items != null && items.size() > 0)
			for (Entry<String, String> item: items.entrySet())
				addRow(new String[] { item.getKey().split(",")[1], item.getKey().split(",")[2], item.getValue() });
	}

	public void setItems(List<Dataflow> items, Mapper<Dataflow> mapper)
	{
		setRowCount(0);
		
		if (items != null && items.size() > 0)
			for (Dataflow item: items)
				addRow(mapper.toMapEntry(item));
	}

	@Override
	public boolean isCellEditable(int row, int column) {  
        return false;  
    }
}
