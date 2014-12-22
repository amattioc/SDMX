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

package it.bankitalia.reri.sia.sdmx.ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import it.bankitalia.reri.sia.sdmx.api.DSDIdentifier;
import it.bankitalia.reri.sia.sdmx.api.Dimension;
import it.bankitalia.reri.sia.sdmx.api.PortableTimeSeries;
import it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler;
import it.bankitalia.reri.sia.util.SdmxException;

import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.junit.BeforeClass;
import org.junit.Test;

public class ECBTest {
	@BeforeClass
	public static void setUp() throws Exception {
	}

	@Test
	public void getDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("ECB", "EXR");
		assertNotNull("Null key family for EXR", keyF);
		assertEquals("Wrong Key Family", "ECB_EXR1", keyF.getId());
		assertEquals("Wrong agency", "ECB", keyF.getAgency());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("ECB", "*Exchange*");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("EXR");
		assertEquals("Wrong description for EXR", "ECB,EXR,1.0 ; Exchange Rates", descr);
	}

	@Test
	public void testGetDimensionsAndCodes() throws SdmxException {
		Map<String, String> codes = SdmxClientHandler.getCodes("ECB", "EXR", "FREQ");
		assertNotNull("Null getCodes result", codes);
		assertEquals("Wrong code for FREQ annual", codes.get("A"), "Annual");
		List<Dimension> dim = SdmxClientHandler.getDimensions("ECB", "EXR");
		assertNotNull("Null getDimensions result", dim);
		String result = "[Dimension [id=FREQ, position=1, codelist=Codelist [id=ECB/CL_FREQ/1.0, codes={D=Daily, B=Business, A=Annual, W=Weekly, S=Half Yearly, semester (value H exists but change to S in 2009, move from H to this new value to be agreed in ESCB context), Q=Quarterly, N=Minutely, M=Monthly, H=Half-yearly, E=Event (not supported)}";
		assertEquals("Wrong dimensions for EXR", result, dim.toString().substring(0, result.length()));
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries>  res = SdmxClientHandler.getTimeSeries("ECB", "EXR/.GBP+USD.EUR.SP00.A", null, null);
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 10, res.size());
		res = SdmxClientHandler.getTimeSeries("ECB", "EXR.*.USD|GBP.EUR.SP00.A", null, null);
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 10, res.size());
		res = SdmxClientHandler.getTimeSeries("ECB", "EXR.A.USD.EUR.SP00.A;EXR.M.USD.EUR.SP00.A", null, null);
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 2, res.size());
	}

}
