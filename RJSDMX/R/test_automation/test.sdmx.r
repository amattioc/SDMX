.setUp <- function() {
}
.tearDown <- function() {
}

test.sdmx <- function() {
  cat('\ngetProviders\n')
  p = getProviders()
  n = length(p)
  checkTrue(n>0, msg='Lista dei providers vuota')
  
  addProvider('test', endpoint = 'https://a.b.c.d', needsCredentials = 'none', needsURLEncoding = F, supportsCompression = T, 
              description = '', sdmxVersion = 'V3', supportsAvailability = T)
  p = getProviders()
  checkTrue(length(p) == n+1, msg='Add provider fallito')
  
  cat('getFlows\n')
  flows = tryCatch({
    getFlows('ECB')
  }, warning = function(w) {
    print(w)
    quit(status=-1)
  }, error = function(e) {
    print(e)
    quit(status=-1)
  })
  checkTrue(length(flows)>0, msg='Lista dei flussi vuota')
  
  cat('getDSDIdentifier\n')
  id = tryCatch({
    getDSDIdentifier  (provider='ECB', dataflow='EXR')
  }, warning = function(w) {
    print(w)
    quit(status=-1)
  }, error = function(e) {
    print(e)
    quit(status=-1)
  })
  checkEquals("ECB/ECB_EXR1/1.0", current=id, msg='Errore nel nome del dsd')
  
  cat('getDimensions\n')
  dims = tryCatch({
    getDimensions(provider='ECB', dataflow='EXR')
  }, warning = function(w) {
    print(w)
    quit(status=-1)
  }, error = function(e) {
    print(e)
    quit(status=-1)
  })
  checkEquals(5, current=length(dims), msg='Errore nel numero di dimensioni')
  
  cat('getTimeSeries\n')
  tts = tryCatch({
    getTimeSeries(provider='ECB',id='EXR.A.USD+GBP.EUR.SP00.A')
  }, warning = function(w) {
    print(w)
    quit(status=-1)
  }, error = function(e) {
    print(e)
    quit(status=-1)
  })   
  checkEquals(2, current=length(tts), msg='Errore nella getTimeSeries con +')
  cat('getTimeSeries\n')
  tts = tryCatch({
  	getTimeSeries(provider='ECB',id='EXR.A.USD.EUR.SP00.A')
  }, warning = function(w) {
  	print(w)
  	quit(status=-1)
  }, error = function(e) {
  	print(e)
  	quit(status=-1)
  })   
  checkEquals(1, current=length(tts), msg='Errore nella getTimeSeries semplice')
  cat('getTimeSeries\n')
  tts = tryCatch({
  	getTimeSeries(provider='ECB',id='EXR..USD.EUR.SP00.A')
  }, warning = function(w) {
  	print(w)
  	quit(status=-1)
  }, error = function(e) {
  	print(e)
  	quit(status=-1)
  })   
  checkEquals(5, current=length(tts), msg='Errore nella getTimeSeries con *')
  cat('getTimeSeriesTable\n')
  tts = tryCatch({
  	getTimeSeriesTable(provider='ECB',id='EXR..USD.EUR.SP00.A')
  }, warning = function(w) {
  	print(w)
  	quit(status=-1)
  }, error = function(e) {
  	print(e)
  	quit(status=-1)
  })   
  checkTrue(nrow(tts)>0, msg='Errore nella getTimeSeriesTable, dataframe vuoto')
}

test.sdmx3 <- function() {
	cat('getFlows\n')
	flows = tryCatch({
		getFlows('DEMO_SDMXV3')
	}, warning = function(w) {
		print(w)
		quit(status=-1)
	}, error = function(e) {
		print(e)
		quit(status=-1)
	})
	checkTrue(length(flows)>0, msg='Lista dei flussi vuota')
	
	cat('getDSDIdentifier\n')
	id = tryCatch({
		getDSDIdentifier  (provider='DEMO_SDMXV3', dataflow='EXR')
	}, warning = function(w) {
		print(w)
		quit(status=-1)
	}, error = function(e) {
		print(e)
		quit(status=-1)
	})
	checkEquals("ECB/ECB_EXR1/1.0", current=id, msg='Errore nel nome del dsd')
	
	cat('getDimensions\n')
	dims = tryCatch({
		getDimensions(provider='DEMO_SDMXV3', dataflow='EXR')
	}, warning = function(w) {
		print(w)
		quit(status=-1)
	}, error = function(e) {
		print(e)
		quit(status=-1)
	})
	checkEquals(5, current=length(dims), msg='Errore nel numero di dimensioni')
	
	cat('getTimeSeries2\n')
	tts = tryCatch({
		getTimeSeries2(provider='DEMO_SDMXV3',dataflow = 'EXR', key = 'A.USD.EUR.SP00.A')
	}, warning = function(w) {
		print(w)
		quit(status=-1)
	}, error = function(e) {
		print(e)
		quit(status=-1)
	})   
	checkEquals(1, current=length(tts), msg='Errore nella getTimeSeries semplice')
	cat('getTimeSeries2\n')
	tts = tryCatch({
		getTimeSeries2(provider='DEMO_SDMXV3', dataflow = 'EXR', key = '*.USD.EUR.SP00.A')
	}, warning = function(w) {
		print(w)
		quit(status=-1)
	}, error = function(e) {
		print(e)
		quit(status=-1)
	})   
	checkEquals(5, current=length(tts), msg='Errore nella getTimeSeries con *')
	cat('getTimeSeries2\n')
	tts = tryCatch({
		getTimeSeries2(provider='DEMO_SDMXV3', dataflow = 'EXR', filter = 'c[CURRENCY]=USD&c[EXR_SUFFIX]=A')
	}, warning = function(w) {
		print(w)
		quit(status=-1)
	}, error = function(e) {
		print(e)
		quit(status=-1)
	})   
	checkEquals(5, current=length(tts), msg='Errore nella getTimeSeries con filtro')
	cat('getTimeSeriesTable2\n')
	tts = tryCatch({
		getTimeSeriesTable2(provider='DEMO_SDMXV3', dataflow = 'EXR', filter = 'c[CURRENCY]=USD&c[EXR_SUFFIX]=A')
	}, warning = function(w) {
		print(w)
		quit(status=-1)
	}, error = function(e) {
		print(e)
		quit(status=-1)
	})   
	checkTrue(nrow(tts)>0, msg='Errore nella getTimeSeriesTable2, dataframe vuoto')
	
	# not working... why?
	# tts = tryCatch({
	# 	getTimeSeries2(provider='DEMO_SDMXV3', dataflow = 'EXR', key = '*.USD.EUR.SP00.*', filter = 'c[EXR_SUFFIX]=A')
	# }, warning = function(w) {
	# 	print(w)
	# 	quit(status=-1)
	# }, error = function(e) {
	# 	print(e)
	# 	quit(status=-1)
	# })   
	# checkEquals(5, current=length(tts), msg='Errore nella getTimeSeries con filtro e key')
	# 
}
