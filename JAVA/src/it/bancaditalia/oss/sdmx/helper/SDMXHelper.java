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
	private boolean exitOnClose = true;
	private Logger logger = Configuration.getSdmxLogger();
	
	public SDMXHelper(boolean exitOnClose) {
		super("SDMX Helper Tool");
		this.exitOnClose = exitOnClose;
	}
	
	public void init(){
		setSize(800, 600);
		
		// for future use (e.g. flows filtering)
		setJMenuBar(createMenuBar());
				
		//Create a scrolled text area for the logging output 
        sdmxMessages = new JTextArea();
        sdmxMessages.setEditable(false);
        sdmxMessages.setBackground(Color.LIGHT_GRAY);
        textAreaHandler = new HelperHandler(sdmxMessages);
        JScrollPane p = new JScrollPane(sdmxMessages);
        
		// immediately populate the first level with the list of SDMX data providers
		// get providers and transfom into nodes
        ProviderNode node;
		List<Provider> providers = new ArrayList<Provider>(SDMXClientFactory.getProviders().values());		
		List<ProviderNode> providerNodes = new ArrayList<ProviderNode>();	
		for (Iterator<Provider> iterator = providers.iterator(); iterator.hasNext();) {
			Provider provider = (Provider) iterator.next();
			node = new ProviderNode(provider.getName(), provider.getDescription(), true);			
			providerNodes.add(node);
		}
		//sort by id
		Collections.sort(providerNodes, new NodeComparator());
        // now add to top node 
		DefaultMutableTreeNode n;
		// the top node is just for description
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new SdmxNode("SDMX", "List of SDMX Data Providers", true));
		for (Iterator<ProviderNode> iterator = providerNodes.iterator(); iterator.hasNext();) {
			ProviderNode pn = (ProviderNode) iterator.next();
			n = new DefaultMutableTreeNode(pn);			
			n.add(new DefaultMutableTreeNode(new SdmxNode("Calling provider, please wait......", "", false)));
			top.add(n);
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
		//add separate thread (tentative fix for issue #41)
		Thread runner = new Thread() {
			public void run() { 
				SDMXHelper h = new SDMXHelper(false);
				h.init();
			}
		};
		runner.start();
    }

	public static void main(String argv[]) {
		//add separate thread (tentative fix for issue #41)
		Thread runner = new Thread() {
			public void run() { 
				SDMXHelper h = new SDMXHelper(true);
				h.init();
			}
		};
		runner.start();
	}
}
