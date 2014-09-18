/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or – as soon they
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
package it.bankitalia.reri.sia.sdmx.helper;

import it.bankitalia.reri.sia.sdmx.client.SDMXClientFactory;
import it.bankitalia.reri.sia.util.Configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author Attilio Mattiocco
 *
 */
public class SDMXHelper extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTree tree;
	private DefaultTreeModel treeModel;
	private JTextArea sdmxMessages;
	private HelperHandler textAreaHandler = null;
	private Logger logger = Configuration.getSdmxLogger();
	
	public SDMXHelper() {
		super("SDMX Helper Tool");
		setSize(800, 600);
		
		// for future use (e.g. flows filtering)
		setJMenuBar(createMenuBar());
				
		//Create a scrolled text area for the logging output 
        sdmxMessages = new JTextArea();
        sdmxMessages.setEditable(false);
        sdmxMessages.setBackground(Color.LIGHT_GRAY);
        textAreaHandler = new HelperHandler(sdmxMessages);
        JScrollPane p = new JScrollPane(sdmxMessages);
        
        // the top node is just for description
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new SdmxNode("SDMX", "List of SDMX Data Providers", true));

		// immediately populate the first level with the list of SDMX data providers
		DefaultMutableTreeNode node;
		String[] providers = SDMXClientFactory.getProviders().keySet().toArray(new String[]{});
		
		for (int k = 0; k < providers.length; k++) {
			node = new DefaultMutableTreeNode(new ProviderNode(providers[k], "", true));
			node.add(new DefaultMutableTreeNode(new SdmxNode("Calling provider, please wait......", "", false)));
			top.add(node);
		}

		treeModel = new DefaultTreeModel(top);
		tree = new JTree(treeModel);
		TreeCellRenderer renderer = new DefaultTreeCellRenderer();
		tree.setCellRenderer(renderer);
		tree.addTreeExpansionListener(new SdmxExpansionListener(treeModel));
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// now design overall graphic structure, split horizontally and add a scroll  
		JScrollPane scroll = new JScrollPane();
		scroll.getViewport().add(tree);
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, p);
		splitPane.setOneTouchExpandable(true);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		logger.addHandler(textAreaHandler);
		
		WindowListener closer = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				JFrame frame = (JFrame)e.getSource();
				//to avoid crashing R
				frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				logger.removeHandler(textAreaHandler);
			}
		};
		addWindowListener(closer);
		setVisible(true);
	}
	
    private JMenuBar createMenuBar() {
        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;

        //Create the menu bar. Useless for now
        menuBar = new JMenuBar();

        menu = new JMenu("Actions");
        menuBar.add(menu);
        menuItem = new JMenuItem("Test", KeyEvent.VK_T);
		menu.add(menuItem);

        return menuBar;
    }
    
    public static void start(){
		new SDMXHelper();
    }

	public static void main(String argv[]) {
		new SDMXHelper();
	}
}
