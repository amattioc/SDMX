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
package it.bankitalia.reri.sia.sdmx.parser.v21;

import it.bankitalia.reri.sia.util.SdmxException;

import java.net.URL;

/**
 * @author Attilio Mattiocco
 *
 */
public class RestQueryBuilder{
	
	
	public static String getDataQuery(URL endpoint, String dataflow, String resource, String start, String end){
		
		if( endpoint!=null && 
				dataflow!=null && !dataflow.isEmpty() &&
				resource!=null && !resource.isEmpty()){
			String query = endpoint + "/data/" + dataflow + "/";
			query += resource ;
			
			if(start != null && start.isEmpty()) start = null;
			if(end != null && end.isEmpty()) end = null;		
			if(start != null || end != null){
				query=query+"?";
				if(start != null){
					query=query+"startPeriod="+start;
				}
				if(start != null && end != null){
					query=query+"&";
				}
				if(end != null){
					query=query+"endPeriod="+end;
				}
			}
			//query += "?version=2.1";
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}
	
	public static String getStructureQuery(URL endpoint, String dsd, String agency, String version){
		if( endpoint!=null &&
				agency!=null && !agency.isEmpty() &&
				dsd!=null && !dsd.isEmpty()){
			String query = endpoint + "/datastructure/" + agency + "/" + dsd;
			if(version!=null && !version.isEmpty()){
				query += "/" + version;
			}
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: agency=" + agency + " dsd=" + dsd + " endpoint=" + endpoint);
		}
	}

	public static String getDataflowQuery(URL endpoint, String dataflow, String agency, String version) throws SdmxException{
		if( endpoint!=null){
			String dataflowKey = agency + "/" + dataflow + "/" + version;
			String query = endpoint + "/dataflow";
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

	public static String getCodelistQuery(URL endpoint, String codeList) throws SdmxException {
		if( endpoint!=null &&
			codeList!=null && !codeList.isEmpty()){
				String query = endpoint + "/codelist/" + codeList ;
				return query;
		}
		else{
			throw new SdmxException("Invalid query parameters: codeList=" + codeList  + " endpoint=" + endpoint);
		}
	}

}
