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
import it.bancaditalia.oss.sdmx.util.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Attilio Mattiocco
 *
 */
public class IMF extends DotStat{
	
	private static String baseEndpoint = "http://sdmxws.imf.org/SDMXRest";
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public IMF() throws MalformedURLException{
		super("IMF", new URL(baseEndpoint + "/sdmx.ashx"), false);
	}

	@Override
	protected String buildDSDQuery(String dsd, String agency, String version){
		if( endpoint!=null  &&
				dsd!=null && !dsd.isEmpty()){
			String query = null;
			if(!dsd.contentEquals("ALL")){
				query = endpoint + "/GetKeyFamily/" + dsd;
			}
			else{
				query = baseEndpoint + "/GetKeyFamily/" + dsd;
			}
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dsd=" + dsd + " endpoint=" + endpoint);
		}
	}

	@Override
	protected String buildDataQuery(Dataflow dataflow, String resource, 
			String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory){
		if( endpoint!=null && 
				dataflow!=null &&
				resource!=null && !resource.isEmpty()){

			// for IMF use the simple DF id
			String query = endpoint + "/GetData?dataflow=" + dataflow.getId() + "&key=";
			query += resource ;
			
			query += "&format=" + format;
			if((startTime != null && !startTime.isEmpty()) || (endTime != null && !endTime.isEmpty())){
				if(startTime != null){
					query=query+"&startTime="+startTime;
				}
				if(endTime != null){
					query=query+"&endTime="+endTime;
				}
			}
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}
}
