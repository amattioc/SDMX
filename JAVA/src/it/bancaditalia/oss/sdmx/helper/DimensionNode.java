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
public class DimensionNode extends SdmxNode {
	
	private String providerId = null;
	private String flowId = null;

	public DimensionNode(String dimensionId, String providerId, String flowId, String description, boolean toBeOrdered) {
		super(dimensionId, description, toBeOrdered);
		this.providerId = providerId;
		this.flowId = flowId;
	}

	@Override
	public List<SdmxNode> listSubNodes() throws SdmxException {
		List<SdmxNode> result = new ArrayList<SdmxNode>();
		Map<String, String> codes = SdmxClientHandler.getCodes(providerId, flowId, id);
		if(codes != null){
			for (Iterator<String> it = codes.keySet().iterator(); it.hasNext();) {
				String code = (String) it.next();
				CodeNode n = new CodeNode(code, codes.get(code), true);
				result.add(n);
			}
		}
		return result;
	}

}
