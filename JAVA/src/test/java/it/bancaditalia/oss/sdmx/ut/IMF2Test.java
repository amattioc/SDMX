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
import it.bancaditalia.oss.sdmx.client.custom.IMF2;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

public class IMF2Test {
	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows(IMF2.class.getSimpleName(), "DS-WHDREO");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("DS-WHDREO");
		assertEquals("Wrong description for DS-WHDREO", "Western Hemisphere Regional Economic Outlook (WHDREO)", descr);
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries<Double>> res = SdmxClientHandler.getTimeSeries(IMF2.class.getSimpleName(), "DS-WHDREO.A.PA.GGXCNL_GDP", "2000", "2015");
		assertNotNull("Null time series result", res);
		//warning: they depend on eventual order
		String annual = res.get(0).getName();
		assertEquals("Wrong name for first time series", "DS-WHDREO.A.PA.GGXCNL_GDP", annual);
		String start = res.get(0).get(0).getTimeslot();
		assertEquals("Wrong start date for time series", "2009", start);
		//System.out.println(res);
	}

}
