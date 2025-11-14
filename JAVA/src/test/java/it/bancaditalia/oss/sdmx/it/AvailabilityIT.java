package it.bancaditalia.oss.sdmx.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

@RunWith(Parameterized.class)
public class AvailabilityIT
{
	@Parameters(name="{0} - {1}")
    public static Collection<Object[]> data() {
        return FilterProvidersToTest.filter(new Object[][] 
    		{
				{ "DEMO_SDMXV3",      "c[FREQ]=A",    "EXR", "5"},
				{ "EUROSTAT",         "...",          "PRC_HICP_MIDX", "4"},
				{ "ILO",              "....",         "DF_EMP_TEMP_SEX_AGE_NB", "5"},
				{ "UNDATA",           ".......",      "DF_UNDATA_COUNTRYDATA", "8"},
				{ "OECD_NEW",         "............", "DSD_NAMAIN1@DF_QNA_EXPENDITURE_CAPITA", "13"},
				{ "OECD_SDMXV3",      "c[FREQ]=A",    "DSD_NAMAIN1@DF_QNA_EXPENDITURE_CAPITA", "13"},
				{ "ISTAT_RI",         "Q.IT.B1G_B_W2_S1....Y.Y..2020M12",    "163_24_DF_DCCN_QNA_1", "10"}
    		}, 0);
    }

	@Parameter(0) public String provider;
    @Parameter(1) public String key;
    @Parameter(2) public String testDataflow;
    @Parameter(3) public String n;

	@Test
	public void filterCodes() throws SdmxException 
	{
		Map<String, Map<String, String>> result = SdmxClientHandler.filterCodes(provider, testDataflow, key);
		assertNotNull("Null getFlows result", result);
		assertEquals("Wrong result", Integer.parseInt(n), result.size()); 
	}
}
