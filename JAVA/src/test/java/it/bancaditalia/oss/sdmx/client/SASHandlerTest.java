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

import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

public class SASHandlerTest {

	public static void main(String[] args) {
		//System.out.println(SASClientHandler.makeGetDimensions("ECB", "EXR"));
		try {
			System.out.println(SASClientHandler.makeGetTimeSeries("ECB", "EXR.A+M.*.EUR.SP00.A", null, null));
			System.out.println();
			for (int i = 0; i < SASClientHandler.getNumberOfData(); i++) {
					System.out.println(SASClientHandler.getDataName(i) + " , " + SASClientHandler.getDataTimestamp(i) + " , " + 
							SASClientHandler.getDataObservation(i));
			}
			for (int i = 0; i < SASClientHandler.getNumberOfMeta(); i++) {
				System.out.println(SASClientHandler.getMetaName(i) + " , " + 
									SASClientHandler.getMetaKey(i) + " , " + 
									SASClientHandler.getMetaValue(i) + " , " + 
									SASClientHandler.getMetaType(i));
			}
			for (int i = 0; i < SASClientHandler.getNumberOfObsMeta(); i++) {
				System.out.println(SASClientHandler.getObsMetaName(i) + " , " + 
									SASClientHandler.getObsMetaKey(i) + " , " + 
									SASClientHandler.getObsMetaValue(i) + " , " + 
									SASClientHandler.getObsMetaDate(i));
			}
		} catch (SdmxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
