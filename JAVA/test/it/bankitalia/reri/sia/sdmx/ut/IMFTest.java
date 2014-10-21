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
import it.bankitalia.reri.sia.sdmx.client.custom.IMF;
import it.bankitalia.reri.sia.util.SdmxException;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class IMFTest {
	//private static SdmxClientHandler handler= SdmxClientHandler.getInstance();
	
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
		assertEquals("Wrong description for PGI", "Principal Global Indicators", descr);
	}

	@Test
	public void testGetDimensions() throws SdmxException {
		List<Dimension> dim = SdmxClientHandler.getDimensions(IMF.class.getSimpleName(), "PGI");
		assertNotNull("Null getDimensions result PGI", dim);
		String result = "[Dimension [id=REF_AREA, position=1, codelist=IMF/CL_AREA], Dimension [id=PGI_CONCEPT, position=2, codelist=IMF/CL_PGI_CONCEPT], Dimension [id=DATASOURCE, position=3, codelist=IMF/CL_PGI_DATASOURCE], Dimension [id=UNITOFMEASURE, position=4, codelist=IMF/CL_PGI_UNITOFMEASURE], Dimension [id=FREQ, position=5, codelist=IMF/CL_FREQ]]";
		assertEquals("Wrong dimensions for PGI", result, dim.toString());
	}
	
	@Test
	public void testGetCodes() throws SdmxException {
			Map<String, String> codes = SdmxClientHandler.getCodes(IMF.class.getSimpleName(), "PGI", "FREQ");
			assertNotNull("Null getCodes result", codes);
			assertEquals("Wrong code for FREQ annual", codes.get("A"), "Annual");
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries> res = SdmxClientHandler.getTimeSeries(IMF.class.getSimpleName(), "PGI.US+JP+CN+GB+CA+FR.PCPI.IFS.PCOCY.A", "1980", "2010");
		assertNotNull("Null time series result", res);
		//warning: they depend on eventual order
		String annual = res.get(0).getName();
		assertEquals("Wrong name for first time series", "PGI.US.IFS.PCPI.A.PCOCY", annual);
		String start = res.get(0).getTimeSlots().get(0);
		assertEquals("Wrong start date for time series", "1980", start);
		//System.out.println(res);
	}

}
