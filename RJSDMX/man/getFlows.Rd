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

\name{getFlows}
\alias{getFlows}
\title{Extract the list of DataFlows of a provider}
\description{
  This function is used to query the list of dataflows of the provider. A matching pattern can be provided, if needed.
}
\usage{
  getFlows(provider, pattern)
}
\arguments{
  \item{provider}{the name of the provider}
  \item{pattern}{the pattern to match against the dataflow id or description. If a pattern is not provided, all dataflows are returned.}
}
\examples{
# not run
# get all flows from ECB
# flows = getFlows('ECB')
# get all flows that contain the 'EXR
# flows = getFlows('ECB','*EXR*')
}
\author{Attilio Mattiocco}
