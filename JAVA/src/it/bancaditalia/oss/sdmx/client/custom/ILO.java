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

/**
 * @author Attilio Mattiocco
 *
 */
public class ILO extends RestSdmx20Client {
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public ILO() throws URISyntaxException {
		//the ILO providers supports https but it gets errors with java 1.6
		super("ILO", new URI("https://www.ilo.org/ilostat/sdmx/ws/rest"), false, "application/vnd.sdmx.structurespecificdata+xml;version=2.1", "");
	}

	@Override
	protected URL buildFlowQuery(String dataflow, String agency, String version) throws SdmxException{
		return Sdmx21Queries.createDataflowQuery(endpoint,dataflow, "ILO", version).buildSdmx21Query();
	}
	
	@Override
	protected URL buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
		
		// just remove the slash 
		URL url = super.buildDataQuery(dataflow, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		try {
			url = new URL(url.toString().substring(0, url.toString().lastIndexOf("/")));
		} catch (MalformedURLException e) {
			throw SdmxExceptionFactory.wrap(e);
		}
		return url;
	}
}
