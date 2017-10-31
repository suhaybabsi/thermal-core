package com.thermo.test.systems;

import com.suhaybabsi.thermodesigner.core.ThermalSystem;
import com.suhaybabsi.thermodesigner.devices.Boiler;
import com.suhaybabsi.thermodesigner.devices.Condenser;
import com.suhaybabsi.thermodesigner.devices.Generator;
import com.suhaybabsi.thermodesigner.devices.Pipe;
import com.suhaybabsi.thermodesigner.devices.Pump;
import com.suhaybabsi.thermodesigner.devices.Shaft;
import com.suhaybabsi.thermodesigner.devices.SteamTurbine;
import com.suhaybabsi.thermodesigner.thermo.fluid.Water;

/**
 *
 * @author suhaybal-absi
 */
public class SteamTurbineSimple extends ThermalSystem {
    
    public SteamTurbineSimple() {
        
        construct();
    }

    private void construct() {
        
        Pump pump = new Pump();
        pump.defineIsentropicEfficiency(0.85);
        pump.defineExitPressure(16000);
        pump.defineMassFlowRate(15);
        
        Boiler boiler = new Boiler();
        boiler.definePressureLoss(0.044);
        boiler.defineExitTemperature(625 + 273.15);
            
        SteamTurbine turbine = new SteamTurbine();
        turbine.defineIsentropicEfficiency(0.87);
        turbine.defineExhaustPressure(10);
        
        Condenser condenser = new Condenser();
        condenser.definePressureLoss(.03);
        condenser.defineSubcoolingAmount(5);
        
        Pipe pipe1 = createPipe(pump, boiler);
        Pipe pipe2 = createPipe(boiler, turbine);
        Pipe pipe3 = createPipe(turbine, condenser);
        Pipe pipe4 = createPipe(condenser, pump);
        
        pipe1.setFluid(Water.getInstance());
        
        pipe1.definePressureLoss(0.01);
        pipe1.defineEnthalpyLoss(0.01);
        
        pipe2.definePressureLoss(0.01);
        pipe2.defineEnthalpyLoss(0.01);
        
        pipe3.definePressureLoss(0.0);
        pipe3.defineEnthalpyLoss(0.0);
        
        pipe4.definePressureLoss(0.01);
        pipe4.defineEnthalpyLoss(0.01);
        
        Generator load = new Generator();
        Shaft shaft = new Shaft();
        pump.connectShaft(shaft);
        turbine.connectShaft(shaft); 
        load.connectShaft(shaft);
        
        addDevice(pipe1);
        addDevice(pipe2);
        addDevice(pipe3);
        addDevice(pipe4);
        
        addDevice(pump);
        addDevice(boiler);
        addDevice(turbine);
        addDevice(condenser);
        addDevice(load);
        addDevice(shaft);
    }

    
}
