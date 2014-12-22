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

public class InegiTest {
	
	@BeforeClass
	public static void setUp() throws Exception {
	}

	@Test
	public void testGetDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("INEGI", "DF_STEI");
		assertNotNull("Null key family for DF_STEI", keyF);
		assertEquals("Wrong Key Family", "DSD_STEI", keyF.getId());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("INEGI", "DF_STEI");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("DF_STEI");
		assertEquals("Wrong description for DF_STEI", "INEGI,DF_STEI,1.0 ; Dataflow Short Term Economic Indicators", descr);
	}

	@Test
	public void testGetDimensionsAndCodes() throws SdmxException {
		Map<String, String> codes = SdmxClientHandler.getCodes("INEGI", "DF_STEI", "REF_AREA");
		assertNotNull("Null getCodes result", codes);
		assertEquals("Wrong code for REF_AREA MEX",  "Mexico", codes.get("MX"));
		List<Dimension> dim = SdmxClientHandler.getDimensions("INEGI", "DF_STEI");
		assertNotNull("Null getDimensions result DF_STEI", dim);
		String result = "[Dimension [id=REF_AREA, position=1, codelist=Codelist [id=INEGI/CL_AREA, codes={TL=Timor-Leste, TK=Tokelau, TJ=Tajikistan, TH=Thailand, TG=Togo, TF=French Southern Territories, GY=Guyana, TD=Chad,";
		assertEquals("Wrong dimensions for DF_STEI", result, dim.toString().substring(0, result.length()));
	}
	
	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries> res = SdmxClientHandler.getTimeSeries("INEGI", "DF_STEI/..C1161+C1162+C5004.....", "1980", "2010");
		assertNotNull("Null time series result", res);
		//warning: they depend on eventual order
		String annual = res.get(0).getName();
		assertEquals("Wrong name for first time series", "DF_STEI.MX.M.C1161.N.29.Z.Z.2003", annual);
		String start = res.get(0).getTimeSlots().get(0);
		assertEquals("Wrong start date for time series", "2001-01", start);
		//System.out.println(res);
	}

}
