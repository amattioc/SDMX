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
package it.bankitalia.reri.sia.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JFrame;

/**
 * @author Attilio Mattiocco
 *
 */
public class Configuration {
	private static final String sourceClass = Configuration.class.getSimpleName();
	
	public static final String CONFIGURATION_FILE = "configuration.properties";
	private static final String CENTRAL_CONFIGURATION_FILE_UX = "/home/opt/dev_dms/configuration.properties";
	private static final String CENTRAL_CONFIGURATION_FILE_WIN = "D:/Dati/configuration.properties";
	private static final String CENTRAL_CONFIGURATION_FILE_PROP = "SDMX_CONF";

	private static final String PROXY_NAME = "http.proxy.name";
		
	private static final String HTTP_AUTH_USER = "http.auth.user";
	private static final String PROXY_AUTH_PW = "http.auth.pw";  

	public static final String USAGE_STATISTICS_FILE = "usage.stats.file";  
	public static final String EXDI_DELAY = "exdi.delay";  
	public static final String REVERSE_DUMP = "reverse.dump";  

	private static Properties props = new Properties();

	protected static final String LOGGER_NAME = "SDMX";
	protected static final String PROXY_AUTH_KERBEROS = "Kerberos";;
	protected static final String PROXY_AUTH_DIGEST = "digest";;
	protected static final String PROXY_AUTH_BASIC = "basic";;
	protected static final String JAVA_SECURITY_KERBEROS_CONF = "java.security.krb5.conf";	
	protected static final String JAVA_SECURITY_AUTH_LOGIN_CONFIG = "java.security.auth.login.config"; 
	protected static final String HTTP_AUTH_PREF = "http.auth.preference";
	protected static final String SSL_DISABLE_CERT_CHECK = "ssl.disable.cert.check";  
	protected static final String SSL_TRUSTSTORE = "javax.net.ssl.trustStore";

	protected static Logger SDMX_LOGGER = null;

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
	
	public static boolean isSSLCertificatesDisabled(){
		return props.getProperty(SSL_DISABLE_CERT_CHECK, "FALSE").equalsIgnoreCase("TRUE");
	}

	public static boolean isReverse(){
		return props.getProperty(REVERSE_DUMP, "TRUE").equalsIgnoreCase("TRUE");
	}

	public static String getStatsFile(){
		return props.getProperty(USAGE_STATISTICS_FILE, null);
	}
	
	public static long getDelay() {
		String delay = props.getProperty(EXDI_DELAY, null);
		if(delay==null){
			return 0;
		}
		return Integer.parseInt(delay);
	}

	public static void init() {
		
		//normal configuration steps:
		// 1 init logger
		// 2 search configuration in this order: local file, global file, BIConfiguration class (only for internal deployments)
		// 3 if none is found, apply defaults: no proxy and INFO Logger
		setSdmxLogger();
		//Hack for Matlab: to avoid being considered a browser
		System.setProperty("http.agent", "");
		
		//get central configuration file location
		String central = System.getenv(CENTRAL_CONFIGURATION_FILE_PROP);
		if(central == null){
			if(isWindows())
				central = CENTRAL_CONFIGURATION_FILE_WIN;
			else
				central = CENTRAL_CONFIGURATION_FILE_UX;
		}
		
		try {
			boolean localConfigurationFound = false;
			InputStream is1 = null;
			InputStream is2 = null;
			try {
				is1 = new FileInputStream(CONFIGURATION_FILE);
				is2 = new FileInputStream(CONFIGURATION_FILE);
				System.err.println("Using local configuration.");
				localConfigurationFound = true;
			} catch (Exception e) {
				SDMX_LOGGER.fine("No local configuration file.");
			}
			if(!localConfigurationFound){
				is1 = new FileInputStream(central);
				is2 = new FileInputStream(central);
				System.err.println("Using central configuration: " + central);
			}
			
			init(is1, is2);
			
		} catch (Exception e) {
			// switch to default configuration, if it exists
			try {
				Class<?> clazz = Class.forName("sdmx.SdmxConfiguration");
				Method method = clazz.getMethod("initBI");
				method.invoke((Object)null);
				System.err.println("Using SdmxConfiguration class.");
			} catch( Exception notFound ) {
				//notFound.printStackTrace();
				System.err.println("No Configuration has been found. Apply defaults.");
				ConsoleHandler handler = new ConsoleHandler();
				handler.setLevel(Level.ALL);
				SDMX_LOGGER.addHandler(handler);
			}
		}
	}
	
