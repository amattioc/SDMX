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
* WITHOUT WARRANTIES OR CONDITIONS OF ANY_LANGUAGE KIND, either
* express or implied.
* See the Licence for the specific language governing
* permissions and limitations under the Licence.
*/
package it.bancaditalia.oss.sdmx.client;

import static it.bancaditalia.oss.sdmx.util.Configuration.getLanguages;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.api.SDMXReference;
import it.bancaditalia.oss.sdmx.api.Message;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.event.DataFooterMessageEvent;
import it.bancaditalia.oss.sdmx.event.OpenEvent;
import it.bancaditalia.oss.sdmx.event.RedirectionEvent;
import it.bancaditalia.oss.sdmx.event.RestSdmxEvent;
import it.bancaditalia.oss.sdmx.event.RestSdmxEventListener;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory;
import it.bancaditalia.oss.sdmx.exceptions.SdmxIOException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxRedirectionException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.parser.v21.CodelistParser;
import it.bancaditalia.oss.sdmx.parser.v21.CompactDataParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;
import it.bancaditalia.oss.sdmx.parser.v21.DataStructureParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataflowParser;
import it.bancaditalia.oss.sdmx.parser.v21.Sdmx21Queries;
import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * @author Attilio Mattiocco
 *
 */
public class RestSdmxClient implements GenericSDMXClient
{
	private static final String		SOURCE_CLASS = RestSdmxClient.class.getSimpleName();
	protected static final Logger	LOGGER = Configuration.getSdmxLogger();

	protected String				sdmxVersion = SDMXClientFactory.SDMX_V2;
	protected String				name;
	protected final boolean			needsURLEncoding;
	protected final boolean			supportsCompression;

	protected ProxySelector			proxySelector;
	protected SSLSocketFactory		sslSocketFactory;
	protected HostnameVerifier		hostnameVerifier;
	protected final boolean			dotStat							= false;
	protected /* final */ URI		endpoint;
	protected boolean				needsCredentials				= false;
	protected boolean				containsCredentials				= false;
	protected String				user							= null;
	protected String				pw								= null;
	protected int					readTimeout;
	protected int					connectTimeout;
	protected RestSdmxEventListener	dataFooterMessageEventListener	= RestSdmxEventListener.NO_OP_LISTENER;
	protected RestSdmxEventListener	redirectionEventListener		= RestSdmxEventListener.NO_OP_LISTENER;
	protected RestSdmxEventListener	openEventListener				= RestSdmxEventListener.NO_OP_LISTENER;
	protected int maxRedirects = 20;
	
	protected final String LATEST_VERSION	= "latest";
	protected final String ALL_AGENCIES	= "all";
	protected String latestKeyword = LATEST_VERSION;

	public RestSdmxClient(String name, URI endpoint, SSLSocketFactory sslSocketFactory, boolean needsCredentials, boolean needsURLEncoding, boolean supportsCompression)
	{
		this.endpoint = endpoint;
		this.name = name;
		this.needsCredentials = needsCredentials;
		this.needsURLEncoding = needsURLEncoding;
		this.supportsCompression = supportsCompression;
		this.proxySelector = null;
		this.sslSocketFactory = sslSocketFactory;
		this.hostnameVerifier = null;
		readTimeout = Configuration.getReadTimeout(getClass().getSimpleName());
		connectTimeout = Configuration.getConnectTimeout(getClass().getSimpleName());
//		languages = LanguageRange.parse(Configuration.getLang());
	}

	public RestSdmxClient(String name, URI endpoint, boolean needsCredentials, boolean needsURLEncoding, boolean supportsCompression)
	{
		this(name, endpoint, null, needsCredentials, needsURLEncoding, supportsCompression);
	}

	public void setProxySelector(ProxySelector proxySelector)
	{
		this.proxySelector = proxySelector;
	}

	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory)
	{
		this.sslSocketFactory = sslSocketFactory;
	}

	public void setHostnameVerifier(HostnameVerifier hostnameVerifier)
	{
		this.hostnameVerifier = hostnameVerifier;
	}

	public void setReadTimeout(int timeout)
	{
		this.readTimeout = timeout;
	}

	public void setConnectTimeout(int timeout)
	{
		this.connectTimeout = timeout;
	}

