package it.bancaditalia.oss.sdmx.exceptions;

public class SdmxUnknownProviderException extends SdmxException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SdmxUnknownProviderException(String provider) {
		super("Provider \"" + provider + "\" is not registered.", null);
	}

	public SdmxUnknownProviderException(String provider, Exception cause) {
		super("Provider \"" + provider + "\" is not registered.", cause);
	}

}
