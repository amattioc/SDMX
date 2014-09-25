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
package it.bankitalia.reri.sia.sdmx.client;

import it.bankitalia.reri.sia.util.SdmxException;



public class EstatClientTest {
	public static void main(String[] args) throws SdmxException{
			//System.err.println(SdmxClientHandler.getCodes("EUROSTAT", "aact_ali01", "FREQ"));
			//System.err.println(SdmxClientHandler.getDataFlowStructure("EUROSTAT", "DS-008573"));
		//	System.err.println(SdmxClientHandler.getTimeSeries("EUROSTAT","prc_hicp_midx/..CP00.EU", null, null));
//		System.err.println(SdmxClientHandler.getDimensions("EUROSTAT", "DS-016890"));
//		System.err.println(SdmxClientHandler.getTimeSeries("EUROSTAT","DS-016890/.DE.FR.87019039.2.VALUE_IN_EUROS.", null, null));
		System.err.println(SdmxClientHandler.getFlows("EUROSTAT", null));
		
	}
}
