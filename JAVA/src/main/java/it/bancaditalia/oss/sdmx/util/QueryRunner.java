package it.bancaditalia.oss.sdmx.util;

import static it.bancaditalia.oss.sdmx.event.RestSdmxEventListener.NO_OP_LISTENER;
import static it.bancaditalia.oss.sdmx.util.Configuration.getLanguages;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.event.OpenEvent;
import it.bancaditalia.oss.sdmx.event.RedirectionEvent;
import it.bancaditalia.oss.sdmx.event.RestSdmxEvent;
import it.bancaditalia.oss.sdmx.event.RestSdmxEventListener;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory;
import it.bancaditalia.oss.sdmx.exceptions.SdmxIOException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxRedirectionException;

public class QueryRunner
{
	private static final String SOURCE_CLASS = QueryRunner.class.getSimpleName();
	private static final Logger LOGGER = Configuration.getSdmxLogger();

	private static ProxySelector proxySelector = null;
	private static HostnameVerifier hostnameVerifier = null;
	private static RestSdmxEventListener	dataFooterMessageEventListener	= NO_OP_LISTENER;
	private static RestSdmxEventListener	redirectionEventListener		= NO_OP_LISTENER;
	private static RestSdmxEventListener	openEventListener				= NO_OP_LISTENER;

	public static <T> T runQuery(Parser<T> parser, URL query, Map<String, String> headers) throws SdmxException
	{
		return runQuery(parser, query, null, null, headers);
	}
	/**
	 * Returns a reader over the result of an http query.
	 *
	 * @param parser a non-null parser used to get the result from the xml file
	 * @param query a non-null url to query
	 * @param headers a non-null map containing headers to use in the REST call
	 * @return The result of the parsing
	 * 
	 * @throws SdmxException
	 */
	public static <T> T runQuery(Parser<T> parser, URL query, String provider, String dumpName, Map<String, String> headers) throws SdmxException
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

			openEventListener.onSdmxEvent(new OpenEvent(url, headers != null ? headers.get("Accept") : "*/*", getLanguages(), proxy));

			int redirects = 0;
			do
			{
				conn = url.openConnection(proxy);

				if (conn instanceof HttpsURLConnection && hostnameVerifier != null)
				{
					LOGGER.fine("Using custom HostnameVerifier");
					((HttpsURLConnection) conn).setHostnameVerifier(hostnameVerifier);
				}

				conn.setConnectTimeout(Configuration.getConnectTimeout());
				conn.setReadTimeout(Configuration.getReadTimeout());

				if (conn instanceof HttpURLConnection)
				{
					((HttpURLConnection) conn).setRequestMethod("GET");
					((HttpURLConnection) conn).setInstanceFollowRedirects(false);
					if (headers != null)
						for (Entry<String, String> e: headers.entrySet())
							conn.addRequestProperty(e.getKey(), e.getValue());
				}

				code = conn instanceof HttpURLConnection ? ((HttpURLConnection) conn).getResponseCode() : HttpURLConnection.HTTP_OK;

				// note on PR #227
				// in some cases in https the proxy will not let the connect pass unless a previous call has been done in http.
				// this is probably caused by the basic auth being disabled in the java env and it can be fixed 
				// re-allowing it (-Djdk.http.auth.tunneling.disabledSchemes=)
				
				if (isRedirection(code))
				{
					URL redirection = getRedirectionURL(conn, code);
//					if (conn instanceof HttpURLConnection)
//						((HttpURLConnection) conn).disconnect();
					if (isDowngradingProtocolOnRedirect(originalURL, redirection))
					{
						throw new SdmxRedirectionException("Downgrading protocol on redirect from '" + originalURL + "' to '" + redirection + "'");
					}
					LOGGER.log(Level.INFO, "Redirecting to: {0}", redirection);
					RestSdmxEvent event = new RedirectionEvent(url, redirection);
					redirectionEventListener.onSdmxEvent(event);
					url = redirection;
					redirects++;
				}
			} while (isRedirection(code) && !(isMaxRedirectionReached(redirects)));

