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
package it.bankitalia.reri.sia.sdmx.client.custom;

import it.bankitalia.reri.sia.sdmx.api.Dataflow;
import it.bankitalia.reri.sia.util.Configuration;
import it.bankitalia.reri.sia.util.SdmxException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Attilio Mattiocco
 *
 */
public class INEGI extends RestSdmx20Client{
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public INEGI() throws MalformedURLException{
		super("INEGI", new URL("http://www.snieg.mx/opendata/NSIRestService"), false, true);
	}
	
	@Override
	protected String buildDataQuery(URL endpoint, Dataflow dataflow, String resource, String startTime, String endTime){
		if( endpoint!=null && 
				dataflow!=null && 
				resource!=null && !resource.isEmpty()){

			String query = endpoint + "/Data/ALL," + dataflow.getId() + ",ALL/" ;
			query += resource + "/" + name + "/";
			
			if(startTime != null && startTime.isEmpty()) startTime = null;
			if(endTime != null && endTime.isEmpty()) endTime = null;		
			if(startTime != null || endTime != null){
				query=query+"?";
				if(startTime != null){
					query=query+"startPeriod="+startTime;
				}
				if(startTime != null && endTime != null){
					query=query+"&";
				}
				if(endTime != null){
					query=query+"endPeriod="+endTime;
				}
			}
			query += "&format=generic";
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}
	
	@Override
	protected String buildDSDQuery(URL endpoint, String dsd, String agency, String version){
		// it seems that the only accepted version is 'ALL' in the query
		version = "ALL";
		if( endpoint!=null  && dsd!=null && !dsd.isEmpty()){
//			String query = endpoint + "/DataStructure/" + agency + "/" + dsd + "/" + version + "?references=children";
			String query = endpoint + "/DataStructure/" + agency + "/" + dsd + "/" + version;
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dsd=" + dsd + " endpoint=" + endpoint);
		}
	}
	
	@Override
	protected String buildFlowQuery(URL endpoint, String flow, String agency, String version) throws SdmxException{
		if( endpoint!=null){
			agency = (agency == null) || agency.equals("all") ? "ALL" : agency;
			version = "ALL";
			String dataflowKey = agency + "/" + flow + "/" + version;
			String query = endpoint + "/Dataflow";
			if(dataflowKey!=null && !dataflowKey.isEmpty()){
				query += "/" + dataflowKey;
			}
			else{
				throw new SdmxException("Invalid query parameters: dataflow=" + dataflowKey);
			}
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: endpoint=" + endpoint);
		}
	}
	
	@Override
	protected String buildCodelistQuery(URL endpoint, String codeList, String agency, String version) throws SdmxException {
		if( endpoint!=null &&
				codeList!=null && !codeList.isEmpty()){
					String query = endpoint + "/Codelist/" + "ALL/" + codeList + "/ALL";
					return query;
			}
			else{
				throw new SdmxException("Invalid query parameters: codeList=" + codeList  + " endpoint=" + endpoint);
			}
	}
}
