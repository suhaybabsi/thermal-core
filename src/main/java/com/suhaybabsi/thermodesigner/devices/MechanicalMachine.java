/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

/**
 *
 * @author suhaybal-absi
 */
public interface MechanicalMachine {
    
    public void connectShaft(Shaft shaft);
    public void disconnectShaft();
    public boolean isShaftConnected();
}
