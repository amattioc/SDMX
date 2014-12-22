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

import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Attilio Mattiocco
 *
 */
public class FlowNode extends SdmxNode {
	private String providerId = null;

	public FlowNode(String flowId, String providerId, String description, boolean toBeOrdered) {
		super(flowId, description, toBeOrdered);
		this.providerId = providerId;
	}

	@Override
	public List<SdmxNode> listSubNodes() throws SdmxException {
		List<SdmxNode> result = new ArrayList<SdmxNode>();
		List<Dimension> dimensions = SdmxClientHandler.getDimensions(providerId, this.id);
		for (Iterator<Dimension> iterator = dimensions.iterator(); iterator.hasNext();) {
			Dimension dim = (Dimension) iterator.next();
			DimensionNode n = new DimensionNode(dim.getId(), providerId, this.id, dim.getCodeList().getFullIdentifier(), true);
			result.add(n);
		}
		return result;
	}

}
