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

import it.bancaditalia.oss.sdmx.api.PortableDataSet;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.DataStructureException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

@RunWith(Parameterized.class)
public class TimeSeriesFromIDsIT
{
	@Parameters(name="{0} - {1}")
    public static Collection<Object[]> data() {
        return FilterProvidersToTest.filter(new Object[][] 
    		{
    			{ "WITS",     "DF_WITS_Tariff_TRAINS/A.068+076.000.250490.Reported", "2000", "2010",    2, null,         null },
    			{ "WB",       "WDI.A.SP_POP_TOTL.USA", "2000", "2010",    0, null,         "WDI.A.SP_POP_TOTL.USA" },
    			{ "UNDATA",   "DF_UNDATA_COUNTRYDATA/A...U....", "2010", "2015",  172, null,         null },
    			{ "ABS",      "ATSI_BIRTHS_SUMM/1...A", "2000", "2010",   16, null,         null },
    			{ "DEMO_SDMXV3", "EXR/.GBP...", null,   null,     8, null,         null },
    			{ "ECB",      "EXR/.GBP+USD...", null,   null,     22, null,         null },
    			{ "ECB",      "EXR.*.USD|GBP.EUR.SP00.A", "2000", "2010",   10, null,         null },
    			{ "ECB",      "EXR.A.USD.EUR.SP00.A;EXR.M.USD.EUR.SP00.A", "2000", "2010",    2, null,         null },
    			{ "ILO",      "DF_EMP_TEMP_SEX_AGE_NB/ITA.Q..SEX_F.", null,   null,      28, null,         null },
    			{ "IMF2",     "DS-WHDREO.A.PA.GGXCNL_GDP", "2000", "2015",    0, "2000",       "DS-WHDREO.A.PA.GGXCNL_GDP" },
    			{ "IMF",      "CPI.ABW.CPI..IX.A", "2001", "2015",    13, "2001",       "CPI.ABW.CPI.CP01.IX.A" },
    			{ "INSEE",    "CHOMAGE-TRIM-NATIONAL.T.CTCHC.VALEUR_ABSOLUE.FM.1.00-24.INDIVIDUS.CVS.TRUE", null,   null,      1, null,         null },
    			{ "ISTAT_RI", "163_24_DF_DCCN_QNA_1/Q.IT.B1G_B_W2_S1....Y.Y..2020M12", "2000", "2010",      0, "2000-Q1",         "163_24_DF_DCCN_QNA_1.Q.IT.B1G_B_W2_S1._T.Z.Z.Y.Y.B.2020M12" },
    			{ "OECD",     "QNA.ITA.B1_GE.CARSA.Q", "2000", "2010",    0, "2000-Q1",    "QNA.ITA.B1_GE.CARSA.Q" },
    			{ "OECD_NEW", "DSD_NAMAIN1@DF_QNA_EXPENDITURE_CAPITA.Q.Y.ISR.S1.S1.B1GQ_POP._Z._Z._Z.USD_PPP_PS.V.LA.T0102", "2000", "2010",    0, "2000-Q1",    "DSD_NAMAIN1@DF_QNA_EXPENDITURE_CAPITA.Q.Y.ISR.S1.S1.B1GQ_POP._Z._Z._Z.USD_PPP_PS.V.LA.T0102" },
    			{ "OECD_SDMXV3","DSD_NAMAIN1@DF_QNA_EXPENDITURE_CAPITA.Q.Y.ISR.S1.S1.B1GQ_POP._Z._Z._Z.USD_PPP_PS.V.LA.T0102", "2000", "2010",    0, "2000-Q1",    "DSD_NAMAIN1@DF_QNA_EXPENDITURE_CAPITA.Q.Y.ISR.S1.S1.B1GQ_POP._Z._Z._Z.USD_PPP_PS.V.LA.T0102" },
       			{ "EUROSTAT", "PRC_HICP_MIDX/..CP00.EU+DE+FR",  "2000", "2013-08", 0, "2000-01",    "PRC_HICP_MIDX.M.I05.CP00.DE" },
       			{ "BBK", 	  "BBASV/Q.DE.M.KV.A10.T.1.AT.S1._T.EUR",   "2017-Q1", "2017-Q4", 0, "2017-Q1",    "BBASV.Q.DE.M.KV.A10.T.1.AT.S1._T.EUR" }
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
	public void timeSeriesFromID() throws SdmxException, DataStructureException
	{
		List<PortableTimeSeries<Double>>  res = SdmxClientHandler.getTimeSeries(provider, query, start, end, false, null, false);
		assertNotNull("Null time series result", res);
		
		if (expectedCount == 0)
			assertTrue("No time series returned", res.size() > 0);
		else
			assertEquals("Wrong number of series returned", expectedCount, res.size());
		
		if (expectedName != null)
			assertEquals("Wrong name for first time series", expectedName, res.get(0).getName());
		if (expectedStart != null)
			assertEquals("Wrong start date for time series", expectedStart, res.get(0).get(0).getTimeslot());
		
		PortableDataSet<Double> res2 = SdmxClientHandler.getTimeSeriesTable(provider, query, start, end, false, null, false);
		assertNotNull("Null dataset result", res2);
		assertTrue("Empty dataset returned", res2.getRowCount() > 0);
	}
}
