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
package it.bancaditalia.oss.sdmx.client;

import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.api.Message;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.parser.v21.CodelistParser;
import it.bancaditalia.oss.sdmx.parser.v21.CompactDataParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;
import it.bancaditalia.oss.sdmx.parser.v21.DataStructureParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataflowParser;
import it.bancaditalia.oss.sdmx.parser.v21.RestQueryBuilder;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.SdmxException;

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
import java.util.zip.GZIPInputStream;

/**
 * @author Attilio Mattiocco
 *
 */
public class RestSdmxClient implements GenericSDMXClient{
		
	protected boolean dotStat = false;
	protected URL endpoint = null;
	protected String name = null;
	protected boolean needsCredentials = false;
	protected boolean needsURLEncoding = false;
	protected boolean supportsCompression = false;
	protected boolean containsCredentials = false;
	protected String user = null;
	protected String pw = null;
	protected HttpURLConnection conn = null;
	protected int readTimeout = Configuration.getReadTimeout(this.getClass().getSimpleName());
	protected int connectTimeout = Configuration.getConnectTimeout(this.getClass().getSimpleName());
	
	private static final String sourceClass = RestSdmxClient.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public RestSdmxClient(String name, URL endpoint, boolean needsCredentials, boolean needsURLEncoding, boolean supportsCompression){
		this.endpoint = endpoint;
		this.name = name;
		this.needsCredentials=needsCredentials;
		this.needsURLEncoding=needsURLEncoding;
		this.supportsCompression=supportsCompression;
	}

	public void setReadTimeout(int timeout) {
		this.readTimeout = timeout;
	}

	public void setConnectTimeout(int timeout) {
		this.connectTimeout = timeout;    
	}

        @Override
	public Map<String, Dataflow> getDataflows() throws SdmxException {
		String query=null;
		InputStreamReader xmlStream = null;
		Map<String, Dataflow> result = null;
		query = buildFlowQuery(SdmxClientHandler.ALL_AGENCIES, "all", SdmxClientHandler.LATEST_VERSION);
		try {
			xmlStream = runQuery(query, null);
			if(xmlStream!=null){
				List<Dataflow> flows = DataflowParser.parse(xmlStream);
				if(flows.size() > 0){
					result = new HashMap<String, Dataflow>();
					for (Iterator<Dataflow> iterator = flows.iterator(); iterator.hasNext();) {
						Dataflow dataflow = (Dataflow) iterator.next();
						result.put(dataflow.getId(), dataflow);
					}
				}
				else{
					throw new SdmxException("The query returned zero dataflows");
				}
			}
			else{
				throw new SdmxException("The query returned a null stream");
			}
		} catch (Exception e) {
			logger.severe("Exception caught parsing results from call to provider " + name);
			logger.log(Level.FINER, "Exception: ", e);
			throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
		} finally{
			if(xmlStream != null){
				try {
					xmlStream.close();
				} catch (IOException e) {
					logger.severe("Exception caught closing stream.");
				}
			}
			if (conn != null) {
		        conn.disconnect();
		    }
		}
		return result;
	}

	@Override
	public Dataflow getDataflow(String dataflow, String agency, String version) throws SdmxException {
		String query=null;
		InputStreamReader xmlStream = null;
		Dataflow result = null;
		query = buildFlowQuery(dataflow, agency, version);
		try {
			xmlStream = runQuery(query, null);
			if(xmlStream!=null){
				List<Dataflow> flows = DataflowParser.parse(xmlStream);
				if(flows.size() >= 1){
					result = flows.get(0);
				}
				else{
					throw new SdmxException("The query returned zero dataflows");
				}
			}
			else{
				throw new SdmxException("The query returned a null stream");
			}
		} catch (Exception e) {
			logger.severe("Exception caught parsing results from call to provider " + name);
			logger.log(Level.FINER, "Exception: ", e);
			throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
		} finally{
			if(xmlStream != null){
				try {
					xmlStream.close();
				} catch (IOException e) {
					logger.severe("Exception caught closing stream.");
				}
			}
			if (conn != null) {
		        conn.disconnect();
		    }
		}
		return result;
	}

