/* Copyright 2023,2023 Bank Of Italy
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

import java.util.List;

import org.junit.Test;

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

public class TimeSeriesFromFiltersTest
{
	@Test
	public void testKeysAndFilters() throws SdmxException
	{
		//A.GBP+USD.EUR.SP00.A and A.USD.EUR.SP00.A
		List<PortableTimeSeries<Double>> result = SdmxClientHandler.getTimeSeries2("DEMO_SDMXV3", "EXR", "A.GBP+USD.EUR.SP00.A", null, null, null, "all", "all", null, false);
		assertEquals(2, result.size());		
		//A.GBP+USD.EUR.SP00.A and A.USD.EUR.SP00.A with filter
		result = SdmxClientHandler.getTimeSeries2("DEMO_SDMXV3", "EXR", null, "c[FREQ]=A&c[CURRENCY]=USD,GBP&c[EXR_SUFFIX]=A", null, null, "all", "all", null, false);
		assertEquals(2, result.size());		
		//.USD.EUR.SP00.A
		result = SdmxClientHandler.getTimeSeries2("DEMO_SDMXV3", "EXR", ".USD.EUR.SP00.A", null, null, null, "all", "all", null, false);
		assertEquals(5, result.size());		
		//.USD.EUR.SP00.A with filter
		result = SdmxClientHandler.getTimeSeries2("DEMO_SDMXV3", "EXR", null, "c[CURRENCY]=USD&c[CURRENCY_DENOMINATOR]=EUR&c[EXR_TYPE]=SP00&c[EXR_SUFFIX]=A", null, null, "all", "all", null, false);
		assertEquals(5, result.size());		
		//mix key and filter
		result = SdmxClientHandler.getTimeSeries2("DEMO_SDMXV3", "EXR", "A..EUR.SP00.A", "c[FREQ]=A&c[CURRENCY]=USD&c[CURRENCY_DENOMINATOR]=EUR&c[EXR_TYPE]=SP00&c[EXR_SUFFIX]=A", null, null, "all", "all", null, false);
		assertEquals(1, result.size());		
		//mix key and filter + time
		result = SdmxClientHandler.getTimeSeries2("DEMO_SDMXV3", "EXR", "A..EUR.SP00.A", "c[FREQ]=A&c[CURRENCY]=USD&c[CURRENCY_DENOMINATOR]=EUR&c[EXR_TYPE]=SP00&c[EXR_SUFFIX]=A", "2001", "2010", "all", "all", null, false);
		assertEquals(1, result.size());		
		assertEquals(10, result.get(0).size());		
		//mix key and filter + serieskeysonly
		result = SdmxClientHandler.getTimeSeries2("DEMO_SDMXV3", "EXR", "A..EUR.SP00.A", "c[FREQ]=A&c[CURRENCY]=USD&c[CURRENCY_DENOMINATOR]=EUR&c[EXR_TYPE]=SP00&c[EXR_SUFFIX]=A", null, null, "none", "none", null, false);
		assertEquals(1, result.size());		
	}
}