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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.stream.XMLStreamException;

import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.api.Message;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory;
import it.bancaditalia.oss.sdmx.exceptions.SdmxIOException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.parser.v21.CodelistParser;
import it.bancaditalia.oss.sdmx.parser.v21.CompactDataParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;
import it.bancaditalia.oss.sdmx.parser.v21.DataStructureParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataflowParser;
import it.bancaditalia.oss.sdmx.parser.v21.RestQueryBuilder;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LanguagePriorityList;
import it.bancaditalia.oss.sdmx.util.SdmxProxySelector;

/**
 * @author Attilio Mattiocco
 *
 */
public class RestSdmxClient implements GenericSDMXClient{

	// TODO: To be replaced by StandardCharsets#UTF_8 in Java 7
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	protected final String name;
	protected final boolean needsURLEncoding;
	protected final boolean supportsCompression;
	protected final SSLSocketFactory sslSocketFactory;

	protected /* final */ URL endpoint;
	protected boolean dotStat = false;
	protected boolean needsCredentials = false;
	protected boolean containsCredentials = false;
	protected String user = null;
	protected String pw = null;
	protected int readTimeout = Configuration.getReadTimeout(this.getClass().getSimpleName());
	protected int connectTimeout = Configuration.getConnectTimeout(this.getClass().getSimpleName());
	protected LanguagePriorityList languages = LanguagePriorityList.parse(Configuration.getLang());
	
	private static final String sourceClass = RestSdmxClient.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public RestSdmxClient(String name, URL endpoint, SSLSocketFactory sslSocketFactory, boolean needsCredentials, boolean needsURLEncoding, boolean supportsCompression)
	{
		this.endpoint = endpoint;
		this.name = name;
		this.needsCredentials = needsCredentials;
		this.needsURLEncoding = needsURLEncoding;
		this.supportsCompression = supportsCompression;
		this.sslSocketFactory = sslSocketFactory;
	}

	public RestSdmxClient(String name, URL endpoint, boolean needsCredentials, boolean needsURLEncoding, boolean supportsCompression)
	{
		this.endpoint = endpoint;
		this.name = name;
		this.needsCredentials = needsCredentials;
		this.needsURLEncoding = needsURLEncoding;
		this.supportsCompression = supportsCompression;
		this.sslSocketFactory = null;
	}

	public void setReadTimeout(int timeout) {
		this.readTimeout = timeout;
	}

	public void setConnectTimeout(int timeout) {
		this.connectTimeout = timeout;    
	}

	public void setLanguages(LanguagePriorityList languages) {
		this.languages = languages;
	}

        @Override
	public Map<String, Dataflow> getDataflows() throws SdmxException {
		String query=null;
		Map<String, Dataflow> result = null;
		query = buildFlowQuery(SdmxClientHandler.ALL_AGENCIES, "all", SdmxClientHandler.LATEST_VERSION);
		List<Dataflow> flows = runQuery(new DataflowParser(), query, null);
		if(flows.size() > 0){
			result = new HashMap<String, Dataflow>();
			for (Iterator<Dataflow> iterator = flows.iterator(); iterator.hasNext();) {
				Dataflow dataflow = (Dataflow) iterator.next();
				result.put(dataflow.getId(), dataflow);
			}
		}
		else{
			throw new SdmxXmlContentException("The query returned zero dataflows");
		}
		return result;
	}

	@Override
	public Dataflow getDataflow(String dataflow, String agency, String version) throws SdmxException {
		String query=null;
		Dataflow result = null;
		query = buildFlowQuery(dataflow, agency, version);
		List<Dataflow> flows = runQuery(new DataflowParser(), query, null);
		if(flows.size() >= 1)
			result = flows.get(0);
		else
			throw new SdmxXmlContentException("The query returned zero dataflows");
		
		return result;
	}

