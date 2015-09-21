\name{sdmxdf}
\alias{sdmxdf}
\title{convert time series to data.frame}
\usage{
sdmxdf(tslist, meta = FALSE, id = TRUE)
}
\arguments{
  \item{tslist}{the list of time series to be converted}

  \item{meta}{set this to TRUE if you want metadata to be
  included (default: FALSE, as this may increase the size of
  the result quite a bit)}

  \item{id}{set this to FALSE if you do not want the time
  series id to be included (default: TRUE)}
}
\description{
This function is used to transform the output of the
getSDMX (or getTimeseries) functions from a list of time
series to a data.frame. The metadata can be requested by
explicitly passing the appropriate parameters.
}
\details{
sdmxdf()
}
\examples{
\dontrun{
a=getSDMX('ECB', 'EXR.A|Q|M|D.USD.EUR.SP00.A')
ddf = sdmxdf(a)
ddf = sdmxdf(a, meta=TRUE)
}
}

