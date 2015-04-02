#' SdmxClient
#'
#' \code{getFlows}: extract the list of DataFlows of a provider. This function is used to query the list of dataflows of the provider. A matching pattern can be provided, if needed.
#'
#' getFlows(provider, pattern)
#'
#' @param agency the agency identifier of the provider
#' @param dataflow the identifier of the dataflow
#' @param dimension the identifier of the dimension
#' @param end the end time - optional
#' @param endpoint the URL where the provider resides
#' @param needsCredentials set this to TRUE if the user needs to authenticate to query the provider
#' @param pattern the pattern to match against the dataflow id or description. If a pattern is not provided, all dataflows are returned.
#' @param provider the name of the provider
#' @param name the name of the provider
#' @param start the start time - optional
#'
#' @author Attilio Mattiocco \email{Attilio.Mattiocco@@bancaditalia.it}, Diana Nicoletti
#' @keywords rJava
#' @rdname SdmxClient
#' @export
#' @examples
#' ## get all flows from ECB
#' flows = getFlows('ECB')
#' ## get all flows that contain the 'EXR
#' flows = getFlows('ECB','*EXR*')
getFlows <- function(provider, pattern='') {
  jlist <- J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getFlows(provider, pattern)
  res = convertHashTable(jlist)
	return(res)
}

#' getDSDIdentifier
#'
#' \code{getDSDIdentifier}: extract the KeyFamily identifier of a DataFlow. This function is used to retrieve the name of the keyfamily of the input dataflow.
#'
#' getDSDIdentifier(provider, dataflow)
#'
#' @rdname SdmxClient
#' @export
#' @examples
#' id = getDSDIdentifier('ECB','EXR')
getDSDIdentifier <- function(provider, dataflow) {
  res <- J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getDSDIdentifier(provider, dataflow)
	return(.jstrVal(res))
}

#' addProvider
#'
#' \code{addProvider}: Configure a new data provider (only SDMX 2.1 REST providers are supported). This function can be used to configure a new (SDMX 2.1 compliant, REST based) data provider.
#'
#' addProvider(name, agency, endpoint, needsCredentials)
#'
#' @rdname SdmxClient
#' @export
#' @examples
#' addProvider('pname', 'pagency', 'pendpoint', F)
#' addProvider <- function(name, endpoint, needsCredentials=F, needsURLEncoding=F, supportsCompression=T, description='') {
  J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$addProvider(name, endpoint, needsCredentials, needsURLEncoding, supportsCompression, description)
}

#' getDimensions
#'
#' \code{getDimensions}: extract the dimensions of a DataFlow. This function is used to retrieve the list of dimensions of the input dataflow
#'
#' getDimensions(provider, dataflow)
#'
#' @rdname SdmxClient
#' @export
#' @examples
#' dims = getDimensions('ECB','EXR')
getDimensions <- function(provider, dataflow) {
  res <- J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getDimensions(provider, dataflow)
  jlist <- .jcall(res,"[Ljava/lang/Object;","toArray");
  res = convertDimList(jlist)
  return(res)
}

#' getTimeSeries
#'
#' \code{getTimeSeries}: extract a list of time series. This function is used to extract a list of time series identified by the parameters provided in input.
#' getTimeSeries(provider, dataflow, start, end)
#'
#' @rdname SdmxClient
#' @export
#' @examples
#' ## get single time series: EXR.A.USD.EUR.SP00.A (alternatively: EXR/A+M.USD.EUR.SP00.A)
#' my_ts=getTimeSeries('ECB','EXR.A.USD.EUR.SP00.A')
#' ## get monthly and annual frequency: 'EXR.A|M.USD.EUR.SP00.A' (alternatively: EXR/A+M.USD.EUR.SP00.A)
#' my_ts=getTimeSeries('ECB','EXR.A|M.USD.EUR.SP00.A')
#' ## get all available frequencies: 'EXR.*.USD.EUR.SP00.A' (alternatively: EXR/.USD.EUR.SP00.A)
#' my_ts=getTimeSeries('ECB','EXR.*.USD.EUR.SP00.A')
getTimeSeries <- function(provider, id, start='', end='') {
  getSDMX(provider, id, start, end)
}

