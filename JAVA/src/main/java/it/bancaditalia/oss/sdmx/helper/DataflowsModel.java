package it.bancaditalia.oss.sdmx.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import it.bancaditalia.oss.sdmx.api.Dataflow;

public class DataflowsModel extends DefaultTableModel {

	private static final long serialVersionUID = 3265022631397431923L;
	
	public DataflowsModel() {
		super();
		
		setColumnCount(4);
		setColumnIdentifiers(new String[] { "Dataflow", "Version", "Agency", "Description" });
		setRowCount(0);
	}
	
	public void setItems(Map<String, Dataflow> flows)
	{
		List<String[]> dataVector = new ArrayList<>(flows.size());
		
		if (flows != null)
			for (Dataflow flow: flows.values())
				dataVector.add(new String[] { flow.getFullIdentifier().split(",")[1], 
						flow.getFullIdentifier().split(",").length >= 3 ? flow.getFullIdentifier().split(",")[2] : "N/D",
						flow.getAgency(),
						flow.getDescription() });

		setDataVector(dataVector.toArray(new Object[0][]), new String[] { "Dataflow", "Version", "Agency", "Description" });
	}

	@Override
	public boolean isCellEditable(int row, int column) {  
        return false;  
    }
}
