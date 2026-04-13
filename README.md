Sdmx Connectors 
====

Setup and Configuration: [SDMX wiki](https://github.com/amattioc/SDMX/wiki)<br> 

[![JAVA CI](https://github.com/amattioc/SDMX/actions/workflows/java-ci.yml/badge.svg?branch=master)](https://github.com/amattioc/SDMX/actions/workflows/java-ci.yml)
[![Mentioned in Awesome Official Statistics ](https://awesome.re/mentioned-badge.svg)](http://www.awesomeofficialstatistics.org)

![Latest Release](https://img.shields.io/github/v/release/amattioc/SDMX)   ![Latest Release downloads](https://img.shields.io/github/downloads/amattioc/SDMX/latest/total)
![CRAN version latest](https://www.r-pkg.org/badges/version-ago/RJSDMX)    ![CRAN downloads month](https://cranlogs.r-pkg.org/badges/RJSDMX)    ![CRAN downloads](https://cranlogs.r-pkg.org/badges/grand-total/RJSDMX)

<img width="1254" height="906" alt="image" src="https://github.com/user-attachments/assets/fc0593e8-0d47-40aa-929c-37933e9982e2" />


**List of available providers:**

`> getProviders()`

`[1]  "ABS"             "AMECO"           "ASTAT"           "BBK"             "BIS_PUBLIC"` <br>
`[6]  "DEMO_SDMXV3"     "ECB"             "EUROSTAT"        "EUROSTAT_COMEXT" "EUROSTAT_COMP"`  <br>
`[11] "EUROSTAT_EMPL"   "EUROSTAT_GROW"   "ILO"             "IMF"             "IMF_RESTR"`  <br>
`[16] "INEGI"           "INSEE"           "ISTAT"           "ISTAT_RI"        "NBB"`  <br>
`[21] "OECD_NEW"        "OECD_SDMXV3"     "PACIFICDATA"     "STATCAN_CENSUS"  "STATCAN_NRG"` <br> 
`[26] "SWISS_STAT"      "UNDATA"          "UNICEF"          "WB"              "WITS"` <br>

**Browse SDMX data providers, build your queries and get data directly in your favourite tool.**

(try the java [helper](https://github.com/amattioc/SDMX/raw/master/RJSDMX/inst/java/SDMX.jar))

====

**Known applications:**

* [VTL Engine & Editor](https://github.com/vpinna80/VTL)
* [Shiny SDMX Browser](https://rjsdmx.shinyapps.io/sdmxBrowser/) (code [here](https://github.com/bowerth/sdmxBrowser))
* [Scala SDMX Play application](http://sdmx.rdata.work/) (code [here](https://github.com/bowerth/sdmxPlay))
* [TSsdmx on CRAN](http://cran.us.r-project.org/web/packages/TSsdmx/index.html) (code [here](http://tsdbi.r-forge.r-project.org/))
* [GETDATA](http://econpapers.repec.org/software/bocbocode/S458093.htm)  on SSC for STATA
* [DotStat extension for JDemetra+](https://github.com/nbbrd/jdemetra-dotstat) 
* [Pentaho Data Integration SDMX Plugin](https://github.com/andtorg/sdmx-kettle) with some [context](http://andtorg.github.io/bi/2016/06/14/pentaho-sdmx-step-plugin)

====

**Available functions**

![Functions](https://github.com/amattioc/SDMX/blob/master/docs/resources/sdmxtable.png) 

**Note 1:** In Windows the helper can be launched just double clicking the SDMX.jar <br>
**Note 2:** New Providers can be added by means of the configuration file <br>

====

**NEW: The SDMX helper can be used from within any statistical tool or as a standalone application (just double click the SDMX.jar)**

![Helper](https://github.com/amattioc/SDMX/blob/master/docs/resources/helper.png)



