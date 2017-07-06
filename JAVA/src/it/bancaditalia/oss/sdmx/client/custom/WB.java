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
public class WB extends DotStat{
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public WB() throws URISyntaxException {
		super("WorldBank", new URI("http://api.worldbank.org"), false, null);
	}
	
	@Override
	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException{
		if( endpoint!=null  && dsd!=null && !dsd.isEmpty()){
			try {
				return new RestQueryBuilder(endpoint).addPath("KeyFamily").addParam("id", dsd).build();
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
		
		return ((Sdmx21Queries) new Sdmx21Queries(endpoint).addPath("v2").addPath("data").addPath(dataflow.getId()).addPath(fixKey(resource))).addParams(startTime, endTime, serieskeysonly, null, false, format).buildSdmx21Query();
	}
	
	// https://github.com/amattioc/SDMX/issues/19
	private static String fixKey(String resource) {
		// the WB provider is BETA and it handles data queries in an unconventional way
		// WDI : freq.series.area  --> area.series
		String[] items = resource.split("\\.", -1);
		if (items.length != 3) {
			return resource;
		}
		StringBuilder result = new StringBuilder();
		result.append(items[2]);
		result.append(".");
		result.append(items[1]);
		return result.toString();
	}
	
}
