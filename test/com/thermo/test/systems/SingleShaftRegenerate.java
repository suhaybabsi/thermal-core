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
import com.suhaybabsi.thermodesigner.devices.GGHeatExchanger;
import com.suhaybabsi.thermodesigner.devices.Intake;
import com.suhaybabsi.thermodesigner.devices.Shaft;
import com.suhaybabsi.thermodesigner.devices.GasTurbine;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Air;

/**
 *
 * @author suhaybal-absi
 */
public class SingleShaftRegenerate extends ThermalSystem {

    public SingleShaftRegenerate() {
        
        construct();
    }
    
    private void construct() {
        
        Intake intake = new Intake();
        intake.setFluid(Air.getInstance());
        intake.defineMassRate(1.0);
        intake.definePressure(101.325);
        intake.defineTemperature(288);
        
        Compressor comp = new Compressor();
        comp.defineCompressionRatio(4.0);
        
        createFlow(intake, comp);
        
        GGHeatExchanger heatExchanger = new GGHeatExchanger();
        heatExchanger.defineEffectiveness(0.8);
        heatExchanger.defineSide1PressureLoss(0.03);
        heatExchanger.defineSide2PressureLoss(0.03);
        
        createFlow(comp, heatExchanger);
        
        Burner burner = new Burner();
        burner.definePressureLoss(0.03);
        burner.defineCombustionEfficiency(0.99);
        burner.defineExitTemperature(1100);
        
        createFlow(heatExchanger, burner);
        
        GasTurbine turbine = new GasTurbine();
        //turbine.defineWorkProduced(1000);
        
        createFlow(burner, turbine);
        createFlow(turbine, heatExchanger);
        
        Exhaust exhaust = new Exhaust();
        exhaust.definePressure(101.325);
        
        createFlow(heatExchanger, exhaust);
        
        Generator generator = new Generator(); 
        //generator.defineConsumedWork(161);
        Shaft shaft = new Shaft();
        
        comp.connectShaft(shaft);
        turbine.connectShaft(shaft);
        generator.connectShaft(shaft);
        
        addDevice(intake);
        addDevice(comp);
        addDevice(heatExchanger);
        addDevice(burner);
        addDevice(turbine);
        addDevice(exhaust);
        addDevice(generator);
        addDevice(shaft);
        
    }
}
