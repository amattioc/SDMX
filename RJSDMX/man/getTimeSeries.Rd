\name{getTimeSeries}
\alias{getTimeSeries}
\title{get time series}
\usage{
getTimeSeries(provider, id, start = "", end = "")
}
\arguments{
  \item{id}{identifier of the time series}

  \item{provider}{the name of the provider}

  \item{end}{the end time - optional}

  \item{start}{the start time - optional}
}
\description{
Extract a list of time series. This function is used to
extract a list of time series identified by the parameters
provided in input. getTimeSeries(provider, dataflow, start,
end)
}
\examples{
\dontrun{
## get single time series: EXR.A.USD.EUR.SP00.A (alternatively: EXR/A+M.USD.EUR.SP00.A)
my_ts=getTimeSeries('ECB','EXR.A.USD.EUR.SP00.A')
## get monthly and annual frequency: 'EXR.A|M.USD.EUR.SP00.A' (alternatively: EXR/A+M.USD.EUR.SP00.A)
my_ts=getTimeSeries('ECB','EXR.A|M.USD.EUR.SP00.A')
## get all available frequencies: 'EXR.*.USD.EUR.SP00.A' (alternatively: EXR/.USD.EUR.SP00.A)
my_ts=getTimeSeries('ECB','EXR.*.USD.EUR.SP00.A')
}
}

