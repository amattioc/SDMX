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
package it.bancaditalia.oss.sdmx.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JFrame;

/**
 * @author Attilio Mattiocco
 *
 */
public class Configuration {
	
	// TODO: will be replaced by StandardCharsets#UTF_8 in Java 7
	public static final Charset UTF_8 = Charset.forName("UTF-8");
	
	protected static Logger SDMX_LOGGER = null;
	protected static final String PROXY_AUTH_KERBEROS = "Kerberos";
	protected static final String PROXY_AUTH_DIGEST = "digest";
	protected static final String PROXY_AUTH_BASIC = "basic";
	protected static final String JAVA_SECURITY_KERBEROS_PROP = "java.security.krb5.conf";	
	protected static final String JAVA_SECURITY_AUTH_LOGIN_CONFIG_PROP = "java.security.auth.login.config"; 
	protected static final String HTTP_AUTH_PREF_PROP = "http.auth.preference";
	protected static final String SSL_DISABLE_CERT_CHECK_PROP = "ssl.disable.cert.check";  
	protected static final String SSL_TRUSTSTORE_PROP = "javax.net.ssl.trustStore";

	protected static final String CENTRAL_CONFIGURATION_FILE_PROP = "SDMX_CONF";
	protected static final String EXTERNAL_PROVIDERS_PROP = "external.providers";
	protected static final String PROXY_NAME_PROP = "http.proxy.name";
	protected static final String PROXY_DEFAULT_PROP = "http.proxy.default";
	protected static final String HTTP_AUTH_USER_PROP = "http.auth.user";
	protected static final String PROXY_AUTH_PW_PROP = "http.auth.pw";  
	protected static final String REVERSE_DUMP_PROP = "reverse.dump";  
	protected static final String SDMX_LANG_PROP = "sdmx.lang";  
	protected static final String LATE_RESP_RETRIES_PROP = "late.response.retries";  
	protected static final String TABLE_DUMP_PROP = "table.dump";  
	protected static final String READ_TIMEOUT_PROP = "read.timeout";  
	protected static final String CONNECT_TIMEOUT_PROP = "connect.timeout";  

	private static final String REVERSE_DUMP_DEFAULT = "FALSE";
	private static final String TABLE_DUMP_DEFAULT = "FALSE";
	private static final String SDMX_DEFAULT_LANG = "en";  
	private static final String SDMX_DEFAULT_TIMEOUT = "0";  
	private static final String LOGGER_NAME = "SDMX";
	private static final String CONFIGURATION_FILE_NAME = "configuration.properties";
	private static  String SDMX_LANG = "en";  

	private static final String sourceClass = Configuration.class.getSimpleName();
	private static Properties props = new Properties();
	private static boolean inited = false;

	protected static void setSdmxLogger(){
		if(SDMX_LOGGER == null){
			SDMX_LOGGER = Logger.getLogger(LOGGER_NAME);
			//SDMX_LOGGER.setUseParentHandlers(false);
			LogManager.getLogManager().addLogger(SDMX_LOGGER);
		}
	}
	
	public static Logger getSdmxLogger(){
		setSdmxLogger();
		return SDMX_LOGGER;
	}
	
	public static Properties getConfiguration(){
		return props;
	}
	
	public static boolean isReverse(){
		return props.getProperty(REVERSE_DUMP_PROP, REVERSE_DUMP_DEFAULT).equalsIgnoreCase("TRUE");
	}

	public static boolean isTable(){
		return props.getProperty(TABLE_DUMP_PROP, TABLE_DUMP_DEFAULT).equalsIgnoreCase("TRUE");
	}

	public static String getExternalProviders(){
		return props.getProperty(Configuration.EXTERNAL_PROVIDERS_PROP);
	}

	public static int getReadTimeout(String provider){
		String timeout = props.getProperty(provider + "." + Configuration.READ_TIMEOUT_PROP, null);
		if(timeout == null){
			timeout = props.getProperty(Configuration.READ_TIMEOUT_PROP, Configuration.SDMX_DEFAULT_TIMEOUT);
		}
		return Integer.parseInt(timeout);	
	}
	
	public static int getConnectTimeout(String provider){
		String timeout = props.getProperty(provider + "." + Configuration.CONNECT_TIMEOUT_PROP, null);
		if(timeout == null){
			timeout = props.getProperty(Configuration.CONNECT_TIMEOUT_PROP, Configuration.SDMX_DEFAULT_TIMEOUT);
		}
		return Integer.parseInt(timeout);	
	}

	public static String getLang(){
		return SDMX_LANG;
	}

	public static String getLateResponseRetries(int defaultRetries){
		return props.getProperty(Configuration.LATE_RESP_RETRIES_PROP, Integer.toString(defaultRetries));
	}

	public static void setLang(String lang){
		SDMX_LANG = lang;
	}

