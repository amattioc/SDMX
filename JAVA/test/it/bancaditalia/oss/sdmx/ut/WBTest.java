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

import java.util.List;
import java.util.Map;

import org.junit.Test;

import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.client.custom.WB;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

public class WBTest {
	@Test
	public void testGetDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier(WB.class.getSimpleName(), "WDI");
		assertNotNull("Null key family for WDI", keyF);
		assertEquals("Wrong Key Family", "WDI", keyF.getId());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows(WB.class.getSimpleName(), "WDI");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("WDI");
		assertEquals("Wrong description for WDI", "World Development Indicators", descr);
	}

	@Test
	public void testGetDimensions() throws SdmxException {
		List<Dimension> dim = SdmxClientHandler.getDimensions(WB.class.getSimpleName(), "WDI");
		assertNotNull("Null getDimensions result WDI", dim);
		String result = "";
		assertEquals("Wrong dimensions for WDI", result, dim.toString().substring(0, result.length()));
	}
	
	@Test
	public void testGetCodes() throws SdmxException {
			Map<String, String> codes = SdmxClientHandler.getCodes(WB.class.getSimpleName(), "WDI", "REF_AREA");
			assertNotNull("Null getCodes result", codes);
			assertEquals("Wrong code for WDI annual", codes.get("GRL"), "Greenland");
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries> res = SdmxClientHandler.getTimeSeries(WB.class.getSimpleName(), "WDI.A.SP_POP_TOTL.US", "2000", "2010");
		assertNotNull("Null time series result", res);
		String annual = res.get(0).getName();
		assertEquals("Wrong name for first time series", "WDI.A.SP_POP_TOTL.US", annual);
		String start = res.get(0).getTimeSlots().get(0);
		assertEquals("Wrong start date for time series", "2000", start);
	}

}