	public static void init(InputStream is1, InputStream is2) throws SecurityException, IOException {
		setSdmxLogger();
		LogManager.getLogManager().readConfiguration(is1);
		is1.close();
		props.load(is2);
		is2.close();
		
		//configure SSL
		String tStore = props.getProperty(SSL_TRUSTSTORE);
		if(tStore != null && !tStore.isEmpty()){
			System.setProperty(SSL_TRUSTSTORE, tStore);
		}
		
		configureProxy(props);

	}

	private static void configureProxy(Properties props){
		final String sourceMethod = "configureProxy";
		Logger logger = SDMX_LOGGER;
		logger.entering(sourceClass, sourceMethod);
		
		SdmxProxySelector proxySelector = new SdmxProxySelector();
		
		for (int i = 0; ; i++) {
			//property: http.proxy.name_n
			String proxy = props.getProperty(PROXY_NAME + i);
			if (proxy != null){
				String[] toks = proxy.split(":");
				if(toks.length != 2  || toks[0] == null || toks[0].isEmpty() || toks[1] == null || toks[1].isEmpty()){
					throw new IllegalArgumentException("Proxy settings must be valid. host: '" + toks[0] + "', port: '" + toks[1] + "'");
				}
				//property: http.proxy.name_n.list
				String urls = props.getProperty(PROXY_NAME + i + ".urls");
				if(urls != null && !urls.isEmpty()){
					String[] urlList = urls.split(",");
					proxySelector.addProxy(toks[0], toks[1], urlList);
					logger.finer("Proxy has been configured: '" + proxy + "' for " + urls);				
				}
			}
			else{
				break;
			}
		}
		
        ProxySelector.setDefault(proxySelector);

		if(props != null){
			//get authentication preferences
			String proxyAuth = props.getProperty(HTTP_AUTH_PREF);
			if(proxyAuth != null){ 
				proxyAuth = proxyAuth.trim();
				System.setProperty(HTTP_AUTH_PREF, proxyAuth);
				logger.finer(proxyAuth + " authentication enabled.");
				
				if(proxyAuth.equalsIgnoreCase(PROXY_AUTH_KERBEROS)){
					//set properties for JAAS
					String conf = props.getProperty(JAVA_SECURITY_KERBEROS_CONF);
					String login = props.getProperty(JAVA_SECURITY_AUTH_LOGIN_CONFIG);
			    	String krbccname = System.getenv().get("KRB5CCNAME");
		
			    	if(krbccname != null && login != null && conf != null){
			    		krbccname = krbccname.trim();
			    		login = login.trim();
			    		conf = conf.trim();
			    		System.setProperty("user.krb5cc", krbccname);
				    	System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
				    	System.setProperty(JAVA_SECURITY_KERBEROS_CONF, conf);
				    	System.setProperty(JAVA_SECURITY_AUTH_LOGIN_CONFIG, login);	    	
				    	logger.finer(JAVA_SECURITY_KERBEROS_CONF + " = " + conf);
			    		logger.finer(JAVA_SECURITY_AUTH_LOGIN_CONFIG + " = " + login);
			    		logger.finer("Environment variable KRB5CCNAME = " + krbccname);
			    	}
			    	else{
			    		logger.warning("Kerberos ticket cache not configured because one of the parameters is not set.");
			    		logger.warning(JAVA_SECURITY_KERBEROS_CONF + " = " + conf);
			    		logger.warning(JAVA_SECURITY_AUTH_LOGIN_CONFIG + " = " + login);
			    		logger.warning("Environment variable KRB5CCNAME = " + krbccname);
			    	}   	    	
				}
				else if(proxyAuth.equalsIgnoreCase(PROXY_AUTH_BASIC)){
					String username = props.getProperty(HTTP_AUTH_USER);
					String password = props.getProperty(PROXY_AUTH_PW);
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
		System.setProperty(HTTP_AUTH_PREF, scheme);
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
				
				logger.entering(sourceClass, sourceMethod);
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
