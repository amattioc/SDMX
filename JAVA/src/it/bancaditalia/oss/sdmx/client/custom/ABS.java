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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Attilio Mattiocco
 *
 */
public class ABS extends OECD{
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public ABS() throws MalformedURLException{
		this.name = "ABS";
		this.wsEndpoint = new URL("http://stat.abs.gov.au/restsdmx/sdmx.ashx");
		this.dotStat = true;
		this.needsCredentials = false;
	}
	
	@Override
	protected String buildDSDQuery(URL endpoint, String dsd, String agency, String version){
		if( endpoint!=null  &&
				dsd!=null && !dsd.isEmpty()){

			String query = endpoint + "/GetDataStructure/" + dsd + "/ABS";
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dsd=" + dsd + " endpoint=" + endpoint);
		}
	}
	
	@Override
	protected String buildDataQuery(URL endpoint, Dataflow dataflow, String resource, String startTime, String endTime){
		String query = super.buildDataQuery(endpoint, dataflow, resource + "/ABS", null, null);
		if(startTime != null && startTime.isEmpty()) startTime = null;
		if(endTime != null && endTime.isEmpty()) endTime = null;		
		if(startTime != null || endTime != null){
			query=query+"?";
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
}
