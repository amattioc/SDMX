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

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

public class ILOTest {
	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("ILO", "*DF_YI_ALL_EMP_TEMP_SEX_AGE_NB*");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("DF_YI_ALL_EMP_TEMP_SEX_AGE_NB");
		assertEquals("Wrong description for DF_YI_ALL_EMP_TEMP_SEX_AGE_NB", "Employment by sex and age", descr);
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries<Double>>  res = SdmxClientHandler.getTimeSeries("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB/YI.MEX.A.463.EMP_TEMP_NB.SEX_F.AGE_10YRBANDS_TOTAL", null, null);
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", res.size() , 1);
		res = SdmxClientHandler.getTimeSeries("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB/YI.MEX.A.463...", null, null);
		assertNotNull("Null time series result", res);
		assertTrue("Wrong result size", res.size() > 0);
	}

}
