package it.bancaditalia.oss.sdmx.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import it.bancaditalia.oss.sdmx.api.Dataflow;

public class DataflowsModel extends DefaultTableModel {

	private static final long serialVersionUID = 3265022631397431923L;
	
	private String[] colHeaders = new String[6];
	
	public DataflowsModel() {
		super();

		setColumnCount(6);
		setRowCount(0);
	}
	
	public void setItems(Map<String, Dataflow> flows)
	{
		List<String[]> dataVector = new ArrayList<>(flows.size());
		
		if (flows != null)
			for (Dataflow flow: flows.values())
				dataVector.add(new String[] { 
						flow.getFullIdentifier().split(",")[1],  //$NON-NLS-1$
						flow.getFullIdentifier().split(",").length >= 3 ? flow.getFullIdentifier().split(",")[2] : "N/D", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						flow.getDsdIdentifier().getId(),
						flow.getDsdIdentifier().getVersion(),
						flow.getAgency(),
						flow.getDescription()
					});

		setDataVector(dataVector.toArray(new Object[0][]), colHeaders);
	}

	@Override
	public boolean isCellEditable(int row, int column) {  
        return false;  
    }
	
	public void updateHeaders(String[] newHeaders)
	{
		System.arraycopy(colHeaders, 0, newHeaders, 0, 6);
		setColumnIdentifiers(colHeaders);
	}
}
