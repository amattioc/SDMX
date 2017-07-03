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
package it.bancaditalia.oss.sdmx.util;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class RestQueryBuilderTest {

	@Test
	public void test() throws MalformedURLException {
		URL entryPoint = new URL("http://ws-entry-point");

		Assert.assertEquals(entryPoint, RestQueryBuilder.of(entryPoint).build(true));
		Assert.assertEquals(new URL("http://ws-entry-point/hello/world"), RestQueryBuilder.of(entryPoint).path("hello").path("world").build(true));
		Assert.assertEquals(new URL("http://ws-entry-point?k1=v1&k2=v2"), RestQueryBuilder.of(entryPoint).param("k1", "v1").param("k2", "v2").build(true));
	}
}
