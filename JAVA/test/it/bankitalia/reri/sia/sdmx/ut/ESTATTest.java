package it.bankitalia.reri.sia.sdmx.ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import it.bankitalia.reri.sia.sdmx.api.DSDIdentifier;
import it.bankitalia.reri.sia.sdmx.api.Dimension;
import it.bankitalia.reri.sia.sdmx.api.PortableTimeSeries;
import it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler;
import it.bankitalia.reri.sia.util.SdmxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;


public class ESTATTest {
	
//private static SdmxClientSdmxClientHandler handler = SdmxClientHandler.getInstance();
	
	@BeforeClass
	public static void setUp() throws Exception {
	}
	
	@Test
	public void testGetDSDIdentifier() throws SdmxException {
		DSDIdentifier dsd = SdmxClientHandler.getDSDIdentifier("EUROSTAT", "prc_hicp_midx");
		assertNotNull("Null key family for prc_hicp_midx", dsd);
		assertEquals("Wrong Key Family", "DSD_prc_hicp_midx", dsd.getId());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("EUROSTAT", "prc_hicp_midx");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("prc_hicp_midx");
		assertEquals("Wrong description for prc_hicp_midx", "HICP (2005 = 100) - monthly data (index)", descr);
	}

	@Test
	public void testGetDimensions() throws SdmxException {
		List<Dimension> dim = SdmxClientHandler.getDimensions("EUROSTAT", "prc_hicp_midx");
		assertNotNull("Null getDimensions result", dim);
		ArrayList<Dimension> dimensions = new ArrayList<Dimension>();

		dimensions.add(0, new Dimension("FREQ", 1, "ESTAT/CL_FREQ/1.0"));
		dimensions.add(1, new Dimension("INFOTYPE", 2, "ESTAT/CL_INFOTYPE/1.0"));
		dimensions.add(2, new Dimension("COICOP", 3, "ESTAT/CL_COICOP/1.0"));
		dimensions.add(3, new Dimension("GEO", 4, "ESTAT/CL_GEO/1.0"));
		assertEquals("Wrong dimensions for EXR", dimensions.toString(), dim.toString());
	}


//	@Test
//	public void testGetCodes() throws SdmxException {
//			Map<String, String> codes = SdmxClientHandler.getCodes("EUROSTAT", "prc_hicp_midx", "FREQ");
//			assertNotNull("Null getCodes result", codes);
//			assertEquals("Wrong code for FREQ annual", codes.get("A"), "Annual");
//	}

	@Test
	public void testGetTimeSeries() throws SdmxException {
		List<PortableTimeSeries> res = SdmxClientHandler.getTimeSeries("EUROSTAT","prc_hicp_midx/..CP00.EU+DE+FR", null, "2013-08");
		assertNotNull("Null time series result", res);
		
		//warning: they depend on eventual order
		String monthly = res.get(0).getName();
		assertEquals("Wrong name for first time series", "prc_hicp_midx.M.I2005.CP00.DE", monthly);
		String start = res.get(0).getTimeSlots().get(0);
		assertEquals("Wrong start date for time series", "2013-08", start);
		//System.out.println(res);
	}

}
