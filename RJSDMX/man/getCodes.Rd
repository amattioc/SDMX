\name{getCodes}
\alias{getCodes}
\title{get dsd codes for dataflow}
\usage{
getCodes(provider, flow, dimension)
}
\arguments{
  \item{flow}{the identifier of the dataflow}

  \item{dimension}{the identifier of the dimension}

  \item{provider}{the name of the provider}
}
\description{
Extract the codes of a dimension. This function is used to
retrieve the list of codes available for the input
dimension and flow.
}
\details{
getCodes(provider, dataflow, dimension)
}
\examples{
\dontrun{
codes=getCodes('ECB', 'EXR', 'FREQ')
}
}

