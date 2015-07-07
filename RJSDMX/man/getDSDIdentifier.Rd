\name{getDSDIdentifier}
\alias{getDSDIdentifier}
\title{get DSD Identifier for dataflow}
\usage{
getDSDIdentifier(provider, dataflow)
}
\arguments{
  \item{provider}{the name of the provider}

  \item{dataflow}{the identifier of the dataflow}
}
\description{
Extract the dsd identifier of a DataFlow. This function is
used to retrieve the name of the keyfamily of the input
dataflow.
}
\details{
getDSDIdentifier(provider, dataflow)
}
\examples{
\dontrun{
id = getDSDIdentifier('ECB','EXR')
}
}

