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

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Message;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxResponseException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.parser.v21.CompactDataParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;
import it.bancaditalia.oss.sdmx.parser.v21.Sdmx21Queries;
import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * @author Attilio Mattiocco
 *
 */
public class EUROSTAT extends RestSdmxClient{
	private static final String EUROSTAT_PROVIDER = "https://ec.europa.eu/eurostat/SDMX/diss-web/rest";
	private int sleepTime = 6000;
	private int retries = Integer.parseInt(Configuration.getLateResponseRetries(10));
	

	public EUROSTAT() throws URISyntaxException {
		super("Eurostat", new URI(EUROSTAT_PROVIDER), false, false, false);
	}

	@Override
	protected URL buildFlowQuery(String dataflow, String agency, String version) throws SdmxException{
		return Sdmx21Queries.createDataflowQuery(endpoint,dataflow, "ESTAT", version).buildSdmx21Query();
	}
	
	@Override
	public List<PortableTimeSeries<Double>> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime,
							String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException 
	{
		DataParsingResult ts = getData(dataflow, dsd, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		if(ts == null || ts.size() == 0)
		{
			Message msg = ts.getMessage();
			
			if(isAsyncDelivery(msg))
			{
				String url = msg.getUrl();
				Parser<DataParsingResult> parser = new CompactDataParser(dsd, dataflow, !serieskeysonly);
				
				for(int i = 1; i <= retries; i++)
				{
					logger.info("Trying late retrieval with URL: " + url + ". Attempt n: " + i);
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e1) {
						// safely ignore
					}
					
					try {
						String dumpName = "data_" + dataflow.getId() + "_" + resource; //.replaceAll("\\p{Punct}", "_");
						return postProcess(runQuery(parser, new URL(msg.getUrl()), null, dumpName));
					} catch (MalformedURLException e) {
						logger.info("Late retrieval attempt " + i + " failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
					} catch (SdmxResponseException e) {
						logger.info("Late retrieval attempt " + i + " failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
					}
				}
			}
		}
		else{
			return postProcess(ts);
		}
		
		throw new SdmxXmlContentException("Late retrieval failed.");
	}
	
	// http://ec.europa.eu/eurostat/en/web/sdmx-web-services/a-few-useful-points
	private static boolean isAsyncDelivery(Message msg) {
		return msg != null && ASYNC_DELIVERY_CODE.equals(msg.getCode()) && msg.getUrl() != null;
	}
	
	private static final String ASYNC_DELIVERY_CODE = String.valueOf(HttpURLConnection.HTTP_ENTITY_TOO_LARGE);
}
