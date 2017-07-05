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
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;

/**
 * @author Attilio Mattiocco
 *
 */
public class IMF extends DotStat{
	
	private static String baseEndpoint = "http://sdmxws.imf.org/SDMXRest";
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public IMF() throws URISyntaxException {
		super("IMF", new URI(baseEndpoint + "/sdmx.ashx"), false);
	}

	@Override
	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException{
		if( endpoint!=null  && dsd!=null && !dsd.isEmpty()){
			RestQueryBuilder query;
			try {
				query = !dsd.contentEquals("ALL")
					? new RestQueryBuilder(endpoint)
					: new RestQueryBuilder(new URI(baseEndpoint));
			} catch (URISyntaxException ex) {
				throw new RuntimeException(ex);
			}
			try {
				return query.addPath("GetKeyFamily").addPath(dsd).build();
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

			// for IMF use the simple DF id
			RestQueryBuilder query = new RestQueryBuilder(endpoint).addPath("GetData")
				.addParam("dataflow", dataflow.getId())
				.addParam("key", resource)
				.addParam("format", format);
			
			if((startTime != null && !startTime.isEmpty()) || (endTime != null && !endTime.isEmpty())){
				if(startTime != null){
					query.addParam("startTime", startTime);
				}
				if(endTime != null){
					query.addParam("endTime", endTime);
				}
			}
			try {
				return query.build();
			} catch (MalformedURLException e) {
				throw SdmxExceptionFactory.wrap(e);
			}
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}
}
