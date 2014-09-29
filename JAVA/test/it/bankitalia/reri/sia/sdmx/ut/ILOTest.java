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

public class ILOTest {
	//private static SdmxClientHandler handler= SdmxClientHandler.getInstance();
	
	@BeforeClass
	public static void setUp() throws Exception {
	}

	@Test
	public void getDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("ILO", "DF_YI_FRA_EMP_TEMP_SEX_AGE_NB");
		assertNotNull("Null key family for DF_YI_FRA_EMP_TEMP_SEX_AGE_NB", keyF);
		assertEquals("Wrong Key Family", "YI_FRA_EMP_TEMP_SEX_AGE_NB", keyF.getId());
		assertEquals("Wrong agency", "ILO", keyF.getAgency());
	}

//	@Test
//	public void testGetFlows() throws SdmxException {
//		Map<String, String> f = SdmxClientHandler.getFlows("ECB", "Exchange*");
//		assertNotNull("Null getFlows result", f);
//		String descr = f.get("EXR");
//		assertEquals("Wrong description for EXR", "Exchange Rates", descr);
//	}

	@Test
	public void testGetDimensions() throws SdmxException {
		List<Dimension> dim = SdmxClientHandler.getDimensions("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB");
		assertNotNull("Null getDimensions result", dim);
		ArrayList<Dimension> dimensions = new ArrayList<Dimension>();
		String result = "[Dimension [id=COLLECTION, position=1, codelist=ILO/CL_COLLECTION], " +
						"Dimension [id=COUNTRY, position=2, codelist=ILO/CL_COUNTRY], " +
						"Dimension [id=FREQ, position=3, codelist=ILO/CL_FREQ], " +
						"Dimension [id=SURVEY, position=4, codelist=ILO/CL_SURVEY], " +
						"Dimension [id=REPRESENTED_VARIABLE, position=5, codelist=ILO/CL_REPRESENTED_VARIABLE], " +
						"Dimension [id=CLASSIF_SEX, position=6, codelist=ILO/CL_SEX], " +
						"Dimension [id=CLASSIF_AGE, position=7, codelist=ILO/CL_AGE]]";

		assertEquals("Wrong dimensions for DF_YI_ALL_EMP_TEMP_SEX_AGE_NB", result, dim.toString());
	}
	
	@Test
	public void testGetCodes() throws SdmxException {
			Map<String, String> codes = SdmxClientHandler.getCodes("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB", "COUNTRY");
			assertNotNull("Null getCodes result", codes);
			assertEquals("Wrong code for COUNTRY Belize", codes.get("BLZ"), "Belize");
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries>  res = SdmxClientHandler.getTimeSeries("ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB/YI.MEX.A.463.EMP_TEMP_NB.SEX_F.AGE_10YRBANDS_TOTAL", null, null);
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 1, res.size());
	}

}
