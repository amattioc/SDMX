# Copyright 2010,2014 Bank Of Italy
#
# Licensed under the EUPL, Version 1.1 or - as soon they
# will be approved by the European Commission - subsequent
# versions of the EUPL (the "Licence");
# You may not use this work except in compliance with the
# Licence.
# You may obtain a copy of the Licence at:
#
#
# http://ec.europa.eu/idabc/eupl
#
# Unless required by applicable law or agreed to in
# writing, software distributed under the Licence is
# distributed on an "AS IS" basis,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
# express or implied.
# See the Licence for the specific language governing
# permissions and limitations under the Licence.
#

# Main class for consuming SDMX web services
#
# Author: Attilio Mattiocco
###############################################################################

#' get provider flow list
#'
#' Extract the list of DataFlows of a provider. This function is used to query the list of dataflows of the provider. A matching pattern can be provided, if needed.
#'
#' getFlows(provider, pattern)
#'
#' @param pattern the pattern to match against the dataflow id or description. If a pattern is not provided, all dataflows are returned.
#' @param provider the name of the provider
#'
#' @author Attilio Mattiocco \email{Attilio.Mattiocco@@bancaditalia.it}, Diana Nicoletti
#' @keywords rJava
#' @rdname getFlows
#' @export
#' @examples
#'\dontrun{
#' ## get all flows from ECB
#' flows = getFlows('ECB')
#' ## get all flows that contain the 'EXR
#' flows = getFlows('ECB','*EXR*')
#' }
getFlows <- function(provider, pattern='') {
  jlist <- J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getFlows(provider, pattern)
  res = convertHashTable(jlist)
	return(res)
}

#' get DSD Identifier for dataflow
#'
#' Extract the dsd identifier of a DataFlow. This function is used to retrieve the name of the keyfamily of the input dataflow.
#'
#' getDSDIdentifier(provider, dataflow)
#'
#' @param provider the name of the provider
#' @param dataflow the identifier of the dataflow
#' @rdname getDSDIdentifier
#' @export
#' @examples
#'\dontrun{
#' id = getDSDIdentifier('ECB','EXR')
#' }
getDSDIdentifier <- function(provider, dataflow) {
  res <- J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getDSDIdentifier(provider, dataflow)
	return(.jstrVal(res))
}

#' add new provider
#'
#' Configure a new data provider (only SDMX 2.1 REST providers are supported). This function can be used to configure a new (SDMX 2.1 compliant, REST based) data provider.
#'
#' addProvider(name, agency, endpoint, needsCredentials)
#'
#' @param name the name of the provider
#' @param endpoint the URL where the provider resides
#' @param needsCredentials set this to TRUE if the user needs to authenticate to query the provider
#' @param needsURLEncoding set this to TRUE if the provider does not handle character '+' in URLs
#' @param supportsCompression set this to TRUE if the provider is able to handle compression
#' @param description a brief text description of the provider
#' @rdname addProvider
#' @export
#' @examples
#' \dontrun{
#' addProvider('pname', 'pendpoint', F)
#' }
addProvider <- function(name, endpoint, needsCredentials=FALSE, needsURLEncoding=FALSE, supportsCompression=TRUE, description='') {
  J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$addProvider(name, endpoint, needsCredentials, needsURLEncoding, supportsCompression, description)
}

#' get dsd dimensions for dataflow
#'
#' Extract the dimensions of a DataFlow. This function is used to retrieve the list of dimensions of the input dataflow
#'
#' getDimensions(provider, dataflow)
#'
#' @param dataflow the identifier of the dataflow
#' @param provider the name of the provider
#' @rdname getDimensions
#' @export
#' @examples
#' \dontrun{
#' dims = getDimensions('ECB','EXR')
#' }
getDimensions <- function(provider, dataflow) {
  res <- J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getDimensions(provider, dataflow)
  jlist <- .jcall(res,"[Ljava/lang/Object;","toArray");
  res = convertDimList(jlist)
  return(res)
}

