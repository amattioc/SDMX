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

import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;

import java.util.ServiceLoader;

public class ProvidersTest {
	public static void main(String[] args){
		ServiceLoader<GenericSDMXClient> ldr = ServiceLoader.load(GenericSDMXClient.class);
	
	    for (GenericSDMXClient provider : ldr) {
	        System.err.println(provider.getClass().getSimpleName());
	    }
	}
}
