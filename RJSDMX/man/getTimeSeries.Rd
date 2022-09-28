\name{getTimeSeries}
\alias{getTimeSeries}
\title{get time series}
\usage{
getTimeSeries(provider, id, start='', end='', dataflow='', filter='')
}
\arguments{
  \item{id}{identifier of the time series}

  \item{provider}{the name of the provider}

  \item{end}{the end time - optional}

  \item{start}{the start time - optional}

  \item{dataflow}{dataflow of the time series - optional}

  \item{filter}{filter to be applied - optional}
}
\description{
Extract a list of time series. 
This function is used to extract a list of time series identified by the parameters provided in input.

getTimeSeries(provider, id, start, end, dataflow, filter)

}
\examples{
\dontrun{
# SDMX V2
## get single time series:
my_ts=getTimeSeries('ECB',id='EXR.A.USD.EUR.SP00.A')
## get monthly and annual frequency: 
my_ts=getTimeSeries('ECB',id='EXR.A+M.USD.EUR.SP00.A')
## get all available frequencies: 
my_ts=getTimeSeries('ECB',id='EXR..USD.EUR.SP00.A')

# SDMX V3

## get single time series: 
my_ts=getTimeSeries('ECB', dataflow='EXR', id='A.USD.EUR.SP00.A')
## get monthly and annual frequency: 
my_ts=getTimeSeries('ECB', dataflow='EXR', id='A+M.USD.EUR.SP00.A')
## get all available frequencies: 
my_ts=getTimeSeries('ECB', dataflow='EXR', id='.USD.EUR.SP00.A')

#or

## get single time series: EXR.A.USD.EUR.SP00.A
my_ts=getTimeSeries('ECB', dataflow='EXR', 
filter='c[FREQ]=A&c[CURRENCY]=USD&c[CURRENCY_DENOM]=EUR&c[EXR_TYPE]=SP00&c[EXR_SUFFIX]=A')
## get monthly and annual frequency: 
my_ts=getTimeSeries('ECB', dataflow='EXR', 
filter='c[FREQ]=A,M&c[CURRENCY]=USD&c[CURRENCY_DENOM]=EUR&c[EXR_TYPE]=SP00&c[EXR_SUFFIX]=A')
## get all available frequencies: 
my_ts=getTimeSeries('ECB', dataflow='EXR', 
filter='c[CURRENCY]=USD&c[CURRENCY_DENOM]=EUR&c[EXR_TYPE]=SP00&c[EXR_SUFFIX]=A')
}
}