#' get time series
#'
#' Extract a list of time series. This function is used to extract a list of time series identified by the parameters provided in input.
#' getTimeSeries(provider, dataflow, start, end)
#'
#' @param id identifier of the time series
#' @param provider the name of the provider
#' @param end the end time - optional
#' @param start the start time - optional
#' @rdname getTimeSeries
#' @export
#' @examples
#' \dontrun{
#' ## get single time series: EXR.A.USD.EUR.SP00.A (or: EXR/A+M.USD.EUR.SP00.A)
#' my_ts=getTimeSeries('ECB','EXR.A.USD.EUR.SP00.A')
#' ## get monthly and annual frequency: 'EXR.A|M.USD.EUR.SP00.A' (or: EXR/A+M.USD.EUR.SP00.A)
#' my_ts=getTimeSeries('ECB','EXR.A|M.USD.EUR.SP00.A')
#' ## get all available frequencies: 'EXR.*.USD.EUR.SP00.A' (or: EXR/.USD.EUR.SP00.A)
#' my_ts=getTimeSeries('ECB','EXR.*.USD.EUR.SP00.A')
#' }
getTimeSeries <- function(provider, id, start='', end='') {
  getSDMX(provider, id, start, end)
}

#' get time series and return a data.frame
#'
#' Extract a list of time series identified by the parameters provided in input, and return a data.frame as result.
#' getTimeSeriesTable(provider, dataflow, start, end)
#'
#' @param id identifier of the time series
#' @param provider the name of the provider
#' @param end the end time - optional
#' @param start the start time - optional
#' @rdname getTimeSeriesTable
#' @export
#' @examples
#' \dontrun{
#' ## get single time series: EXR.A.USD.EUR.SP00.A (alternatively: EXR/A+M.USD.EUR.SP00.A)
#' my_df=getTimeSeriesTable('ECB','EXR.A.USD.EUR.SP00.A')
#' ## get monthly and annual frequency: 'EXR.A|M.USD.EUR.SP00.A' (alternatively: EXR/A+M.USD.EUR.SP00.A)
#' my_df=getTimeSeriesTable('ECB','EXR.A|M.USD.EUR.SP00.A')
#' ## get all available frequencies: 'EXR.*.USD.EUR.SP00.A' (alternatively: EXR/.USD.EUR.SP00.A)
#' my_df=getTimeSeriesTable('ECB','EXR.*.USD.EUR.SP00.A')
#' }
getTimeSeriesTable <- function(provider, id, start='', end='') {
  res <- J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getTimeSeriesTable(provider, id, start, end)
  #convert to an R data.frame
  res = convertTSDF(res)
  return(res)
}

#' get data revisions
#'
#' Extract a list of time series starting from a specific update time and 
#' with history of revisions. This function works as \bold{getTimeSeries} and \bold{getSDMX} but the 
#' query can be narrowed to getting only observations that
#' were updated after a specific point in time, and eventually it returns the revision history of
#' the matching time series. This means that the result list can contain multiple instances 
#' of the same time series, each with the specific action and validity attributes.
#'
#' getTimeSeriesRevisions(provider, id, start, end, updatedAfter, includeHistory)
#'
#' @param id identifier of the time series
#' @param provider the name of the provider
#' @param end the end time - optional
#' @param start the start time - optional
#' @param updatedAfter the updatedAfter time - optional. It has to be in the form: 'YYYY-MM-DD'
#' @param includeHistory boolean parameter - optional. If true the full list of revisions will be returned
#' @rdname getTimeSeriesRevisions 
#' @export
#' @examples
#' \dontrun{
#' # get single time series with history: 
#' my_ts=getTimeSeriesRevisions('ECB','EXR.A.USD.EUR.SP00.A', includeHistory=TRUE)
#' # get single time series (only observations updated after january 1st 2015): 
#' my_ts=getTimeSeriesRevisions('ECB','EXR.A.USD.EUR.SP00.A', updatedAfter='2015', includeHistory=F)
#' # get single time series (full revision history starting from january 1st 2015): 
#' my_ts=getTimeSeriesRevisions('ECB','EXR.A.USD.EUR.SP00.A', updatedAfter='2015', includeHistory=TRUE)
#' }
getTimeSeriesRevisions <- function(provider, id, start='', end='', updatedAfter='', includeHistory=TRUE) {
  res <- J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getTimeSeriesRevisions(provider, id, start, end, updatedAfter, includeHistory)
  #convert to an R list
  res = convertTSList(res)
  return(res)
}