#' getSDMX
#'
#' \code{getSDMX}: extract a list of time series. This function is exactly the same as \code{getTimeSeries}.
#'
#' getSDMX(provider, dataflow, start, end)
#'
#' @rdname SdmxClient
#' @export
#' @examples
#' ## get single time series: EXR.A.USD.EUR.SP00.A (alternatively: EXR/A+M.USD.EUR.SP00.A)
#' my_ts=getSDMX('ECB','EXR.A.USD.EUR.SP00.A')
#' ## get monthly and annual frequency: 'EXR.A|M.USD.EUR.SP00.A' (alternatively: EXR/A+M.USD.EUR.SP00.A)
#' my_ts=getSDMX('ECB','EXR.A|M.USD.EUR.SP00.A')
#' ## get all available frequencies: 'EXR.*.USD.EUR.SP00.A' (alternatively: EXR/.USD.EUR.SP00.A)
#' my_ts=getSDMX('ECB','EXR.*.USD.EUR.SP00.A')
getSDMX <- function(provider, id, start='', end='') {
  res <- J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getTimeSeries(provider, id, start, end)
  #convert to an R list
  res = convertTSList(res)
  return(res)
}

#' getProviders
#'
#' \code{getProviders}: extract the list of available Data Providers. This function is used to query the list of data providers.
#'
#' getProviders()
#'
#' @rdname SdmxClient
#' @export
#' @examples
#' getProviders()
getProviders <- function() {
  jlist <- J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getProviders()
	res = convertStringList(jlist)
	return(res)
}

#' getCodes
#'
#' \code{getCodes}: extract the codes of a dimension. This function is used to retrieve the list of codes available for the input dimension and flow.
#'
#' getCodes(provider, dataflow, dimension)
#'
#' @rdname SdmxClient
#' @export
#' @examples
#' dims=getCodes('ECB', 'EXR', 'FREQ')
getCodes <- function(provider, flow, dimension){
  javaKeys<-J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getCodes(provider, flow, dimension)
  keys = convertHashTable(javaKeys)
  return(keys)
}

#' sdmxHelp
#'
#' \code{sdmxHelp}: open a helper graphical application. This function opens a small sdmx metadata browser that can be helpful when building queries.
#'
#' sdmxHelp()
#'
#' @rdname SdmxClient
#' @export
#' @examples
#' sdmxHelp()
sdmxHelp<- function(internalJVM=T){
  # fix for #41 on OS X
  if(internalJVM){
    J("it.bancaditalia.oss.sdmx.helper.SDMXHelper")$start()
  }
  else{
    JAVA = Sys.which('java')
    if(length(JAVA) > 0 && nchar(JAVA[1]) > 0){
      javaExe = JAVA[1]
      message(paste0('JVM detected: ', javaExe))
      system(paste0(javaExe, ' -classpath ', file.path(find.package('RJSDMX'), 'java', 'SDMX.jar'), ' it.bancaditalia.oss.sdmx.helper.SDMXHelper'), wait=F)
    }
    else{
      stop('Could not detect external JVM.')
    }
  }
}

# convert from list of zoo to data.frame
sdmxdf<- function(tslist, meta=F){
  ddf = NULL
  if(!missing(tslist) && length(tslist) != 0 && is.list(tslist)){
    dflist=lapply(tslist, sdmxzoo2df, meta)
    ddf=Reduce(function(x, y) merge(x, y, all=TRUE), dflist)
    rownames(ddf)<-NULL
  }
  else{
    cat('The time series list in input is missing or invalid\n')
  }
  return(ddf)
}