			if (isMaxRedirectionReached(redirects))
			{
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
//					String resource = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8.name()).replaceAll(endpoint.getPath() + "/?", "")
//							.replaceFirst("/$", "").replaceAll("\\p{Punct}", "_") + ".xml";
					System.err.println(Configuration.getDumpPrefix());
					File dumpfilename = Paths.get(Configuration.getDumpPrefix(), provider, dumpName + ".xml").toFile();
					if (dumpfilename.getParentFile().exists() || dumpfilename.getParentFile().mkdirs())
					{
						LOGGER.info("Dumping xml to file " + dumpfilename.getAbsolutePath());
						stream = new TeeInputStream(stream, dumpfilename);
					}
					else
						LOGGER.warning("Error creating path to dump file: " + dumpfilename);
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
				LOGGER.severe("Connection error. Code:" + code);
				InputStream stream = ((HttpURLConnection) conn).getErrorStream();
				if(stream != null){
					String encoding = conn.getContentEncoding() == null ? "" : conn.getContentEncoding();
					if (encoding.equalsIgnoreCase("gzip"))
						stream = new GZIPInputStream(stream);
					else if (encoding.equalsIgnoreCase("deflate"))
						stream = new InflaterInputStream(stream);
					String msg = new BufferedReader(new InputStreamReader(stream)).lines().collect(joining(lineSeparator()));
					LOGGER.severe("Message:" + msg);
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
		catch (XMLStreamException | URISyntaxException e)
		{
			LOGGER.severe("Exception caught parsing query results: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			LOGGER.log(Level.FINER, "Exception: ", e);
			throw SdmxExceptionFactory.wrap(e);
		}
		finally
		{
			if (conn != null && conn instanceof HttpURLConnection)
				((HttpURLConnection) conn).disconnect();
		}
	}

	private static boolean isRedirection(int code)
	{
		return (code >= HttpURLConnection.HTTP_MULT_CHOICE && code <= HttpURLConnection.HTTP_SEE_OTHER) || code == 307; // TEMPORARY REDIRECT
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

	private static boolean isMaxRedirectionReached(int redirects)
	{
		return redirects > Configuration.getMaxRedirects();
	}

	/**
	 * https://en.wikipedia.org/wiki/Downgrade_attack
	 *
	 * @param oldUrl
	 * @param newUrl
	 * @return
	 */
	private static boolean isDowngradingProtocolOnRedirect(URL oldUrl, URL newUrl)
	{
		return "https".equalsIgnoreCase(oldUrl.getProtocol()) && !"https".equalsIgnoreCase(newUrl.getProtocol());
	}

	// some 2.0 providers are apparently adding a BOM
	private static BufferedReader skipBOM(Reader xmlBuffer) throws SdmxException
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

	public static ProxySelector getProxySelector()
	{
		return proxySelector;
	}

	public static void setProxySelector(ProxySelector proxySelector)
	{
		QueryRunner.proxySelector = proxySelector;
	}

	public static HostnameVerifier getHostnameVerifier()
	{
		return hostnameVerifier;
	}

	public static void setHostnameVerifier(HostnameVerifier hostnameVerifier)
	{
		QueryRunner.hostnameVerifier = hostnameVerifier;
	}

	public static RestSdmxEventListener getDataFooterMessageEventListener()
	{
		return dataFooterMessageEventListener;
	}

	public static void setDataFooterMessageEventListener(RestSdmxEventListener dataFooterMessageEventListener)
	{
		QueryRunner.dataFooterMessageEventListener = dataFooterMessageEventListener;
	}

	public static RestSdmxEventListener getRedirectionEventListener()
	{
		return redirectionEventListener;
	}

	public static void setRedirectionEventListener(RestSdmxEventListener redirectionEventListener)
	{
		QueryRunner.redirectionEventListener = redirectionEventListener;
	}

	public static RestSdmxEventListener getOpenEventListener()
	{
		return openEventListener;
	}

	public static void setOpenEventListener(RestSdmxEventListener openEventListener)
	{
		QueryRunner.openEventListener = openEventListener;
	}
}
