/*Copyright (c) 2008-2018, DYNATRACE LLC
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of the dynaTrace software nor the names of its contributors
      may be used to endorse or promote products derived from this software without
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
DAMAGE.
*/

package com.dynatrace.diagnostics.plugins;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.logging.Level;

import oracle.jdbc.pool.OracleDataSource;


import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.PluginEnvironment;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;

import com.dynatrace.diagnostics.plugins.domain.ObjectStatus;

import com.dynatrace.diagnostics.plugins.utils.HelperUtils;

public class OraclePluginGenericSQL {
	
	static public final String DEFAULT_ENCODING = System.getProperty("file.encoding","UTF-8");
	
	

	private static String CONFIG_DB_HOST_NAME = "hostName";
    private static String CONFIG_DB_NAME = "dbName";
	private static String CONFIG_ORACLE_CON_TYPE = "oracleConType";

    private static String CONFIG_DB_USERNAME = "dbUsername";
    private static String CONFIG_DB_PASSWORD = "dbPassword";
	private static String CONFIG_RAC_NODEID = "nodeId";
    private static String CONFIG_PORT = "dbPort";
	private static String CONFIG_GENERIC_SQL = "genericsql";
    private static String CONFIG_DB_SECURITY = "security";
    private static String CONFIG_DB_COMPLETESTRING = "completestring";
 
    private String MONITOR_METRIC_GROUP = "Oracle Query";
	
    private java.sql.Connection con = null;

    private String urloracle = "jdbc:oracle:thin";

    private  String host;
    private  String dbName;
	private  String oracleConType;

    private  String userName;
	private  long nodeid;
    private  String password;
    private  String port;
	private  String security;
	private  String completestring;
	
	private java.util.Properties properties = new java.util.Properties();	
	
    public String connectionUrl = "";

    private static final Logger log = Logger.getLogger(OraclePluginGenericSQL.class.getName());
    
	private PluginEnvironment env;
    	
	public String GENERIC_SQL_STATEMENT = "";

	public String GENERIC_SQL_CURRENT = "genericsql";


   
    private String getConnectionUrl(String url, String host, String port, String dbName, String security, String completestring) {
    	log.finer("Inside getConnectionUrl method ...");

			log.finer("oracleConType is: " + oracleConType);
			if(oracleConType.equals("SID")) {
				// SID
				log.finer("getConnectionUrl method: connection string is " + url + ":" + "//@" + host + ":" + port + ":" + dbName + ";");
				return url + ":" + "//@" + host + ":" + port + ":" + dbName;
			}
		
			// ServiceName
			if(oracleConType.equals("servicename")) {
				log.finer("getConnectionUrl method: connection string is " + url + ":" + "@//" + host + ":" + port + "/" + dbName + ";");
				return url + ":" + "@//" + host + ":" + port + "/" + dbName;
			}
			// CompleteString
			if(oracleConType.equals("connectionstring")) {
				log.finer("getConnectionUrl method: connection string is " + completestring);
				return completestring;
			}
			// SecurityString
			else {
			log.finer("getConnectionUrl method: connection string is: " + url + " +:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcps)(HOST=" + host + ")(PORT=" + port + "))(CONNECT_DATA=(SERVICE_NAME=" + dbName + "))( //security=(" + security +" + ))");
				return url + ":@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcps)(HOST=" + host + ")(PORT=" + port + "))(CONNECT_DATA=(SERVICE_NAME=" + dbName + "))( security=(" + security +"))";      			

			} 
 }

    

