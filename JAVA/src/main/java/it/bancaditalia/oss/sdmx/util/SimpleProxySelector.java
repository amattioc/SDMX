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
import java.util.List;

/**
 * @author Attilio Mattiocco
 *
 */
public class SimpleProxySelector extends ProxySelector
{
	private Proxy				defaultProxy	= Proxy.NO_PROXY;

	public SimpleProxySelector(String defaultProxyHost, int defaultProxyPort)
	{
		if (defaultProxyHost != null && !defaultProxyHost.isEmpty())
		{
			defaultProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(defaultProxyHost, defaultProxyPort));
		}
	}

	@Override
	public void connectFailed(URI arg0, SocketAddress arg1, IOException arg2)
	{
		// Do nothing
	}

	@Override
	public List<Proxy> select(URI targetURI)
	{
		List<Proxy> res = new ArrayList<>();
		res.add(defaultProxy);
		return res;
	}

}
