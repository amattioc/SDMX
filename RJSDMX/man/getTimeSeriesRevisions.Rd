% Copyright 2010,2014 Bank Of Italy
%
% Licensed under the EUPL, Version 1.1 or as soon they
% will be approved by the European Commission - subsequent
% versions of the EUPL (the "Licence");
% You may not use this work except in compliance with the
% Licence.
% You may obtain a copy of the Licence at:
%
%
% http://ec.europa.eu/idabc/eupl
%
% Unless required by applicable law or agreed to in
% writing, software distributed under the Licence is
% distributed on an "AS IS" basis,
% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
% express or implied.
% See the Licence for the specific language governing
% permissions and limitations under the Licence.
%

\name{getTimeSeriesRevisions}
\alias{getTimeSeriesRevisions}
\title{Extract a list of time series starting from a specific update time and with history of revisions}
\description{
  This function works as \bold{getSDMX} and \bold{getSDMX} but teh query can be narrowed to getting only observations that
  were updated after a specific point in time, and eventually it returns the revision history of
  the matching time series. This means that the result list can contain multiple instances 
  of the same time series, each with the specific action and validity attributes.
}
\usage{
  getTimeSeriesRevisions(provider, id, start, end, updatedAfter, includeHistory)
}
\arguments{
  \item{provider}{the name of the provider}
  \item{id}{the identifier of the time series}
  \item{start}{the start time - optional}
  \item{end}{the end time - optional}
  \item{updatedAfter}{the updatedAfter time - optional. It has to be in the form: 'YYYY-MM-DD'}
  \item{includeHistory}{boolean parameter - optional. If true the full list of revisions will be returned}
}
\author{Attilio Mattiocco}
\examples{
# not run
# get single time series with history: 
# my_ts=getTimeSeriesRevisions('ECB','EXR.A.USD.EUR.SP00.A', includeHistory=T)
# get single time series (only observations updated after january 1st 2015): 
# my_ts=getTimeSeriesRevisions('ECB','EXR.A.USD.EUR.SP00.A', updatedAfter='2015-01-01', includeHistory=F)
# get single time series (full revision history starting from january 1st 2015): 
# my_ts=getTimeSeriesRevisions('ECB','EXR.A.USD.EUR.SP00.A', updatedAfter='2015-01-01', includeHistory=T)
}
\seealso{\bold{getSDMX}}
