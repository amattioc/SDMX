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
public class ILO extends RestSdmx20Client {
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public ILO() throws MalformedURLException{
		super("ILO", new URL("http://www.ilo.org/ilostat/sdmx/ws/rest"), false, true);
	}

//	@Override
//	public Dataflow getFlow(String dataflow, String agency, String version) throws SdmxException {
//		//quick and dirty: ILO just prepones 'DF_' to the dsd id
//		Dataflow df = new DSDIdentifier(dataflow.substring(3), null, null);
//		return dsd;
//	}

	@Override
	protected String buildDataQuery(URL endpoint, Dataflow dataflow, String resource, String startTime, String endTime){
		if( endpoint!=null && 
				dataflow!=null &&
				resource!=null && !resource.isEmpty()){

			// for ILO use the simple DF id
			String query = endpoint + "/data/" + dataflow.getFullIdentifier() + "/";
			query += resource ;
			
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
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}
	
	@Override
	protected String buildDSDQuery(URL endpoint, String dsd, String agency, String version){
		if( endpoint!=null  &&
				dsd!=null && !dsd.isEmpty()){

			String query = endpoint + "/datastructure/ILO/" + dsd;
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dsd=" + dsd + " endpoint=" + endpoint);
		}
	}

	@Override
	protected String buildFlowQuery(URL endpoint, String flow, String agency, String version) throws SdmxException{
		agency = (agency == null) ? "ALL" : agency;
		version = (version == null) ? "latest" : version;
		if( endpoint!=null  &&
				flow!=null && !flow.isEmpty()){

			String query = endpoint + "/dataflow/ILO/" + flow;
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + flow + " endpoint=" + endpoint);
		}
	}
	
}
