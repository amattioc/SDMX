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

import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultEditorKit;

/**
 * @author Attilio Mattiocco
 *
 */
public class BuildCommandContentFrame extends JFrame{
	private static final long serialVersionUID = 1L;
	
	public BuildCommandContentFrame() throws SdmxException {
		setSize(800, 600);
		this.setLocationRelativeTo(this.getContentPane());

		JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Edit");
        menuBar.add(menu);
        JMenuItem menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
		menuItem.setText("Copy Selection");
		menuItem.setMnemonic(KeyEvent.VK_C);
		menu.add(menuItem);
		setJMenuBar(menuBar);
		
		JScrollPane commandPane = new JScrollPane();
		JTextArea text = new JTextArea();
		StringBuffer buf = new StringBuffer();
		String query = QueryPanel.sdmxQuery.getText();
		if(query == null || query.isEmpty()){
			throw new SdmxException("The sdmx query is not valid yet: '" + query + "'");
		}
		// R
		buf.append(	"R COMMAND:\n");
		buf.append(	"result = getTimeSeries('" + QueryPanel.selectedProvider + "', '" + query + "');\n\n");
		buf.append(	"MATLAB COMMAND:\n");
		buf.append(	"result = getTimeSeries('" + QueryPanel.selectedProvider + "', '" + query + "');\n\n");
		buf.append(	"SAS COMMAND:\n");
		buf.append(	"%gettimeseries(provider=\"" + QueryPanel.selectedProvider + "\", tsKey=\"" + query + "\", metadata=1);\n\n");
		buf.append(	"URL:\n");
		buf.append(	SdmxClientHandler.getDataURL(QueryPanel.selectedProvider, query, null, null, false));
		
		text.setText(buf.toString());
		commandPane.getViewport().add(text);
		add(commandPane);
	}
}