#' get data
#'
#' Extract a list of time series. This function is exactly the same as \code{getTimeSeries}.
#'
#' getSDMX(provider, dataflow, start, end)
#'
#' @param id identifier of the time series
#' @param provider the name of the provider
#' @param end the end time - optional
#' @param start the start time - optional
#' @rdname getSDMX
#' @export
#' @examples
#' \dontrun{
#' ## get single time series: EXR.A.USD.EUR.SP00.A (or: EXR/A+M.USD.EUR.SP00.A)
#' my_ts=getSDMX('ECB','EXR.A.USD.EUR.SP00.A')
#' ## get monthly and annual frequency: 'EXR.A|M.USD.EUR.SP00.A' (or: EXR/A+M.USD.EUR.SP00.A)
#' my_ts=getSDMX('ECB','EXR.A|M.USD.EUR.SP00.A')
#' ## get all available frequencies: 'EXR.*.USD.EUR.SP00.A' (or: EXR/.USD.EUR.SP00.A)
#' my_ts=getSDMX('ECB','EXR.*.USD.EUR.SP00.A')
#' }
getSDMX <- function(provider, id, start='', end='') {
  res <- J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getTimeSeries(provider, id, start, end)
  #convert to an R list
  res = convertTSList(res)
  return(res)
}

#' get available providers
#'
#' Extract the list of available Data Providers. This function is used to query the list of data providers.
#'
#' getProviders()
#'
#' @rdname getProviders
#' @export
#' @examples
#' \dontrun{
#' getProviders()
#' }
getProviders <- function() {
  jlist <- J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getProviders()
	res = convertStringList(jlist)
	return(res)
}

#' get dsd codes for dataflow
#'
#' Extract the codes of a dimension. This function is used to retrieve the list of codes available for the input dimension and flow.
#'
#' getCodes(provider, dataflow, dimension)
#'
#' @param flow the identifier of the dataflow
#' @param dimension the identifier of the dimension
#' @param provider the name of the provider
#' @rdname getCodes
#' @export
#' @examples
#' \dontrun{
#' codes=getCodes('ECB', 'EXR', 'FREQ')
#' }
getCodes <- function(provider, flow, dimension){
  javaKeys<-J("it.bancaditalia.oss.sdmx.client.SdmxClientHandler")$getCodes(provider, flow, dimension)
  keys = convertHashTable(javaKeys)
  return(keys)
}

#' open helper 
#'
#' Open a helper graphical application. This function opens a small sdmx metadata browser that can be helpful when building queries.
#'
#' sdmxHelp()
#'
#' @param internalJVM true (default) if the GUI has to live in the R JVM. Set this to false in MAC, to avoid issue #41
#' @rdname sdmxHelp
#' @export
#' @examples
#' \dontrun{
#' #opens the helper in the R JVM
#' sdmxHelp() 
#' #opens the helper in an external JVM
#' sdmxHelp(F) 
#' }
sdmxHelp <- function(internalJVM=T){
  # fix for #41 on OS X
  if(internalJVM){
    J("it.bancaditalia.oss.sdmx.helper.SDMXHelper")$start()
  }
  else{
    JAVA = Sys.which('java')
    if(length(JAVA) > 0 && nchar(JAVA[1]) > 0){
      javaExe = JAVA[1]
      message(paste0('JVM detected: ', javaExe))
      system(paste0(javaExe, ' -classpath ', file.path(find.package('RJSDMX'), 'java', 'SDMX.jar'), ' it.bancaditalia.oss.sdmx.helper.SDMXHelper'), wait=FALSE)
    }
    else{
      stop('Could not detect external JVM.')
    }
  }
}

#' convert time series to data.frame
#'
#' This function is used to transform the output of the getSDMX (or getTimeseries) 
#' functions from a list of time series to a data.frame. The metadata can be requested by explicitly passing the
#' appropriate parameters.
#'
#' sdmxdf()
#'
#' @param tslist the list of time series to be converted
#' @param meta set this to TRUE if you want metadata to be included (default: F, as this may increase the size of the result quite a bit)
#' @param id set this to FALSE if you do not want the time series id to be included (default: T)
#' @rdname sdmxdf
#' @export
#' @examples
#' \dontrun{
#' # a=getSDMX('ECB', 'EXR.A|Q|M|D.USD.EUR.SP00.A')
#' ddf = sdmxdf(a)
#' ddf = sdmxdf(a, meta=T)
#' }
sdmxdf <- function(tslist, meta=FALSE, id=TRUE){
  ddf = NULL
  if(!missing(tslist) && length(tslist) != 0 && is.list(tslist)){
    dflist=lapply(tslist, sdmxzoo2df, id, meta)
    ddf=Reduce(function(x, y) merge(x, y, all=TRUE), dflist)
    rownames(ddf)<-NULL
  }
  else{
    cat('The time series list in input is missing or invalid\n')
  }
  return(ddf)
}
