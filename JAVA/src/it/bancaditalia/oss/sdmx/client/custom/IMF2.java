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

import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.parser.v21.RestQueryBuilder;
import it.bancaditalia.oss.sdmx.util.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Attilio Mattiocco
 *
 */
public class IMF2 extends RestSdmx20Client{
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public IMF2() throws MalformedURLException{
		super("IMF2", new URL("http://dataservices.imf.org/REST/SDMX_XML.svc"), false, "", "compact_v2");
	}
	
	@Override
	protected String buildFlowQuery(String flow, String agency, String version) throws SdmxException{
		if( endpoint!=null){
			String query = endpoint + "/Dataflow";
//			if(flow != null && !flow.isEmpty() && !flow.equalsIgnoreCase("ALL")){
//				query += "/" + flow;				
//			}
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: endpoint=" + endpoint);
		}
	}
	
	@Override
	protected String buildDSDQuery(String dsd, String agency, String version, boolean full){
		if( endpoint!=null  && dsd!=null && !dsd.isEmpty()){
	
			String query = endpoint + "/DataStructure/" + dsd;
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dsd=" + dsd + " endpoint=" + endpoint);
		}
	}
	
	@Override
	protected String buildDataQuery(Dataflow dataflow, String resource, 
			String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException{
		if( endpoint!=null && 
				dataflow!=null &&
				resource!=null && !resource.isEmpty()){
			
			String query = endpoint + "/CompactData/" + dataflow.getDsdIdentifier().getId() + "/";
			query += resource ;
			query += RestQueryBuilder.addParams(startTime, endTime, 
					serieskeysonly, updatedAfter, includeHistory, format);
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}

}
