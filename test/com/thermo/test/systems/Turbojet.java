package com.thermo.test.systems;

import com.suhaybabsi.thermodesigner.core.Fuel;
import com.suhaybabsi.thermodesigner.core.ThermalSystem;
import com.suhaybabsi.thermodesigner.devices.AirFlow;
import com.suhaybabsi.thermodesigner.devices.Burner;
import com.suhaybabsi.thermodesigner.devices.Compressor;
import com.suhaybabsi.thermodesigner.devices.Diffuser;
import com.suhaybabsi.thermodesigner.devices.GasTurbine;
import com.suhaybabsi.thermodesigner.devices.Nozzle;
import com.suhaybabsi.thermodesigner.devices.Shaft;

/**
 *
 * @author suhaybal-absi
 */
public class Turbojet extends ThermalSystem {

    public Turbojet() {
        construct();
    }

    private void construct() {
        
        AirFlow airFlow = new AirFlow();
        airFlow.defineAltitude(0);
        airFlow.defineMachNumber(0.75);
        airFlow.defineMassFlowRate(74.83);
        
        Diffuser diffuser = new Diffuser();
        diffuser.definePressureLoss(0.01);
        diffuser.adiapatic();
        createFlow(airFlow, diffuser);
        
        Compressor comp = new Compressor();
        comp.defineCompressionRatio(15);
        createFlow(diffuser, comp);
        
        Burner burner = new Burner();
        burner.definePressureLoss(0.03);
        burner.defineCombustionEfficiency(0.99);
        burner.defineExitTemperature(1389);
        burner.setFuel(Fuel.DIESEL);
        createFlow(comp, burner);
        
        GasTurbine turbine = new GasTurbine();
        //turbine.definePolytropicEfficiency(0.89);
        createFlow(burner, turbine);
        
        Nozzle nozzle = new Nozzle();
        nozzle.defineExhaustStaticPressure(104);
        nozzle.definePressureLoss(0.01);
        nozzle.adiapatic();
        createFlow(turbine, nozzle);
        
        Shaft shaft = new Shaft();
        
        comp.connectShaft(shaft);
        turbine.connectShaft(shaft);
        
        addDevice(airFlow);
        addDevice(diffuser);
        addDevice(comp);
        addDevice(burner);
        addDevice(turbine);
        addDevice(nozzle);
        addDevice(shaft); 
    }
    
    
}
