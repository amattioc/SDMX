\name{getTimeSeries}
\alias{getTimeSeries}
\title{get time series}
\usage{
getTimeSeries(provider, id, start='', end='')
}
\arguments{
  \item{provider}{the name of the provider}
  \item{id}{identifier of the time series}
  \item{end}{the end time - optional}
  \item{start}{the start time - optional}
}
\description{
Extract a list of time series. 
This function is used to extract a list of time series identified by the parameters provided in input.

getTimeSeries(provider, id, start, end)

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
}
}

