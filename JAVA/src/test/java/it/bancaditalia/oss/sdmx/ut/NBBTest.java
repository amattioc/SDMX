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
import it.bancaditalia.oss.sdmx.client.custom.NBB;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

public class NBBTest {
	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows(NBB.class.getSimpleName(), "AFCSURV");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("AFCSURV");
		assertEquals("Wrong description for AFCSURV", "Quarterly survey on the assessment of financing conditions", descr);
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries<Double>> res = SdmxClientHandler.getTimeSeries(NBB.class.getSimpleName(), "NICP2013/HEALTH+XEFUN0.M", "2006", "2010");
		assertNotNull("Null time series result", res);
		String annual = res.get(0).getName();
		assertEquals("Wrong name for first time series", "NICP2013.HEALTH.M", annual);
		String start = res.get(0).get(0).getTimeslot();
		assertEquals("Wrong start date for time series", "2006-06", start);
	}

}
