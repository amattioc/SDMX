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
import it.bancaditalia.oss.sdmx.parser.v21.RestQueryBuilder;
import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * @author Attilio Mattiocco
 *
 */
/**
 * @author m027907
 *
 */
public class EUROSTAT extends RestSdmxClient{
	private static final String EUROSTAT_PROVIDER = "http://ec.europa.eu/eurostat/SDMX/diss-web/rest";
	private int sleepTime = 6000;
	private int retries = Integer.parseInt(Configuration.getLateResponseRetries(10));
	

	public EUROSTAT() throws MalformedURLException{
		super("Eurostat", new URL(EUROSTAT_PROVIDER), false, false, false);
	}

	@Override
	protected String buildFlowQuery(String dataflow, String agency, String version) throws SdmxException{
		String query = RestQueryBuilder.getDataflowQuery(endpoint,dataflow, "ESTAT", version);
		return query;
	}
	
	@Override
	public List<PortableTimeSeries> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime,
							String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException 
	{
		DataParsingResult data = getData(dataflow, dsd, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		List<PortableTimeSeries> ts = data.getData();
		if(ts == null || ts.size() == 0)
		{
			Message msg = data.getMessage();
			
			if(msg != null && msg.getCode() != null && msg.getCode().equalsIgnoreCase("413") && msg.getUrl() != null)
			{
				String url = msg.getUrl();
				Parser<DataParsingResult> parser = new CompactDataParser(dsd, dataflow.getId(), !serieskeysonly);
				
				for(int i = 1; i <= retries; i++)
				{
					logger.info("Trying late retrieval with URL: " + url + ". Attempt n: " + i);
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e1) {
						// safely ignore
					}
					
					try {
						return runQuery(parser, msg.getUrl(), null).getData();
					} catch (SdmxResponseException e) {
						logger.info("Late retrieval attempt " + i + " failed with exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
					}
				}
			}
		}
		else{
			return ts;
		}
		
		throw new SdmxXmlContentException("Late retrieval failed.");
	}
}
