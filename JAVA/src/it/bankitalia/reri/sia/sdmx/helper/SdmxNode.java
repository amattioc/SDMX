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

import it.bankitalia.reri.sia.util.Configuration;
import it.bankitalia.reri.sia.util.SdmxException;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Attilio Mattiocco
 *
 */
public class SdmxNode {
	
	protected static Logger logger = Configuration.getSdmxLogger();
	
	protected String id;
	protected String description;
	private boolean orderLexicographically;

	public SdmxNode(String id, String description, boolean toBeOrdered) {
		this.id = id;
		this.description = description;
		this.orderLexicographically = toBeOrdered;
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public String toString() {
		return getId() + " : " + getDescription();
	}

	public void expand(DefaultMutableTreeNode parent) {
		parent.removeAllChildren(); 
		List<SdmxNode> subNodes = null;
		try {
			subNodes = listSubNodes();
		} catch (SdmxException e) {
			logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
		}
		if(subNodes != null){
			Vector<SdmxNode> v = orderNodes(subNodes);
			// now add to tree
			for (int i = 0; i < v.size(); i++) {
				SdmxNode sdmxStuff = (SdmxNode) v.elementAt(i);
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(sdmxStuff);
				if(sdmxStuff.hasSubNodes()){
					//trick to have the empty levels always with one element
					node.add(new DefaultMutableTreeNode(new SdmxNode("DUMMY", "Placeholder", false)));
				}
				parent.add(node);
			}
		}
	}

	public boolean hasSubNodes() {
		return true;
	}

	private int compareTo(SdmxNode toCompare) {
		return id.compareToIgnoreCase(toCompare.getId());
	}
	
	private Vector<SdmxNode> orderNodes(List<SdmxNode> subNodes) {
		Vector<SdmxNode> v = new Vector<SdmxNode>();
		if(!orderLexicographically){
			v.addAll(subNodes);
		}
		else{
			// Lexicographical ordering 
			for (Iterator<SdmxNode> iterator = subNodes.iterator(); iterator.hasNext();) {
				SdmxNode newNode = (SdmxNode) iterator.next();
				boolean isAdded = false;
				for (int i = 0; i < v.size(); i++) {
					SdmxNode nd = (SdmxNode) v.elementAt(i);
					if (newNode.compareTo(nd) < 0) {
						v.insertElementAt(newNode, i);
						isAdded = true;
						break;
					}
				}
				if (!isAdded)
					v.addElement(newNode);
			}
		}
		return v;
	}
	
	public List<SdmxNode> listSubNodes() throws SdmxException {return null;}
}
