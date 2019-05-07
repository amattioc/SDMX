package it.bancaditalia.oss.sdmx.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

@RunWith(Parameterized.class)
public class TimeSeriesFromIDsIT
{
	@Parameters(name="{0} - {1}")
    public static Collection<Object[]> data() {
        return FilterProvidersToTest.filter(new Object[][] 
    		{
    			{ "WITS",     "DF_WITS_Tariff_TRAINS/A.076.000.250490.AVEEstimated",                              null,   null,      1, null,         null },
    			{ "WITS",     "DF_WITS_Tariff_TRAINS/A.068+076.000.250490.Reported",                              "2000", "2010",    2, null,         null },
    			{ "WB",       "WDI.A.SP_POP_TOTL.USA",                                                            "2000", "2010",    0, null,         "WDI.A.SP_POP_TOTL.USA" },
    			{ "WB",       "WDI.A.SP_POP_TOTL.",                                                               "2000", "2010",    0, null,         null },
    			{ "WB",       "WDI.A.SP_POP_TOTL.ITA+USA",                                                        null,   null,      0, null,         null },
    			{ "UNDATA",   "DF_UNDATA_COUNTRYDATA/A...U....",                                                  "2010", "2015",  172, null,         null },
    			{ "UIS",      "CE/............IT..",                                                              null,   "2015",  285, null,         null },
    			{ "ABS",      "ATSI_BIRTHS_SUMM/1...A",                                                           null,   null,     16, null,         null },
    			{ "ABS",      "ATSI_BIRTHS_SUMM/1...A",                                                           "2000", "2010",   16, null,         null },
    			{ "ABS",      "ATSI_BIRTHS_SUMM/1+4...A",                                                         "2000", "2010",   32, null,         null },
    			{ "ECB",      "EXR/.GBP+USD...",                                                          null,   null,     16, null,         null },
    			{ "ECB",      "EXR.*.USD|GBP.EUR.SP00.A",                                                         "2000", "2010",   10, null,         null },
    			{ "ECB",      "EXR.A.USD.EUR.SP00.A;EXR.M.USD.EUR.SP00.A",                                        "2000", "2010",    2, null,         null },
    			{ "ILO",      "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB/YI.MEX.A.463.EMP_TEMP_NB.SEX_F.AGE_10YRBANDS_TOTAL", null,   null,      1, null,         null },
    			{ "ILO",      "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB/YI.MEX.A.463...",                                    null,   null,      0, null,         null },
    			{ "IMF2",     "DS-WHDREO.A.PA.GGXCNL_GDP",                                                        "2000", "2015",    0, "2011",       "DS-WHDREO.A.PA.GGXCNL_GDP" },
    			{ "IMF_SDMX_CENTRAL", "SPI.SPI.FR.FPE_IX._Z.D",                                                  "2008", null,       0, "2015-02-02", "SPI.SPI.FR.FPE_IX._Z.D" },
    			{ "INEGI",    "DF_STEI/..C1161+C1162+C5004.....",                                                 "1980", "2010",    0, "2001-01",    "DF_STEI.MX.M.C1161.N.29.Z.Z.2003" },
    			{ "INSEE",    "CNA-2010-CONSO-SI..CNA_CONSO_SI.S15.PCH.P3.A88-88.VALEUR_ABSOLUE.FE.EUR2010.BRUT", null,   null,      1, null,         null },
    			{ "ISTAT",    "115_362/M....",                                                                    null,   null,      0, null,         "115_362.M.F.N.IT.CONS_PROD" },
    			{ "NBB",      "NICP2013/HEALTH+XEFUN0.M",                                                         "2006", "2010",    0, "2006-06",    "NICP2013.HEALTH.M" },
    			{ "OECD",     "QNA.ITA.B1_GE.CARSA.Q",                                                            "2000", "2010",    0, "2000-Q1",    "QNA.ITA.B1_GE.CARSA.Q" },
    			{ "EUROSTAT", "prc_hicp_midx/..CP00.EU+DE+FR",                                                    "2000", "2013-08", 0, "2013-08",    "prc_hicp_midx.M.I05.CP00.DE" },
    		}, 0);
    }
    
	@Parameter(0) public String provider;
    @Parameter(1) public String query;
    @Parameter(2) public String start;
    @Parameter(3) public String end;
    @Parameter(4) public int    expectedCount;
    @Parameter(5) public String expectedStart;
    @Parameter(6) public String expectedName;

	@Test
	public void timeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries<Double>>  res = SdmxClientHandler.getTimeSeries(provider, query, start, end);
		assertNotNull("Null time series result", res);
		if (expectedCount == 0)
			assertTrue("No time series returned", res.size() > 0);
		else
			assertEquals("Wrong number of series returned", expectedCount, res.size());
		if (expectedName != null)
			assertEquals("Wrong name for first time series", expectedName, res.get(0).getName());
		if (expectedStart != null)
			assertEquals("Wrong start date for time series", expectedStart, res.get(0).get(0).getTimeslot());
	}
}
