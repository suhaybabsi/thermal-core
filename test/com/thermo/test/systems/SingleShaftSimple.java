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
import com.suhaybabsi.thermodesigner.devices.Generator;
import com.suhaybabsi.thermodesigner.devices.Intake;
import com.suhaybabsi.thermodesigner.devices.Shaft;
import com.suhaybabsi.thermodesigner.devices.GasTurbine;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Air;

/**
 *
 * @author suhaybal-absi
 */
public class SingleShaftSimple extends ThermalSystem {

    public SingleShaftSimple() {
        
        construct(4.0);
    }
    
    public SingleShaftSimple(double r) {
        
        construct(r);
    }
    
    private void construct(double r) {
        
        Intake intake = new Intake();
        intake.setFluid(Air.getInstance());
        intake.defineMassRate(1.0);
        intake.definePressure(101.325);
        intake.defineTemperature(288);
        
        Flow flow1 = new Flow();
        intake.getFlowManager().addOut(flow1);
        
        Compressor comp = new Compressor();
        comp.defineCompressionRatio(r);
               
        Flow flow2 = new Flow();
        comp.getFlowManager().addIn(flow1);
        comp.getFlowManager().addOut(flow2);
        
        Burner burner = new Burner();
        burner.definePressureLoss(0.03);
        burner.defineCombustionEfficiency(0.99);
        //burner.defineExitTemperature(1100);
        burner.defineFuelAirRatio(0.02);
        
        Flow flow3 = new Flow();
        burner.getFlowManager().addIn(flow2);
        burner.getFlowManager().addOut(flow3);
        
        GasTurbine turbine = new GasTurbine();
        
        Flow flow4 = new Flow();
        turbine.getFlowManager().addIn(flow3);
        turbine.getFlowManager().addOut(flow4);
        
        Exhaust exhaust = new Exhaust();
        exhaust.definePressure(101.325 * (1 + 0.01));
        exhaust.getFlowManager().addIn(flow4);
        
        Generator generator = new Generator();        
        Shaft shaft = new Shaft();
        
        comp.connectShaft(shaft);
        turbine.connectShaft(shaft);
        generator.connectShaft(shaft);
        
        addDevice(intake);
        addDevice(comp);
        addDevice(burner);
        addDevice(turbine);
        addDevice(exhaust);
        addDevice(generator);
        addDevice(shaft);
    }
}
