package it.bancaditalia.oss.sdmx.client.custom;

import it.bancaditalia.oss.sdmx.api.Dataflow;

import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory;
import it.bancaditalia.oss.sdmx.parser.v21.Sdmx21Queries;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;

import javax.net.ssl.SSLSocketFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * ABS ReST 2.1 endpoint
 */
public class ABS2 extends RestSdmxClient {

  public static String ENDPOINT = "http://nsi-stable-siscc.redpelicans.com/rest";

  /**
   * @throws URISyntaxException
   */
  public ABS2() throws URISyntaxException
  {
    super("ABS2", new URI(ENDPOINT), false, false, false);
  }

}
