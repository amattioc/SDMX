/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or - as soon they
* will be approved by the European Commission - subsequent
* versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the
* Licence.
* You may obtain a copy of the Licence at:
*
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in
* writing, software distributed under the Licence is
* distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied.
* See the Licence for the specific language governing
* permissions and limitations under the Licence.
*/

package it.bankitalia.reri.sia.sdmx.ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import it.bankitalia.reri.sia.sdmx.api.DSDIdentifier;
import it.bankitalia.reri.sia.sdmx.api.Dimension;
import it.bankitalia.reri.sia.sdmx.api.PortableTimeSeries;
import it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler;
import it.bankitalia.reri.sia.sdmx.client.custom.OECD;
import it.bankitalia.reri.sia.util.SdmxException;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class SdmxInterfaceTest {
	@BeforeClass
	public static void setUp() throws Exception {
	}

	@Test
	public void testGetAddProvider() throws SdmxException {
		SdmxClientHandler.addProvider("TEST", "http://sdw-wsrest.ecb.europa.eu/service", false);
		List<PortableTimeSeries> a = SdmxClientHandler.getTimeSeries("TEST", "EXR.A.GBP.EUR.SP00.A", null, null);
		assertNotNull("Null getTimeSeries result", a);
		assertEquals("Wrong number of results for getTimeSeries", 1, a.size());
	}
	@Test(expected=SdmxException.class)
	public void getDSDIdentifierFail1() throws SdmxException{
		SdmxClientHandler.getDSDIdentifier(null, "FFF");
	}
	@Test(expected=SdmxException.class)
	public void getDSDIdentifierFail2() throws SdmxException{
		SdmxClientHandler.getDSDIdentifier("", "FFF");
	}
	@Test(expected=SdmxException.class)
	public void getDSDIdentifierFail3() throws SdmxException{
		SdmxClientHandler.getDSDIdentifier("ECB", null);
	}
	@Test(expected=SdmxException.class)
	public void getDSDIdentifierFail4() throws SdmxException{
		SdmxClientHandler.getDSDIdentifier("ECB", "");
	}
	@Test(expected=SdmxException.class)
	public void getDSDIdentifierFail5() throws SdmxException{
		SdmxClientHandler.getDSDIdentifier("DUMMY", "EXR");
	}
	@Test(expected=SdmxException.class)
	public void getDSDIdentifierFail6() throws SdmxException{
		SdmxClientHandler.getDSDIdentifier("ECB", "DUMMY");
	}
	@Test
	public void getDSDIdentifier() throws SdmxException{
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("ECB", "EXR");
		assertNotNull("Null key family for EXR", keyF);
		assertEquals("Wrong Key Family", "ECB_EXR1", keyF.getId());
		assertEquals("Wrong agency", "ECB", keyF.getAgency());
	}

	@Test(expected=SdmxException.class)
	public void testGetFlowsFail1() throws SdmxException{
		SdmxClientHandler.getFlows(null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetFlowsFail2() throws SdmxException{
		SdmxClientHandler.getFlows("", null);
	}
	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("EUROSTAT", "prc_hicp_midx");
		assertNotNull("Null getFlows result", f);
		assertEquals("Wrong number of results for prc_hicp_midx", 1, f.size());
		String descr = f.get("prc_hicp_midx");
		assertEquals("Wrong description for prc_hicp_midx", "ESTAT,prc_hicp_midx,1.0 ; HICP (2005 = 100) - monthly data (index)", descr);
		f = SdmxClientHandler.getFlows("EUROSTAT", null);
		assertEquals("Wrong number of results for prc_hicp_midx", f.size() > 0, true);
		f = SdmxClientHandler.getFlows("EUROSTAT", "");
		assertEquals("Wrong number of results for prc_hicp_midx", f.size() > 0, true);
		f = SdmxClientHandler.getFlows("EUROSTAT", "pippo");
		assertEquals("Wrong number of results for pippo", f.size() == 0, true);
	}
	
	@Test(expected=SdmxException.class)
	public void testGetDimensionsFail1() throws SdmxException{
		SdmxClientHandler.getDimensions(null, "EXR");
	}
	@Test(expected=SdmxException.class)
	public void testGetDimensionsFail2() throws SdmxException{
		SdmxClientHandler.getDimensions("", "EXR");
	}
	@Test(expected=SdmxException.class)
	public void testGetDimensionsFail3() throws SdmxException{
		SdmxClientHandler.getDimensions("ECB", null);
	}
	@Test(expected=SdmxException.class)
	public void testGetDimensionsFail4() throws SdmxException{
		SdmxClientHandler.getDimensions("ECB", "");
	}
	@Test(expected=SdmxException.class)
	public void testGetDimensionsFail5() throws SdmxException{
		SdmxClientHandler.getDimensions("ECB", "FFF");
	}

	@Test
	public void testGetDimensions() throws SdmxException {
		List<Dimension> dim = SdmxClientHandler.getDimensions(OECD.class.getSimpleName(), "QNA");
		assertNotNull("Null getDimensions result QNA", dim);	
		String result = "[Dimension [id=LOCATION, position=1, codelist=Codelist [id=OECD/CL_QNA_LOCATION, codes={G7M=G7, CHE=Switzerland, EST=Estonia, BEL=Belgium, ESP=Spain, PRT=Portugal, HUN=Hungary, CZE=Czech Republic, POL=Poland, CAN=Canada, EA18=Euro area (18 countries), FIN=Finland, SWE=Sweden, SAU=Saudi Arabia, GRC=Greece, NOR=Norway, IND=India, GBR=United Kingdom, FRME=Former Economies, USA=United States, OEU=OECD - Europe, LVA=Latvia, ITA=Italy, NLD=Netherlands, FRA=France, SVN=Slovenia, JPN=Japan, IDN=Indonesia, SVK=Slovak Republic, LUX=Luxembourg, EU28=European Union (28 countries), OEF=OECD - FORMER EUROPE (19 COUNTRIES), ISR=Israel, OTO=OECD - Total, ZAF=South Africa, MEX=Mexico, ISL=Iceland, TUR=Turkey, KOR=Korea, DEW=Former Federal Republic of Germany, OTF=OECD - FORMER TOTAL, DEU=Germany, NZL=New Zealand, AUT=Austria, AUS=Australia, NAT=NAFTA, NMEC=Non-OECD Member Economies, DNK=Denmark, RUS=Russia, BRA=Brazil, CHN=China (People s Republic of), EU15=European Union (15 countries), CHL=Chile, G20=G20, ARG=Argentina, IRL=Ireland}]], Dimension [id=SUBJECT, position=2, codelist=Codelist [id=OECD/CL_QNA_SUBJECT, codes={P7=Imports of goods and services, P32S13=Collective consumption expenditure of general government, P6=Exports of goods and services, P5=Gross capital formation, P3=Final consumption expenditure, B11=External balance of goods and services, RB1_GE=Residual item, P53=Acquisitions less disposals of valuables, P52=Changes in inventories, P51=Gross fixed capital formation, B1_GE=Gross domestic product - expenditure approach, P31S14_S15=Private final consumption expenditure, P52_P53=Changes in inventories and acquisitions less disposals of valuables, P3S13=General government final consumption expenditure, P3_P51=add. Final domestic demand (final consumption+GFCF), P62=Exports of services, P61=Exports of goods, P3_P5=add. Gross national expenditure (final consumption+gross capital formation), PPPGDP=add. Purchasing power parity of GDP, GDP=Gross domestic product, P31S15=Final consumption expenditure of non-profit institutions serving households, P41=add. Actual individual consumption, P72=Imports of services, P31S14=Final consumption expenditure of households, P71=Imports of goods, P31S13=Individual consumption expenditure of general government}]], Dimension [id=MEASURE, position=3, codelist=Codelist [id=OECD/CL_QNA_MEASURE, codes={VNBARSA=Millions of national currency, constant prices, national base year, annual levels, seasonally adjusted, VNBQR=Millions of national currency, constant prices, national base year, quarterly levels, VIXNBSA=Volume index, national base/reference year, seasonally adjusted, LNBQRSA=Millions of national currency, chained volume estimates, national reference year, quarterly levels, seasonally adjusted, DOBSA=Deflator, OECD reference year, seasonally adjusted, CTQRGPSA=Contributions to Q-o-Q GDP growth, seasonally adjusted, CPCARSA=Millions of US dollars, current prices, current PPPs, annual levels, seasonally adjusted, VOBARSA=Millions of national currency, volume estimates, OECD reference year, annual levels, seasonally adjusted, VIXOBSA=Volume index, OECD reference year, seasonally adjusted, CUR=Current prices, CARSA=Millions of national currency, current prices, annual levels, seasonally adjusted, GRW=Growth rates, VNBQRSA=Millions of national currency, constant prices, national base year, quarterly levels, seasonally adjusted, HRSSA=Millions of hours worked, seasonally adjusted, POP=Population and employment measures, CAR=Millions of national currency, current prices, annual levels, IND=Volume and price indices, VNBAR=Millions of national currency, constant prices, national base year, annual levels, CQRSA=Millions of national currency, current prices, quarterly levels, seasonally adjusted, LNBARSA=Millions of national currency, chained volume estimates, national reference year, annual levels, seasonally adjusted, GYSA=Growth rate compared to the same quarter of previous year, seasonally adjusted, GPSA=Growth rate compared to previous quarter, seasonally adjusted, VIXNB=Volume index, national base/reference year, VOL=Volumes, LNBQR=Millions of national currency, chained volume estimates, national reference year, quarterly levels, PERSA=Thousands of persons, seasonally adjusted, CQR=Millions of national currency, current prices, quarterly levels, VPVOBARSA=Millions of US dollars, volume estimates, fixed PPPs, OECD reference year, annual levels, seasonally adjusted, DNBSA=Deflator, national base/reference year, seasonally adjusted, HRS=Millions of hours worked, PER=Thousands of persons}]], Dimension [id=FREQUENCY, position=4, codelist=Codelist [id=OECD/CL_QNA_FREQUENCY, codes={A=Annual, Q=Quarterly}]]]";
		assertEquals("Wrong dimensions for QNA", result, dim.toString());
	}
	
	@Test(expected=SdmxException.class)
	public void testGetCodesFail1() throws SdmxException{
		SdmxClientHandler.getCodes(null, "EXR", "FREQ");
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail2() throws SdmxException{
		SdmxClientHandler.getCodes("", "EXR", "FREQ");
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail3() throws SdmxException{
		SdmxClientHandler.getCodes("ECB", null, "FREQ");
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail4() throws SdmxException{
		SdmxClientHandler.getCodes("ECB", "", "FREQ");
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail5() throws SdmxException{
		SdmxClientHandler.getCodes("ECB", "EXR", null);
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail6() throws SdmxException{
		SdmxClientHandler.getCodes("ECB", "EXR", "");
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail7() throws SdmxException{
		SdmxClientHandler.getCodes("ECB", "DUMMY", "FREQ");
	}
	@Test(expected=SdmxException.class)
	public void testGetCodesFail8() throws SdmxException{
		SdmxClientHandler.getCodes("ECB", "EXR", "DUMMY");
	}
	
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail1() throws SdmxException {
		SdmxClientHandler.getTimeSeries(null, "EXR.A.GBP+USD.EUR.SP00.A", null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail2() throws SdmxException {
		SdmxClientHandler.getTimeSeries("", "EXR.A.GBP+USD.EUR.SP00.A", null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail3() throws SdmxException {
		SdmxClientHandler.getTimeSeries("DUMMY", "EXR.A.USD.EUR.SP00.A", null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail4() throws SdmxException {
		//wrong flow
		SdmxClientHandler.getTimeSeries("ECB", "DUMMY.A.USD.EUR.SP00.A", null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail5() throws SdmxException {
		//no flow
		SdmxClientHandler.getTimeSeries("ECB", "A.GBP.EUR.SP00.A", null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail6() throws SdmxException {
		//wrong dimension list
		SdmxClientHandler.getTimeSeries("ECB", "EXR.EUR.SP00.A", null, null);
	}
	@Test(expected=SdmxException.class)
	public void testGetTimeSeriesFail7() throws SdmxException {
		//wrong dimension list
		SdmxClientHandler.getTimeSeries("ECB", "EXR.A.N.SS.A.A.EUR.SP00.A", null, null);
	}

}
