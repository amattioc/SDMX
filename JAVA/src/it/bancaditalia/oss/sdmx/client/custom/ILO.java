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
package it.bancaditalia.oss.sdmx.client.custom;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.parser.v20.DataStructureParser;
import it.bancaditalia.oss.sdmx.parser.v21.RestQueryBuilder;
import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * @author Attilio Mattiocco
 *
 */
public class ILO extends RestSdmx20Client {
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public ILO() throws MalformedURLException{
		//the ILO providers supports https but it gets errors with java 1.6
		super("ILO", new URL("http://www.ilo.org/ilostat/sdmx/ws/rest"), false, "application/vnd.sdmx.structurespecificdata+xml;version=2.1", "");
	}

	@Override
	public Map<String, Dataflow> getDataflows() throws SdmxException {
		Map<String, Dataflow> result = new HashMap<String, Dataflow>();
		Map<String, String> collections = getCodes("CL_COLLECTION", "ILO", "latest");
		// this algorithm will be replaced as soon as the ILO provider supports 
		// 'ALL_MULTI' queries for dataflows
		for (Iterator<String> iterator = collections.keySet().iterator(); iterator.hasNext();) 
		{
			String coll = (String) iterator.next();
			String query = endpoint + "/datastructure" + "/ILO/" + coll + "_ALL_MULTI";
			List<DataFlowStructure> dfs = runQuery(new DataStructureParser(), query, null);
			if(dfs.size() > 0){
				for (Iterator<DataFlowStructure> iterator1 = dfs.iterator(); iterator1.hasNext();) 
				{
					DataFlowStructure dsd = (DataFlowStructure) iterator1.next();
					Dataflow tmp = new Dataflow();
					tmp.setId("DF_" + dsd.getId());
					tmp.setName(dsd.getName());
					tmp.setAgency(dsd.getAgency());
					tmp.setVersion(dsd.getVersion());					
					DSDIdentifier dsdId = new  DSDIdentifier();
					dsdId.setAgency(dsd.getAgency());
					dsdId.setId(dsd.getId());
					dsdId.setVersion(dsd.getVersion());
					tmp.setDsdIdentifier(dsdId);
					result.put(tmp.getId(), tmp);
				}
			}
			else
				throw new SdmxXmlContentException("The query returned a null stream");
		}
		
		return result;
	}
	
	protected String buildFlowQuery(String dataflow, String agency, String version) throws SdmxException{
		String query = RestQueryBuilder.getDataflowQuery(endpoint,dataflow, "ILO", version);
		return query;
	}
}
