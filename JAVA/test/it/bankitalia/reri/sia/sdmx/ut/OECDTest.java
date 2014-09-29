package it.bankitalia.reri.sia.sdmx.ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import it.bankitalia.reri.sia.sdmx.api.DSDIdentifier;
import it.bankitalia.reri.sia.sdmx.api.Dimension;
import it.bankitalia.reri.sia.sdmx.api.PortableTimeSeries;
import it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler;
import it.bankitalia.reri.sia.sdmx.client.custom.OECD;
import it.bankitalia.reri.sia.util.SdmxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class OECDTest {
	//private static SdmxClientHandler handler= SdmxClientHandler.getInstance();
	
	@BeforeClass
	public static void setUp() throws Exception {
	}

	@Test
	public void testGetDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier(OECD.class.getSimpleName(), "QNA");
		assertNotNull("Null key family for QNA", keyF);
		assertEquals("Wrong Key Family", "QNA", keyF.getId());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows(OECD.class.getSimpleName(), "QNA");
		assertNotNull("Null getFlows result", f);
		String descr = f.get("QNA");
		assertEquals("Wrong description for AEO", "Quarterly National Accounts", descr);
	}

	@Test
	public void testGetDimensions() throws SdmxException {
		List<Dimension> dim = SdmxClientHandler.getDimensions(OECD.class.getSimpleName(), "QNA");
		assertNotNull("Null getDimensions result QNA", dim);
		ArrayList<Dimension> dimensions = new ArrayList<Dimension>();
		dimensions.add(0, new Dimension("LOCATION", 1, "OECD/CL_QNA_LOCATION"));
		dimensions.add(1,  new Dimension("SUBJECT", 2, "OECD/CL_QNA_SUBJECT"));
		dimensions.add(2,  new Dimension("MEASURE", 3, "OECD/CL_QNA_MEASURE"));
		dimensions.add(3,  new Dimension("FREQUENCY", 4, "OECD/CL_QNA_FREQUENCY"));
		assertEquals("Wrong dimensions for QNA", dimensions.toString(), dim.toString());
	}
	
	@Test
	public void testGetCodes() throws SdmxException {
			Map<String, String> codes = SdmxClientHandler.getCodes(OECD.class.getSimpleName(), "QNA", "FREQUENCY");
			assertNotNull("Null getCodes result", codes);
			assertEquals("Wrong code for FREQUENCY annual", codes.get("A"), "Annual");
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries> res = SdmxClientHandler.getTimeSeries(OECD.class.getSimpleName(), "QNA.ITA.B1_GE.CARSA.Q", null, null);
		assertNotNull("Null time series result", res);
		//warning: they depend on eventual order
		String annual = res.get(0).getName();
		assertEquals("Wrong name for first time series", "QNA.ITA.B1_GE.CARSA.Q", annual);
		String start = res.get(0).getTimeSlots().get(0);
		assertEquals("Wrong start date for time series", "1960Q1", start);
		//System.out.println(res);
	}

}
