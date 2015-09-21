\name{sdmxHelp}
\alias{sdmxHelp}
\title{open helper}
\usage{
sdmxHelp(internalJVM = T)
}
\arguments{
  \item{internalJVM}{TRUE (default) if the GUI has to live
  in the R JVM. Set this to FALSE in MAC, to avoid issue
  #41}
}
\description{
Open a helper graphical application. This function opens a
small sdmx metadata browser that can be helpful when
building queries.
}
\details{
sdmxHelp()
}
\examples{
\dontrun{
#opens the helper in the R JVM
sdmxHelp()
#opens the helper in an external JVM
sdmxHelp(FALSE)
}
}

