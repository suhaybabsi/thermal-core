/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.Work;

/**
 *
 * @author suhaybal-absi
 */
public abstract class TurboMachine extends SteadyFlowDevice implements MechanicalMachine {
    
    protected Work work;
    private Shaft shaft;

    protected TurboMachine(String name, String jsonType) {
        super(name, jsonType);
    }
    @Override
    public void connectShaft(Shaft shaft) {
        if(this.shaft != null){
            disconnectShaft();
        }
        
        this.shaft = shaft;
        shaft.addWork(work);
    }
    @Override
    public void disconnectShaft(){
        if(this.shaft != null){
            this.shaft.removeWork(work);
            this.shaft = null;
        }
    }

    @Override
    public boolean isShaftConnected() {
        return this.shaft != null;
    } 
}
