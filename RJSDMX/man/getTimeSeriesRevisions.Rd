\name{getTimeSeriesRevisions}
\alias{getTimeSeriesRevisions}
\title{get data revisions}
\usage{
getTimeSeriesRevisions(provider, id, start = "", end = "",
  updatedAfter = "", includeHistory = TRUE)
}
\arguments{
  \item{id}{identifier of the time series}

  \item{provider}{the name of the provider}

  \item{end}{the end time - optional}

  \item{start}{the start time - optional}

  \item{updatedAfter}{the updatedAfter time - optional. It
  has to be in the form: 'YYYY-MM-DD'}

  \item{includeHistory}{boolean parameter - optional. If
  TRUE the full list of revisions will be returned}
}
\description{
Extract time series starting from a specific update time and 
with history of revisions. This function works as getTimeSeriesTable but the 
query can be narrowed to getting only observations that
were updated after a specific point in time, 
and eventually it returns the revision history of the matching time series. 

The result is packed into a data.frame
}
\details{
getTimeSeriesRevisions(provider, id, start, end,
updatedAfter, includeHistory)
}
\examples{
\dontrun{
  # get single time series with history:
  my_ts=getTimeSeriesRevisions('ECB','EXR.A.USD.EUR.SP00.A', includeHistory=TRUE)

  # get single time series (only observations updated after january 1st 2015):
  my_ts=getTimeSeriesRevisions('ECB','EXR.A.USD.EUR.SP00.A',
                   updatedAfter='2015', includeHistory=FALSE)

  # get single time series (full revision history starting from january 1st 2015):
  my_ts=getTimeSeriesRevisions('ECB','EXR.A.USD.EUR.SP00.A', 
                   updatedAfter='2015', includeHistory=TRUE)
}
}

