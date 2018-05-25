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

package it.bancaditalia.oss.sdmx.ut;

import static org.junit.Assert.assertEquals;
import it.bancaditalia.oss.sdmx.util.SdmxProxySelector;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.Test;

public class ProxyTest
{
	@Test
	public void testProxyConfiguration() throws URISyntaxException
	{
		SdmxProxySelector noDefault = new SdmxProxySelector(null, 0);
		noDefault.addToDefaultProxy("toDefault.domain.com");
		noDefault.addProxy("proxy1.domain.com", "8080", new String[] { "toProxy1.domain.com" });
		ArrayList<Proxy> result = new ArrayList<Proxy>();
		result.add(Proxy.NO_PROXY);
		assertEquals(result, noDefault.select(new URI("http://testnoproxy.domain.com")));
		assertEquals(result, noDefault.select(new URI("http://toDefault.domain.com")));
		result = new ArrayList<Proxy>();
		result.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy1.domain.com", 8080)));
		assertEquals(result, noDefault.select(new URI("http://toProxy1.domain.com")));

		SdmxProxySelector testDefault = new SdmxProxySelector("defaultproxy.domain.com", 8080);
		testDefault.addToDefaultProxy("toDefault.domain.com");
		testDefault.addProxy("proxy1.domain.com", "8080", new String[] { "toProxy1.domain.com" });
		result = new ArrayList<Proxy>();
		result.add(Proxy.NO_PROXY);
		assertEquals(result, testDefault.select(new URI("http://testnoproxy.domain.com")));
		result = new ArrayList<Proxy>();
		result.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("defaultproxy.domain.com", 8080)));
		assertEquals(result, testDefault.select(new URI("http://toDefault.domain.com")));
		result = new ArrayList<Proxy>();
		result.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy1.domain.com", 8080)));
		assertEquals(result, testDefault.select(new URI("http://toProxy1.domain.com")));

	}
}
