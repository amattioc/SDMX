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

import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.util.Collections;
import java.util.List;
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
			if(orderLexicographically){
				Collections.sort(subNodes, new NodeComparator());
			}
			// now add to tree
			for (int i = 0; i < subNodes.size(); i++) {
				SdmxNode sdmxStuff = (SdmxNode) subNodes.get(i);
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(sdmxStuff);
				if(sdmxStuff.hasSubNodes()){
					//trick to have the empty levels always with one element
					node.add(new DefaultMutableTreeNode(new SdmxNode("Calling provider, please wait...", "", false)));
				}
				parent.add(node);
			}
		}
	}

	public boolean hasSubNodes() {
		return true;
	}

	public List<SdmxNode> listSubNodes() throws SdmxException {return null;}
}
