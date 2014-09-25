Sdmx Connectors for Statistical Software
====

Browse SDMX data providers, build your queries and get data directly in your favourite tool. 

Info: [SDMX wiki](https://github.com/amattioc/SDMX/wiki)


![My image](https://github.com/amattioc/SDMX/blob/master/docs/resources/sdmx.png)

## Installation

### R

```r
require(devtools)
install_github(repo = "SDMX", username = "amattioc", subdir = "RJSDMX")
```

if the Multiarch (`i386`, `x64`) installation does not work on Windows 64bit, unset the JAVA_HOME environment variable before loading `rJava` or attempting to install

```r
if (Sys.getenv("JAVA_HOME")!="") Sys.setenv(JAVA_HOME="")
```

#### Proxy server

- copy the file `/JAVA/configuration.properties` to your working directory (`getenv()`) and modify `http.proxy.name0` accordingly
- alternatively, set the `SDXM_CONF` environment variable to point to the package installation location:

```r
if (Sys.getenv("SDMX_CONF")=="") Sys.setenv(SDMX_CONF=file.path(find.package("RJSDMX"), "install", "configuration.properties"))
```
