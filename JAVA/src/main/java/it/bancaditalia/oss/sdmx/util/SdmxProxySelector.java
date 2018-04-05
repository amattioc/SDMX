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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Attilio Mattiocco
 *
 */
// This proxy selector simulates prvides for an explicit or default proxy handling. If not 
// explicitly configured, the default proxy is Proxy.NO_PROXY (DIRECT). See alse: the ClientFactory.createClient. 
// This type of selection startegy is necessary for handling authenticating proxies, that often redirect to themselves 
public class SdmxProxySelector extends ProxySelector{
	private final String NO_PROXY = "NOPROXY";
	private Proxy defaultProxy = Proxy.NO_PROXY;
	private Map<String, Proxy> proxyTable = null;

	private static final String sourceClass = SdmxProxySelector.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();

	public SdmxProxySelector(String defaultProxyHost, int defaultProxyPort){
		final String sourceMethod = "SdmxProxySelector";
		logger.entering(sourceClass, sourceMethod);

		if(defaultProxyHost != null && !defaultProxyHost.isEmpty() && !defaultProxyHost.equalsIgnoreCase(NO_PROXY)){
			defaultProxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(defaultProxyHost,defaultProxyPort));
		}
		
		proxyTable = new HashMap<>();
		
		logger.exiting(sourceClass, sourceMethod);
	}

	// the host will be configured for using the default proxy (or DIRECT if not configured)
	public void addToDefaultProxy(String url){
		final String sourceMethod = "addProxy";
		logger.entering(sourceClass, sourceMethod);
		Proxy p = proxyTable.get(url);
		//add url to default proxy only if proxy not explicitly set at init time
		if(p == null){
			logger.finer("Default proxy has been added for " + url);		
			proxyTable.put(url, defaultProxy);
		}
		logger.exiting(sourceClass, sourceMethod);
	}
	
	//add a proxy entry for the given URLs 
	public void addProxy(String proxyHost, String proxyPort, String... hosts)
	{
		final String sourceMethod = "addProxy";
		logger.entering(sourceClass, sourceMethod);
		Proxy p = null;
		if(proxyHost != null && proxyPort != null && !proxyHost.isEmpty() && !proxyPort.isEmpty())
		{
			if(!proxyHost.equalsIgnoreCase(NO_PROXY))
				p = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(proxyHost.trim(),Integer.parseInt(proxyPort.trim())));
			else
				p = Proxy.NO_PROXY;

			for (String host: hosts)
				if(host != null && !host.isEmpty())
				{
					proxyTable.put(host, p);
					logger.finer("Proxy has been added: '" + p + "' for " + host);				
				}
		}
		else
			throw new IllegalArgumentException("Proxy settings must be valid. host: '" + proxyHost + "', port: '" + proxyPort + "'");

		logger.exiting(sourceClass, sourceMethod);

	}
	
	@Override
	public void connectFailed(URI arg0, SocketAddress arg1, IOException arg2) {
		final String sourceMethod = "connectFailed";
		logger.entering(sourceClass, sourceMethod);
		logger.warning("FAILED PROXY CALL to URI: " + arg0 + ", socket: " + arg1);	
		logger.exiting(sourceClass, sourceMethod);
	}

	@Override
	public List<Proxy> select(URI targetURI) {
		final String sourceMethod = "select";
		List<Proxy> res = new ArrayList<>();
		logger.entering(sourceClass, sourceMethod);
		String targetHost = targetURI.getHost();
		if(targetHost != null)
		{
			logger.finer("Getting proxy for host: " + targetHost);	
			Proxy proxy = proxyTable.get(targetHost);
			//if no proxy has been found in the table for this URL, then go DIRECT
			//it's important to behave this way for avoiding problems with the internal infrastructure
			//like LDAP that needs a direct connection
			res.add(proxy != null ? proxy : Proxy.NO_PROXY);
			logger.finer("proxy: " + res);
		}
		else{
			logger.warning("No host component found for " + targetURI);
			res.add(Proxy.NO_PROXY);
		}
		logger.exiting(sourceClass, sourceMethod);
		return res;
	}

	public Proxy addUrlHostToProxy(URL target, Proxy proxy)
	{
		return proxyTable.put(target.getHost(), proxy);
	}

}
