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
public class CalculationException extends Exception {
    
    
    private CalculationError error;
    public CalculationException(CalculationError error) {
        super(error.toString());
        this.error = error;
    } 
    public CalculationException(Device device, String message) {
        this(new CalculationError(device, message));
    }

    public Object getJsonMessage() {
        return error.getJsonMessage();
    }
}
