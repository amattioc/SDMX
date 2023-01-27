package it.bancaditalia.oss.sdmx.event;

/**
 *
 * @author Philippe Charles
 */
public interface RestSdmxEventListener 
{
	public static final RestSdmxEventListener NO_OP_LISTENER = event -> {};
	
	public void onSdmxEvent(RestSdmxEvent event);
}
