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
import static org.junit.Assert.assertNotNull;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.client.custom.IMF;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class IMFTest {
	@BeforeClass
	public static void setUp() throws Exception {
	}

	@Test
	public void testGetDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier(IMF.class.getSimpleName(), "PGI");
		assertNotNull("Null key family for PGI", keyF);
		assertEquals("Wrong Key Family", "PGI", keyF.getId());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows(IMF.class.getSimpleName(), "PGI");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("PGI");
		assertEquals("Wrong description for PGI", "IMF,PGI ; Principal Global Indicators", descr);
	}

	@Test
	public void testGetDimensions() throws SdmxException {
		List<Dimension> dim = SdmxClientHandler.getDimensions(IMF.class.getSimpleName(), "PGI");
		assertNotNull("Null getDimensions result PGI", dim);
		String result = "[Dimension [id=REF_AREA, position=1, codelist=Codelist [id=IMF/CL_AREA, codes={JP=Japan, DK=Denmark, DE=Germany, PL=Poland, GB=United Kingdom, SG=Singapore, SE=Sweden, SA=Saudi Arabia, A1=World, FR=France, IT=Italy";
		assertEquals("Wrong dimensions for PGI", result, dim.toString().substring(0, result.length()));
	}
	
	@Test
	public void testGetCodes() throws SdmxException {
			Map<String, String> codes = SdmxClientHandler.getCodes(IMF.class.getSimpleName(), "PGI", "FREQ");
			assertNotNull("Null getCodes result", codes);
			assertEquals("Wrong code for FREQ annual", codes.get("A"), "Annual");
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries> res = SdmxClientHandler.getTimeSeries(IMF.class.getSimpleName(), "PGI.US.FAA.PGI.PCOCY.A", "1980", "2010");
		assertNotNull("Null time series result", res);
		//warning: they depend on eventual order
		String annual = res.get(0).getName();
		assertEquals("Wrong name for first time series", "PGI.US.FAA.PGI.PCOCY.A", annual);
		String start = res.get(0).getTimeSlots().get(0);
		assertEquals("Wrong start date for time series", "2002", start);
		//System.out.println(res);
	}

}
