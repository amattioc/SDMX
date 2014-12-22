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

public class OECDTest {
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
		assertEquals("Wrong description for AEO", "OECD,QNA ; Quarterly National Accounts", descr);
	}

	@Test
	public void testGetDimensions() throws SdmxException {
		List<Dimension> dim = SdmxClientHandler.getDimensions(OECD.class.getSimpleName(), "QNA");
		assertNotNull("Null getDimensions result QNA", dim);
		String result = "[Dimension [id=LOCATION, position=1, codelist=Codelist [id=OECD/CL_QNA_LOCATION, codes={G7M=G7, CHE=Switzerland, EST=Estonia, BEL=Belgium, ESP=Spain, PRT=Portugal, HUN=Hungary, CZE=Czech Republic, POL=Poland, CAN=Canada, EA18=Euro area (18 countries), FIN=Finland, SWE=Sweden, SAU=Saudi Arabia, GRC=Greece, NOR=Norway, IND=India, GBR=United Kingdom, FRME=Former Economies, USA=United States, OEU=OECD - Europe, LVA=Latvia, ITA=Italy, NLD=Netherlands, FRA=France, SVN=Slovenia, JPN=Japan, IDN=Indonesia, SVK=Slovak Republic, LUX=Luxembourg, EU28=European Union (28 countries), OEF=OECD - FORMER EUROPE (19 COUNTRIES), ISR=Israel, OTO=OECD - Total, ZAF=South Africa, MEX=Mexico, ISL=Iceland, TUR=Turkey, KOR=Korea, DEW=Former Federal Republic of Germany, OTF=OECD - FORMER TOTAL, DEU=Germany, NZL=New Zealand, AUT=Austria, AUS=Australia, NAT=NAFTA, NMEC=Non-OECD Member Economies, DNK=Denmark, RUS=Russia, BRA=Brazil, CHN=China (People s Republic of), EU15=European Union (15 countries), CHL=Chile, G20=G20, ARG=Argentina, IRL=Ireland}]], Dimension [id=SUBJECT, position=2, codelist=Codelist [id=OECD/CL_QNA_SUBJECT, codes={P7=Imports of goods and services, P32S13=Collective consumption expenditure of general government, P6=Exports of goods and services, P5=Gross capital formation, P3=Final consumption expenditure, B11=External balance of goods and services, RB1_GE=Residual item, P53=Acquisitions less disposals of valuables, P52=Changes in inventories, P51=Gross fixed capital formation, B1_GE=Gross domestic product - expenditure approach, P31S14_S15=Private final consumption expenditure, P52_P53=Changes in inventories and acquisitions less disposals of valuables, P3S13=General government final consumption expenditure, P3_P51=add. Final domestic demand (final consumption+GFCF), P62=Exports of services, P61=Exports of goods, P3_P5=add. Gross national expenditure (final consumption+gross capital formation), PPPGDP=add. Purchasing power parity of GDP, GDP=Gross domestic product, P31S15=Final consumption expenditure of non-profit institutions serving households, P41=add. Actual individual consumption, P72=Imports of services, P31S14=Final consumption expenditure of households, P71=Imports of goods, P31S13=Individual consumption expenditure of general government}]], Dimension [id=MEASURE, position=3, codelist=Codelist [id=OECD/CL_QNA_MEASURE, codes={VNBARSA=Millions of national currency, constant prices, national base year, annual levels, seasonally adjusted, VNBQR=Millions of national currency, constant prices, national base year, quarterly levels, VIXNBSA=Volume index, national base/reference year, seasonally adjusted, LNBQRSA=Millions of national currency, chained volume estimates, national reference year, quarterly levels, seasonally adjusted, DOBSA=Deflator, OECD reference year, seasonally adjusted, CTQRGPSA=Contributions to Q-o-Q GDP growth, seasonally adjusted, CPCARSA=Millions of US dollars, current prices, current PPPs, annual levels, seasonally adjusted, VOBARSA=Millions of national currency, volume estimates, OECD reference year, annual levels, seasonally adjusted, VIXOBSA=Volume index, OECD reference year, seasonally adjusted, CUR=Current prices, CARSA=Millions of national currency, current prices, annual levels, seasonally adjusted, GRW=Growth rates, VNBQRSA=Millions of national currency, constant prices, national base year, quarterly levels, seasonally adjusted, HRSSA=Millions of hours worked, seasonally adjusted, POP=Population and employment measures, CAR=Millions of national currency, current prices, annual levels, IND=Volume and price indices, VNBAR=Millions of national currency, constant prices, national base year, annual levels, CQRSA=Millions of national currency, current prices, quarterly levels, seasonally adjusted, LNBARSA=Millions of national currency, chained volume estimates, national reference year, annual levels, seasonally adjusted, GYSA=Growth rate compared to the same quarter of previous year, seasonally adjusted, GPSA=Growth rate compared to previous quarter, seasonally adjusted, VIXNB=Volume index, national base/reference year, VOL=Volumes, LNBQR=Millions of national currency, chained volume estimates, national reference year, quarterly levels, PERSA=Thousands of persons, seasonally adjusted, CQR=Millions of national currency, current prices, quarterly levels, VPVOBARSA=Millions of US dollars, volume estimates, fixed PPPs, OECD reference year, annual levels, seasonally adjusted, DNBSA=Deflator, national base/reference year, seasonally adjusted, HRS=Millions of hours worked, PER=Thousands of persons}]], Dimension [id=FREQUENCY, position=4, codelist=Codelist [id=OECD/CL_QNA_FREQUENCY, codes={A=Annual, Q=Quarterly}]]]";
		assertEquals("Wrong dimensions for QNA", result, dim.toString());
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
		String annual = res.get(0).getName();
		assertEquals("Wrong name for first time series", "QNA.ITA.B1_GE.CARSA.Q", annual);
		String start = res.get(0).getTimeSlots().get(0);
		assertEquals("Wrong start date for time series", "1960Q1", start);
	}

}