//	public void setLanguages(List<LanguageRange> languages)
//	{
//		this.languages = languages;
//	}

	public void setDataFooterMessageEventListener(RestSdmxEventListener eventListener)
	{
		this.dataFooterMessageEventListener = eventListener;
	}

	public void setRedirectionEventListener(RestSdmxEventListener eventListener)
	{
		this.redirectionEventListener = eventListener;
	}

	public void setOpenEventListener(RestSdmxEventListener eventListener)
	{
		this.openEventListener = eventListener;
	}

	public void setMaxRedirects(int maxRedirects)
	{
		this.maxRedirects = maxRedirects;
	}

	@Override
	public Map<String, Dataflow> getDataflows() throws SdmxException
	{
		Map<String, Dataflow> result = null;
		URL query = buildFlowQuery(ALL_AGENCIES, "all", latestKeyword);
		List<Dataflow> flows = runQuery(new DataflowParser(), query, null, "dataflow_all");
		if (flows.size() > 0)
		{
			result = new HashMap<>();
			for (Dataflow dataflow : flows)
				result.put(dataflow.getFullIdentifier(), dataflow);
		}
		else
		{
			throw new SdmxXmlContentException("The query returned zero dataflows");
		}
		return result;
	}

	@Override
	public Dataflow getDataflow(String dataflow, String agency, String version) throws SdmxException
	{
		Dataflow result = null;
		if(agency == null) agency = ALL_AGENCIES;
		if(version == null) version = this.latestKeyword;
		URL query = buildFlowQuery(dataflow, agency, version);
		List<Dataflow> flows = runQuery(new DataflowParser(), query, null, "dataflow_" + dataflow);
		if (flows.size() >= 1)
			result = flows.get(0);
		else
			throw new SdmxXmlContentException("The query returned zero dataflows");

		return result;
	}

	@Override
	public DataFlowStructure getDataFlowStructure(SDMXReference dsd, boolean full) throws SdmxException
	{
		if (dsd == null)
			throw new SdmxInvalidParameterException("getDataFlowStructure(): Null dsd in input");
		else
		{
			URL query = buildDSDQuery(dsd.getId(), dsd.getAgency(), dsd.getVersion(), full);
			return runQuery(new DataStructureParser(), query, null, "datastructure_" + dsd.getId()).get(0);
		}
	}

	@Override
	public Codelist getCodes(String codeList, String agency, String version) throws SdmxException
	{
		URL query = buildCodelistQuery(codeList, agency, version);
		return runQuery(new CodelistParser(), query, null, "codelist_" + codeList);
	}

	@Override
	public List<PortableTimeSeries<Double>> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String resource, 
			String startTime, String endTime,
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		return postProcess(getData(dataflow, dsd, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory));
	}

	@Override
	public List<PortableTimeSeries<Double>> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String resource, String filter, 
			String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
		if(filter != null && !filter.isEmpty())
			throw new SdmxInvalidParameterException("This method can only be called on SDMX V3 providers.");
		else
			return getTimeSeries(dataflow, dsd, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
	}

	@Override
	public Map<String, List<String>> getAvailableCubeRegion(Dataflow dataflow, String filter, String mode) throws SdmxException {
		throw new SdmxInvalidParameterException("This method can only be called on SDMX V3 providers.");
	}

	@Override
	public Integer getAvailableTimeSeriesNumber(Dataflow dataflow, String filter) throws SdmxException {
		throw new SdmxInvalidParameterException("This method can only be called on SDMX V3 providers.");
	}

	protected DataParsingResult getData(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, boolean serieskeysonly,
			String updatedAfter, boolean includeHistory) throws SdmxException
	{
		URL query = buildDataQuery(dataflow, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		String dumpName = "data_" + dataflow.getId() + "_" + resource; //.replaceAll("\\p{Punct}", "_");
		DataParsingResult ts = runQuery(new CompactDataParser(dsd, dataflow, !serieskeysonly), query,
				"application/vnd.sdmx.structurespecificdata+xml;version=2.1", dumpName);
		Message msg = ts.getMessage();
		if (msg != null)
		{
			LOGGER.log(Level.INFO, "The sdmx call returned messages in the footer:\n {0}", msg);
			RestSdmxEvent event = new DataFooterMessageEvent(query, msg);
			dataFooterMessageEventListener.onSdmxEvent(event);
		}
		return ts;
	}

	@Override
	public boolean needsCredentials()
	{
		return needsCredentials;
	}

	@Override
	public void setCredentials(String user, String pw)
	{
		this.user = user;
		this.pw = pw;
		this.needsCredentials = false;
		this.containsCredentials = true;
	}

	@Override
	public URI getEndpoint()
	{
		return endpoint;
	}

	@Override
	public void setEndpoint(URI endpoint)
	{
		this.endpoint = endpoint;
	}

	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	public String getSdmxVersion() {
		return sdmxVersion;
	}

	public void setSdmxVersion(String sdmxVersion) {
		this.sdmxVersion = sdmxVersion;
	}

	@Override
	public String buildDataURL(Dataflow dataflow, String resource, String startTime, String endTime, boolean seriesKeyOnly, String updatedAfter,
			boolean includeHistory) throws SdmxException
	{
		return buildDataQuery(dataflow, resource, startTime, endTime, seriesKeyOnly, updatedAfter, includeHistory).toString();
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
	protected final <T> T runQuery(Parser<T> parser, URL query, String acceptHeader, String dumpName) throws SdmxException
	{
		final String sourceMethod = "runQuery";
		LOGGER.entering(SOURCE_CLASS, sourceMethod);

		URLConnection conn = null;
		URL url = null;
		LOGGER.log(Level.INFO, "Contacting web service with query: {0}", query);

		try
		{
			int code;
			url = query;
			URL originalURL = url;

			Proxy proxy = (proxySelector != null ? proxySelector : ProxySelector.getDefault()).select(url.toURI()).get(0);
			LOGGER.fine("Using proxy: " + proxy);
			
			openEventListener.onSdmxEvent(new OpenEvent(url, acceptHeader, getLanguages(), proxy));

			int redirects = 0;
			do
			{
				conn = url.openConnection(proxy);

				if (conn instanceof HttpsURLConnection && sslSocketFactory != null)
				{
					LOGGER.fine("Using custom SSLSocketFactory for provider " + name);
					((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
				}

				if (conn instanceof HttpsURLConnection && hostnameVerifier != null)
				{
					LOGGER.fine("Using custom HostnameVerifier for provider " + name);
					((HttpsURLConnection) conn).setHostnameVerifier(hostnameVerifier);
				}

				conn.setReadTimeout(readTimeout);
				conn.setConnectTimeout(connectTimeout);

				if (conn instanceof HttpURLConnection)
				{
					((HttpURLConnection) conn).setRequestMethod("GET");
					((HttpURLConnection) conn).setInstanceFollowRedirects(false);
					handleHttpHeaders((HttpURLConnection) conn, acceptHeader);
				}

				code = conn instanceof HttpURLConnection ? ((HttpURLConnection) conn).getResponseCode() : HttpURLConnection.HTTP_OK;
				if (code == HttpURLConnection.HTTP_PROXY_AUTH)
				{
					LOGGER.fine("Error with proxy. Second attempt after forcing acces to http website in first place.");
					URI uritest= new URI("http://google.com");
					URL urltest = uritest.toURL();
					conn = urltest.openConnection(proxy);
					((HttpURLConnection) conn).setRequestMethod("GET");
					code = conn instanceof HttpURLConnection ? ((HttpURLConnection) conn).getResponseCode() : HttpURLConnection.HTTP_OK;
					conn = url.openConnection(proxy);
					((HttpURLConnection) conn).setRequestMethod("GET");
					code = conn instanceof HttpURLConnection ? ((HttpURLConnection) conn).getResponseCode() : HttpURLConnection.HTTP_OK;

				}
				if (isRedirection(code))
				{
					URL redirection = getRedirectionURL(conn, code);
//					if (conn instanceof HttpURLConnection)
//						((HttpURLConnection) conn).disconnect();
					if (isDowngradingProtocolOnRedirect(originalURL, redirection)) {
						throw new SdmxRedirectionException("Downgrading protocol on redirect from '" + originalURL + "' to '" + redirection + "'");
					}
					LOGGER.log(Level.INFO, "Redirecting to: {0}", redirection);
					RestSdmxEvent event = new RedirectionEvent(url, redirection);
					redirectionEventListener.onSdmxEvent(event);
					url = redirection;
					redirects++;
				}
			} while (isRedirection(code) && !(isMaxRedirectionReached(redirects)));
			
			if (isMaxRedirectionReached(redirects)) {
				throw new SdmxRedirectionException("Max redirection reached");
			}
			
			if (code == HttpURLConnection.HTTP_OK)
			{
				LOGGER.fine("Connection opened. Code: " + code);
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

				if (Configuration.isDumpXml() && dumpName != null) // skip providers < sdmx v2.1
				{
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buf = new byte[4096];
					int i;
					while ((i = stream.read(buf, 0, 4096)) > 0)
						baos.write(buf, 0, i);
					baos.close();
//					String resource = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8.name()).replaceAll(endpoint.getPath() + "/?", "")
//							.replaceFirst("/$", "").replaceAll("\\p{Punct}", "_") + ".xml";
					System.err.println(Configuration.getDumpPrefix());
					File dumpfilename = new File(Configuration.getDumpPrefix() + File.separator + name, dumpName + ".xml");
					if (!dumpfilename.getParentFile().exists() && !dumpfilename.getParentFile().mkdirs()) {
					    LOGGER.warning("Error creating path to dump file: " + dumpfilename);
					}
					else{
						LOGGER.info("Dumping xml to file " + dumpfilename.getAbsolutePath());
						FileOutputStream dumpfile = new FileOutputStream(dumpfilename);
						dumpfile.write(baos.toByteArray());
						dumpfile.close();
						stream = new ByteArrayInputStream(baos.toByteArray());
					}
				}

				try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8))
				{
					XMLInputFactory inputFactory = XMLInputFactory.newFactory();
					preventXXE(inputFactory);
					BufferedReader br = skipBOM(reader);
					// InputStream in = new ByteArrayInputStream(xmlBuffer);
					XMLEventReader eventReader = inputFactory.createXMLEventReader(br);

					return parser.parse(eventReader, getLanguages());
				}
			}
			else
			{
				InputStream is = ((HttpURLConnection)conn).getErrorStream();
				if(is != null){
					String msg = new BufferedReader(new InputStreamReader(is)).readLine();
					LOGGER.severe(msg);
				}
				SdmxException ex = SdmxExceptionFactory.createRestException(code, null, null);
				if (conn instanceof HttpURLConnection)
					((HttpURLConnection) conn).disconnect();
				throw ex;
			}
		}
		catch (IOException e)
		{
			LOGGER.severe("Exception. Class: " + e.getClass().getName() + " - Message: " + e.getMessage());
			LOGGER.log(Level.FINER, "Exception: ", e);
			throw SdmxExceptionFactory.wrap(e);
		}
		catch (XMLStreamException e)
		{
			LOGGER.severe("Exception caught parsing results from call to provider " + name);
			LOGGER.log(Level.FINER, "Exception: ", e);
			throw SdmxExceptionFactory.wrap(e);
		}
		catch (URISyntaxException e)
		{
			LOGGER.severe("Exception caught parsing results from call to provider " + name);
			LOGGER.log(Level.FINER, "Exception: ", e);
			throw SdmxExceptionFactory.wrap(e);
		}
		finally
		{
			if (conn != null && conn instanceof HttpURLConnection)
				((HttpURLConnection) conn).disconnect();
		}
	}

	protected void handleHttpHeaders(HttpURLConnection conn, String acceptHeader)
	{
		String lList = Configuration.getLanguages().stream()
			.map(lr -> format(Locale.US, "%s;q=%.1f", lr.getRange(), lr.getWeight()))
			.collect(joining(","));
		conn.addRequestProperty("Accept-Language", lList);
		if (containsCredentials)
		{
			LOGGER.fine("Setting http authorization");
			// https://stackoverflow.com/questions/1968416/how-to-do-http-authentication-in-android/1968873#1968873
			//String auth = Base64.encodeToString((user + ":" + pw).getBytes(), Base64.NO_WRAP);
			String auth = java.util.Base64.getEncoder().encodeToString((user + ":" + pw).getBytes());
			conn.setRequestProperty("Authorization", "Basic " + auth);
		}
		if (supportsCompression)
		{
			conn.addRequestProperty("Accept-Encoding", "gzip,deflate");
		}
		if (acceptHeader != null && !"".equals(acceptHeader))
			conn.setRequestProperty("Accept", acceptHeader);
		else
			conn.setRequestProperty("Accept", "*/*");
	}

	protected URL buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		if (endpoint != null && dataflow != null && resource != null && !resource.isEmpty())
			return Sdmx21Queries
					.createDataQuery(endpoint, dataflow.getFullIdentifier(), resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory, null)
					.buildSdmx21Query();
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
	}

	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException
	{
		if (endpoint != null && agency != null && !agency.isEmpty() && dsd != null && !dsd.isEmpty())
			return Sdmx21Queries.createStructureQuery(endpoint, dsd, agency, version, full).buildSdmx21Query();
		else
			throw new RuntimeException("Invalid query parameters: agency=" + agency + " dsd=" + dsd + " endpoint=" + endpoint);
	}

	protected URL buildFlowQuery(String dataflow, String agency, String version) throws SdmxException
	{
		return Sdmx21Queries.createDataflowQuery(endpoint, dataflow, agency, version).buildSdmx21Query();
	}

	protected URL buildCodelistQuery(String codeList, String agency, String version) throws SdmxException
	{
		return Sdmx21Queries.createCodelistQuery(endpoint, codeList, agency, version).buildSdmx21Query();
	}

	private static boolean isRedirection(int code)
	{
		return (code >= HttpURLConnection.HTTP_MULT_CHOICE && code <= HttpURLConnection.HTTP_SEE_OTHER) 
				|| code == 307; // TEMPORARY REDIRECT
	}

	private static URL getRedirectionURL(URLConnection conn, int code) throws SdmxIOException
	{
		String location = conn.getHeaderField("Location");
		if (location == null || location.isEmpty())
		{
			throw new SdmxIOException("The endpoint returned redirect code: " + code + ", but the location was empty.", null);
		}
		try
		{
			return new URL(location);
		}
		catch (MalformedURLException ex)
		{
			throw new SdmxIOException("The endpoint returned redirect code: " + code + ", but the location was malformed: '" + location + "'.", null);
		}
	}

	// some 2.0 providers are apparently adding a BOM
	public BufferedReader skipBOM(Reader xmlBuffer) throws SdmxException
	{
		BufferedReader br = new BufferedReader(xmlBuffer) {
			@Override
			public void close() throws IOException
			{
				LOGGER.fine("GenericDataParser::skipBOM: closing stream.");
				super.close();
			}
		};
		try
		{
			// java uses Unicode big endian
			char[] cbuf = new char[1];
			// TODO: Source of problems here
			br.mark(1);
			br.read(cbuf, 0, 1);
			LOGGER.fine(String.format("0x%2s", Integer.toHexString(cbuf[0])));
			if ((byte) cbuf[0] == (byte) 0xfeff)
			{
				LOGGER.fine("BOM found and skipped");
			}
			else
			{
				// TODO: Source of problems here
				LOGGER.fine("GenericDataParser::skipBOM: Resetting stream.");
				br.reset();
			}
		}
		catch (IOException e)
		{
			throw SdmxExceptionFactory.wrap(e);
		}
		return br;
	}

	// https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#XMLInputFactory_.28a_StAX_parser.29
	private static void preventXXE(XMLInputFactory factory)
	{
		if (factory.isPropertySupported(XMLInputFactory.SUPPORT_DTD))
		{
			factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		}
		if (factory.isPropertySupported(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES))
		{
			factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		}
	}

	/**
	 * Override this method in subclasses to perform some post processing of the retrieved series.
	 * 
	 * @param result The retrieved series for a given query.
	 * @return The processed series.
	 */
	protected List<PortableTimeSeries<Double>> postProcess(DataParsingResult result)
	{
		return result;
	}
	
	private boolean isMaxRedirectionReached(int redirects) {
		return redirects > maxRedirects;
	}
	
	/**
	 * https://en.wikipedia.org/wiki/Downgrade_attack
	 *
	 * @param oldUrl
	 * @param newUrl
	 * @return
	 */
	private static boolean isDowngradingProtocolOnRedirect(URL oldUrl, URL newUrl) {
		return "https".equalsIgnoreCase(oldUrl.getProtocol())
			&& !"https".equalsIgnoreCase(newUrl.getProtocol());
	}
}
