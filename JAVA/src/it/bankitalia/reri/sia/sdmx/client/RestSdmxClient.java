/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or – as soon they
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
package it.bankitalia.reri.sia.sdmx.client;

import it.bankitalia.reri.sia.sdmx.api.DSDIdentifier;
import it.bankitalia.reri.sia.sdmx.api.DataFlowStructure;
import it.bankitalia.reri.sia.sdmx.api.Dataflow;
import it.bankitalia.reri.sia.sdmx.api.GenericSDMXClient;
import it.bankitalia.reri.sia.sdmx.api.PortableTimeSeries;
import it.bankitalia.reri.sia.sdmx.parser.v21.CodelistParser;
import it.bankitalia.reri.sia.sdmx.parser.v21.CompactDataParser;
import it.bankitalia.reri.sia.sdmx.parser.v21.DataStructureParser;
import it.bankitalia.reri.sia.sdmx.parser.v21.DataflowParser;
import it.bankitalia.reri.sia.sdmx.parser.v21.RestQueryBuilder;
import it.bankitalia.reri.sia.util.Configuration;
import it.bankitalia.reri.sia.util.SdmxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Attilio Mattiocco
 *
 */
public class RestSdmxClient implements GenericSDMXClient{
		
	protected boolean dotStat = false;
	protected URL wsEndpoint = null;
	protected String agencyID = null;
	protected String name = null;
	protected boolean needsCredentials = false;
	protected boolean containsCredentials = false;
	protected String user = null;
	protected String pw = null;
	
	private static final String sourceClass = RestSdmxClient.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public RestSdmxClient(String name, URL endpoint, String agency, boolean needsCredentials, boolean dotStat){
		this.wsEndpoint = endpoint;
		this.agencyID = agency;
		this.name = name;
		this.needsCredentials=needsCredentials;
		this.dotStat=dotStat;
	}

	protected String runQuery(String query, String acceptHeader) throws SdmxException{
		final String sourceMethod = "runQuery";
		logger.entering(sourceClass, sourceMethod);
		logger.info("Contacting web service with query: " + query);

		StringBuilder result = new StringBuilder(1000);
		try {
			URL url = new URL(query);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			
			handleHttpHeaders(conn, acceptHeader);

			int code = conn.getResponseCode();
			if (code == 200) {
				logger.fine("Connection opened. Code: " +code);
	
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
				String output;	
				while ((output = br.readLine()) != null) {
					result.append(output);
				}
				conn.disconnect();
				logger.fine("Connection closed.");
			}
			else{
				String msg = "Connection failed. HTTP error code : " + conn.getResponseCode() + " Message: " + conn.getResponseMessage();
				logger.severe(msg);
				throw new SdmxException(msg);
			}
		}
		catch (IOException e) {
			logger.severe("Exception caught calling provider " + getAgency());
			logger.log(Level.FINER, "Exception: ", e);
			throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
		}
		logger.exiting(sourceClass, sourceMethod);
		String res =  result.toString();
		return(res);
	}

	@Override
	public List<PortableTimeSeries> getTimeSeries(String dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime) throws SdmxException {
		String query=null;
		String xml = null;
		List<PortableTimeSeries> ts = null;
		query = buildDataQuery(wsEndpoint, dataflow, resource, startTime, endTime);
			
		xml = runQuery(query, "application/vnd.sdmx.structurespecificdata+xml;version=2.1");
		if(xml!=null && !xml.isEmpty()){
			logger.finest(xml);
			try {
				ts = CompactDataParser.parse(xml, dsd, dataflow);
				//ts = GenericDataParser.parse(xml);
			} catch (Exception e) {
				logger.severe("Exception caught parsing results from call to provider " + getAgency());
				logger.log(Level.FINER, "Exception: ", e);
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			}
		}
		else{
			throw new SdmxException("The query returned an empty result");
		}
			
		return ts;
	}

