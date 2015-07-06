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
package it.bancaditalia.oss.sdmx.parser.v21;

import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.net.URL;

/**
 * @author Attilio Mattiocco
 *
 */
public class RestQueryBuilder{
	
	
	public static String getDataQuery(URL endpoint, String dataflow, String resource, 
			String start, String end, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory, String format) throws SdmxException{
		
		if( endpoint!=null && 
				dataflow!=null && !dataflow.isEmpty() &&
				resource!=null && !resource.isEmpty()){
			String query = endpoint + "/data/" + dataflow + "/";
			query += resource ;
			query += addParams(start, end, serieskeysonly, updatedAfter, includeHistory, format);
			return query;
		}
		else{
			throw new SdmxException("Invalid query parameters: dataflow=" + 
					dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}
	
	public static String getStructureQuery(URL endpoint, String dsd, 
			String agency, String version) throws SdmxException{
		return getStructureQuery(endpoint, dsd, agency, version, false);
	}

	public static String getStructureQuery(URL endpoint, String dsd, 
			String agency, String version, boolean full) throws SdmxException{
		if( endpoint!=null &&
				agency!=null && !agency.isEmpty() &&
				dsd!=null && !dsd.isEmpty()){
			String query = endpoint + "/datastructure/" + agency + "/" + dsd;
			if(version!=null && !version.isEmpty()){
				query += "/" + version;
			}
			if(full){
				query += "?references=children";
			}
			return query;
		}
		else{
			throw new SdmxException("Invalid query parameters: agency=" + 
					agency + " dsd=" + dsd + " endpoint=" + endpoint);
		}
	}

	public static String getDataflowQuery(URL endpoint, String dataflow, 
			String agency, String version) throws SdmxException{
		if( endpoint!=null || dataflow != null){
			String dataflowKey = dataflow;
			if(agency != null){
				dataflowKey = agency + "/" + dataflowKey;
			}
			if(version != null){
				dataflowKey = dataflowKey + "/" + version;
			}
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
			throw new SdmxException("Invalid query parameters: dataflow: " + 
					dataflow + ", endpoint=" + endpoint);
		}
	}

	public static String getCodelistQuery(URL endpoint, String codeList, 
			String agency, String version) throws SdmxException {
		if( endpoint!=null &&
			codeList!=null && !codeList.isEmpty()){
				String codelistKey = codeList; 
				if(agency != null){
					codelistKey = agency + "/" + codelistKey;
				}
				if(version != null){
					codelistKey = codelistKey + "/" + version;
				}
				String query = endpoint + "/codelist/" + codelistKey ;
				return query;
		}
		else{
			throw new SdmxException("Invalid query parameters: codeList=" + 
					codeList  + " endpoint=" + endpoint);
		}
	}
	
	public static String addParams(String start, String end, boolean serieskeysonly, 
			String updatedAfter, boolean includeHistory, String format){
		String query = "";
		boolean first = true;
		if(start != null && !start.isEmpty()){
			query = query + (first ? "?" : "&") + "startPeriod="+start;
			first = false;
		}
		if(end != null && !end.isEmpty()){
			query = query + (first ? "?" : "&") + "endPeriod="+end;
			first = false;
		}
		if(serieskeysonly){
			query = query + (first ? "?" : "&") + "detail=serieskeysonly";
			first = false;
		}
		if(includeHistory){
			query = query + (first ? "?" : "&") + "includeHistory=true";
			first = false;
		}
		if(updatedAfter != null && !updatedAfter.isEmpty()){
			query = query + (first ? "?" : "&") + "updatedAfter=" + updatedAfter;
			first = false;
		}
		if(format != null && !format.isEmpty()){
			query = query + (first ? "?" : "&") + "format=" + format;
			first = false;
		}
		return query;
	}
	
}
