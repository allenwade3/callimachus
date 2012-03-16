/*
 * Copyright (c) 2012 3 Round Stones Inc., Some Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.callimachusproject.installer.validators;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.installer.AutomatedInstallData;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.callimachusproject.installer.Configure;

/**
 * A custom IzPack (see http://izpack.org) validator to validate
 * the status of com.izforge.izpack.panels.CallimachusConfigurationPanel.
 * 
 * @author David Wood (david @ http://3roundstones.com)
 * 
 */
public class CallimachusConfigurationValidator implements DataValidator {
    
    static String DATA_VALIDATOR_CLASSNAME_TAG = "CallimachusConfigurationValidator";
    static String DATA_VALIDATOR_TAG = "CallimachusConfigurationValidator tag";
	protected AutomatedInstallData adata;
	protected Map<String,String> defaults = new HashMap<String,String>(20); // Default values for variables.
    
    public boolean getDefaultAnswer() {
        return true;
    }
    
    public String getErrorMessageId() {
        return "CallimachusConfigurationPanel reported an error.  Run this installer from a command line for a full stack trace.";
    }
    
    public String getWarningMessageId() {
        return "CallimachusConfigurationPanel reported a warning.  Run this installer from a command line for a full stack trace.";
    }
    
    public DataValidator.Status validateData(AutomatedInstallData adata) {

        this.adata = adata;
        initializeDefaults();
        
        try {
			String installPath = adata.getInstallPath();
			Configure configure = new Configure(new File(installPath));
			
			try {
        		boolean running = configure.isServerRunning();
    			if (running) {
        			if (!configure.stopServer()) {
        				System.err.println("Server must be shut down to continue");
            			return Status.ERROR;
        			}
        		}
    		} catch (Exception e) {
    			e.printStackTrace();
    			return Status.ERROR;
    		}

			// Set IzPack variables for callimachus.conf:
			String[] confProperties = {"PORT", "acceptallrealms", "otherrealm1", "otherrealm2", "otherrealm3", "otherrealm4", "otherrealm5"};
			setCallimachusVariables(configure.getServerConfiguration(), confProperties);
            setOriginVariables(configure.getServerConfiguration());

			// Set IzPack variables for mail.properties:
			String[] mailProperties = {"mail.transport.protocol", "mail.from", "mail.smtps.host", "mail.smtps.port", "mail.smtps.auth", "mail.user", "mail.password"};
			setCallimachusVariables(configure.getMailProperties(), mailProperties);
		} catch (IOException e) {
			// This is an unknown error.
    		e.printStackTrace();
			return Status.ERROR;
		}
        
        return Status.OK;
    }
    
    /**
	 * Set IzPack variables for each property provided.
	 *
	 * @param prop A Java Properties object holding values from a Callimachus config file.
	 * @param properties A list of property names to convert to IzPack variables.
	 */
	private void setCallimachusVariables(Properties prop, String[] properties) {
		// Get the values of relevant properties and convert them to IzPack
		// variables with the same names but prepended by "callimachus." to
		// avoid namespace conflicts.
		String tempProperty;
		int propertiesLength = properties.length;
		for (int i = 0; i < propertiesLength; i++) {
			if ( prop.getProperty(properties[i]) == null ) {
			    tempProperty = defaults.get(properties[i]);
			} else {
				tempProperty = prop.getProperty(properties[i]);
			}
			adata.setVariable("callimachus." + properties[i], tempProperty);
		}
	}

    /**
	 * Set IzPack variables for the origin variable.
	 *
	 * @param prop A Java Properties object holding values from a Callimachus config file.
	 */
	private void setOriginVariables(Properties prop) {
	    String origin = prop.getProperty("ORIGIN");
        if ( origin == null ) {
            origin = defaults.get("ORIGIN");
        }
        String[] origins = origin.split("\\s+");
        String[] properties = {"ORIGIN", "secondaryauthority1", "secondaryauthority2", "secondaryauthority3", "secondaryauthority4", "secondaryauthority5"};
		String tempProperty;
		// Use the on-disk values for any variable.
		for (int i = 0; i < origins.length; i++) {
			if ( origins[i] == null ) {
			    tempProperty = defaults.get(properties[i]);
			} else {
				tempProperty = origins[i];
			}
    		adata.setVariable("callimachus." + properties[i], tempProperty);
		}
		// Write default values for anything not on disk.
		for (int i = origins.length; i < properties.length; i++) {
		    tempProperty = defaults.get(properties[i]);
		    adata.setVariable("callimachus." + properties[i], tempProperty);
		}
	}
	
	/**
	 * Initializes the default configuration variable values.
	 *
	 */
	private void initializeDefaults() {
	    defaults.put("PORT", "8080");
    	defaults.put("ORIGIN", "http://localhost:8080");
        defaults.put("mail.transport.protocol", "smtps");
        defaults.put("mail.from", "user@example.com");
        defaults.put("mail.smtps.host", "mail.example.com");
        defaults.put("mail.smtps.port", "465");
        defaults.put("mail.smtps.auth", "no");
        defaults.put("mail.user", "");
        defaults.put("mail.password", "");
        defaults.put("secondaryauthority1", "");
        defaults.put("secondaryauthority2", "");
        defaults.put("secondaryauthority3", "");
        defaults.put("secondaryauthority4", "");
        defaults.put("secondaryauthority5", "");
        defaults.put("acceptallrealms", "on");
        defaults.put("otherrealm1", "");
        defaults.put("otherrealm2", "");
        defaults.put("otherrealm3", "");
        defaults.put("otherrealm4", "");
        defaults.put("otherrealm5", "");
	}
}