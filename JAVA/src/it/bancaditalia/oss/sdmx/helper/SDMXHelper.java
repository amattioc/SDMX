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

import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.SDMXClientFactory;
import it.bancaditalia.oss.sdmx.util.Configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

/**
 * @author Attilio Mattiocco
 *
 */
public class SDMXHelper extends JFrame{
	private static final long serialVersionUID = 1L;
	private static Logger logger = Configuration.getSdmxLogger();
	private boolean exitOnClose = true;
	static QueryPanel query = new QueryPanel();
	private JTextArea sdmxMessages;
	private HelperHandler textAreaHandler = null;
		
	public SDMXHelper() {
		super("SDMX Helper Tool");
	}

	public SDMXHelper(boolean exitOnClose) {
		super("SDMX Helper Tool");
		this.exitOnClose = exitOnClose;
	}
	
	public void init(){
		try {
			 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			logger.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
			logger.log(Level.FINER, "", ex);
		}

		setSize(800, 600);
		
		setJMenuBar(createMenus());
				
		//Create a scrolled text area for the logging output 
        sdmxMessages = new JTextArea();
        sdmxMessages.setEditable(false);
        sdmxMessages.setBackground(Color.LIGHT_GRAY);
        textAreaHandler = new HelperHandler(sdmxMessages);
		logger.addHandler(textAreaHandler);
		JScrollPane msg = new JScrollPane(sdmxMessages);
		msg.setAutoscrolls(true);
        
		// now design overall graphic structure  
		JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, query, msg);
		mainSplit.setResizeWeight(.5d);
		getContentPane().add(mainSplit, BorderLayout.CENTER);
		
		// manage window close ops
		WindowListener closer = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				JFrame frame = (JFrame)e.getSource();
				if(exitOnClose){
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
				else{
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
				logger.removeHandler(textAreaHandler);
			}
		};
		addWindowListener(closer);
		setVisible(true);
	}
	
    private JMenuBar createMenus() {
        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;

        menuBar = new JMenuBar();
        menu = new JMenu("Providers");
        menuBar.add(menu);
 		List<Provider> providers = new ArrayList<Provider>(SDMXClientFactory.getProviders().values());		
		Collections.sort(providers, new ProviderComparator());
		for (Iterator<Provider> iterator = providers.iterator(); iterator.hasNext();) {
			Provider p = iterator.next();
			String provider = p.getName() + ": " + p.getDescription();
			menuItem = new JMenuItem(provider);
			menu.add(menuItem);
			menuItem.addActionListener(new ProviderActionListener(this));
		}
		
		menu = new JMenu("Actions");
		menuBar.add(menu);
		menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
		menuItem.setText("Copy Selection");
		menuItem.setMnemonic(KeyEvent.VK_C);
		menu.add(menuItem);
		menuItem = new JMenuItem("Build commands");
		menuItem.addActionListener(new BuildCommandActionListener(this));
		menu.add(menuItem);

		menu = new JMenu("Help");
		menuBar.add(menu);
		menuItem = new JMenuItem("About SDMX Connectors...");
		menuItem.addActionListener(new AboutActionListener(this));
		menu.add(menuItem);

        return menuBar;
    }
    
    public static void start(){
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	    		SDMXHelper h = new SDMXHelper(false);
	    		h.init();
	        }
	    });
    }

	public static void main(String argv[]) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
				SDMXHelper h = new SDMXHelper(true);
				h.init();
	        }
	    });
	}
}
