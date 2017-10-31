/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thermo.test.systems;

import com.suhaybabsi.thermodesigner.core.ThermalSystem;
import com.suhaybabsi.thermodesigner.devices.Boiler;
import com.suhaybabsi.thermodesigner.devices.ClosedFeedWaterHeater;
import com.suhaybabsi.thermodesigner.devices.Condenser;
import com.suhaybabsi.thermodesigner.devices.CrossPoint;
import com.suhaybabsi.thermodesigner.devices.Generator;
import com.suhaybabsi.thermodesigner.devices.MixingChamber;
import com.suhaybabsi.thermodesigner.devices.Pipe;
import com.suhaybabsi.thermodesigner.devices.Pump;
import com.suhaybabsi.thermodesigner.devices.Shaft;
import com.suhaybabsi.thermodesigner.devices.SteamTurbine;
import com.suhaybabsi.thermodesigner.thermo.fluid.Water;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class SteamTurbineRegenrative2 extends ThermalSystem{

    public SteamTurbineRegenrative2() {
        
        construct();
    }
    private void construct() {
        
        
        Pump pump1 = new Pump();
        pump1.defineIsentropicEfficiency(0.85);
        pump1.defineMassFlowRate(10);
        //pump1.defineExitPressure(1500);
        
        Pump pump2 = new Pump();
        pump2.defineIsentropicEfficiency(0.85);
        pump2.defineMassFlowRate(5);
        //pump2.defineExitPressure(15000);
        
        ClosedFeedWaterHeater feedHeater = new ClosedFeedWaterHeater();
        
        MixingChamber mixingChamber = new MixingChamber();
        mixingChamber.definePressure(15000);
        
        Boiler boiler = new Boiler();
        boiler.definePressureLoss(0.0);
        boiler.defineExitTemperature(625 + 273.15);
            
        SteamTurbine turbine1 = new SteamTurbine();
        turbine1.setName("Turbine 1");
        turbine1.defineIsentropicEfficiency(0.88);
        turbine1.defineExhaustPressure(4000);
        
        CrossPoint extractionPoint = new CrossPoint();
        
        SteamTurbine turbine2 = new SteamTurbine();
        turbine2.setName("Turbine 2");
        turbine2.defineIsentropicEfficiency(0.88);
        turbine2.defineExhaustPressure(10);
        
        Condenser condenser = new Condenser();
        condenser.definePressureLoss(.01);
        condenser.defineSubcoolingAmount(0);
        
        Pipe pipe1 = createPipe(condenser, pump1);
        Pipe pipe2 = createPipe(pump1, feedHeater);
        Pipe pipe3 = createPipe(feedHeater, mixingChamber);
        Pipe pipe4 = createPipe(mixingChamber, boiler);
        Pipe pipe5 = createPipe(boiler, turbine1);
        
        Pipe pipe6 = createPipe(turbine1, extractionPoint);
        Pipe pipe7 = createPipe(extractionPoint, turbine2);
        Pipe pipe8 = createPipe(turbine2, condenser);
        Pipe pipe9 = createPipe(extractionPoint, feedHeater);
        Pipe pipe10 = createPipe(feedHeater, pump2);
        Pipe pipe11 = createPipe(pump2, mixingChamber);
        
        List<Pipe> allPipes = new ArrayList<Pipe>();
        allPipes.add(pipe1);
        allPipes.add(pipe2);
        allPipes.add(pipe3);
        allPipes.add(pipe4);
        allPipes.add(pipe5);
        allPipes.add(pipe6);
        allPipes.add(pipe7);
        allPipes.add(pipe8);
        allPipes.add(pipe9);
        allPipes.add(pipe10);
        allPipes.add(pipe11);
        
        pipe5.setFluid(Water.getInstance());
        
        for(int i= 0; i < allPipes.size(); i++){
            
            Pipe pipe = allPipes.get(i);
            
            //if(i >= 5 && i <= 7){
                
                pipe.definePressureLoss(0.0);
                pipe.defineEnthalpyLoss(0.0);
            //}else{
                
            //    pipe.definePressureLoss(0.01);
            //    pipe.defineEnthalpyLoss(0.01);
            //}
            
            addDevice(pipe);
        }
        
        Generator load = new Generator();
        Shaft shaft = new Shaft();
        turbine1.connectShaft(shaft);
        turbine2.connectShaft(shaft);
        load.connectShaft(shaft);
        
        addDevice(pump1);
        addDevice(pump2);
        addDevice(feedHeater);
        addDevice(mixingChamber);
        addDevice(boiler);
        addDevice(turbine1);
        addDevice(turbine2);
        addDevice(condenser);
        addDevice(extractionPoint);
        addDevice(load);
        addDevice(shaft);
        
        System.out.println("Devices count: " + this.getDeviceList().size());
    }
}
