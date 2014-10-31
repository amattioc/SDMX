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

# get the list of dataflows of this provider
getFlows <- function(provider, pattern='') {		
  jlist <- J("it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler")$getFlows(provider, pattern)
  res = convertHashTable(jlist)
	return(res)
}

# get the key family of the given dataflow (after retrieving the key family)
getDSDIdentifier <- function(provider, dataflow) {		
  res <- J("it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler")$getDSDIdentifier(provider, dataflow)
	return(.jstrVal(res))
}

# add a new sdmx provider
addProvider <- function(name, endpoint, needsCredentials=F) {  	
  J("it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler")$addProvider(name, endpoint, needsCredentials)
}

# get dimensions of the given dataflow (after retrieving the key family)
getDimensions <- function(provider, dataflow) {           
  res <- J("it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler")$getDimensions(provider, dataflow)
  jlist <- .jcall(res,"[Ljava/lang/Object;","toArray");
  res = convertDimList(jlist)
  return(res)
}

# get the time series matching the parameters
getTimeSeries <- function(provider, id, start='', end='') {		
  getSDMX(provider, id, start, end)
}

# get the time series matching the parameters
getSDMX <- function(provider, id, start='', end='') {
  res <- J("it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler")$getTimeSeries(provider, id, start, end)
  #convert to an R list
  res = convertTSList(res)
  return(res) 
}

# get the list of available data providers
getProviders <- function() {		
  jlist <- J("it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler")$getProviders()
	res = convertStringList(jlist)
	return(res) 
}

# get the list of codes for a dimension
getCodes <- function(provider, flow, dimension){
  javaKeys<-J("it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler")$getCodes(provider, flow, dimension)
  keys = convertHashTable(javaKeys)
  return(keys)
}

# call help browser
sdmxHelp<- function(){
  J("it.bankitalia.reri.sia.sdmx.helper.SDMXHelper")$start()
}