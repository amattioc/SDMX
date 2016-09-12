/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package it.bancaditalia.oss.sdmx.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

/**
 * InputStreamReader that disconnects HttpURLConnection on close.
 *
 * @author Philippe Charles
 */
public final class DisconnectOnCloseReader extends InputStreamReader {

	/**
	 * Factory that creates a reader from stream, charset and connection.
	 *
	 * @param stream a non-null stream
	 * @param charset a non-null charset
	 * @param conn a non-null connection
	 * @return a non-null reader
	 */
	public static final DisconnectOnCloseReader of(InputStream stream, Charset charset, HttpURLConnection conn) {
		return new DisconnectOnCloseReader(stream, charset, conn);
	}

	private final HttpURLConnection conn;

	private DisconnectOnCloseReader(InputStream stream, Charset charset, HttpURLConnection conn) {
		super(stream, charset);
		this.conn = conn;
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
		} finally {
			conn.disconnect();
		}
	}
}
