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
package it.bancaditalia.oss.sdmx.client;

import it.bancaditalia.oss.sdmx.exceptions.DataStructureException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;



public class ECBClientTest {
	public static void main(String[] args) throws SdmxException, DataStructureException{
//		System.err.println(SdmxClientHandler.getFlows("ECB", "Exchange*"));
//		
//		System.err.println(SdmxClientHandler.getDSDIdentifier("ECB", "MOBILE_EXR"));
//		
//		System.out.println(SdmxClientHandler.getDataFlowStructure("ECB", "EXR"));
//		System.err.println(SdmxClientHandler.getDimensions("ECB", "EXR"));
//		System.err.println(SdmxClientHandler.getTimeSeries("ECB", "EXR.Q|M|W.USD.EUR.SP00.A", null, null));
//		System.err.println(SdmxClientHandler.getTimeSeriesRevisions("ECB", "EXR.M.USD.EUR.SP00.A", null, null, "2015-01-01", true));
//		System.err.println(SdmxClientHandler.getTimeSeriesRevisions("ECB", "EXR.M.USD.EUR.SP00.A", null, null, null, true));
//		System.err.println(SdmxClientHandler.getFlows("ECB", "ICPF"));
//		System.err.println(SdmxClientHandler.getDimensions("ECB", "ICPF"));
//		System.err.println(SdmxClientHandler.getCodes("ECB", "ICPF", "FREQ"));
//		System.err.println(SdmxClientHandler.getTimeSeriesTable("ECB", "EXR.M.USD.EUR.SP00.A",  null, null, false, null, false));
//		SdmxClientHandler.getTimeSeries2("DEMO_SDMXV3", "EXR", "A..EUR.SP00.A", "c[FREQ]=A&c[CURRENCY]=USD&c[CURRENCY_DENOMINATOR]=EUR&c[EXR_TYPE]=SP00&c[EXR_SUFFIX]=A", null, null, "none", "none", null, false);
		System.err.println(SdmxClientHandler.filterCodes("BIS_PUBLIC", "WS_EER", "...AE"));
	}
}