	@Override
	public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd, boolean full) throws SdmxException {
		String query=null;
		InputStreamReader xmlStream = null;
		DataFlowStructure str = null;
		if(dsd!=null){
			query = buildDSDQuery(dsd.getId(), dsd.getAgency(), dsd.getVersion(), full);
			try {
				xmlStream = runQuery(query, null);
				if(xmlStream!=null){
					str = DataStructureParser.parse(xmlStream).get(0);
				}
				else{
					throw new SdmxException("The query returned a null stream");
				}
			}
			catch (Exception e) {
				logger.severe("Exception caught parsing results from call to provider " + name);
				logger.log(Level.FINER, "Exception: ", e);
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			} finally{
				if(xmlStream != null){
					try {
						xmlStream.close();
					} catch (IOException e) {
						logger.severe("Exception caught closing stream.");
					}
				}
				if (conn != null) {
			        conn.disconnect();
			    }
			}
		}
			
		else{
			throw new SdmxException("Null dsd in input");
		}
		return str;
	
	}

	@Override
	public Map<String,String> getCodes(String codeList, String agency, String version) throws SdmxException{
		String query=null;
		InputStreamReader xmlStream = null;
		Map<String, String> result = null;
		query = buildCodelistQuery(codeList, agency, version);
		try {
			xmlStream = runQuery(query, null);
			if(xmlStream!=null){
				result = CodelistParser.parse(xmlStream);
			}
			else{
				throw new SdmxException("The query returned a null stream");
			}
		} catch (Exception e) {
			logger.severe("Exception caught parsing results from call to provider " + name);
			logger.log(Level.FINER, "Exception: ", e);
			throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
		} finally{
			if(xmlStream != null){
				try {
					xmlStream.close();
				} catch (IOException e) {
					logger.severe("Exception caught closing stream.");
				}
			}
			if (conn != null) {
		        conn.disconnect();
		    }
		}
		return result;
	}
	
	@Override
	public List<PortableTimeSeries> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
		DataParsingResult ts = getData(dataflow, dsd, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		return ts.getData();
	}

	protected DataParsingResult getData(Dataflow dataflow, DataFlowStructure dsd, String resource, 
			String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
		String query=null;
		InputStreamReader xmlStream = null;
		DataParsingResult ts = new DataParsingResult();
		query = buildDataQuery(dataflow, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		try {
			xmlStream = runQuery(query, "application/vnd.sdmx.structurespecificdata+xml;version=2.1");
			if(xmlStream!=null){
				ts = CompactDataParser.parse(xmlStream, dsd, dataflow.getId(), !serieskeysonly);
				Message msg = ts.getMessage();
				if(msg != null){
					logger.info("The sdmx call returned messages in the footer:\n " + msg.toString() );
				}
			}
			else{
				throw new SdmxException("The query returned a null stream");
			}
		} catch (SdmxException se) {
			throw se;
		} catch (Exception e) {
			logger.severe("Exception caught parsing results from call to provider " + name);
			logger.log(Level.FINER, "Exception: ", e);
			throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
		} finally{
			if(xmlStream != null){
				try {
					xmlStream.close();
				} catch (IOException e) {
					logger.severe("Exception caught closing stream.");
				}
			}
			if (conn != null) {
		        conn.disconnect();
		    }
		}
		return ts;
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
	
	@Override
	public URL getEndpoint() {
		return endpoint;
	}

	@Override
	public void setEndpoint(URL endpoint) {
		this.endpoint = endpoint;		
	}

	@Override
	public String buildDataURL(Dataflow dataflow, String resource, 
			String startTime, String endTime, 
			boolean seriesKeyOnly, String updatedAfter, boolean includeHistory) throws SdmxException {
		return buildDataQuery(dataflow, resource, startTime, endTime, seriesKeyOnly, updatedAfter, includeHistory);
	}

	protected InputStreamReader runQuery(String query, String acceptHeader) throws SdmxException{
		final String sourceMethod = "runQuery";
		logger.entering(sourceClass, sourceMethod);
		if(needsURLEncoding){
			query = query.replace("|", "%2B");
			query = query.replace("+", "%2B");
		}
		
		logger.info("Contacting web service with query: " + query);
		try {
			URL url = new URL(query);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setReadTimeout(readTimeout);
			conn.setConnectTimeout(connectTimeout);
			
			handleHttpHeaders(conn, acceptHeader);
	
			int code = conn.getResponseCode();
			String encoding = conn.getContentEncoding() == null ? "" : conn.getContentEncoding();
			
			if (code == 200) {
				logger.fine("Connection opened. Code: " +code);
				if(supportsCompression || encoding.equalsIgnoreCase("gzip")){
					return new InputStreamReader(new GZIPInputStream(conn.getInputStream()), "UTF-8");
				}
				else{
					return new InputStreamReader(conn.getInputStream(), "UTF-8");
				}
			}
			else{
				// REF: https://github.com/amattioc/sdmx-rest/blob/master/v2_1/ws/rest/docs/rest_cheat_sheet.pdf
				String msg = "Connection failed. HTTP error code : " + code + ", message: "+ conn.getResponseMessage() +"\n";
				switch (code) {
					case 304:
						msg += "SDMX meaning: No change since the timestamp supplied in the If-Modified-Since header";
						logger.severe(msg);
						throw new SdmxException(msg);
					case 400:
						msg += "SDMX meaning: There is a problem with the syntax of the query";
						logger.severe(msg);
						throw new SdmxException(msg);
					case 401:
						msg += "SDMX meaning: Credentials needed";
						logger.severe(msg);
						throw new SdmxException(msg);
					case 403:
						msg += "SDMX meaning: The syntax of the query is OK but it has no meaning";
						logger.severe(msg);
						throw new SdmxException(msg);
					case 404:
						msg += "SDMX meaning: No results matching the query.";
						logger.severe(msg);
						throw new SdmxException(msg);
					case 406:
						msg += "SDMX meaning: Not a supported format.";
						logger.severe(msg);
						throw new SdmxException(msg);
					case 413:
						msg += "SDMX meaning: Results too large.";
						logger.severe(msg);
						throw new SdmxException(msg);
					case 500:
						msg += "SDMX meaning: Error on the provider side.";
						logger.severe(msg);
						throw new SdmxException(msg);
					case 501:
						msg += "SDMX meaning: Feature not supported.";
						logger.severe(msg);
						throw new SdmxException(msg);
					case 503:
						msg += "SDMX meaning: Service temporarily unavailable. Please try again later..";
						logger.severe(msg);
						throw new SdmxException(msg);
					default:
						logger.severe(msg);
						throw new SdmxException(msg);
				}
			}
		}
		catch (IOException e) {
			logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "Exception: ", e);
			throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
		}
	}

	protected void handleHttpHeaders(HttpURLConnection conn, String acceptHeader){
		if(containsCredentials){
			logger.fine("Setting http authorization");		
			String auth = javax.xml.bind.DatatypeConverter.printBase64Binary((user + ":" + pw).getBytes());
			conn.setRequestProperty("Authorization", "Basic " + auth);
		}
		if(supportsCompression){
			conn.addRequestProperty("Accept-Encoding","gzip");
		}
		if(acceptHeader!=null){
			conn.setRequestProperty("Accept", acceptHeader);
		}
	}

	protected String buildDataQuery(Dataflow dataflow, String resource, 
			String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException{
		if( endpoint!=null && 
				dataflow!=null &&
				resource!=null && !resource.isEmpty()){

			String query = RestQueryBuilder.getDataQuery(endpoint, dataflow.getFullIdentifier(), resource, 
					startTime, endTime, serieskeysonly, updatedAfter, includeHistory, null);
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + 
					" resource=" + resource + " endpoint=" + endpoint);
		}
	}
	
	protected String buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException{
		if( endpoint!=null  &&
				agency!=null && !agency.isEmpty() &&
				dsd!=null && !dsd.isEmpty()){

			String query = RestQueryBuilder.getStructureQuery(endpoint, dsd, agency,  version, full);
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: agency=" + 
					agency + " dsd=" + dsd + " endpoint=" + endpoint);
		}
	}
	
	protected String buildFlowQuery(String dataflow, String agency, String version) throws SdmxException{
		String query = RestQueryBuilder.getDataflowQuery(endpoint,dataflow, agency, version);
		return query;
	}
	
	protected String buildCodelistQuery(String codeList, String agency, String version) throws SdmxException {
		String query = RestQueryBuilder.getCodelistQuery(endpoint, codeList, agency, version);
		return query;
	}


}
