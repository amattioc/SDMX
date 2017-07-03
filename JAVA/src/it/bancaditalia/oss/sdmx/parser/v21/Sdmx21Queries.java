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

import java.net.URL;

import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;

/**
 * @author Attilio Mattiocco
 *
 */
public class Sdmx21Queries{
	
	
	public static RestQueryBuilder getDataQuery(URL endpoint, String dataflow, String resource, 
			String start, String end, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory, String format) throws SdmxInvalidParameterException{
		
		if( endpoint!=null && dataflow!=null && !dataflow.isEmpty() && resource!=null && !resource.isEmpty())
		{
			RestQueryBuilder result = RestQueryBuilder.of(endpoint).path("data").path(dataflow).path(resource);
			return addParams(result, start, end, serieskeysonly, updatedAfter, includeHistory, format);
		}
		else{
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}
	
	public static RestQueryBuilder getStructureQuery(URL endpoint, String dsd, 
			String agency, String version) throws SdmxException{
		return getStructureQuery(endpoint, dsd, agency, version, false);
	}

	public static RestQueryBuilder getStructureQuery(URL endpoint, String dsd, 
			String agency, String version, boolean full) throws SdmxInvalidParameterException{
		if( endpoint!=null &&
				agency!=null && !agency.isEmpty() &&
				dsd!=null && !dsd.isEmpty()){
			RestQueryBuilder query = RestQueryBuilder.of(endpoint).path("datastructure").path(agency).path(dsd);
			if(version!=null && !version.isEmpty()){
				query.path(version);
			}
			if(full){
				query.param("references", "children");
			}
			return query;
		}
		else{
			throw new SdmxInvalidParameterException("Invalid query parameters: agency=" + agency + " dsd=" + dsd + " endpoint=" + endpoint);
		}
	}

	public static RestQueryBuilder getDataflowQuery(URL endpoint, String dataflow, 
			String agency, String version) throws SdmxInvalidParameterException{
		if( endpoint!=null || dataflow != null){
			RestQueryBuilder query = RestQueryBuilder.of(endpoint).path("dataflow");
			return addResourceId(query, agency, dataflow, version);
		}
		else{
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow: " + dataflow + ", endpoint=" + endpoint);
		}
	}

	public static RestQueryBuilder getCodelistQuery(URL endpoint, String codeList, 
			String agency, String version) throws SdmxInvalidParameterException {
		if( endpoint!=null && codeList!=null && !codeList.isEmpty()){
			RestQueryBuilder query = RestQueryBuilder.of(endpoint).path("codelist");
			return addResourceId(query, agency, codeList, version);
		}
		else{
			throw new SdmxInvalidParameterException("Invalid query parameters: codeList=" + codeList  + " endpoint=" + endpoint);
		}
	}

	public static RestQueryBuilder addResourceId(RestQueryBuilder query, String agencyId, String resourceId, String version) {
		if(agencyId != null){
			query.path(agencyId);
		}
		query.path(resourceId);
		if(version != null){
			query.path(version);
		}
		return query;
	}
	
	public static RestQueryBuilder addParams(RestQueryBuilder query, String start, String end, boolean serieskeysonly, 
			String updatedAfter, boolean includeHistory, String format){
		if(start != null && !start.isEmpty()){
			query.param("startPeriod", start);
		}
		if(end != null && !end.isEmpty()){
			query.param("endPeriod", end);
		}
		if(serieskeysonly){
			query.param("detail", "serieskeysonly");
		}
		if(includeHistory){
			query.param("includeHistory", "true");
		}
		if(updatedAfter != null && !updatedAfter.isEmpty()){
			query.param("updatedAfter", updatedAfter);
		}
		if(format != null && !format.isEmpty()){
			query.param("format", format);
		}
		return query;
	}
	
}
