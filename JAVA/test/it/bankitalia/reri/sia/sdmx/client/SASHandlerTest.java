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

public class SASHandlerTest {

	public static void main(String[] args) {
		//System.out.println(SASClientHandler.makeGetDimensions("ECB", "EXR"));
		SASClientHandler h = new SASClientHandler();
		System.out.println(h.makeGetTimeSeries("ECB", "EXR.A+M.*.EUR.SP00.A", null, null));
		System.err.println(h.getName(0));
//		for (int i = 0; i < SASClientHandler.getNumberOfMeta(); i++) {
//			System.out.println(SASClientHandler.getMeta(i));
//		}
//		for (int i = 0; i < SASClientHandler.getNumberOfData(); i++) {
//			System.out.println(SASClientHandler.getName(i) + " , " + SASClientHandler.getTimestamp(i) + " , " + 
//					SASClientHandler.getData(i) + " , " + SASClientHandler.getStatus(i));
//		}
	}

}
