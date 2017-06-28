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

public class WITSTest {
	@Test
	public void getDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("WITS", "DF_WITS_Tariff_TRAINS");
		assertNotNull("Null key family for DF_WITS_Tariff_TRAINS", keyF);
		assertEquals("Wrong Key Family", "TARIFF_TRAINS", keyF.getId());
		assertEquals("Wrong agency", "WBG_WITS", keyF.getAgency());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("WITS", "*Tariff*");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("DF_WITS_Tariff_TRAINS");
		assertEquals("Wrong description for DF_WITS_Tariff_TRAINS", "WITS - UNCTAD TRAINS Tariff Data", descr);
	}

	@Test
	public void testGetDimensionsAndCodes() throws SdmxException {
		Map<String, String> codes = SdmxClientHandler.getCodes("WITS", "DF_WITS_Tariff_TRAINS", "FREQ");
		assertNotNull("Null getCodes result", codes);
		assertEquals("Wrong code for FREQ annual", codes.get("A"), "Annual");
		List<Dimension> dim = SdmxClientHandler.getDimensions("WITS", "DF_WITS_Tariff_TRAINS");
		assertNotNull("Null getDimensions result", dim);
		String result = "[Dimension [id=FREQ, position=1, codelist=Codelist [id=WBG_WITS/CL_FREQ_WITS/1.0, codes={A=Annual}]]";
		assertEquals("Wrong dimensions for EXR", result, dim.toString().substring(0, result.length()));
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries>  res = SdmxClientHandler.getTimeSeries("WITS", "DF_WITS_Tariff_TRAINS/A.076.000.250490.AVEEstimated", null, null);
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 1, res.size());
		res = SdmxClientHandler.getTimeSeries("WITS", "DF_WITS_Tariff_TRAINS/A.068+076.000.250490.Reported", "2000", "2010");
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 2, res.size());
	}

}
