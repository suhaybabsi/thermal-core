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
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;

/**
 *
 * @author suhaybal-absi
 */
public class SingleShaftSeriesReheat extends ThermalSystem {
    
    public SingleShaftSeriesReheat() {
        
        construct();
    }
    
    private void construct() {
        
        Intake intake = new Intake();
        intake.setFluid(Air.getInstance());
        intake.defineMassRate(1.0);
        intake.definePressure(1.01);
        intake.defineTemperature(288);
        
        Compressor comp = new Compressor();
        comp.defineCompressionRatio(30.0);
        comp.definePolytropicEfficiency(0.89);
        createFlow(intake, comp);
        
        Burner burner1 = new Burner();
        burner1.definePressureLoss(0.02);
        burner1.defineCombustionEfficiency(0.99);
        burner1.defineExitTemperature(1525);
        createFlow(comp, burner1);
        
        GasTurbine turbine1 = new GasTurbine();
        turbine1.definePressureRatio(5.3);
        turbine1.definePolytropicEfficiency(0.89);
        createFlow(burner1, turbine1);
        
        Burner burner2 = new Burner();
        burner2.definePressureLoss(0.04);
        burner2.defineCombustionEfficiency(0.99);
        burner2.defineExitTemperature(1525);
        createFlow(turbine1, burner2);
        
        GasTurbine turbine2 = new GasTurbine();
        turbine2.definePolytropicEfficiency(0.89);
        createFlow(burner2, turbine2);
        
        Exhaust exhaust = new Exhaust();
        exhaust.definePressure(1.02);
        createFlow(turbine2, exhaust);
        
        Generator generator = new Generator();
        Shaft shaft = new Shaft();
        
        comp.connectShaft(shaft);
        turbine1.connectShaft(shaft);
        turbine2.connectShaft(shaft);
        generator.connectShaft(shaft);
        
        addDevice(intake);
        addDevice(comp);
        addDevice(burner1);
        addDevice(turbine1);
        addDevice(burner2);
        addDevice(turbine2);
        addDevice(exhaust);
        addDevice(generator);
        addDevice(shaft);
    }
}
