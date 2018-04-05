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


public class ESTATTest {
	
	@Test
	public void testGetFlows() throws SdmxException 
	{
		Map<String, String> f = SdmxClientHandler.getFlows("EUROSTAT", "prc_hicp_midx");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("prc_hicp_midx");
		assertEquals("Wrong description for prc_hicp_midx", "HICP (2015 = 100) - monthly data (index)", descr);
	}

	@Test
	public void testGetTimeSeries() throws SdmxException {
		List<PortableTimeSeries<Double>> res = SdmxClientHandler.getTimeSeries("EUROSTAT","prc_hicp_midx/..CP00.EU+DE+FR", "2000", "2013-08");
		assertNotNull("Null time series result", res);
		
		//warning: they depend on eventual order
		String monthly = res.get(0).getName();
		assertEquals("Wrong name for first time series", "prc_hicp_midx.M.I05.CP00.DE", monthly);
		String start = res.get(0).get(0).getTimeslot();
		assertEquals("Wrong start date for time series", "2000-01", start);
		//check delayed response
//for now		res = SdmxClientHandler.getTimeSeries("EUROSTAT","lfsa_pgaied/.....", null, null);
//		assertNotNull("Null time series result", res);
//		assertTrue("Zero time series returned", res.size() > 0);
	}

}
