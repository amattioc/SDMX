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

import it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler;
import it.bankitalia.reri.sia.util.SdmxException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Attilio Mattiocco
 *
 */
public class ProviderNode extends SdmxNode {

	public ProviderNode(String providerId, String description, boolean toBeOrdered) {
		super(providerId, description, toBeOrdered);
	}

	@Override
	public List<SdmxNode> listSubNodes() throws SdmxException {
		List<SdmxNode> result = new ArrayList<SdmxNode>();
		Map<String, String> flows = SdmxClientHandler.getFlows(this.id, null);
		for (Iterator<String> iterator = flows.keySet().iterator(); iterator.hasNext();) {
			String flow = (String) iterator.next();
			FlowNode n = new FlowNode(flow, this.id, flows.get(flow), false);
			result.add(n);
		}
		return result;
	}

}
