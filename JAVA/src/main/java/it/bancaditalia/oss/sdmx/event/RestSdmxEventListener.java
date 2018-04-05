package it.bancaditalia.oss.sdmx.event;

/**
 *
 * @author Philippe Charles
 */
public interface RestSdmxEventListener 
{
	public static final RestSdmxEventListener NO_OP_LISTENER = new RestSdmxEventListener() {

		@Override
		public void onSdmxEvent(RestSdmxEvent event)
		{
			// Do nothing
		}
	};
	
	public void onSdmxEvent(RestSdmxEvent event);
}
