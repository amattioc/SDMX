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

	public void addDefaultProxy(String url){
		final String sourceMethod = "addProxy";
		logger.entering(sourceClass, sourceMethod);
		Proxy p = proxyTable.get(url);
		//add url to default proxy only if proxy not explicitly set at init time
		if(p == null){
			logger.finer("Proxy has been added: '" + defaultProxy.address().toString() + "' for " + url);		
			proxyTable.put(url, defaultProxy);
		}
		logger.exiting(sourceClass, sourceMethod);
	}
	
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
					logger.finer("Proxy has been added: '" + p.address().toString() + "' for " + url);				
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
		List<Proxy> res = null;
		logger.entering(sourceClass, sourceMethod);
		String target = arg0.getHost();
		logger.finer("Getting proxy for host: " + target);	
		Proxy p = proxyTable.get(target);
		if(p != null){
			res = new ArrayList<Proxy>();
			res.add(p);
		}
		else{
			res = new ArrayList<Proxy>();
			res.add(Proxy.NO_PROXY);
		}
		logger.finer("proxy: " + res);
		logger.exiting(sourceClass, sourceMethod);
		return res;
	}

}
