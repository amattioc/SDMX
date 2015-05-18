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

\name{sdmxdf}
\alias{sdmxdf}
\title{transform the list of time series retrieved with getSDMX in a merged data.frame}
\description{
  This function is used to transform the output of the getSDMX (or getTimeseries) 
  functions from a list of time series to a data.frame. The metadata can be requested by explicitly passing the
  'meta' parameter.
}
\usage{
  sdmxdf(tslist, meta = F, id = T)
}
\arguments{
  \item{tslist}{the list of time series to be converted}
  \item{meta}{set this to TRUE if you want metadata to be included (default: F, as this may increase the size of the result quite a bit)}
  \item{id}{set this to FALSE if you do not want the time series id to be included (default: T) }
}
\examples{
# not run
# a=getSDMX('ECB', 'EXR.A|Q|M|D.USD.EUR.SP00.A')
# ddf = sdmxdf(a)
# ddf = sdmxdf(a, meta=T)
}
\author{Attilio Mattiocco}
