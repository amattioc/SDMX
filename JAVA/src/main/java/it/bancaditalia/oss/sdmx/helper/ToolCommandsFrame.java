package it.bancaditalia.oss.sdmx.helper;

import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;

class ToolCommandsFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ToolCommandsFrame(String query, String provider) throws SdmxException
	{
		setSize(800, 600);
		this.setLocationRelativeTo(this.getContentPane());

		JMenuBar menuBar = new JMenuBar();
	    JMenu menu = new JMenu("Edit");
	    menuBar.add(menu);
	    JMenuItem menuItem = new JMenuItem(SDMXHelper.copyAction);
		menuItem.setText("Copy Selection");
		menuItem.setMnemonic(KeyEvent.VK_C);
		menu.add(menuItem);
		setJMenuBar(menuBar);
		
		JScrollPane commandPane = new JScrollPane();
		JTextArea text = new JTextArea();
		StringBuffer buf = new StringBuffer();
		
		if(query == null || query.isEmpty()){
			throw new SdmxInvalidParameterException("The sdmx query is not valid yet: '" + query + "'");
		}
		
		buf.append(	"R COMMAND:\n");
		buf.append(	"result = getTimeSeries('" + provider + "', '" + query + "');\n\n");
		buf.append(	"MATLAB COMMAND:\n");
		buf.append(	"result = getTimeSeries('" + provider + "', '" + query + "');\n\n");
		buf.append(	"SAS COMMAND:\n");
		buf.append(	"%gettimeseries(provider=\"" + provider + "\", tsKey=\"" + query + "\", metadata=1);\n\n");
		buf.append(	"STATA COMMAND:\n");
		buf.append(	"getTimeSeries " + provider + " " + query + " \"\" \"\" 0 0\n\n");
		buf.append(	"URL:\n");
		buf.append(	SdmxClientHandler.getDataURL(provider, query, null, null, false, null, false));
	
		text.setText(buf.toString());
		commandPane.setViewportView(text);
		getContentPane().add(commandPane);

		setTitle("Commands in statistical tools");
	    setVisible( true );
	}
}