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
	return(lapply(javaList, .jstrVal))
}

# convert a java Iterable<Dimension>
convertDimList <- function (javaList) {
	return(lapply(javaList, function(x) setNames(list(x$getCodeList()$getFullIdentifier()), x$getId())))
}

# convert a java Map<String, String> into a named list
convertHashTable <- function (javaMap) {
	return(sapply(javaMap$entrySet(), function (x) setNames(list(x$getValue()), x$getKey())))
}

# convert a java List<PortableTimeSeries>
convertTSList <- function (javaList, plain = F) {
	result <- sapply(javaList, function (x) setNames(list(convertSingleTS(x, plain)), x$getName()))
	return(result);
}

# convert a java PortableDataSet
convertTSDF <- function (jtable, gregorianTime) {
	isNumeric <- .jcall(jtable,"Z","isNumeric");
	dataflow <- .jcall(jtable,"Ljava/lang/String;","getDataflow");
	if(gregorianTime)
		time = .jcall(jtable,"[Ljava/lang/String;","getGregorianTimeStamps", evalString = TRUE, evalArray = TRUE)
	else
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
	if(attr(result, 'IS_ERROR') <- jtable$isErrorFlag()) {
		errorObjects = .jcall(jtable,"Ljava/lang/String;","getErrorObjects", evalString = TRUE);
		attr(result, 'ERROR_OBJECTS') = errorObjects
		warning('The results contain errors. Please check the ERROR_OBJECTS attribute')
	}
	attr(result, 'IS_NUMERIC') <- isNumeric
	attr(result, 'dataflow') <- dataflow
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

convertSingleTS<-function(jpts, plain = F){
	result <- NULL	
	
	freq <- jpts$getFrequency()
	timeSlots = .jcall(jpts,"[Ljava/lang/String;","getTimeSlotsArray", evalArray = TRUE, evalString = TRUE);
	isNumeric = jpts$isNumeric();
	if (isNumeric){
		observations = J("it.bancaditalia.oss.sdmx.util.Utils")$toDoubleArray(jpts)
	} else {
		observations = sapply(jpts$getObservations(), .jcall,"Ljava/lang/String;","toString", evalString = T)
	}
	
	numOfObs = length(observations);
	numOfTimes = length(timeSlots);
	if( numOfObs > 0 && numOfObs == numOfTimes) {
		if (is.null(freq)) {
			message (paste0(jpts$getName(), ": frequency is NULL. Irregular timeseries defined"))		 
		}
		result <- makeSDMXTS(freq, timeSlots, observations, plain);
		attrs <- attributes(result)
		
		# set timeseries attributes, dimensions and obs-level attributes, including ID
		# NOTE: Possibile attributes overwrite
		attrs <- append(attrs, sapply(jpts$getObsLevelAttributesNames(), function(x) setNames(list(sapply(jpts$getObsLevelAttributes(x), .jstrVal)), .jstrVal(x))))
		attrs <- append(attrs, sapply(jpts$getDimensionsMap()$entrySet(), function(entry) setNames(entry$getValue(), entry$getKey())))
		attrs <- append(attrs, sapply(jpts$getAttributesMap()$entrySet(), function(entry) setNames(entry$getValue(), entry$getKey())))
		attrs[["ID"]] <- jpts$getName()
		attrs[['IS_NUMERIC']] = isNumeric
		
		#check errors
		if (attrs[["IS_ERROR"]] <- jpts$isErrorFlag()) {
			attrs[["ERROR_MSG"]] <- jpts$getErrorMessage()
			warning(paste('The time series ', jpts$getName(), ' contains errors. Please check the ERROR_MSG attribute'))
		}

		attributes(result) <- attrs
	}
	else{
		message(paste("Warning building timeseries '", jpts$getName(), "': number of observations and time slots equal to zero, or not matching: ", numOfObs, " ", numOfTimes, "\n"));
	}
	return(result)
}

# parameters used:
# freq = sdmx frequency
# times= array of dates
# values= array of values
# convert the frequency from SDMX-like codelist to numeric, e.g. 'M'-> 12 etc..
makeSDMXTS<- function (freq,times,values, plain = F) {

	if(length(values > 0)) {
		if(plain){
			# format will always be: yyyy-mm-ddThh:mm:ss
			if (is.null(freq)) {
				tmp_ts <- zoo(values, order.by = as.Date(times))
			}
			else {
				if(freq == 'M'){
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
				tmp_ts <- zoo(values, order.by=times)
			}
			else {
				if(freq == 'A'){
					# we expect a string year ('1984')
					tmp_ts<- zoo(values, order.by = as.integer(substr(times, 1,4)),frequency=1)
				} else if(freq == 'M'){
					# we expect '1984-02'
					times = sub(pattern='[-]?M', replacement='-', times) # OECD only '1984-M2'
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
	else
	{
		# if the time series is empty
		tmp_ts <- zoo()
	}

	return(tmp_ts)
}
