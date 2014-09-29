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

import it.bankitalia.reri.sia.util.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Attilio Mattiocco
 *
 */
public class IMF extends OECD{
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public IMF() throws MalformedURLException{
		//super("IMF", new URL("http://sdmxws.imf.org/RestSDMX2/sdmx.ashx"), "IMF", false, true);
		this.name = "IMF";
		this.agencyID = "IMF";
		this.wsEndpoint = new URL("http://sdmxws.imf.org/RestSDMX2/sdmx.ashx");
		this.dotStat = true;
		this.needsCredentials = false;
	}

	
	protected String buildDataQuery(URL endpoint, String dataflow, String resource, String startTime, String endTime){
		if( endpoint!=null && 
				dataflow!=null && !dataflow.isEmpty() &&
				resource!=null && !resource.isEmpty()){

			String query = endpoint + "/GetData?dataflow=" + dataflow + "&key=";
			query += resource ;
			
			if(startTime != null && startTime.isEmpty()) startTime = null;
			if(endTime != null && endTime.isEmpty()) endTime = null;		
			if(startTime != null || endTime != null){
				query=query+"&";
				if(startTime != null){
					query=query+"startTime="+startTime;
				}
				if(startTime != null && endTime != null){
					query=query+"&";
				}
				if(endTime != null){
					query=query+"endTime="+endTime;
				}
			}
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}
	
	protected String buildStructureQuery(URL endpoint, String dsd){
		if( endpoint!=null  &&
				dsd!=null && !dsd.isEmpty()){

			String query = endpoint + "/GetKeyFamily/" + dsd;
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dsd=" + dsd + " endpoint=" + endpoint);
		}
	}
	
	protected String buildFlowQuery(URL endpoint, String flow){
		return(buildStructureQuery(endpoint, flow));
	}
}
