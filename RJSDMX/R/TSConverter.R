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

# Basic class for converting from Java objects to R

# convert a zoo to a df
sdmxzoo2df <- function (tts, setId, setMeta) {
  ddf = NULL
  if(!missing(tts) && length(tts) != 0 && is.zoo(tts)){
    n = length(tts)
    time=as.character(index(tts))
    data=as.numeric(tts)
    header=c('TIME_PERIOD', 'OBS_VALUE')
    ddf=data.frame(time, data)      
    if(setMeta){
      metaddf = data.frame(row.names = 1:n)
      metaheader = NULL
      for(x in names(attributes(tts))){
        if(x != 'ID' && x != 'class' && x != 'frequency' && x != 'index'){
          val = attr(tts, x)
          if(length(val) == 1){
            # ts level attributes go close to id
            metaddf = cbind(metaddf, rep(attr(tts, x), n))
            metaheader=c(metaheader, x)
          }
          else if(length(val) == n){
            # obs level attributes go close to data
            ddf = cbind(val, ddf)
            header=c(x, header)
          }
        }
      }
      ddf = cbind(metaddf, ddf)
      header=c(metaheader, header)
    }
    if(setId){
      id=rep(attr(tts, 'ID'), n)
      ddf=cbind(id, ddf)
      header=c('ID', header)
    }
    colnames(ddf) = header
  }
  else{
    message('The time series in input is missing or invalid\n')
  }
  return(ddf)
}

# convert a java List<String>
convertStringList <- function (javaList) {
	rList = as.list(javaList)
	numOfStrings = length(rList)
  result=unlist(lapply(rList, .jstrVal))
	return(result)
}

# convert a java List<Dimension>
convertDimList <- function (javaList) {
  rList = as.list(javaList)
  numOfDims = length(rList)
  result = list()
  if( numOfDims > 0 ) {
    for (i in 1:numOfDims)  {
      name=.jcall(rList[[i]],"Ljava/lang/String;","getId");
      codelist=.jcall(rList[[i]],"Lit/bancaditalia/oss/sdmx/api/Codelist;","getCodeList");
      codelist = .jcall(codelist,"Ljava/lang/String;","getFullIdentifier");
      result[[name]] = codelist
    }
  }
  return(result)
}

# convert a java HashTable<String, String>
convertHashTable <- function (javaList) {
	idSet = .jcall(javaList,"Ljava/util/Set;","keySet");
	ids = .jcall(idSet,"[Ljava/lang/Object;","toArray");
	rIDs = as.list(ids)

	nameSet = .jcall(javaList,"Ljava/util/Collection;","values");
	names = .jcall(nameSet,"[Ljava/lang/Object;","toArray");
	rNames = as.list(names)

  flowIdentifiers = lapply(rIDs, .jstrVal)
	result = lapply(rNames, .jstrVal)
	names(result) <- flowIdentifiers
	return(result)
}

# convert a java List<PortableTimeSeries>
convertTSList <- function (javaList) {
	rList = as.list(javaList);
	numOfTS = length(rList);
	names = lapply(X=rList, FUN=getNames)
	result = lapply(X=rList, FUN=convertSingleTS)
  	names(result) <- names
	return(result);
}

# convert a java PortableDataSet
convertTSDF <- function (jtable) {
  time = .jcall(jtable,"[Ljava/lang/String;","getTimeStamps", evalString = TRUE, evalArray = TRUE)
  values = .jcall(jtable,"[Ljava/lang/Double;","getObservations", evalArray = TRUE)
  values = sapply(values, .jcall,"D","doubleValue")
  metaNames = .jcall(jtable,"[Ljava/lang/String;","getMetadataNames", evalString = TRUE, evalArray = TRUE)
  metaList = lapply(metaNames, getMeta, jtable = jtable)
  names(metaList) = metaNames
  result = as.data.frame(metaList)
  result = cbind('OBS_VALUE'=values, result)
  result = cbind('TIME_PERIOD'=time, result)
  return(result);
}

getNames<-function(ttss){
  s = .jcast(ttss, new.class = "it/bancaditalia/oss/sdmx/api/PortableTimeSeries", check = TRUE);
  name = .jcall(s,"Ljava/lang/String;","getName", evalString = TRUE);
  return(name)
}

