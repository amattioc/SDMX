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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
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
	private Hashtable<String, Proxy> proxyTable = null;

	private static final String sourceClass = SdmxProxySelector.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();

	public SdmxProxySelector(String defaultProxyHost, int defaultProxyPort){
		final String sourceMethod = "SdmxProxySelector";
		logger.entering(sourceClass, sourceMethod);

		if(defaultProxyHost != null && !defaultProxyHost.isEmpty() && !defaultProxyHost.equalsIgnoreCase(NO_PROXY)){
			defaultProxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(defaultProxyHost,defaultProxyPort));
		}
		
		proxyTable = new Hashtable<String, Proxy>();
		
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
	public void addProxy(String host, String port, String[] urls){
		final String sourceMethod = "addProxy";
		logger.entering(sourceClass, sourceMethod);
		Proxy p = null;
		if(host != null && port != null && !host.isEmpty() && !port.isEmpty()){
			if(!host.equalsIgnoreCase(NO_PROXY)){
				p = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(host.trim(),Integer.parseInt(port.trim())));
			}
			else{
				p = Proxy.NO_PROXY;
			}
			for (int i = 0; i < urls.length; i++) {
				String url = urls[i].trim();
				if(url != null && !url.isEmpty()){
					proxyTable.put(url, p);
					logger.finer("Proxy has been added: '" + p + "' for " + url);				
				}
			}
		}
		else {
			throw new IllegalArgumentException("Proxy settings must be valid. host: '" + host + "', port: '" + port + "'");
		}

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
	public List<Proxy> select(URI arg0) {
		final String sourceMethod = "select";
		List<Proxy> res = new ArrayList<Proxy>();
		logger.entering(sourceClass, sourceMethod);
		String target = arg0.getHost();
		if(target != null){
			logger.finer("Getting proxy for host: " + target);	
			Proxy p = proxyTable.get(target);
			if(p != null){
				res.add(p);
			}
			else{
				//if no proxy has been found in the table for this URL, then go DIRECT
				res.add(Proxy.NO_PROXY);
			}
			logger.finer("proxy: " + res);
		}
		else{
			logger.warning("No host component found for " + arg0);
			res.add(Proxy.NO_PROXY);
		}
		logger.exiting(sourceClass, sourceMethod);
		return res;
	}

}
