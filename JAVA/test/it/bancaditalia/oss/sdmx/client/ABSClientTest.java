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



public class ABSClientTest {
	public static void main(String[] args) throws SdmxException{
//		System.err.println(SdmxClientHandler.getFlows("ABS", null));
//		System.err.println(SdmxClientHandler.getDSDIdentifier("ABS", "ABS_NRP9_ASGS"));
//		System.err.println(SdmxClientHandler.getDimensions("ABS", "ABS_NRP9_ASGS"));
//		System.err.println(SdmxClientHandler.getDataFlowStructure("ABS", "ABS_NRP9_ASGS"));
//		System.err.println(SdmxClientHandler.getCodes("ABS", "ABS_NRP9_ASGS", "FREQUENCY"));
		System.err.println(SdmxClientHandler.getTimeSeries("ABS", "ABS_NRP9_ASGS.EC43.AUS.0.A", "2000", "2010"));
		

	}
}
