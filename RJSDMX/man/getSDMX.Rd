\name{getSDMX}
\alias{getSDMX}
\title{get data}
\usage{
getSDMX(provider, id, start = "", end = "")
}
\arguments{
  \item{id}{identifier of the time series}

  \item{provider}{the name of the provider}

  \item{end}{the end time - optional}

  \item{start}{the start time - optional}
}
\description{
Extract a list of time series. This function is exactly the
same as \code{getTimeSeries}.
}
\details{
getSDMX(provider, dataflow, start, end)
}
\examples{
\dontrun{
## get single time series: EXR.A.USD.EUR.SP00.A (or: EXR/A+M.USD.EUR.SP00.A)
my_ts=getSDMX('ECB','EXR.A.USD.EUR.SP00.A')
## get monthly and annual frequency: 'EXR.A|M.USD.EUR.SP00.A' (or: EXR/A+M.USD.EUR.SP00.A)
my_ts=getSDMX('ECB','EXR.A|M.USD.EUR.SP00.A')
## get all available frequencies: 'EXR.*.USD.EUR.SP00.A' (or: EXR/.USD.EUR.SP00.A)
my_ts=getSDMX('ECB','EXR.*.USD.EUR.SP00.A')
}
}