	@Override
	public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd) throws SdmxException {
		String query=null;
		String xml = null;
		DataFlowStructure str = null;
		if(dsd!=null){
			query = buildStructureQuery(wsEndpoint, dsd.getAgency(), dsd.getId(), dsd.getVersion());
			
			xml = runQuery(query, null);
			if(xml!=null && !xml.isEmpty()){
				logger.finest(xml);
				try {
					str = DataStructureParser.parse(xml).get(0);
				} catch (Exception e) {
					logger.severe("Exception caught parsing results from call to provider " + getAgency());
					logger.log(Level.FINER, "Exception: ", e);
					throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
				}
			}
			else{
				throw new SdmxException("The query returned an empty result");
			}
		}
		else{
			throw new SdmxException("Null dsd in input");
		}
		return str;

	}

	@Override
	public DSDIdentifier getDSDIdentifier(String dataflow, String version) throws SdmxException {
		String query=null;
		String xml = null;
		DSDIdentifier dsd = null;
		query = RestQueryBuilder.getDataflowQuery(wsEndpoint, dataflow, agencyID, version);
		xml = runQuery(query, null);
		if(xml!=null && !xml.isEmpty()){
			logger.finest(xml);
			try {
				List<Dataflow> flows = DataflowParser.parse(xml);
				if(flows.size() == 1){
					Dataflow result = flows.get(0);
					dsd = new DSDIdentifier(result.getDsd(), result.getDsdAgency(), result.getDsdVersion());
				}
				else{
					throw new SdmxException("The query returned zero dataflows");
				}
			} catch (Exception e) {
				logger.severe("Exception caught parsing results from call to provider " + getAgency());
				logger.log(Level.FINER, "Exception: ", e);
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			}

		}
		else{
			throw new SdmxException("The query returned an empty result");
		}
		return dsd;
	}

	@Override
	public Map<String, String> getDataflows() throws SdmxException {
		String query=null;
		String xml = null;
		Map<String, String> result = null;
		query = buildDFQuery(wsEndpoint, "all", agencyID, "latest");
		xml = runQuery(query, null);
		if(xml!=null && !xml.isEmpty()){
			logger.finest(xml);
			try {
				List<Dataflow> flows = DataflowParser.parse(xml);
				if(flows.size() > 0){
					result = new HashMap<String, String>();
					for (Iterator<Dataflow> iterator = flows.iterator(); iterator.hasNext();) {
						Dataflow dataflow = (Dataflow) iterator.next();
						result.put(dataflow.getId(), dataflow.getName());
					}
				}
				else{
					throw new SdmxException("The query returned zero dataflows");
				}
				
			} catch (Exception e) {
				logger.severe("Exception caught parsing results from call to provider " + getAgency());
				logger.log(Level.FINER, "Exception: ", e);
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			}
		}
		else{
			throw new SdmxException("The query returned an empty result");
		}
		return result;
	}
	
	@Override
	public Map<String,String> getCodes(String provider, String codeList) throws SdmxException {
		String query=null;
		String xml = null;
		Map<String, String> result = null;
		query = buildCodelistQuery(wsEndpoint, codeList);
		xml = runQuery(query, null);
		if(xml!=null && !xml.isEmpty()){
			logger.finest(xml);
			try {
				result = CodelistParser.parse(xml);
			} catch (Exception e) {
				logger.severe("Exception caught parsing results from call to provider " + getAgency());
				logger.log(Level.FINER, "Exception: ", e);
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			}
			
		}
		else{
			throw new SdmxException("The query returned an empty result");
		}
		return result;
	}

	public String getAgency() {
		return agencyID;
	}

	@Override
	public boolean needsCredentials() {
		return needsCredentials;
	}

	@Override
	public void setCredentials(String user, String pw) {
		this.user=user;
		this.pw=pw;
		this.needsCredentials=false;
		this.containsCredentials=true;
	}
	
	protected void handleHttpHeaders(HttpURLConnection conn, String acceptHeader){
		if(containsCredentials){
			logger.fine("Setting http authorization");		
			String auth = javax.xml.bind.DatatypeConverter.printBase64Binary((user + ":" + pw).getBytes());
			conn.setRequestProperty("Authorization", "Basic " + auth);
		}
		if(acceptHeader!=null){
			conn.setRequestProperty("Accept", acceptHeader);
		}
	}

	protected String buildDataQuery(URL endpoint, String dataflow, String resource, String startTime, String endTime){
		if( endpoint!=null && 
				dataflow!=null && !dataflow.isEmpty() &&
				resource!=null && !resource.isEmpty()){

			String query = RestQueryBuilder.getDataQuery(endpoint, dataflow, resource, startTime, endTime);
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}
	
	protected String buildStructureQuery(URL endpoint, String agency, String dsd, String version){
		if( endpoint!=null  &&
				agency!=null && !agency.isEmpty() &&
				dsd!=null && !dsd.isEmpty()){

			String query = RestQueryBuilder.getStructureQuery(endpoint, agency, dsd, version);
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: agency=" + agency + " dsd=" + dsd + " endpoint=" + endpoint);
		}
	}
	
	protected String buildDFQuery(URL endpoint, String dataflow, String agency, String version) throws SdmxException{
		String query = RestQueryBuilder.getDataflowQuery(endpoint, dataflow, agency, version);
		return query;
	}
	
	protected String buildCodelistQuery(URL endpoint, String codeList) throws SdmxException {
		String query = RestQueryBuilder.getCodelistQuery(endpoint, codeList);
		return query;
	}


}
