.setUp <- function() {
}
.tearDown <- function() {
}

test.sdmx <- function() {
  cat('\ngetProviders\n')
  p = getProviders()
  checkTrue(length(p)>0, msg='Lista dei providers vuota')
  
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
  
  cat('getSDMX\n')
  dims = tryCatch({
    getSDMX(provider='ECB',id='EXR.A.USD|GBP.EUR.SP00.A')
  }, warning = function(w) {
    print(w)
    quit(status=-1)
  }, error = function(e) {
    print(e)
    quit(status=-1)
  })   
  checkEquals(2, current=length(dims), msg='Errore nella getTimeSeries')
  
}