    public Status setup(PluginEnvironment env) throws Exception {

    		
        log.finer("Inside setup method ...");

        //get configuration
        host = env.getConfigString(CONFIG_DB_HOST_NAME);
        dbName = env.getConfigString(CONFIG_DB_NAME);
        oracleConType = env.getConfigString(CONFIG_ORACLE_CON_TYPE);
        userName = env.getConfigString(CONFIG_DB_USERNAME);
        port = env.getConfigString(CONFIG_PORT);
        password = env.getConfigPassword(CONFIG_DB_PASSWORD);
		security = env.getConfigString(CONFIG_DB_SECURITY);	
		completestring = env.getConfigString(CONFIG_DB_COMPLETESTRING);
		nodeid = env.getConfigLong(CONFIG_RAC_NODEID);
        Status stat;
		
        connectionUrl = getConnectionUrl(urloracle, host, port, dbName, security, completestring);

		properties.setProperty("user", userName); 
		properties.setProperty("password", password); 		
		
	    // get connection to the monitored database
        try {
            log.info("setup method: Connecting to Oracle ...");
			log.info("setup method: Connection string is ... " + connectionUrl);
            log.info("setup method: Opening database connection ...");
            Class.forName("oracle.jdbc.driver.OracleDriver");
			
 		    con = DriverManager.getConnection(connectionUrl, properties); 
			
            stat = new Status();
        } catch (ClassNotFoundException e) {
        	log.log(Level.SEVERE, e.getMessage(), e);
        	return getErrorStatus(e);
         } catch (SQLException e) {
        	log.log(Level.SEVERE, e.getMessage(), e);
            return getErrorStatus(e);
        } finally {
        	// do nothing here
        }
	   

    stat = new Status();
    return stat;
    }
    
        
     
    private Status getErrorStatus(Exception e) {
    	Status stat = new Status();
    	stat.setStatusCode(Status.StatusCode.ErrorTargetService);
        stat.setShortMessage(e.getMessage());
        stat.setMessage(e.getMessage());
        stat.setException(e);
        
        return stat;
    }
	
	public Status execute(PluginEnvironment env)  throws Exception {	
		//reconnect, if database connection was lost
    	if (con == null || con.isClosed()) {
    		this.setup(env);
    	}
        Status stat = new Status();

		log.finer("Inside execute method ...");
		// Oracle RDBMS metrics
		try {
			populateGerericSQL((MonitorEnvironment)env);
			stat = new Status();
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			stat = getErrorStatus(e);
			log.severe("Last SQL was: " + env.getConfigString(CONFIG_GENERIC_SQL));
			
		}

    return stat;
  }
    
    


	private Timestamp getCurrentTimestamp(Connection con) throws Exception {
		Timestamp timestamp = null;
		ResultSet rs = null;
		Statement st = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery("select sysdate from dual");
			timestamp = rs.getTimestamp(1);
			
		} catch (Exception e) {
			log.severe("getCurrentTimestamp method: '" + HelperUtils.getExceptionAsString(e) + "'");
			throw e;
		} finally {				
        	if (st != null) st.close();
		}
		return timestamp;

	}
  
  public void populateGerericSQL(MonitorEnvironment env) throws SQLException {

        Collection<MonitorMeasure> measures = null;
        double timeBefore = 0;
        double timeAfter = 0;
        double totalConnectionTime = 0;
        Connection timerCon = null;
		ResultSet systemResult = null;
        Statement st = null;
		
		
		GENERIC_SQL_STATEMENT = env.getConfigString(CONFIG_GENERIC_SQL);
    	    
        log.finer("SQL Statement: " + GENERIC_SQL_STATEMENT );	
		
        log.finer("Inside populateGerericSQL method ...");
        

        try {
            log.finer("populateGerericSQL method: Connecting to Oracle ...");
            log.finer("populateGerericSQL method: Connection string is ... " + connectionUrl);
            log.finer("populateGerericSQL method: Opening database connection ...");
            timeBefore = System.currentTimeMillis();
			
		    timerCon = DriverManager.getConnection(connectionUrl, properties);

            timeAfter = System.currentTimeMillis();
            timerCon.close();
            totalConnectionTime = timeAfter - timeBefore;
          
		
			st = con.createStatement();
			
            systemResult = st.executeQuery(GENERIC_SQL_STATEMENT);
            systemResult.next();
			

            if ((measures = env.getMonitorMeasures(MONITOR_METRIC_GROUP, GENERIC_SQL_CURRENT)) != null) {
                for (MonitorMeasure measure : measures) {
                    log.finer("populateGerericSQL method: Populating GENERIC_SQL ... ");
					log.finer("SQL: " + GENERIC_SQL_STATEMENT);
                    measure.setValue(systemResult.getDouble("METRIC"));
                }
            }
        
	
		
        } 
		finally {				
        	if (st != null) st.close();
		}
		
  }

 

  public void teardown(PluginEnvironment env) throws Exception {
        log.finer("teardown method: Exiting Oracle Monitor Plugin ... ");
        if (con != null)
        	con.close();
  }
    


  }
