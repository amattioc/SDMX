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

public class UNDATATest {
	@Test
	public void getDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("UNDATA", "DF_UNDATA_COUNTRYDATA");
		assertNotNull("Null key family for DF_UNDATA_COUNTRYDATA", keyF);
		assertEquals("Wrong Key Family", "CountryData", keyF.getId());
		assertEquals("Wrong agency", "UNSD", keyF.getAgency());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("UNDATA", null);
		assertNotNull("Null getFlows result", f);
		String descr = f.get("DF_UNDATA_COUNTRYDATA");
		assertEquals("Wrong description for DF_UNDATA_COUNTRYDATA", "SDMX-CountryData", descr);
	}

	@Test
	public void testGetDimensionsAndCodes() throws SdmxException {
		Map<String, String> codes = SdmxClientHandler.getCodes("UNDATA", "DF_UNDATA_COUNTRYDATA", "FREQ");
		assertNotNull("Null getCodes result", codes);
		assertEquals("Wrong code for FREQ annual", codes.get("A"), "Annual");
		List<Dimension> dim = SdmxClientHandler.getDimensions("UNDATA", "DF_UNDATA_COUNTRYDATA");
		assertNotNull("Null getDimensions result", dim);
		String result = "[Dimension [id=FREQ, position=1, codelist=Codelist [id=IAEG/CL_FREQ_MDG/1.0, codes={A=Annual, 2A=Two-year average, 3A=Three-year average, S=Half-yearly, semester, Q=Quarterly, M=Monthly}]], Dimension [id=SERIES, position=2, codelist=Codelist [id=UNSD/CL_SERIES_COUNTRY_DATA/0.9, codes=null]], Dimension [id=UNIT, position=3, codelist=Codelist [id=UNSD";
		assertEquals("Wrong dimensions for DF_UNDATA_COUNTRYDATA", result, dim.toString().substring(0, result.length()));
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries>  res = SdmxClientHandler.getTimeSeries("UNDATA", "DF_UNDATA_COUNTRYDATA/A...U....", "2010", "2015");
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 171, res.size());
	}

}
