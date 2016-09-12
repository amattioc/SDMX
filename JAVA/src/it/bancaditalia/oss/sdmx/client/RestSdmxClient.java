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
import it.bancaditalia.oss.sdmx.util.DisconnectOnCloseReader;
import it.bancaditalia.oss.sdmx.util.SdmxException;
import it.bancaditalia.oss.sdmx.util.SdmxResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
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
			List<Dataflow> flows = DataflowParser.parse(xmlStream);
			if(flows.size() >= 1){
				result = flows.get(0);
			}
			else{
				throw new SdmxException("The query returned zero dataflows");
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
				str = DataStructureParser.parse(xmlStream).get(0);
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
			result = CodelistParser.parse(xmlStream);
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
			ts = CompactDataParser.parse(xmlStream, dsd, dataflow.getId(), !serieskeysonly);
			Message msg = ts.getMessage();
			if(msg != null){
				logger.info("The sdmx call returned messages in the footer:\n " + msg.toString() );
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

	/**
	 * Returns a reader over the result of an http query.
	 *
	 * @param query a non-null query
	 * @param acceptHeader a nullable accept header
	 * @return a non-null reader
	 * @throws SdmxException 
	 */
	protected InputStreamReader runQuery(String query, String acceptHeader) throws SdmxException{
		final String sourceMethod = "runQuery";
		logger.entering(sourceClass, sourceMethod);
		if(needsURLEncoding){
			query = query.replace("|", "%2B");
			query = query.replace("+", "%2B");
		}

		HttpURLConnection conn = null;
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
				InputStream stream = conn.getInputStream();
				boolean gzip = supportsCompression || encoding.equalsIgnoreCase("gzip");
				return DisconnectOnCloseReader.of(gzip ? new GZIPInputStream(stream) : stream, UTF_8, conn);
			}
			else{
				SdmxResponseException ex = SdmxResponseException.of(code, conn.getResponseMessage());
				logger.severe(ex.getMessage());
				conn.disconnect();
				throw ex;
			}
		}
		catch (IOException e) {
			logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "Exception: ", e);
			if (conn != null) {
				conn.disconnect();
			}
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

	// will be replaced by StandardCharsets#UTF_8 in JDK7
	private static final Charset UTF_8 = Charset.forName("UTF-8");
}
