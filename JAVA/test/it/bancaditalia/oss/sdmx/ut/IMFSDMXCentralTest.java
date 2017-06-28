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
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class IMFSDMXCentralTest {
	@Test
	public void testGetDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("IMF_SDMX_CENTRAL", "SPI");
		assertNotNull("Null key family for SPI", keyF);
		assertEquals("Wrong Key Family", "ECOFIN_DSD", keyF.getId());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("IMF_SDMX_CENTRAL", "SPI");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("SPI");
		assertEquals("Wrong description for SPI", "Stock Market: Share Price Index", descr);
	}

	@Test
	public void testGetDimensions() throws SdmxException {
		List<Dimension> dim = SdmxClientHandler.getDimensions("IMF_SDMX_CENTRAL", "SPI");
		assertNotNull("Null getDimensions result SPI", dim);
		String result = "[Dimension [id=DATA_DOMAIN, position=1, codelist=Codelist [id=IMF/CL_DATADOMAIN/1.0, codes=null]]";
		assertEquals("Wrong dimensions for SPI", result, dim.toString().substring(0, result.length()));
	}
	
	@Test
	public void testGetCodes() throws SdmxException {
			Map<String, String> codes = SdmxClientHandler.getCodes("IMF_SDMX_CENTRAL", "SPI", "FREQ");
			assertNotNull("Null getCodes result", codes);
			assertEquals("Wrong code for FREQ annual", codes.get("A"), "Annual");
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries> res = SdmxClientHandler.getTimeSeries("IMF_SDMX_CENTRAL", "SPI.SPI.FR.FPE_IX._Z.D", "2008", null);
		assertNotNull("Null time series result", res);
		//warning: they depend on eventual order
		String annual = res.get(0).getName();
		assertEquals("Wrong name for first time series", "SPI.SPI.FR.FPE_IX._Z.D", annual);
		String start = res.get(0).getTimeSlots().get(0);
		assertEquals("Wrong start date for time series", "2015-02-02", start);
		//System.out.println(res);
	}

}
