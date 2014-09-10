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

\name{getTimeSeries}
\alias{getTimeSeries}
\title{Extract a list of time series}
\description{
  This function is exactly the same as \bold{getSDMX} 
}
\usage{
  getTimeSeries(provider, id, start, end)
}
\arguments{
  \item{provider}{the name of the provider}
  \item{id}{the identifier of the time series}
  \item{start}{the start time - optional}
  \item{end}{the end time - optional}
}
\author{Attilio Mattiocco}
\examples{
# not run
# get single time series: EXR.A.USD.EUR.SP00.A (alternatively: EXR/A+M.USD.EUR.SP00.A)
# my_ts=getTimeSeries('ECB','EXR.A.USD.EUR.SP00.A')
# get monthly and annual frequency: 'EXR.A|M.USD.EUR.SP00.A' (alternatively: EXR/A+M.USD.EUR.SP00.A)
# my_ts=getTimeSeries('ECB','EXR.A|M.USD.EUR.SP00.A')
# get all available frequencies: 'EXR.*.USD.EUR.SP00.A' (alternatively: EXR/.USD.EUR.SP00.A)
# my_ts=getTimeSeries('ECB','EXR.*.USD.EUR.SP00.A')
}
\seealso{\bold{getSDMX}}
