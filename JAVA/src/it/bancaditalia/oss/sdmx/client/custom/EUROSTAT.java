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

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Message;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.parser.v21.CompactDataParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;
import it.bancaditalia.oss.sdmx.parser.v21.RestQueryBuilder;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.DisconnectOnCloseReader;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Attilio Mattiocco
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
	public List<PortableTimeSeries> getTimeSeries(Dataflow dataflow,
							DataFlowStructure dsd, String resource, String startTime,
							String endTime, boolean serieskeysonly, String updatedAfter,
							boolean includeHistory) throws SdmxException {
		
		DataParsingResult data = null;
		InputStreamReader isr = null;
		List<PortableTimeSeries> ts = null;
		data = getData(dataflow, dsd, resource, startTime, endTime,
									serieskeysonly, updatedAfter, includeHistory);
		ts = data.getData();
		if(ts == null || ts.size() == 0){
			Message msg = data.getMessage();
			if(msg != null && msg.getCode() != null && msg.getCode().equalsIgnoreCase("413") && msg.getUrl() != null){
				isr = pollLateResponse(msg.getUrl());
				if(isr != null){
					try{
						data = CompactDataParser.parse(isr, dsd, dataflow.getId(), !serieskeysonly);
					} catch (Exception e) {
						logger.severe("Exception caught parsing results from call to provider " + name);
						logger.log(Level.FINER, "Exception: ", e);
						throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
					}
				}
				if(isr != null){
					try {
						isr.close();
					} catch (IOException e) {
						logger.severe("Exception caught closing stream.");
					}
				}
			}
		}
		return data.getData();
	}
	
	private InputStreamReader pollLateResponse(String urlStr) throws SdmxException{
		if(urlStr != null && !urlStr.isEmpty()){
			HttpURLConnection conn = null;
			try {
				URL url = new URL(urlStr);
				ZipInputStream zis = null;
				for(int i = 1; i <= retries; i++){
					logger.info("Trying late retrieval with URL: " + url + ". Attempt n: " + i);
					Thread.sleep(sleepTime);
					
					//connect to url
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					int code = conn.getResponseCode();
					if (code == 200) {
						logger.fine("Connection opened. Code: " +code);
						zis = new ZipInputStream(conn.getInputStream());
						ZipEntry e = zis.getNextEntry(); // the archie just contains one file
						if(e != null){
							return DisconnectOnCloseReader.of(zis, null, conn);
						}
					}
					conn.disconnect();
				}
			} catch (MalformedURLException e) {
				logger.warning("URL for late retrieval is not valid: " + urlStr);
			} catch (Exception e) {
				logger.severe("Exception during late retrieval. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
				logger.log(Level.FINER, "Exception: ", e);
				if (conn != null) {
					conn.disconnect();
				}
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			}
		}
		else{
			logger.warning("URL for late retrieval is not valid: " + urlStr);
		}
		return null;
	}
}
