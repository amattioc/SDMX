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

import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;

import org.junit.Test;

import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.client.SDMXClientFactory;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

@SuppressWarnings("javadoc")
public class SdmxInterfaceTest
{
	@Test
	public void testGetAddProvider() throws SdmxException, MalformedURLException
	{
		SdmxClientHandler.addProvider("TEST", "http://sdw-wsrest.ecb.europa.eu/service", false, false, false,
				"test provider");
		GenericSDMXClient a = SDMXClientFactory.createClient("TEST");
		assertNotNull("Add Provider failed", a);
	}

	@Test(expected = SdmxException.class)
	public void getDSDIdentifierFail1() throws SdmxException
	{
		SdmxClientHandler.getDSDIdentifier(null, "FFF");
	}

	@Test(expected = SdmxException.class)
	public void getDSDIdentifierFail2() throws SdmxException
	{
		SdmxClientHandler.getDSDIdentifier("", "FFF");
	}

	@Test(expected = SdmxException.class)
	public void getDSDIdentifierFail3() throws SdmxException
	{
		SdmxClientHandler.getDSDIdentifier("ECB", null);
	}

	@Test(expected = SdmxException.class)
	public void getDSDIdentifierFail4() throws SdmxException
	{
		SdmxClientHandler.getDSDIdentifier("ECB", "");
	}

	@Test(expected = SdmxException.class)
	public void getDSDIdentifierFail5() throws SdmxException
	{
		SdmxClientHandler.getDSDIdentifier("DUMMY", "EXR");
	}

	/* TODO: Move to IT tests
	@Test(expected = SdmxException.class)
	public void getDSDIdentifierFail6() throws SdmxException
	{
		SdmxClientHandler.getDSDIdentifier("ECB", "DUMMY");
	}

	@Test
	public void getDSDIdentifier() throws SdmxException
	{
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("ECB", "EXR");
		assertNotNull("Null key family for EXR", keyF);
		assertEquals("Wrong Key Family", "ECB_EXR1", keyF.getId());
		assertEquals("Wrong agency", "ECB", keyF.getAgency());
	}
	*/

	@Test(expected = SdmxException.class)
	public void testGetFlowsFail1() throws SdmxException
	{
		SdmxClientHandler.getFlows(null, null);
	}

	@Test(expected = SdmxException.class)
	public void testGetFlowsFail2() throws SdmxException
	{
		SdmxClientHandler.getFlows("", null);
	}

	/* TODO: Move to IT tests
	@Test
	public void testGetFlows() throws SdmxException
	{
		Map<String, String> f = SdmxClientHandler.getFlows("EUROSTAT", "prc_hicp_midx");
		assertNotNull("Null getFlows result", f);
		assertEquals("Wrong number of results for prc_hicp_midx", 1, f.size());
		String descr = f.get("prc_hicp_midx");
		assertEquals("Wrong description for prc_hicp_midx", "HICP (2015 = 100) - monthly data (index)", descr);
		f = SdmxClientHandler.getFlows("EUROSTAT", null);
		assertEquals("Wrong number of results for prc_hicp_midx", f.size() > 0, true);
		f = SdmxClientHandler.getFlows("EUROSTAT", "");
		assertEquals("Wrong number of results for prc_hicp_midx", f.size() > 0, true);
		f = SdmxClientHandler.getFlows("EUROSTAT", "pippo");
		assertEquals("Wrong number of results for pippo", f.size() == 0, true);
	}
	*/
	
	@Test(expected = SdmxException.class)
	public void testGetDimensionsFail1() throws SdmxException
	{
		SdmxClientHandler.getDimensions(null, "EXR");
	}

	@Test(expected = SdmxException.class)
	public void testGetDimensionsFail2() throws SdmxException
	{
		SdmxClientHandler.getDimensions("", "EXR");
	}

	@Test(expected = SdmxException.class)
	public void testGetDimensionsFail3() throws SdmxException
	{
		SdmxClientHandler.getDimensions("ECB", null);
	}

	@Test(expected = SdmxException.class)
	public void testGetDimensionsFail4() throws SdmxException
	{
		SdmxClientHandler.getDimensions("ECB", "");
	}

	/* TODO: Move to IT tests
	@Test(expected = SdmxException.class)
	public void testGetDimensionsFail5() throws SdmxException
	{
		SdmxClientHandler.getDimensions("ECB", "FFF");
	}
	*/
	
	@Test(expected = SdmxException.class)
	public void testGetCodesFail1() throws SdmxException
	{
		SdmxClientHandler.getCodes(null, "EXR", "FREQ");
	}

