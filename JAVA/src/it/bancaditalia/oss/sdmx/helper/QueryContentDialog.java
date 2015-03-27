/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or - as soon they
* will be approved by the European Commission - subsequent
* versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the
* Licence.
* You may obtain a copy of the Licence at:
*
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in
* writing, software distributed under the Licence is
* distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied.
* See the Licence for the specific language governing
* permissions and limitations under the Licence.
*/
package it.bancaditalia.oss.sdmx.helper;

import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 * @author Attilio Mattiocco
 *
 */
public class QueryContentDialog extends JDialog{
	private static final long serialVersionUID = 1L;
	private DefaultListModel tsListModel = new DefaultListModel();
	
	public QueryContentDialog() {
		setSize(400, 300);
		this.setLocationRelativeTo(this.getContentPane());
        this.setModal(true);
		JScrollPane tsPane = new JScrollPane();
		JList tsList = new JList(tsListModel);	
		tsPane.getViewport().add(tsList);
		add(tsPane);
	}
	
	public void addList(List<String> tslist){
		int i=0;
		for (Iterator<String> iterator = tslist.iterator(); iterator.hasNext(); i++) {
			String ts = (String) iterator.next();
			tsListModel.add(i, ts);
		}
	}
}
