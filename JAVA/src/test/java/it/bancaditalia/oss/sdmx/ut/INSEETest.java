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

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class INSEETest {
	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("INSEE", null);
		assertNotNull("Null getFlows result", f);
		String descr = f.get("CNA-2010-CONSO-SI");
		assertEquals("Wrong description for CNA-2010-CONSO-SI", "Final consumption expenditure by institutional sectors", descr);
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries<Double>>  res = SdmxClientHandler.getTimeSeries("INSEE", "CNA-2010-CONSO-SI..CNA_CONSO_SI.S15.PCH.P3.A88-88.VALEUR_ABSOLUE.FE.EUR2010.BRUT", null, null);
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 1, res.size());
	}

}
