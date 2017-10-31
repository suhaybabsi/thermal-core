/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.core;

import com.suhaybabsi.thermodesigner.devices.Device;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

/**
 *
 * @author suhaybal-absi
 */
public class CalculationError {
    
    private Device device;
    private String message;

    public CalculationError(Device device, String message) {
        this.device = device;
        this.message = message;
    }
    
    public Object getJsonMessage() {
        
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

    public String getMessage() {
        return message;
    }
    

    @Override
    public String toString() {
        String deviceName = device != null ? device.getName() : "System";
        return deviceName + ": "+ message;
    }
    
    
    
}
