/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.core;

import com.suhaybabsi.thermodesigner.devices.MechanicalMachine;

/**
 *
 * @author suhaybal-absi
 */
public interface WorkConsumingDevice extends MechanicalMachine {
    
    public double getWorkConsumed();
}
