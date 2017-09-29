*test SDMX in Stata

adopath++ "."

log using "test_sdmx.log",t replace
log on
capture getTimeSeries ECB EXR.A.USD.EUR.SP00.A
if _rc != 0 {
	display " getTimeSeries: call error"
	exit -1
}
if _N == 0 {
	display " getTimeSeries: results error"
	exit -1
}

capture getTimeSeries ECB EXR.A.USD.EUR.SP00.A "2001" "2010" 1 1
if _rc != 0 {
	display " getTimeSeries with time: call error"
	exit -1
}
if _N == 0 {
	display " getTimeSeries with time: results error"
	exit -1
}

log off
log close
