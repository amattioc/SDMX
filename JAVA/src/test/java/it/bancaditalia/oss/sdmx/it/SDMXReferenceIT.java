package it.bancaditalia.oss.sdmx.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import it.bancaditalia.oss.sdmx.api.SDMXReference;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

@RunWith(Parameterized.class)
public class SDMXReferenceIT
{
	@Parameters(name="{0} - {1}")
    public static Collection<Object[]> data() {
        return FilterProvidersToTest.filter(new Object[][] 
    		{
    			{ "ABS",              "QBIS",                          "ABS",      "QBIS" },
        		{ "ECB",              "EXR",                           "ECB",      "ECB_EXR1" },
        		{ "DEMO_SDMXV3",      "EXR",                           "ECB",      "ECB_EXR1" },
        		{ "WITS",             "DF_WITS_Tariff_TRAINS",         "WBG_WITS", "TARIFF_TRAINS" },
        		{ "WB",               "WDI",                           null,       "WDI" },
        		{ "UNDATA",           "DF_UNDATA_COUNTRYDATA",         "UNSD",     "CountryData" },
        		{ "OECD",             "QNA",                           null,       "QNA" },
        		{ "OECD_NEW",         "DSD_NAMAIN1@DF_QNA_EXPENDITURE_CAPITA","OECD.SDD.NAD",       "DSD_NAMAIN1" },
        		{ "OECD_SDMXV3",      "DSD_NAMAIN1@DF_QNA_EXPENDITURE_CAPITA","OECD.SDD.NAD",       "DSD_NAMAIN1" },
        		{ "NBB",              "AFCSURV",                       null,       "AFCSURV" },
        		{ "ISTAT",            "144_125",                       null,       "DCSP_NICDUE" },
        		{ "ISTAT_RI",         "163_24",                        null,       "DCCN_QNA" },
        		{ "INSEE",            "CNA-2010-CONSO-SI",             "FR1",      "CNA-2010-CONSO-SI" },
        		{ "IMF2",             "DS-WHDREO",                     null,       "WHDREO" },
        		{ "IMF",              "CPI",                           "IMF.STA",  "DSD_CPI" },
        		{ "ILO",              "DF_EMP_TEMP_SEX_AGE_NB", 	   "ILO",      "EMP_TEMP_SEX_AGE_NB" },
        		{ "EUROSTAT",         "PRC_HICP_MIDX",                 null,       "PRC_HICP_MIDX" },
        		{ "BBK",              "BBASV",                 		   "BBK",      "BBK_ACS2" }
    		}, 0);
    }

	@Parameter(0) public String provider;
    @Parameter(1) public String dataflow;
    @Parameter(2) public String expectedAgency;
    @Parameter(3) public String expectedRef;
    
	@Test
	public void testDSDIdentifier() throws SdmxException {
		SDMXReference keyF = SdmxClientHandler.getDSDIdentifier(provider, dataflow);
		assertNotNull("Null key family", keyF);
		if (expectedAgency != null)
			assertEquals("Wrong agency", expectedAgency, keyF.getAgency());
		assertEquals("Wrong Key Family", expectedRef, keyF.getId());
	}
}
