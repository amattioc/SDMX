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

import java.util.Map;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SdmxClientFactoryTest {

    @Test
    public void getProviders() {
        final Map<String, Provider> providers = SDMXClientFactory.getProviders();
        assertFalse(providers.isEmpty());

        final Provider ecb = providers.get("ECB");
        assertNotNull(ecb);
        assertEquals("ECB", ecb.getName());
        assertEquals("http://sdw-wsrest.ecb.europa.eu/service", ecb.getEndpoint().toString());
        assertFalse(ecb.isNeedsCredentials());
        assertFalse(ecb.isNeedsURLEncoding());
        assertTrue(ecb.isSupportsCompression());
        assertEquals("European Central Bank", ecb.getDescription());
    }
}
