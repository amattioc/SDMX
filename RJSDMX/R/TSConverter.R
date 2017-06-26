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
    data=coredata(tts)
    header=c('TIME_PERIOD', 'OBS_VALUE')
    ddf=data.frame(time, data, stringsAsFactors = F)      
    if(setMeta){
      metaddf = data.frame(row.names = 1:n)
      metaheader = NULL
      for(x in names(attributes(tts))){
        if(x != 'ID' && x != 'class' && x != 'frequency' && x != 'index'){
          val = attr(tts, x)
          if(length(val) == 1){
            # ts level attributes go close to id
            metaddf = cbind(metaddf, rep(attr(tts, x), n), stringsAsFactors = F)
            metaheader=c(metaheader, x)
          }
          else if(length(val) == n){
            # obs level attributes go close to data
            ddf = cbind(val, ddf, stringsAsFactors = F)
            header=c(x, header)
          }
        }
      }
      ddf = cbind(metaddf, ddf, stringsAsFactors = F)
      header=c(metaheader, header)
    }
    if(setId){
      id=rep(attr(tts, 'ID'), n)
      ddf=cbind(id, ddf, stringsAsFactors = F)
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
convertTSList <- function (javaList, plain = F) {
	rList = as.list(javaList);
	numOfTS = length(rList);
	names = lapply(X=rList, FUN=getNames)
	result = lapply(X=rList, FUN=convertSingleTS, plain = plain)
  names(result) <- names
	return(result);
}

# convert a java PortableDataSet
convertTSDF <- function (jtable) {
  isNumeric = .jcall(jtable,"Z","isNumeric");
  time = .jcall(jtable,"[Ljava/lang/String;","getTimeStamps", evalString = TRUE, evalArray = TRUE)
  values = .jcall(jtable,"[Ljava/lang/Object;","getObservations", evalArray = TRUE)
  if(isNumeric){
    values = sapply(values, .jcall,"D","doubleValue")
  }
  else{
    values = sapply(values, .jcall,"Ljava/lang/String;","toString", evalString = T)
  }
  metaNames = .jcall(jtable,"[Ljava/lang/String;","getMetadataNames", evalString = TRUE, evalArray = TRUE)
  metaList = lapply(metaNames, getMeta, jtable = jtable)
  names(metaList) = metaNames
  result = as.data.frame(metaList, stringsAsFactors = FALSE)
  result = cbind('OBS_VALUE'=values, result, stringsAsFactors = FALSE)
  result = cbind('TIME_PERIOD'=time, result, stringsAsFactors = FALSE)
  #check errors
  errorObjects = NULL
  flag = .jcall(jtable,"Z","isErrorFlag");
  if(flag){
    errorObjects = .jcall(jtable,"Ljava/lang/String;","getErrorObjects", evalString = TRUE);
    attr(result, which = 'IS_ERROR') = T
    attr(result, which = 'ERROR_OBJECTS') = errorObjects
    warning('The results contain errors. Please check the ERROR_OBJECTS attribute')
  }
  else{
    attr(result, which = 'IS_ERROR') = F      
  }
  attr(result, which = 'IS_NUMERIC') = isNumeric
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

convertSingleTS<-function(ttss, plain = F){
  tts = NULL  
  s = .jcast(ttss, new.class = "it/bancaditalia/oss/sdmx/api/PortableTimeSeries", check = TRUE);
  name = .jcall(s,"Ljava/lang/String;","getName", evalString = TRUE);
  freq = .jcall(s,"Ljava/lang/String;","getFrequency", evalString = TRUE);
  dimensions = .jcall(s,"[Ljava/lang/String;","getDimensionNamesArray", evalArray = TRUE,
                      evalString = TRUE);
  attributes = .jcall(s,"[Ljava/lang/String;","getAttributeNamesArray", evalArray = TRUE,
                      evalString = TRUE);
  timeSlots = .jcall(s,"[Ljava/lang/String;","getTimeSlotsArray", evalArray = TRUE,
                     evalString = TRUE);
  isNumeric = .jcall(s,"Z","isNumeric");
  observationsJ = .jcall(s,"[Ljava/lang/Object;","getObservationsArray", evalArray = TRUE);
  if(isNumeric){
    observations = sapply(observationsJ, .jcall,"D","doubleValue")
  }
  else{
    observations = sapply(observationsJ, .jcall,"Ljava/lang/String;","toString", evalString = T)
  }
  
  numOfObs = length(observations);
  numOfTimes = length(timeSlots);
  if( numOfObs > 0 && numOfObs == numOfTimes){
    obsAttrNames = .jcall(s,"[Ljava/lang/String;","getObsLevelAttributesNamesArray", evalArray = TRUE,
                          evalString = TRUE);
    obsAttr= list()
    for(x in obsAttrNames){
      obsAttr[[x]] =  .jcall(s,"[Ljava/lang/String;","getObsLevelAttributesArray", x)
    }
    tts = makeSDMXTS(ttss, name, freq, timeSlots, observations, attributes, dimensions, obsAttr, plain);
    #check errors
    errorMessage = NULL
    flag = .jcall(s,"Z","isErrorFlag");
    if(flag){
      errorMessage = .jcall(s,"Ljava/lang/String;","getErrorMessage", evalString = TRUE);
      attr(tts, which = 'IS_ERROR') = T
      attr(tts, which = 'ERROR_MSG') = errorMessage
      warning(paste('The time series', name, 'contains errors. Please check the ERROR_MSG attribute'))
    }
    else{
      attr(tts, which = 'IS_ERROR') = F      
    }
    attr(tts, which = 'IS_NUMERIC') = isNumeric  
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
makeSDMXTS<- function (ttss, tsname,freq,times,values,series_attr, series_dims, obsAttr, plain = F) {

	if(length(values > 0)) {
    if(plain){
      # format will always be: yyyy-mm-ddThh:mm:ss
      if (is.null(freq)) {
        message (paste0(tsname, ": frequency is NULL. Irregular timeseries defined"))     
        tmp_ts <- zoo(values, order.by = as.Date(times))
      }
      else {
        if(freq == 'A'){
          tmp_ts<- zoo(values, order.by = as.integer(substr('2016-07-03T00:00:00', 1,4)),frequency=1)
        } else if(freq == 'M'){
          tmp_ts<- zoo(values, order.by = as.yearmon(as.Date(times)),frequency=12)
        } else if(freq == 'Q'){
          tmp_ts<- zoo(values, order.by = as.yearqtr(as.Date(times)),frequency=4)
        } else {
          tmp_ts<- zoo(values, order.by = as.Date(times))
        }
      }
    }
    else{
  		if (is.null(freq)) {
  			message (paste0(tsname, ": frequency is NULL. Irregular timeseries defined"))     
  			tmp_ts <- zoo(values, order.by=times)
  		}
  		else {
        if(freq == 'A'){
          # we expect a string year ('1984')
          tmp_ts<- zoo(values, order.by = as.integer(times),frequency=1)
        } else if(freq == 'M'){
          # we expect '1984-02'
          times = sub(pattern='M', replacement='-', times) # OECD only '1984-M2'
          tmp_ts<- zoo(values, order.by = as.yearmon(times),frequency=12)
        } else if(freq == 'Q'){
          # we expect '1984-Q2'
          tmp_ts<- zoo(values, order.by = as.yearqtr(sub(pattern='-', replacement=' ', times)),frequency=4)
        } else if(freq == 'D' || freq == 'B'){
          # we expect '1984-03-27'
          tmp_ts<- zoo(values, order.by = as.Date(times))
        } else {
          # nothing we can forecast
  	      tmp_ts <- zoo(values, order.by=times)
        }
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
		for (x in series_dims) {
      dim = .jcall(ttss,"Ljava/lang/String;","getDimension", x, evalString = T);
			attr(tmp_ts, x) <- dim
		}
	}
	if (length(series_attr) >0){
		for (x in series_attr) {
			attr = .jcall(ttss,"Ljava/lang/String;","getAttribute", x, evalString = T);
			attr(tmp_ts, x) <- attr
		}
	}
	return(tmp_ts)
}
