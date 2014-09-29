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

public class ECBTest {
	//private static SdmxClientHandler handler= SdmxClientHandler.getInstance();
	
	@BeforeClass
	public static void setUp() throws Exception {
	}

	@Test
	public void getDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("ECB", "EXR");
		assertNotNull("Null key family for EXR", keyF);
		assertEquals("Wrong Key Family", "ECB_EXR1", keyF.getId());
		assertEquals("Wrong agency", "ECB", keyF.getAgency());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("ECB", "Exchange*");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("EXR");
		assertEquals("Wrong description for EXR", "Exchange Rates", descr);
	}

	@Test
	public void testGetDimensions() throws SdmxException {
		List<Dimension> dim = SdmxClientHandler.getDimensions("ECB", "EXR");
		assertNotNull("Null getDimensions result", dim);
		ArrayList<Dimension> dimensions = new ArrayList<Dimension>();
		dimensions.add(0, new Dimension("FREQ", 1, "ECB/CL_FREQ/1.0"));
		dimensions.add(1, new Dimension("CURRENCY", 2, "ECB/CL_CURRENCY/1.0"));
		dimensions.add(2, new Dimension("CURRENCY_DENOM", 3, "ECB/CL_CURRENCY/1.0"));
		dimensions.add(3, new Dimension("EXR_TYPE", 4, "ECB/CL_EXR_TYPE/1.0"));
		dimensions.add(4, new Dimension("EXR_SUFFIX",5, "ECB/CL_EXR_SUFFIX/1.0"));
		assertEquals("Wrong dimensions for EXR", dimensions.toString(), dim.toString());
	}
	
	@Test
	public void testGetCodes() throws SdmxException {
			Map<String, String> codes = SdmxClientHandler.getCodes("ECB", "EXR", "FREQ");
			assertNotNull("Null getCodes result", codes);
			assertEquals("Wrong code for FREQ annual", codes.get("A"), "Annual");
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries>  res = SdmxClientHandler.getTimeSeries("ECB", "EXR/.GBP+USD.EUR.SP00.A", null, null);
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 10, res.size());
		res = SdmxClientHandler.getTimeSeries("ECB", "EXR.*.USD|GBP.EUR.SP00.A", null, null);
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 10, res.size());
	}

}
