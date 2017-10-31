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
public class Work extends Property {
    
    private WorkType type;
    public Work(WorkType type) {
        super("work");
        this.type = type;
    }

    public boolean isConsumed() {
        return type == WorkType.CONSUMED;
    }
    public boolean isProduced() {
        return type == WorkType.PRODUCED;
    }
}
