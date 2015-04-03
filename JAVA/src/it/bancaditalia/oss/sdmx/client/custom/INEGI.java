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
import it.bancaditalia.oss.sdmx.parser.v21.RestQueryBuilder;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.SdmxException;

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
		super("INEGI", new URL("http://www.snieg.mx/opendata/NSIRestService"), false);
	}
	
	@Override
	protected String buildCodelistQuery(String codeList, String agency, String version) throws SdmxException {
		if( endpoint!=null &&
				codeList!=null && !codeList.isEmpty()){
					String query = endpoint + "/Codelist/" + "ALL/" + codeList + "/ALL";
					return query;
			}
			else{
				throw new SdmxException("Invalid query parameters: codeList=" + codeList  + " endpoint=" + endpoint);
			}
	}

	@Override
	protected String buildFlowQuery(String flow, String agency, String version) throws SdmxException{
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
	protected String buildDSDQuery(String dsd, String agency, String version){
		// it seems that the only accepted version is 'ALL' in the query
		version = "ALL";
		if( endpoint!=null  && dsd!=null && !dsd.isEmpty()){
			String query = endpoint + "/DataStructure/" + agency + "/" + dsd + "/" + version;
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dsd=" + dsd + " endpoint=" + endpoint);
		}
	}

	@Override
	protected String buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, boolean serieskeysonly){
		if( endpoint!=null && 
				dataflow!=null && 
				resource!=null && !resource.isEmpty()){
	
			String query = endpoint + "/Data/ALL," + dataflow.getId() + ",ALL/" ;
			query += resource + "/" + name + "/";
			
			//query=query+"?";
			//query += "&format=compact_v2";
			query += RestQueryBuilder.addParams(startTime, endTime, serieskeysonly);
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}
}
