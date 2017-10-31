/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thermo.test.systems;

import com.suhaybabsi.thermodesigner.core.ThermalSystem;
import com.suhaybabsi.thermodesigner.devices.Burner;
import com.suhaybabsi.thermodesigner.devices.Compressor;
import com.suhaybabsi.thermodesigner.devices.Exhaust;
import com.suhaybabsi.thermodesigner.devices.GasIntercooler;
import com.suhaybabsi.thermodesigner.devices.Generator;
import com.suhaybabsi.thermodesigner.devices.Intake;
import com.suhaybabsi.thermodesigner.devices.Shaft;
import com.suhaybabsi.thermodesigner.devices.GasTurbine;
import com.suhaybabsi.thermodesigner.thermo.fluid.Air;

/**
 *
 * @author suhaybal-absi
 */
public class SingleShaftIntercooling extends ThermalSystem {

    public SingleShaftIntercooling() {
        
        construct(4.5);
    }
    
    public SingleShaftIntercooling(double r) {
        
        construct(r);
    }
    
    private void construct(double r) {
        
        Intake intake = new Intake();
        intake.setFluid(Air.getInstance());
        intake.defineMassRate(1.0);
        intake.definePressure(101.325);
        intake.defineTemperature(288);
        
        Compressor comp1 = new Compressor();
        comp1.defineCompressionRatio(r);
        
        createFlow(intake, comp1);
        
        GasIntercooler intercooler = new GasIntercooler();
        intercooler.definePressureLoss(0.03);
        intercooler.defineExitFlowTemperature(288);
        
        createFlow(comp1, intercooler);
        
        Compressor comp2 = new Compressor();
        comp2.defineCompressionRatio(r);
        
        createFlow(intercooler, comp2);
        
        Burner burner = new Burner();
        burner.definePressureLoss(0.03);
        burner.defineCombustionEfficiency(0.99);
        burner.defineExitTemperature(1100);
        
        createFlow(comp2, burner);
        
        GasTurbine turbine = new GasTurbine();
        
        createFlow(burner, turbine);
        
        Exhaust exhaust = new Exhaust();
        exhaust.definePressure(101.325 * (1 + 0.01));
        
        createFlow(turbine, exhaust);
        
        Generator generator = new Generator();        
        Shaft shaft = new Shaft();
        
        comp1.connectShaft(shaft);
        comp2.connectShaft(shaft);
        turbine.connectShaft(shaft);
        generator.connectShaft(shaft);
        
        addDevice(intake);
        addDevice(comp1);
        addDevice(intercooler);
        addDevice(comp2);
        addDevice(burner);
        addDevice(turbine);
        addDevice(exhaust);
        addDevice(generator);
        addDevice(shaft);
    }
    
    
}
