% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/SdmxClient.R
\name{getTimeSeries2}
\alias{getTimeSeries2}
\title{get time series (SDMX v3)}
\usage{
getTimeSeries2(
  provider,
  dataflow,
  key = "",
  filter = "",
  start = "",
  end = "",
  attributes = "all",
  measures = "all"
)
}
\arguments{
 \item{provider}{the name of the provider}
 \item{dataflow}{dataflow of the time series}
 \item{key}{timeseries key - optional}
 \item{filter}{optional filter to be applied - optional}
 \item{start}{the start time - optional}
 \item{end}{the end time - optional}
 \item{attributes}{the comma separated list of attributes to be returned - optional, default='all', 'none' for no attributes}
 \item{measures}{the comma separated list of measures to be returned - optional, default='all', 'none' for no measures}
}
\description{
Extract a list of time series . 
This function is used to extract a list of time series identified by the parameters provided in input.

}
\examples{
\dontrun{
# SDMX V3

## get single time series: 
my_ts=getTimeSeries2('ECB', dataflow='EXR', key='A.USD.EUR.SP00.A')
## get all available frequencies: 
my_ts=getTimeSeries2('ECB', dataflow='EXR', key='.USD.EUR.SP00.A')

#or

#' ## get single time series: EXR.A.USD.EUR.SP00.A
my_ts=getTimeSeries2('ECB', dataflow='EXR', 
filter='c[FREQ]=A&c[CURRENCY]=USD&c[CURRENCY_DENOM]=EUR&c[EXR_TYPE]=SP00&c[EXR_SUFFIX]=A')
## get monthly and annual frequency: 
my_ts=getTimeSeries2('ECB', dataflow='EXR', 
filter='c[FREQ]=A,M&c[CURRENCY]=USD&c[CURRENCY_DENOM]=EUR&c[EXR_TYPE]=SP00&c[EXR_SUFFIX]=A')
## get all available frequencies: 
my_ts=getTimeSeries2('ECB', dataflow='EXR', 
filter='c[CURRENCY]=USD&c[CURRENCY_DENOM]=EUR&c[EXR_TYPE]=SP00&c[EXR_SUFFIX]=A')
}
}