getMeta<-function(metaName, jtable){
  meta = .jcall(jtable,"[Ljava/lang/String;","getMetadata", metaName, evalString = TRUE, evalArray = TRUE)
  return(meta)
}

convertSingleTS<-function(ttss){
  tts = NULL  
  s = .jcast(ttss, new.class = "it/bancaditalia/oss/sdmx/api/PortableTimeSeries", check = TRUE);
  name = .jcall(s,"Ljava/lang/String;","getName", evalString = TRUE);
  freq = .jcall(s,"Ljava/lang/String;","getFrequency", evalString = TRUE);
  dimensions = .jcall(s,"[Ljava/lang/String;","getDimensionsArray", evalArray = TRUE,
                      evalString = TRUE);
  attributes = .jcall(s,"[Ljava/lang/String;","getAttributesArray", evalArray = TRUE,
                      evalString = TRUE);
  timeSlots = .jcall(s,"[Ljava/lang/String;","getTimeSlotsArray", evalArray = TRUE,
                     evalString = TRUE);
  observationsJ = .jcall(s,"[Ljava/lang/Double;","getObservationsArray", evalArray = TRUE);
  numOfObs = length(observationsJ);
  numOfTimes = length(timeSlots);
  if( numOfObs > 0 && numOfObs == numOfTimes){
    obsAttrNames = .jcall(s,"[Ljava/lang/String;","getObsLevelAttributesNamesArray", evalArray = TRUE,
                          evalString = TRUE);
    obsAttr= list()
    for(x in obsAttrNames){
      obsAttr[[x]] =  .jcall(s,"[Ljava/lang/String;","getObsLevelAttributesArray", x)
    }
    observations = sapply(observationsJ, .jcall,"D","doubleValue")

    tts = makeSDMXTS(name, freq, timeSlots, observations, attributes, dimensions, obsAttr);
  }
  else{
    message(paste("Error building timeseries '", name, "': number of observations and time slots equal to zero, or not matching: ", numOfObs, " ", numOfTimes, "\n"));
  }
  return(tts)
}

# parameters used:
# tsname = name to be given to the series
# freq = sdmx frequency
# times= array of dates
# values= array of values
# series_attr_names= list of names of series attributes
# series_attr_values= list of values of series attributes
# status= list of values of status attribute
# convert the frequency from SDMX-like codelist to numeric, e.g. 'M'-> 12 etc..
makeSDMXTS<- function (tsname,freq,times,values,series_attr, series_dims, obsAttr) {

	if(length(values > 0)) {

		if (is.null(freq)) {
			message (paste0(tsname, ": frequency is NULL. Irregular timeseries defined"))     
			tmp_ts <- zoo(values, order.by=times)
		}
		else {
      if(freq == 'A'){
        tmp_ts<- zoo(values, order.by = as.integer(times),frequency=1)
      } else if(freq == 'M'){
        times = sub(pattern='M', replacement='-', times) # OECD only
        tmp_ts<- zoo(values, order.by = as.yearmon(times),frequency=12)
      } else if(freq == 'Q'){
        tmp_ts<- zoo(values, order.by = as.yearqtr(sub(pattern='-', replacement=' ', times)),frequency=4)
      } else if(freq == 'D' || freq == 'B'){
        tmp_ts<- zoo(values, order.by = as.Date(times))
      } else {
	      tmp_ts <- zoo(values, order.by=times)
      }
	}
	}
	else{
		# if the time series is empty
		tmp_ts <- zoo()
	}

  	# define ts attributes
	attr(tmp_ts,"ID") <- tsname
	for(x in names(obsAttr)){
	  attr(tmp_ts, x) <- obsAttr[[x]]
	}

  	# define the timeseries attributes (dimensions + attributes)
	if (length(series_dims) >0){
		for (l in 1:length(series_dims)) {
			attrComp=(unlist(strsplit(series_dims[l], "=", fixed = TRUE)))
			attr(tmp_ts,attrComp[1]) <- (attrComp[2])
		}
	}
	if (length(series_attr) >0){
		for (l in 1:length(series_attr)) {
			attrComp=(unlist(strsplit(series_attr[l], "=", fixed = TRUE)))
			attr(tmp_ts,attrComp[1]) <- (attrComp[2])
		}
	}
	return(tmp_ts)

}
