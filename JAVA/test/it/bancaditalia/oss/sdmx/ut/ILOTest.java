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

import org.junit.BeforeClass;
import org.junit.Test;

public class ILOTest {
	@BeforeClass
	public static void setUp() throws Exception {
	}

	@Test
	public void getDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("ILO", "DF_YI_FRA_EMP_TEMP_SEX_AGE_NB");
		assertNotNull("Null key family for DF_YI_FRA_EMP_TEMP_SEX_AGE_NB", keyF);
		assertEquals("Wrong Key Family", "YI_FRA_EMP_TEMP_SEX_AGE_NB", keyF.getId());
		assertEquals("Wrong agency", "ILO", keyF.getAgency());
	}

//	@Test
//	public void testGetFlows() throws SdmxException {
//		Map<String, String> f = SdmxClientHandler.getFlows("ECB", "Exchange*");
//		assertNotNull("Null getFlows result", f);
//		String descr = f.get("EXR");
//		assertEquals("Wrong description for EXR", "Exchange Rates", descr);
//	}

	@Test
	public void testGetDimensionsAndCodes() throws SdmxException {
		Map<String, String> codes = SdmxClientHandler.getCodes("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB", "COLLECTION");
		assertNotNull("Null getCodes result", codes);
		assertEquals("Wrong code for COLLECTION KILM national data", codes.get("KILMNSO"), "KILM national data");
		List<Dimension> dim = SdmxClientHandler.getDimensions("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB");
		assertNotNull("Null getDimensions result", dim);
		String result = "[Dimension [id=COLLECTION, position=1, codelist=Codelist [id=ILO/CL_COLLECTION, codes={KILMNSO=KILM national data, CP=Country Profiles, MIG=International Labour Migration";
		assertEquals("Wrong dimensions for DF_YI_ALL_EMP_TEMP_SEX_AGE_NB", result,dim.toString().substring(0, result.length()));

	}
	
	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries>  res = SdmxClientHandler.getTimeSeries("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB/YI.MEX.A.463.EMP_TEMP_NB.SEX_F.AGE_10YRBANDS_TOTAL", null, null);
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 1, res.size());
	}

}
