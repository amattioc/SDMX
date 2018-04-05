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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory;
import it.bancaditalia.oss.sdmx.parser.v21.Sdmx21Queries;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;

/**
 * @author Attilio Mattiocco
 *
 */
public class IMF2 extends RestSdmx20Client{
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public IMF2() throws URISyntaxException {
		super("IMF2", new URI("http://dataservices.imf.org/REST/SDMX_XML.svc"), false, "", "compact_v2");
	}
	
	@Override
	protected URL buildFlowQuery(String flow, String agency, String version) throws SdmxException{
		if( endpoint!=null){
			try {
				return new RestQueryBuilder(endpoint).addPath("Dataflow").build();
			} catch (MalformedURLException e) {
				throw SdmxExceptionFactory.wrap(e);
			}
//			if(flow != null && !flow.isEmpty() && !flow.equalsIgnoreCase("ALL")){
//				query += "/" + flow;				
//			}
		}
		else{
			throw new RuntimeException("Invalid query parameters: endpoint=" + endpoint);
		}
	}
	
	@Override
	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException{
		if( endpoint!=null  && dsd!=null && !dsd.isEmpty()){
	
			try {
				return new RestQueryBuilder(endpoint).addPath("DataStructure").addPath(dsd).build();
			} catch (MalformedURLException e) {
				throw SdmxExceptionFactory.wrap(e);
			}
		}
		else{
			throw new RuntimeException("Invalid query parameters: dsd=" + dsd + " endpoint=" + endpoint);
		}
	}
	
	@Override
	protected URL buildDataQuery(Dataflow dataflow, String resource, 
			String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException{
		if( endpoint!=null && 
				dataflow!=null &&
				resource!=null && !resource.isEmpty()){
			
			return ((Sdmx21Queries) new Sdmx21Queries(endpoint).addPath("CompactData").addPath(dataflow.getDsdIdentifier().getId()).addPath(resource)).addParams(startTime, endTime,
					serieskeysonly, updatedAfter, includeHistory, format).buildSdmx21Query();
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}

}
