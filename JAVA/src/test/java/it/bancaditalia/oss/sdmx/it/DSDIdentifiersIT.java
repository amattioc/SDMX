package it.bancaditalia.oss.sdmx.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

@RunWith(Parameterized.class)
public class DSDIdentifiersIT
{
	@Parameters(name="{0} - {1}")
    public static Collection<Object[]> data() {
        return FilterProvidersToTest.filter(new Object[][] 
    		{
    			{ "ABS",              "QBIS",                          "ABS",      "QBIS" },
        		{ "ECB",              "EXR",                           "ECB",      "ECB_EXR1" },
        		{ "WITS",             "DF_WITS_Tariff_TRAINS",         "WBG_WITS", "TARIFF_TRAINS" },
        		{ "WB",               "WDI",                           null,       "WDI" },
        		{ "UNDATA",           "DF_UNDATA_COUNTRYDATA",         "UNSD",     "CountryData" },
        		{ "UIS",              "CE",                            "UNESCO",   "CE" },
        		{ "OECD",             "QNA",                           null,       "QNA" },
        		{ "NBB",              "AFCSURV",                       null,       "AFCSURV" },
        		{ "ISTAT",            "144_125",                       null,       "DCSP_NICDUE" },
        		{ "INSEE",            "CNA-2010-CONSO-SI",             "FR1",      "CNA-2010-CONSO-SI" },
        		{ "INEGI",            "DF_STEI",                       null,       "DSD_STEI" },
        		{ "IMF_SDMX_CENTRAL", "SPI",                           null,       "ECOFIN_DSD" },
        		{ "IMF2",             "DS-WHDREO",                     null,       "WHDREO" },
        		{ "ILO",              "DF_YI_FRA_EMP_TEMP_SEX_AGE_NB", "ILO",      "YI_FRA_EMP_TEMP_SEX_AGE_NB" },
        		{ "EUROSTAT",         "prc_hicp_midx",                 null,       "DSD_prc_hicp_midx" }
    		}, 0);
    }

	@Parameter(0) public String provider;
    @Parameter(1) public String dataflow;
    @Parameter(2) public String expectedAgency;
    @Parameter(3) public String expectedDSDIdent;
    
	@Test
	public void testDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier(provider, dataflow);
		assertNotNull("Null key family", keyF);
		if (expectedAgency != null)
			assertEquals("Wrong agency", expectedAgency, keyF.getAgency());
		assertEquals("Wrong Key Family", expectedDSDIdent, keyF.getId());
	}
}