	public static void init() {
		
		if(inited) return; else inited = true;
		
		//normal configuration steps:
		// 1 init logger
		// 2 search configuration in this order: local, global, Configuration class   
		// 3 if none is found, apply defaults: no proxy and INFO Logger
		setSdmxLogger();
		//for Matlab: to avoid being considered a browser
		System.setProperty("http.agent", "SDMX");
		
		String confType = null;
		
		InputStream is1 = null;
		InputStream is2 = null;
		// try local configuration. If found apply and exit
		if(new File(CONFIGURATION_FILE_NAME).exists()){
			try {
				is1 = new FileInputStream(CONFIGURATION_FILE_NAME);
				is2 = new FileInputStream(CONFIGURATION_FILE_NAME);
				init(is1, is2);
				confType = System.getProperty("user.dir") + File.separator + CONFIGURATION_FILE_NAME;
				SDMX_LOGGER.info("Local configuration file found: " + confType );
			} 
			catch (Exception e) {
				//logger could be not available
				e.printStackTrace();
			}
		}
		// now try  globally configured file configuration and, if necessary, class configuration 
		else {
			// try global configuration
			String central = System.getenv(CENTRAL_CONFIGURATION_FILE_PROP);
			if( central != null && !central.isEmpty()){
				if(new File(central).exists()){
					try {
						is1 = new FileInputStream(central);
						is2 = new FileInputStream(central);
						init(is1, is2);
						confType = central;
						SDMX_LOGGER.info("Central configuration file found: " + confType );
					} 
					catch (Exception e) {
						//logger could be not available
						e.printStackTrace();
					}
				}
			}
			if(confType == null){
				// try configuration class. 
				try {
					Class<?> clazz = Class.forName("it.bancaditalia.oss.sdmx.util.SdmxConfiguration");
					Method method = clazz.getMethod("init");
					method.invoke((Object)null);
					confType = clazz.getCanonicalName();
					SDMX_LOGGER.info("Class configuration found: " + confType);
				} catch( Exception notFound ) {
				}
			}
		}
		// no class or file configuration found, apply some defaults
		if(confType == null){
			ConsoleHandler handler = new ConsoleHandler();
			handler.setLevel(Level.INFO);
			SDMX_LOGGER.addHandler(handler);
			confType = "default";
			SDMX_LOGGER.info("No configuration found. Apply defaults.");
		}
	}
	
	private static void init(InputStream is1, InputStream is2) throws SecurityException, IOException {
		setSdmxLogger();
		LogManager.getLogManager().readConfiguration(is1);
		is1.close();
		props.load(is2);
		is2.close();
		
		//configure SSL
		String tStore = props.getProperty(SSL_TRUSTSTORE_PROP);
		if(tStore != null && !tStore.isEmpty()){
			System.setProperty(SSL_TRUSTSTORE_PROP, tStore);
		}
		
		setupTrustAllCerts();
		
		//configure default language if not already set explicitly
		SDMX_LANG = props.getProperty(SDMX_LANG_PROP, SDMX_DEFAULT_LANG);
		
		configureProxy(props);
	}

	private static void setupTrustAllCerts() {
		if (props.getProperty(SSL_DISABLE_CERT_CHECK_PROP, "FALSE").equalsIgnoreCase("TRUE")) {
			SDMX_LOGGER.fine("The SSL Certificate checks are disabled...");
			TrustManager[] alwaysTrust = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {
				}
				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {
				}
			} };

