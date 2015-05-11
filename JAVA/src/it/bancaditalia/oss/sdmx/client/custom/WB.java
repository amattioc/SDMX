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
import it.bancaditalia.oss.sdmx.parser.v21.RestQueryBuilder;
import it.bancaditalia.oss.sdmx.util.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Attilio Mattiocco
 *
 */
public class WB extends DotStat{
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public WB() throws MalformedURLException{
		super("WorldBank", new URL("http://api.worldbank.org"), false, null);
	}
	
	@Override
	protected String buildDSDQuery(String dsd, String agency, String version){
		if( endpoint!=null  &&
				dsd!=null && !dsd.isEmpty()){
			String query = endpoint + "/KeyFamily?id=" + dsd;
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
		String query = endpoint + "/v2/data/" + dataflow.getId() + "/" + fixKey(resource);
		query += RestQueryBuilder.addParams(startTime, endTime, serieskeysonly, null, false, format);
		return query;
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
