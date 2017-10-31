/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.ConfigurationException;

/**
 *
 * @author suhaybal-absi
 */
public class DummyDevice extends SteadyFlowDevice {

    public DummyDevice() {
        super("Dummy", "dummy");
    }

    @Override
    protected void configureEquations() throws ConfigurationException {
        
    }
    
}