	@Override
	public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd, boolean full) throws SdmxException {
		if (dsd == null)
			throw new SdmxInvalidParameterException("getDataFlowStructure(): Null dsd in input");
		else
		{
			String query = buildDSDQuery(dsd.getId(), dsd.getAgency(), dsd.getVersion(), full);
			return runQuery(new DataStructureParser(), query, null).get(0);
		}
	}

	@Override
	public Map<String,String> getCodes(String codeList, String agency, String version) throws SdmxException
	{
		String query = buildCodelistQuery(codeList, agency, version);
		return runQuery(new CodelistParser(), query, null);
	}
	
	@Override
	public List<PortableTimeSeries> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
		return getData(dataflow, dsd, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory).getData();
	}

	protected DataParsingResult getData(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException 
	{
		String query = buildDataQuery(dataflow, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		DataParsingResult ts = runQuery(new CompactDataParser(dsd, dataflow.getId(), !serieskeysonly), query, "application/vnd.sdmx.structurespecificdata+xml;version=2.1");
		Message msg = ts.getMessage();
		if(msg != null)
			logger.info("The sdmx call returned messages in the footer:\n " + msg.toString() );
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
	 * 
	 * @throws SdmxException 
	 */
	protected final <T> T runQuery(Parser<T> parser, String query, String acceptHeader) throws SdmxException
	{
		final String sourceMethod = "runQuery";
		logger.entering(sourceClass, sourceMethod);
		if(needsURLEncoding){
			query = query.replace("|", "%2B");
			query = query.replace("+", "%2B");
		}

		URLConnection conn = null;
		URL url = null;
		logger.info("Contacting web service with query: " + query);
		
		// TODO: implement in Java 7 with try-with-resource
		try {
			int code;
			url = new URL(query);
			
			do{
				conn = url.openConnection();
			
				if (conn instanceof HttpsURLConnection && sslSocketFactory != null)
				{
					logger.fine("Using custom SSLSocketFactory for provider " + name);
					((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
				}
			
				conn.setReadTimeout(readTimeout);
				conn.setConnectTimeout(connectTimeout);

				if (conn instanceof HttpURLConnection)
				{
					((HttpURLConnection) conn).setRequestMethod("GET");
					handleHttpHeaders((HttpURLConnection) conn, acceptHeader);
				}

				code = conn instanceof HttpURLConnection ? ((HttpURLConnection) conn).getResponseCode() : HttpURLConnection.HTTP_OK;
			
				if (isRedirection(code)) 
				{
					Proxy proxy = ProxySelector.getDefault().select(url.toURI()).get(0);
					String newquery = conn.getHeaderField("Location");
					if(newquery != null && !newquery.isEmpty()){
						url = new URL(newquery);
						if (ProxySelector.getDefault() instanceof SdmxProxySelector)
							((SdmxProxySelector)ProxySelector.getDefault()).addUrlHostToProxy(url, proxy);
						logger.info("Redirecting to: " + url.toString());
						if (conn instanceof HttpURLConnection)
							((HttpURLConnection) conn).disconnect();
					}
					else{
						throw new SdmxIOException("The endpoint returned redirect code: " + code + ", but the location was empty.", null);
					}
				}			
			} while(isRedirection(code));
			
			if (code == HttpURLConnection.HTTP_OK) {
				logger.fine("Connection opened. Code: " + code);
				InputStream stream = conn.getInputStream();
				String encoding = conn.getContentEncoding() == null ? "" : conn.getContentEncoding();
				if (encoding.equalsIgnoreCase("gzip"))
					stream = new GZIPInputStream(stream);
				else if (encoding.equalsIgnoreCase("deflate"))
					stream = new InflaterInputStream(stream);
				else if (conn.getContentType() != null && conn.getContentType().contains("application/octet-stream"))
				{
					stream = new ZipInputStream(stream);
					((ZipInputStream) stream).getNextEntry();
				}
				
				if (Configuration.isDumpXml())
				{
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buf = new byte[4096];
					int i;
					while ((i = stream.read(buf, 0, 4096)) > 0)
						baos.write(buf, 0, i);
					baos.close();
					File dumpfilename = new File(Configuration.getDumpPrefix(), url.getPath().replaceAll("\\p{Punct}", "_") + ".xml");
					logger.info("Dumping xml to file " + dumpfilename.getAbsolutePath());
					FileOutputStream dumpfile = new FileOutputStream(dumpfilename);
					dumpfile.write(baos.toByteArray());
					dumpfile.close();
					stream = new ByteArrayInputStream(baos.toByteArray());
				}
				
				Reader reader = null;
				
				try 
				{
					reader = new InputStreamReader(stream, UTF_8);
					return parser.parse(reader, languages != null ? languages : LanguagePriorityList.ANY);
				} finally {
					if (reader != null)
						reader.close();
				}
			}
			else{
				SdmxException ex = SdmxExceptionFactory.createRestException(code, null, null);
				logger.severe(ex.getMessage());
				if (conn instanceof HttpURLConnection)
					((HttpURLConnection) conn).disconnect();
				throw ex;
			}
		}
		catch (IOException e) 
		{
			logger.severe("Exception. Class: " + e.getClass().getName() + " - Message: " + e.getMessage());
			logger.log(Level.FINER, "Exception: ", e);
			throw SdmxExceptionFactory.wrap(e); 
		} catch (XMLStreamException e) {
			logger.severe("Exception caught parsing results from call to provider " + name);
			logger.log(Level.FINER, "Exception: ", e);
			throw SdmxExceptionFactory.wrap(e); 
		} catch (URISyntaxException e) {
			logger.severe("Exception caught parsing results from call to provider " + name);
			logger.log(Level.FINER, "Exception: ", e);
			throw SdmxExceptionFactory.wrap(e); 
		} finally {
			if (conn != null && conn instanceof HttpURLConnection)
				((HttpURLConnection) conn).disconnect();
		}
	}

	protected void handleHttpHeaders(HttpURLConnection conn, String acceptHeader){
		if(containsCredentials){
			logger.fine("Setting http authorization");		
			String auth = javax.xml.bind.DatatypeConverter.printBase64Binary((user + ":" + pw).getBytes());
			conn.setRequestProperty("Authorization", "Basic " + auth);
		}
		if(supportsCompression){
			conn.addRequestProperty("Accept-Encoding","gzip,deflate");
		}
		if (languages != null) {
			conn.addRequestProperty("Accept-Language", languages.toString());
		}
        if (acceptHeader != null && !"".equals(acceptHeader))
        	conn.setRequestProperty("Accept", acceptHeader);
        else
        	conn.setRequestProperty("Accept", "*/*");
	}

	protected String buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		if( endpoint!=null && dataflow!=null && resource!=null && !resource.isEmpty())
			return RestQueryBuilder.getDataQuery(endpoint, dataflow.getFullIdentifier(), resource, 
					startTime, endTime, serieskeysonly, updatedAfter, includeHistory, null);
		else
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
	}
	
	protected String buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException
	{
		if( endpoint!=null  && agency!=null && !agency.isEmpty() && dsd!=null && !dsd.isEmpty())
			return RestQueryBuilder.getStructureQuery(endpoint, dsd, agency,  version, full);
		else
			throw new RuntimeException("Invalid query parameters: agency=" + agency + " dsd=" + dsd + " endpoint=" + endpoint);
	}
	
	protected String buildFlowQuery(String dataflow, String agency, String version) throws SdmxException{
		return RestQueryBuilder.getDataflowQuery(endpoint,dataflow, agency, version);
	}
	
	protected String buildCodelistQuery(String codeList, String agency, String version) throws SdmxException {
		return RestQueryBuilder.getCodelistQuery(endpoint, codeList, agency, version);
	}
	
	private static boolean isRedirection(int code) {
		return code >= HttpURLConnection.HTTP_MULT_CHOICE && code <= HttpURLConnection.HTTP_SEE_OTHER;
	}
}
