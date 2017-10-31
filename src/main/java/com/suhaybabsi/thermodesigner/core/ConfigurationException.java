/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.core;

import com.suhaybabsi.thermodesigner.devices.Device;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

/**
 *
 * @author suhaybal-absi
 */
public class ConfigurationException extends Exception{

    public static class Error{
    
        private Device device;
        private String message;

        public Error(Device device, String message) {
            this.device = device;
            this.message = message;
        }

        private Object getJsonMessage() {
            
            try {
                JSONObject json = new JSONObject();

                String deviceName = device != null ? device.getName() : "System";

                json.put("device", deviceName);
                json.put("message", message);

                return json;
            } catch (JSONException ex) {
                Logger.getLogger(CalculationError.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
        
        public String description(){
            String deviceName = device != null ? device.getName() : "System";
            return "Error in device: " + deviceName + "{\n\t" + message+"\n}";
        }
    }
    
    private final List<Error> errorsList = new ArrayList<Error>();
    public ConfigurationException(Device device, String message) {
        super(device.getName() + ": "+ message);
        errorsList.add(new Error(device, message));
        
    }
    public ConfigurationException(List<Error> errors) {
        super("Model cannot be configured properly");
        errorsList.addAll(errors);
    } 

    public List<Error> getErrorsList() {
        return errorsList;
    }
    public Object getJsonMessage() {
        
        JSONArray json = new JSONArray();
        for (Error error : errorsList) {
            json.put(error.getJsonMessage());
        }
        return json;
    }
}
