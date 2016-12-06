package it.bancaditalia.oss.sdmx.ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ISTATTest {
	@Test
	public void testGetDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("ISTAT", "144_125");
		assertNotNull("Null key family for 144_125_SUBNAZ", keyF);
		assertEquals("Wrong Key Family", "SEP_PREZZI_NIC_FOI_2010", keyF.getId());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("ISTAT", null);
		assertNotNull("Null getFlows result", f);
		String descr = f.get("144_125");
		assertEquals("Wrong description for 144_125", "Consumer price index for the whole nation - annual average (NIC - until 2010)", descr);
	}

	@Test
	public void testGetDimensionsAndCodes() throws SdmxException {
		Map<String, String> codes = SdmxClientHandler.getCodes("ISTAT", "144_125", "FREQ");
		assertNotNull("Null getCodes result", codes);
		assertEquals("Wrong code for FREQ annual", codes.get("A"), "annual");

		List<Dimension> dim = SdmxClientHandler.getDimensions("ISTAT", "144_125");
		assertNotNull("Null getDimensions result for 144_125", dim);
		String result = "[Dimension [id=FREQ, position=1, codelist=Codelist [id=IT1/CL_FREQ/1.0, codes={W=weekly, A=annual, H=half-yearly, Q=quarterly, E=event (not ";
		assertEquals("Wrong dimensions for 144_125", result, dim.toString().substring(0, result.length()));
	}
	
	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries> res = SdmxClientHandler.getTimeSeries("ISTAT", "115_362/M....", null, null);
		assertNotNull("Null time series result", res);
		
		//warning: they depend on eventual order
		String monthly = res.get(0).getName();
		assertEquals("Wrong name for first time series", "115_362.M.F.N.IT.CONS_PROD", monthly);
		//System.out.println(res);
	}

}