	@Test(expected = SdmxException.class)
	public void testGetCodesFail2() throws SdmxException
	{
		SdmxClientHandler.getCodes("", "EXR", "FREQ");
	}

	@Test(expected = SdmxException.class)
	public void testGetCodesFail3() throws SdmxException
	{
		SdmxClientHandler.getCodes("ECB", null, "FREQ");
	}

	@Test(expected = SdmxException.class)
	public void testGetCodesFail4() throws SdmxException
	{
		SdmxClientHandler.getCodes("ECB", "", "FREQ");
	}

	@Test(expected = SdmxException.class)
	public void testGetCodesFail5() throws SdmxException
	{
		SdmxClientHandler.getCodes("ECB", "EXR", null);
	}

	@Test(expected = SdmxException.class)
	public void testGetCodesFail6() throws SdmxException
	{
		SdmxClientHandler.getCodes("ECB", "EXR", "");
	}

	/* TODO: Move to IT tests
	@Test(expected = SdmxException.class)
	public void testGetCodesFail7() throws SdmxException
	{
		SdmxClientHandler.getCodes("ECB", "DUMMY", "FREQ");
	}

	@Test(expected = SdmxException.class)
	public void testGetCodesFail8() throws SdmxException
	{
		SdmxClientHandler.getCodes("ECB", "EXR", "DUMMY");
	}
	*/
	
	@Test(expected = SdmxException.class)
	public void testGetTimeSeriesFail1() throws SdmxException
	{
		SdmxClientHandler.getTimeSeries(null, "EXR.A.GBP+USD.EUR.SP00.A", null, null);
	}

	@Test(expected = SdmxException.class)
	public void testGetTimeSeriesFail2() throws SdmxException
	{
		SdmxClientHandler.getTimeSeries("", "EXR.A.GBP+USD.EUR.SP00.A", null, null);
	}

	@Test(expected = SdmxException.class)
	public void testGetTimeSeriesFail3() throws SdmxException
	{
		SdmxClientHandler.getTimeSeries("DUMMY", "EXR.A.USD.EUR.SP00.A", null, null);
	}

