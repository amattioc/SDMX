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

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

class SdmxExpansionListener implements TreeExpansionListener {
	private DefaultTreeModel model = null;
	
	public SdmxExpansionListener(DefaultTreeModel model){
		this.model = model;
	}
	
	public void treeExpanded(TreeExpansionEvent event) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
		SdmxNode n = (SdmxNode)node.getUserObject();
		//perform the sdmx call
		n.expand(node);
		//and reload the tree model
		model.reload(node);
	}
	
	public void treeCollapsed(TreeExpansionEvent event) {
	}	
}