			SSLContext context = null;
			try {
				context = SSLContext.getInstance("SSL");
				context.init(null, alwaysTrust, new java.security.SecureRandom());
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (context != null)
				HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
			
			//we also want to avoid verification of the chains 
			HostnameVerifier alwaysValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			HttpsURLConnection.setDefaultHostnameVerifier(alwaysValid);
		}
	}

	private static void configureProxy(Properties props){
		final String sourceMethod = "configureProxy";
		Logger logger = SDMX_LOGGER;
		logger.entering(sourceClass, sourceMethod);
		
		//property: http.proxy.default
		String defaultproxy = props.getProperty(PROXY_DEFAULT_PROP);
		String defaultHost = null;
		int defaultPort = 0;
		boolean useProxy = false;
		if(defaultproxy != null && !defaultproxy.isEmpty()){
			useProxy = true;
			String[] toks = defaultproxy.split(":");
			if(toks.length != 2  || toks[0] == null || toks[0].isEmpty() || toks[1] == null || toks[1].isEmpty()){
				throw new IllegalArgumentException("Proxy settings must be valid. found: '" + defaultproxy + "'");
			}
			defaultHost = toks[0].trim();
			defaultPort = Integer.parseInt(toks[1].trim());
		}
		SdmxProxySelector proxySelector = new SdmxProxySelector(defaultHost, defaultPort);
		
		for (int i = 0; ; i++) {
			//property: http.proxy.name_n
			String proxy = props.getProperty(PROXY_NAME_PROP + i);
			if (proxy != null && ! proxy.isEmpty()){
				useProxy = true;
				String[] toks = null;
				toks = proxy.split(":");
				if(toks == null || toks.length != 2  || toks[0] == null || toks[0].isEmpty() || toks[1] == null || toks[1].isEmpty()){
					throw new IllegalArgumentException("Proxy settings must be valid. host: '" + toks[0] + "', port: '" + toks[1] + "'");
				}
				//property: http.proxy.name_n.list
				String urls = props.getProperty(PROXY_NAME_PROP + i + ".urls");
				if(urls != null && !urls.isEmpty()){
					String[] urlList = urls.split(",");
					proxySelector.addProxy(toks[0], toks[1], urlList);
					logger.finer("Proxy has been configured: '" + proxy + "' for " + urls);				
				}
				else {
					throw new IllegalArgumentException("Proxy settings must be valid. host: '" + toks[0] + "', port: '" + toks[1] + "'" + ", urls: " + urls);
				}
			}
			else{
				break;
			}
		}
		
		if(useProxy)
			ProxySelector.setDefault(proxySelector);

		if(props != null && useProxy){
			//get authentication preferences
			String proxyAuth = props.getProperty(HTTP_AUTH_PREF_PROP);
			if(proxyAuth != null){ 
				proxyAuth = proxyAuth.trim();
				System.setProperty(HTTP_AUTH_PREF_PROP, proxyAuth);
				logger.finer(proxyAuth + " authentication enabled.");
				
				if(proxyAuth.equalsIgnoreCase(PROXY_AUTH_KERBEROS)){
					//set properties for JAAS
					String conf = props.getProperty(JAVA_SECURITY_KERBEROS_PROP);
					String login = props.getProperty(JAVA_SECURITY_AUTH_LOGIN_CONFIG_PROP);
			    	String krbccname = System.getenv().get("KRB5CCNAME");
		
			    	if(krbccname != null && login != null && conf != null){
			    		krbccname = krbccname.trim();
			    		login = login.trim();
			    		conf = conf.trim();
			    		System.setProperty("user.krb5cc", krbccname);
				    	System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
				    	System.setProperty(JAVA_SECURITY_KERBEROS_PROP, conf);
				    	System.setProperty(JAVA_SECURITY_AUTH_LOGIN_CONFIG_PROP, login);	    	
				    	logger.finer(JAVA_SECURITY_KERBEROS_PROP + " = " + conf);
			    		logger.finer(JAVA_SECURITY_AUTH_LOGIN_CONFIG_PROP + " = " + login);
			    		logger.finer("Environment variable KRB5CCNAME = " + krbccname);
			    	}
			    	else{
			    		logger.warning("Kerberos ticket cache not configured because one of the parameters is not set.");
			    		logger.warning(JAVA_SECURITY_KERBEROS_PROP + " = " + conf);
			    		logger.warning(JAVA_SECURITY_AUTH_LOGIN_CONFIG_PROP + " = " + login);
			    		logger.warning("Environment variable KRB5CCNAME = " + krbccname);
			    	}   	    	
				}
				else if(proxyAuth.equalsIgnoreCase(PROXY_AUTH_BASIC)){
					String username = props.getProperty(HTTP_AUTH_USER_PROP);
					String password = props.getProperty(PROXY_AUTH_PW_PROP);
					setCredentials(proxyAuth, username, password);
				}
				else{
					logger.finer("Authentication type not supported: " + proxyAuth);
				}
			}
			else{
				logger.finer("No authentication enabled.");
			}
		}
		logger.exiting(sourceClass, sourceMethod);
	}
	
	private static void setCredentials(String scheme, String username, String password){
		//Logger logger = SDMX_LOGGER;
		System.setProperty(HTTP_AUTH_PREF_PROP, scheme);
		if(username == null || password == null){
			//logger.warning("Proxy User and password not found.");
			final JFrame frame = new JFrame("Proxy Authentication");
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			LoginDialog loginDlg = new LoginDialog(frame, "Proxy Authentication");
            loginDlg.setVisible(true);
            username = loginDlg.getUsername();
            password = loginDlg.getPassword();
            frame.dispose();
		}
		final String user = username.trim();
		final String pw = password.trim();
		Authenticator.setDefault(new Authenticator()
		{
			protected  PasswordAuthentication  getPasswordAuthentication(){
				final String sourceMethod = "getPasswordAuthentication";
				Logger logger = SDMX_LOGGER;
				logger.entering(sourceClass, sourceMethod);

				PasswordAuthentication p=new PasswordAuthentication(user, pw.toCharArray());
				logger.finer("Requesting Host  : " + getRequestingHost());
				logger.finer("Requesting Port  : " + getRequestingPort());
				logger.finer("Requesting Protocol: " + getRequestingProtocol());
				logger.finer("Requesting Scheme : " + getRequestingScheme());
				
				logger.exiting(sourceClass, sourceMethod);
				return p;
			}
        }); 
	}
	
	public static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		// windows
		return (os.indexOf("win") >= 0);
	}
}
