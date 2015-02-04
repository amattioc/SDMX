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
	
	private ArrayList<Proxy> defaultProxyTable = null;
	private Hashtable<String, Proxy> proxyTable = null;

	private static final String sourceClass = SdmxProxySelector.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();

	public SdmxProxySelector(String defaultProxy, int port){
		final String sourceMethod = "SdmxProxySelector";
		logger.entering(sourceClass, sourceMethod);

		defaultProxyTable = new ArrayList<Proxy>();
		Proxy p = null;
		if(defaultProxy != null && !defaultProxy.isEmpty()){
			p = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(defaultProxy,port));
		}
		else{
			p = Proxy.NO_PROXY;
		}
		defaultProxyTable.add(p);
		
		proxyTable = new Hashtable<String, Proxy>();
		
		logger.exiting(sourceClass, sourceMethod);
	}
	
	public void addProxy(String host, String port, String[] urls){
		final String sourceMethod = "addProxy";
		logger.entering(sourceClass, sourceMethod);
		if(host != null && port != null && !host.isEmpty() && !port.isEmpty()){
			Proxy p = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(host.trim(),Integer.parseInt(port.trim())));
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
		List<Proxy> res = defaultProxyTable;
		logger.entering(sourceClass, sourceMethod);
		logger.finer("Getting proxy for host: " + arg0.getHost());	
		Proxy p = proxyTable.get(arg0.getHost());
		if(p != null){
			res = new ArrayList<Proxy>();
			res.add(p);
		}
		logger.finer("proxy: " + res);
		logger.exiting(sourceClass, sourceMethod);
		return res;
	}

}
