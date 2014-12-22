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
import it.bancaditalia.oss.sdmx.client.custom.OECD;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class SdmxInterfaceTest {
	@BeforeClass
	public static void setUp() throws Exception {
	}

	@Test
	public void testGetAddProvider() throws SdmxException {
		SdmxClientHandler.addProvider("TEST", "http://sdw-wsrest.ecb.europa.eu/service", false, false, false, "test provider");
		List<PortableTimeSeries> a = SdmxClientHandler.getTimeSeries("TEST", "EXR.A.GBP.EUR.SP00.A", null, null);
		assertNotNull("Null getTimeSeries result", a);
		assertEquals("Wrong number of results for getTimeSeries", 1, a.size());
	}
	@Test(expected=SdmxException.class)
	public void getDSDIdentifierFail1() throws SdmxException{
		SdmxClientHandler.getDSDIdentifier(null, "FFF");
	}
	@Test(expected=SdmxException.class)
	public void getDSDIdentifierFail2() throws SdmxException{
		SdmxClientHandler.getDSDIdentifier("", "FFF");
	}
	@Test(expected=SdmxException.class)
	public void getDSDIdentifierFail3() throws SdmxException{
		SdmxClientHandler.getDSDIdentifier("ECB", null);
	}
	@Test(expected=SdmxException.class)
	public void getDSDIdentifierFail4() throws SdmxException{
		SdmxClientHandler.getDSDIdentifier("ECB", "");
	}
	@Test(expected=SdmxException.class)
	public void getDSDIdentifierFail5() throws SdmxException{
		SdmxClientHandler.getDSDIdentifier("DUMMY", "EXR");
	}
	@Test(expected=SdmxException.class)
	public void getDSDIdentifierFail6() throws SdmxException{
		SdmxClientHandler.getDSDIdentifier("ECB", "DUMMY");
	}
	@Test
	public void getDSDIdentifier() throws SdmxException{
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("ECB", "EXR");
		assertNotNull("Null key family for EXR", keyF);
		assertEquals("Wrong Key Family", "ECB_EXR1", keyF.getId());
		assertEquals("Wrong agency", "ECB", keyF.getAgency());
	}

	@Test(expected=SdmxException.class)
	public void testGetFlowsFail1() throws SdmxException{
		SdmxClientHandler.getFlows(null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetFlowsFail2() throws SdmxException{
		SdmxClientHandler.getFlows("", null);
	}
	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("EUROSTAT", "prc_hicp_midx");
		assertNotNull("Null getFlows result", f);
		assertEquals("Wrong number of results for prc_hicp_midx", 1, f.size());
		String descr = f.get("prc_hicp_midx");
		assertEquals("Wrong description for prc_hicp_midx", "ESTAT,prc_hicp_midx,1.0 ; HICP (2005 = 100) - monthly data (index)", descr);
		f = SdmxClientHandler.getFlows("EUROSTAT", null);
		assertEquals("Wrong number of results for prc_hicp_midx", f.size() > 0, true);
		f = SdmxClientHandler.getFlows("EUROSTAT", "");
		assertEquals("Wrong number of results for prc_hicp_midx", f.size() > 0, true);
		f = SdmxClientHandler.getFlows("EUROSTAT", "pippo");
		assertEquals("Wrong number of results for pippo", f.size() == 0, true);
	}
	
	@Test(expected=SdmxException.class)
	public void testGetDimensionsFail1() throws SdmxException{
		SdmxClientHandler.getDimensions(null, "EXR");
	}
	@Test(expected=SdmxException.class)
	public void testGetDimensionsFail2() throws SdmxException{
		SdmxClientHandler.getDimensions("", "EXR");
	}
	@Test(expected=SdmxException.class)
	public void testGetDimensionsFail3() throws SdmxException{
		SdmxClientHandler.getDimensions("ECB", null);
	}
	@Test(expected=SdmxException.class)
	public void testGetDimensionsFail4() throws SdmxException{
		SdmxClientHandler.getDimensions("ECB", "");
	}
	@Test(expected=SdmxException.class)
	public void testGetDimensionsFail5() throws SdmxException{
		SdmxClientHandler.getDimensions("ECB", "FFF");
	}

	@Test
	public void testGetDimensions() throws SdmxException {
		List<Dimension> dim = SdmxClientHandler.getDimensions(OECD.class.getSimpleName(), "QNA");
		assertNotNull("Null getDimensions result QNA", dim);	
		String result = "[Dimension [id=LOCATION, position=1, codelist=Codelist [id=OECD/CL_QNA_LOCATION, codes={CHE=Switzerland, OECDE=O";
		assertEquals("Wrong dimensions for QNA", result, dim.toString().substring(0, result.length()));
	}
	
	@Test(expected=SdmxException.class)
	public void testGetCodesFail1() throws SdmxException{
		SdmxClientHandler.getCodes(null, "EXR", "FREQ");
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail2() throws SdmxException{
		SdmxClientHandler.getCodes("", "EXR", "FREQ");
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail3() throws SdmxException{
		SdmxClientHandler.getCodes("ECB", null, "FREQ");
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail4() throws SdmxException{
		SdmxClientHandler.getCodes("ECB", "", "FREQ");
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail5() throws SdmxException{
		SdmxClientHandler.getCodes("ECB", "EXR", null);
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail6() throws SdmxException{
		SdmxClientHandler.getCodes("ECB", "EXR", "");
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail7() throws SdmxException{
		SdmxClientHandler.getCodes("ECB", "DUMMY", "FREQ");
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail8() throws SdmxException{
		SdmxClientHandler.getCodes("ECB", "EXR", "DUMMY");
	}
	
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail1() throws SdmxException {
		SdmxClientHandler.getTimeSeries(null, "EXR.A.GBP+USD.EUR.SP00.A", null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail2() throws SdmxException {
		SdmxClientHandler.getTimeSeries("", "EXR.A.GBP+USD.EUR.SP00.A", null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail3() throws SdmxException {
		SdmxClientHandler.getTimeSeries("DUMMY", "EXR.A.USD.EUR.SP00.A", null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail4() throws SdmxException {
		//wrong flow
		SdmxClientHandler.getTimeSeries("ECB", "DUMMY.A.USD.EUR.SP00.A", null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail5() throws SdmxException {
		//no flow
		SdmxClientHandler.getTimeSeries("ECB", "A.GBP.EUR.SP00.A", null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail6() throws SdmxException {
		//wrong dimension list
		SdmxClientHandler.getTimeSeries("ECB", "EXR.EUR.SP00.A", null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail7() throws SdmxException {
		//wrong dimension list
		SdmxClientHandler.getTimeSeries("ECB", "EXR.A.N.SS.A.A.EUR.SP00.A", null, null);
	}

}
