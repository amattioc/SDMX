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
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class INSEETest {
	@BeforeClass
	public static void setUp() throws Exception {
	}

	@Test
	public void getDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("INSEE", "IPI-1970-DET");
		assertNotNull("Null key family for IPI-1970-DET", keyF);
		assertEquals("Wrong Key Family", "IPI-1970-DET", keyF.getId());
		assertEquals("Wrong agency", "FR1", keyF.getAgency());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("INSEE", null);
		assertNotNull("Null getFlows result", f);
		String descr = f.get("IPI-1970-DET");
		assertEquals("Wrong description for IPI-1970-DET", "Industrial production index (base 1970) - Main branches  - Stopped series", descr);
	}

	@Test
	public void testGetDimensionsAndCodes() throws SdmxException {
		Map<String, String> codes = SdmxClientHandler.getCodes("INSEE", "IPI-1970-DET", "FREQ");
		assertNotNull("Null getCodes result", codes);
		assertEquals("Wrong code for FREQ annual", codes.get("A"), "Annual");
		List<Dimension> dim = SdmxClientHandler.getDimensions("INSEE", "IPI-1970-DET");
		assertNotNull("Null getDimensions result", dim);
		String result = "[Dimension [id=FREQ, position=1, codelist=Codelist [id=FR1/CL_FREQ/1.0, codes={A=Annual, I=Irregular, T=Quarterly, S=Semi-annual, Q=Quarterly, B=Two-monthly, M=Monthly}]], Dimension [id=BRANCHE, position=2, codelist=Codelist [id=FR1/CL_NI00054/1.0, codes=null]], Dimension [id=NATURE, position=3, codelist=Codelist [id=FR1/CL_NATURE/1.0, codes=null]]]";
		assertEquals("Wrong dimensions for IPI-1970-DET", result, dim.toString().substring(0, result.length()));
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries>  res = SdmxClientHandler.getTimeSeries("INSEE", "IPI-1970-DET/.50.", null, null);
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 7, res.size());
	}

}
