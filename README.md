# Oracle System Info

https://github.com/T-Systems-MMS/Dynatrace-AppMon-Oracle-Plugin-SystemInfo

dynatrace Plugin extended based on the Oralce Monitor Plugin from dynatrace.
https://github.com/Dynatrace/Dynatrace-AppMon-Oracle-Monitor-Plugin


#add libaries in dynatrace 
commons-codec-1.8.jar
https://mvnrepository.com/artifact/commons-codec/commons-codec

commons-io-2.4.jar
https://mvnrepository.com/artifact/commons-io/commons-io

jsoup-1.7.2.jar
https://mvnrepository.com/artifact/org.jsoup/jsoup

jxl.jar 
https://mvnrepository.com/artifact/net.sourceforge.jexcelapi/jxl

ojdbc7.jar 
https://www.oracle.com/technetwork/database/features/jdbc/jdbc-drivers-12c-download-1958347.html

# How to use (from source):
1. clone the folder and "Import Folder" as plugin on a dynatrace server
2. Download the libaries and add them in the PluginConfig lib

# How to use (from jar) if you have
clone the jar and "Install Plugin" as plugin on a dynatrace server 

# New Features:
* Secure Connection with Oracle
* Reconnection if connection is corrupt
* Executes one specific SQL

# Removed Feature:
* Top SQL Statements
The reason is the complexity of installation of the plugin to use the feature.

# Configuration

oracleConType: List to sepcify 
* SID  - jdbc:oracle:thin://@<host>:<port>:<dbName>;
* servicename - jdbc:oracle:thin:@//<host>:<port>:<dbName>;
* string - jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcps)(HOST=<host>)(PORT=<port>))(CONNECT_DATA=(SERVICE_NAME=<dbname>))(security=<security>)
hostName: database Host name
dbName: dbname or serviceid
dbPort: port typical 1521 or 2484
dbUserName: user
dbPassword: passowrd
nodeID: for RAC Server specify the node id
security: security information it connectiontype is string:
for example: ssl_server_cert_dn="CN=<fulldomainname>,O=<company>,L=<city>,ST=<state>,C=<country>"))


genericSQL: select 1 as metric from dual
dont forget to set "metric" as keyword

If you use security (oracleConTyp: string) you need to import the certificate in the jre of the collector that is execution the monitor
example:
/dynatrace/dynatrace-7.0/jre/bin/keytool -import -alias <alias> -keystore /dynatrace/dynatrace-7.0/jre/lib/security/cacerts -file <your_cer_file>


# License: 
Copyright (c) 2018 T-Systems Multimedia Solutions GmbH (https://www.t-systems-mms.com/)  - file: OraclePluginSystemInfo.java + de.tsystems.mms.tic.apm.dynatrace.OraclePluginGenericSQL_1.1.1.jar
Copyright (c) 2008-2018, DYNATRACE LLC - all files exept de.tsystems.mms.tic.apm.dynatrace.OraclePluginGenericSQL_1.1.1.jar

License Files: license_dynatrace + license_mms

# Author: 
Kay Koedel, kay.koedel@t-systems.com

