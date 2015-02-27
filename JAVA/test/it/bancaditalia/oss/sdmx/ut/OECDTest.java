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
import it.bancaditalia.oss.sdmx.client.custom.OECD;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class OECDTest {
	@BeforeClass
	public static void setUp() throws Exception {
	}

	@Test
	public void testGetDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier(OECD.class.getSimpleName(), "QNA");
		assertNotNull("Null key family for QNA", keyF);
		assertEquals("Wrong Key Family", "QNA", keyF.getId());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows(OECD.class.getSimpleName(), "AEO");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("AEO");
		assertEquals("Wrong description for AEO", "OECD,African Economic Outlook", descr);
	}

	@Test
	public void testGetDimensions() throws SdmxException {
		List<Dimension> dim = SdmxClientHandler.getDimensions(OECD.class.getSimpleName(), "QNA");
		assertNotNull("Null getDimensions result QNA", dim);
		String result = "[Dimension [id=LOCATION, position=1, codelist=Codelist [id=OECD/CL_QNA_LOCATION, codes={CHE=Switzerland, OECDE=O";
		assertEquals("Wrong dimensions for QNA", result, dim.toString().substring(0, result.length()));
	}
	
	@Test
	public void testGetCodes() throws SdmxException {
			Map<String, String> codes = SdmxClientHandler.getCodes(OECD.class.getSimpleName(), "QNA", "FREQUENCY");
			assertNotNull("Null getCodes result", codes);
			assertEquals("Wrong code for FREQUENCY annual", codes.get("A"), "Annual");
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries> res = SdmxClientHandler.getTimeSeries(OECD.class.getSimpleName(), "QNA.ITA.B1_GE.CARSA.Q", "2000", "2010");
		assertNotNull("Null time series result", res);
		String annual = res.get(0).getName();
		assertEquals("Wrong name for first time series", "QNA.ITA.B1_GE.CARSA.Q", annual);
		String start = res.get(0).getTimeSlots().get(0);
		assertEquals("Wrong start date for time series", "2000-Q1", start);
	}

}
