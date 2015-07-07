\name{getDimensions}
\alias{getDimensions}
\title{get dsd dimensions for dataflow}
\usage{
getDimensions(provider, dataflow)
}
\arguments{
  \item{dataflow}{the identifier of the dataflow}

  \item{provider}{the name of the provider}
}
\description{
Extract the dimensions of a DataFlow. This function is used
to retrieve the list of dimensions of the input dataflow
}
\details{
getDimensions(provider, dataflow)
}
\examples{
\dontrun{
dims = getDimensions('ECB','EXR')
}
}

