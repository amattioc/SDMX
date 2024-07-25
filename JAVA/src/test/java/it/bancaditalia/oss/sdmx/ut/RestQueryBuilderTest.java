/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package it.bancaditalia.oss.sdmx.ut;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;

/**
 *
 * @author Philippe Charles
 */
public class RestQueryBuilderTest
{
	private static class RestQueryBuilderC extends RestQueryBuilder<RestQueryBuilderC>
	{
		private RestQueryBuilderC(URI entryPoint)
		{
			super(entryPoint);
		}
	}
		
	@Test
	public void test() throws SdmxException, URISyntaxException, MalformedURLException
	{
		URI entryPoint = new URI("http://ws-entry-point");
		
		Assert.assertEquals(entryPoint, new RestQueryBuilderC(entryPoint).build().toURI());
		Assert.assertEquals(new URL("http://ws-entry-point/hello/world"), new RestQueryBuilderC(entryPoint).addPath("hello").addPath("world").build());
		Assert.assertEquals(new URL("http://ws-entry-point?k1=v1&k2=v2"), new RestQueryBuilderC(entryPoint).withParam("k1", "v1").withParam("k2", "v2").build());
	}
}
