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

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

public class ECBTest {
	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("ECB", "*Exchange*");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("EXR");
		assertEquals("Wrong description for EXR", "Exchange Rates", descr);
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries<Double>>  res = SdmxClientHandler.getTimeSeries("ECB", "EXR/.GBP+USD.EUR.SP00.A", null, null);
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 10, res.size());
		res = SdmxClientHandler.getTimeSeries("ECB", "EXR.*.USD|GBP.EUR.SP00.A", "2000", "2010");
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 10, res.size());
		res = SdmxClientHandler.getTimeSeries("ECB", "EXR.A.USD.EUR.SP00.A;EXR.M.USD.EUR.SP00.A", "2000", "2010");
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 2, res.size());
	}

}
