import it.bancaditalia.oss.sdmx.client.SdmxClientHandler
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries

object GetSeries {
  def main(args: Array[String]): Unit = {

    val provider = "ECB"
    // val query = "EXR.A.USD.EUR.SP00.A"
    val query = "EXR.A.USD+NZD.EUR.SP00.A"
    val start = "2000"
    val end = "2005"
    val res = SdmxClientHandler.getTimeSeries(provider, query, start, end)
    val res2 = res.toArray.map(_.asInstanceOf[PortableTimeSeries])

    val dim = res2(0).getDimensionsArray
    res2(0).getObservations

  }
}