	/* TODO: Move to IT tests
	@Test(expected = SdmxException.class)
	public void testGetTimeSeriesFail4() throws SdmxException
	{
		// wrong flow
		SdmxClientHandler.getTimeSeries("ECB", "DUMMY.A.USD.EUR.SP00.A", null, null);
	}

	@Test(expected = SdmxException.class)
	public void testGetTimeSeriesFail5() throws SdmxException
	{
		// no flow
		SdmxClientHandler.getTimeSeries("ECB", "A.GBP.EUR.SP00.A", null, null);
	}

	@Test(expected = SdmxException.class)
	public void testGetTimeSeriesFail6() throws SdmxException
	{
		// wrong dimension list
		SdmxClientHandler.getTimeSeries("ECB", "EXR.EUR.SP00.A", null, null);
	}

	@Test(expected = SdmxException.class)
	public void testGetTimeSeriesFail7() throws SdmxException
	{
		// wrong dimension list
		SdmxClientHandler.getTimeSeries("ECB", "EXR.A.N.SS.A.A.EUR.SP00.A", null, null);
	}

	@Test(expected = SdmxException.class)
	public void testGetTimeSeriesFail8() throws SdmxException
	{
		// zero time series, 2.0 provider
		SdmxClientHandler.getTimeSeries("IMF", "PGI.CA.BIS.FOSLB.A.L_M", null, null);
	}

	@Test
	public void testGetTimeSeriesNames() throws SdmxException
	{
		List<PortableTimeSeries<Double>> ts = SdmxClientHandler.getTimeSeriesNames("ECB", "EXR.A.USD.EUR.SP00.A");
		assertNotNull("Null gettimeseries result", ts);
		assertEquals(true, ts.size() > 0);
		PortableTimeSeries<Double> ts1 = ts.get(0);
		assertEquals("EXR.A.USD.EUR.SP00.A", ts1.getName());
		assertEquals(true, ts1.size() == 0);
	}

	@Test
	public void testGetTimeSeries() throws SdmxException
	{
		List<PortableTimeSeries<Double>> ts = SdmxClientHandler.getTimeSeries("ECB", "EXR.A.USD.EUR.SP00.A", "2000",
				"2010");
		assertNotNull("Null gettimeseries result", ts);
		assertEquals(true, ts.size() == 1);
		PortableTimeSeries<?> ts1 = ts.get(0);
		assertEquals("EXR.A.USD.EUR.SP00.A", ts1.getName());
		int nobs = ts1.size();
		assertEquals(true, nobs == ts1.size());
		for (Iterator<String> iterator = ts1.getObsLevelAttributesNames().iterator(); iterator.hasNext();)
		{
			String name = (String) iterator.next();
			Collection<String> att = ts1.getObsLevelAttributes(name);
			assertEquals(true, nobs == att.size());
		}
	}

	@Test
	public void testGetTimeSeriesRevisions() throws SdmxException
	{
		List<PortableTimeSeries<Double>> tslist1 = SdmxClientHandler.getTimeSeriesRevisions("ECB",
				"EXR.A.USD.EUR.SP00.A", null, null, null, true);
		assertNotNull("Null gettimeseries result", tslist1);
		assertEquals(true, tslist1.size() > 0);
		PortableTimeSeries<?> ts1 = tslist1.get(0);
		assertEquals("EXR.A.USD.EUR.SP00.A", ts1.getName());
		assertEquals(true, ts1.size() > 0);
		List<PortableTimeSeries<Double>> tslist2 = SdmxClientHandler.getTimeSeriesRevisions("ECB",
				"EXR.A.USD.EUR.SP00.A", null, null, "2015-01-01", true);
		assertNotNull("Null gettimeseries result", tslist2);
		assertEquals(true, tslist2.size() > 0);
		PortableTimeSeries<?> ts2 = tslist2.get(0);
		assertEquals("EXR.A.USD.EUR.SP00.A", ts2.getName());
		assertEquals(true, ts2.size() > 0);
		assertEquals(true, tslist1.size() > tslist2.size());
	}

	@Test
	public void testGetTimeSeriesList() throws SdmxException
	{
		List<PortableTimeSeries<Double>> tslist1 = SdmxClientHandler.getTimeSeries("ECB",
				"EXR.A.USD.EUR.SP00.A ; EXR.M.USD.EUR.SP00.A", null, null);
		assertNotNull("Null gettimeseries result", tslist1);
		assertEquals(true, tslist1.size() == 2);
	}

	@Test
	public void testGetTimeSeriesTable() throws SdmxException, DataStructureException
	{
		PortableDataSet<?> tstable = SdmxClientHandler.getTimeSeriesTable("ECB", "EXR.A.USD.EUR.SP00.A", null, null);
		assertNotNull("Null getTimeSeriesTable result", tstable);
		assertTrue("No columns", tstable.getColumnCount() > 0);
		assertTrue("No rows", tstable.getRowCount() > 0);
		assertEquals("Error TIME_PERIOD", tstable.getColumnName(0), "TIME_PERIOD");
		assertEquals("Error OBS_VALUE", tstable.getColumnName(1), "OBS_VALUE");
		assertEquals("Error OBS_VALUE position", tstable.getColumnIndex("OBS_VALUE"), 1);
		assertEquals("Error TIME_PERIOD position", tstable.getColumnIndex("TIME_PERIOD"), 0);
		assertTrue("Error OBS_VALUE value", ((Double) tstable.getValueAt(0, 1)) > 0);
	}

	@Test
	public void testSASHandler() throws SdmxException
	{
		int result = SASClientHandler.makeGetTimeSeries("ECB", "EXR.A.USD.EUR.SP00.A", null, null);
		assertTrue(result > 0);
		int data1 = SASClientHandler.getNumberOfData();
		assertTrue(data1 > 0);
		for (int i = 0; i < SASClientHandler.getNumberOfData(); i++)
		{
			assertNotNull(SASClientHandler.getDataName(i));
			assertNotNull(SASClientHandler.getDataTimestamp(i));
			assertNotNull(SASClientHandler.getDataObservation(i));
		}
		result = SASClientHandler.getNumberOfMeta();
		assertTrue(result > 0);
		for (int i = 0; i < SASClientHandler.getNumberOfMeta(); i++)
		{
			assertNotNull(SASClientHandler.getMetaName(i));
			assertNotNull(SASClientHandler.getMetaKey(i));
			assertNotNull(SASClientHandler.getMetaValue(i));
			assertNotNull(SASClientHandler.getMetaType(i));
		}
		result = SASClientHandler.getNumberOfObsMeta();
		assertTrue(result > 0);
		for (int i = 0; i < SASClientHandler.getNumberOfObsMeta(); i++)
		{
			assertNotNull(SASClientHandler.getObsMetaName(i));
			assertNotNull(SASClientHandler.getObsMetaKey(i));
			assertNotNull(SASClientHandler.getObsMetaValue(i));
			assertNotNull(SASClientHandler.getObsMetaDate(i));
		}
		SASClientHandler.makeGetTimeSeries("ECB", "EXR.A.USD.EUR.SP00.A", "2000", "2001");
		int data2 = SASClientHandler.getNumberOfData();
		assertTrue(data1 > data2);
	}
	*/
}
