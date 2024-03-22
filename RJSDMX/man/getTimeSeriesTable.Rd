\name{getTimeSeriesTable}
\alias{getTimeSeriesTable}
\title{get time series and return a data.frame}
\usage{
getTimeSeriesTable(provider, id, start='', end='', gregorianTime=F)
}
\arguments{
  \item{id}{identifier of the time series}
  \item{provider}{the name of the provider}
  \item{end}{the end time - optional}
  \item{start}{the start time - optional}
  \item{gregorianTime}{set to true to have all daily dates - optional}
}
\description{
Extract a list of time series identified by the parameters provided in input, and return a data.frame as result.

getTimeSeriesTable(provider, id, start, end, gregorianTime)
}
\examples{
\dontrun{
  # SDMX V2
## get single time series:
my_ts=getTimeSeriesTable('ECB',id='EXR.A.USD.EUR.SP00.A')
## get monthly and annual frequency: 
my_ts=getTimeSeriesTable('ECB',id='EXR.A+M.USD.EUR.SP00.A')
## get all available frequencies: 
my_ts=getTimeSeriesTable('ECB',id='EXR..USD.EUR.SP00.A')

}
}

