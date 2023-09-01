package it.bancaditalia.oss.sdmx.ut;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import org.junit.Test;

import java.util.Map;

public class HttpHeadersTest {
    @Test
    public void defaultAcceptHeaderTest() throws SdmxException {
        final String providerName = HttpHeadersTest.class.getSimpleName();
        final String providerEndpoint = "https://server/sdmx/2.1";

        SdmxClientHandler.addProvider(providerName, providerEndpoint,
                false, false, false, "Test provider");

        final Map<String, String> flows = SdmxClientHandler.getFlows(providerName, "");
    }
}
