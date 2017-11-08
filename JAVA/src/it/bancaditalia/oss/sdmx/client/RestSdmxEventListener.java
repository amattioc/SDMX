package it.bancaditalia.oss.sdmx.client;

import it.bancaditalia.oss.sdmx.api.Message;
import java.net.URL;

/**
 *
 * @author Philippe Charles
 */
public class RestSdmxEventListener {

	public static final RestSdmxEventListener NO_OP = new RestSdmxEventListener();
	
	public void onDataFooterMessage(URL query, Message msg) {
	}

	public void onRedirection(URL oldURL, URL newURL) {
	}
}
