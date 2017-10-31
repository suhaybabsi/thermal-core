/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thermo.test;

import com.hummeling.if97.IF97;
import com.suhaybabsi.thermodesigner.core.CalculationException;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.ThermalSystem;
import com.suhaybabsi.thermodesigner.devices.Boiler;
import com.suhaybabsi.thermodesigner.devices.ClosedFeedWaterHeater;
import com.suhaybabsi.thermodesigner.devices.Condenser;
import com.suhaybabsi.thermodesigner.devices.CrossPoint;
import com.suhaybabsi.thermodesigner.devices.DummyDevice;
import com.suhaybabsi.thermodesigner.devices.OpenFeedWaterHeater;
import com.suhaybabsi.thermodesigner.devices.Pipe;
import com.suhaybabsi.thermodesigner.devices.Pump;
import com.suhaybabsi.thermodesigner.devices.SteadyFlowDevice;
import com.suhaybabsi.thermodesigner.devices.SteamTurbine;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Water;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author suhaybal-absi
 */
public class SteamCycleTest {
    
    public static void main(String[] args) {
        
        
        doPumpTest();
        //doBoilerTest();
        //doTurbineTest();
        //doCondenserTest();
        //doPipeTest();
        //doFeedWaterHeaterTest();
        //doClosedFeedWaterHeaterTest();
        //doFeedWaterExtraction();
        //doCrossPoint();
        //doTurbineExtraction();
    }
    
    public static Pipe createPipe(SteadyFlowDevice dvc1, SteadyFlowDevice dvc2){
        
        Pipe pipe = new Pipe();
        
        Flow f1 = new Flow();
        dvc1.getFlowManager().addOut(f1);
        pipe.getFlowManager().addIn(f1);
        
        Flow f2 = new Flow();
        pipe.getFlowManager().addOut(f2);
        dvc2.getFlowManager().addIn(f2);
        
        pipe.setInletDevice(dvc1);
        pipe.setOutletDevice(dvc2);
        
        return pipe;
    }

    private static void doPumpTest() {
        
        try {
            
            DummyDevice dummy1 = new DummyDevice();
            DummyDevice dummy2 = new DummyDevice();
            
            Pump pump = new Pump();
            pump.defineIsentropicEfficiency(0.85);
            //pump.defineExitPressure(16000);
            pump.defineMassFlowRate(15);
            pump.defineWorkConsumed(285);
            
            Pipe in_pipe = createPipe(dummy1, pump);
            in_pipe.setFluid(Water.getInstance());
            in_pipe.getEndState().definePressure(9);
            in_pipe.getEndState().defineTemperature(38 + 273.15);
            
            Pipe out_pipe = createPipe(pump, dummy2);
            
            in_pipe.configure();
            pump.configure();
            out_pipe.configure();
            
            in_pipe.calculateModel();
            pump.calculateModel();
            
            System.out.println(pump);
            
        } catch (ConfigurationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CalculationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void doFeedWaterHeaterTest() {
        
        
        DummyDevice dummy1 = new DummyDevice();
        DummyDevice dummy2 = new DummyDevice();
        DummyDevice dummy3 = new DummyDevice();
        OpenFeedWaterHeater feedHeater = new OpenFeedWaterHeater();
        
        Pipe in_pipe1 = createPipe(dummy1, feedHeater);
        Pipe in_pipe2 = createPipe(dummy2, feedHeater);
        Pipe out_pipe = createPipe(feedHeater, dummy3);
        
        try {
            
            in_pipe1.setFluid(Water.getInstance());
            in_pipe1.getEndState().defineEntropy(6.929413046984801);
            
            in_pipe2.setFluid(Water.getInstance());
            in_pipe2.getEndState().definePressure(1200);
            in_pipe2.getEndState().defineTemperature(313.4705433668645);
            in_pipe2.getEndState().defineEntropy(0.5764265006207172);
            
            out_pipe.defineMassRate(15.0);
            out_pipe.getStartState().definePressure(1200);
            
            feedHeater.configure();
            feedHeater.calculateModel();
            
            //System.out.println(feedHeater);
            
        } catch (ConfigurationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CalculationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(in_pipe1.printEnergyResults());
        System.out.println(in_pipe2.printEnergyResults());
        System.out.println(feedHeater.printEnergyResults());
        System.out.println(out_pipe.printEnergyResults());
    }

    private static void doBoilerTest() {
        
        DummyDevice dummy1 = new DummyDevice();  
        DummyDevice dummy2 = new DummyDevice();
        Boiler boiler = new Boiler();
        
        try {
            
            Pipe in_pipe = createPipe(dummy1, boiler);
            Pipe out_pipe = createPipe(boiler, dummy2);
            
            in_pipe.setFluid(Water.getInstance());
            in_pipe.defineMassRate(15);
            in_pipe.getEndState().definePressure(15900);
            in_pipe.getEndState().defineTemperature(35 + 273.15);
            
            boiler.definePressureLoss(0.044);
            boiler.defineExitTemperature(625 + 273.15);
            
            boiler.configure();
            boiler.calculateModel();
            
            System.out.println(boiler);
            System.out.println(in_pipe);
            System.out.println(out_pipe);
            
        } catch (ConfigurationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CalculationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void doTurbineTest() {
        
        DummyDevice dummy1 = new DummyDevice();  
        DummyDevice dummy2 = new DummyDevice();
        SteamTurbine turbine = new SteamTurbine();
        
        try {
            
            Pipe in_pipe = createPipe(dummy1, turbine);
            Pipe out_pipe = createPipe(turbine, dummy2);
            
            in_pipe.setFluid(Water.getInstance());
            in_pipe.defineMassRate(15);
            in_pipe.getEndState().definePressure(15000);
            in_pipe.getEndState().defineTemperature(600 + 273.15);
            
            turbine.defineIsentropicEfficiency(0.87);
            turbine.defineExhaustPressure(10);
            
            turbine.configure();
            turbine.calculateModel();
            /*
            in_pipe.configure();
            out_pipe.configure();
            in_pipe.calculateModel();
            out_pipe.calculateModel();
            */
            System.out.println(turbine);
            System.out.println(in_pipe);
            System.out.println(out_pipe);
            
        } catch (ConfigurationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CalculationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void doCondenserTest() {
        
        try {
            
            DummyDevice dummy1 = new DummyDevice();
            DummyDevice dummy2 = new DummyDevice();
            Condenser condenser = new Condenser();
            
            Pipe in_pipe = createPipe(dummy1, condenser);
            Pipe out_pipe = createPipe(condenser, dummy2);
            
            in_pipe.setFluid(Water.getInstance());
            in_pipe.defineMassRate(15);
            in_pipe.getEndState().definePressure(10);
            in_pipe.getEndState().defineTemperature(45.81 + 273.15);
            in_pipe.getEndState().defineVapourFraction(0.8);
            
            condenser.definePressureLoss(.03);
            condenser.defineSubcoolingAmount(5);
            
            condenser.configure();
            condenser.calculateModel();
            
            System.out.println(condenser);
            System.out.println(in_pipe);
            System.out.println(out_pipe);
            
        } catch (ConfigurationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CalculationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        IF97 if97 = new IF97();
        System.out.println("X (s=0.6492): "+if97.vapourFractionPS(0.01, 7.6492) );
        System.out.println("T (s=0.6492): "+if97.specificEnthalpyPX(.01, 0.9333) );
        
    }

    private static void doPipeTest() {
        try {
            
            DummyDevice dummy1 = new DummyDevice();
            DummyDevice dummy2 = new DummyDevice();
            
            Pipe pipe = createPipe(dummy1, dummy2);
            
            pipe.setFluid(Water.getInstance());
            pipe.defineMassRate(15);
            pipe.definePressureLoss(0.0);
            pipe.defineEnthalpyLoss(0.0);
            
            pipe.getStartState().definePressure(1200.0);
            pipe.getStartState().defineTemperature(461.1146416213004);
            pipe.getStartState().defineVapourFraction(0);
            
            pipe.configure();
            pipe.calculateModel();
            
            System.out.println(pipe);
            
        } catch (ConfigurationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CalculationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void doCrossPoint() {
        
        DummyDevice dummy1 = new DummyDevice();
        DummyDevice dummy2 = new DummyDevice();
        DummyDevice dummy3 = new DummyDevice();
        CrossPoint crossPoint = new CrossPoint();
        
        Pipe in_pipe = createPipe(dummy1, crossPoint);
        Pipe out_pipe1 = createPipe(crossPoint, dummy2);
        Pipe out_pipe2 = createPipe(crossPoint, dummy3);
        
        try {
            
            in_pipe.defineMassRate(15);
            in_pipe.setFluid(Water.getInstance());
            //in_pipe.getEndState().definePressure(15000);
            
            out_pipe1.setFluid(Water.getInstance());
            out_pipe1.defineMassRate(3.0);
            out_pipe1.getStartState().definePressure(1200);
            //out_pipe1.getStartState().defineTemperature(313.4705433668645);
            out_pipe1.getStartState().defineEntropy(0.5764265006207172);
            
            //out_pipe2.defineMassRate(15.0);
            //out_pipe2.getStartState().definePressure(1200);
            //out_pipe2.getStartState().defineTemperature(461.1146416213004);
            //out_pipe2.getStartState().defineEntropy(2.2162961195070965);
            
            ThermalSystem system = new ThermalSystem();
            
            system.addDevice(crossPoint);
            system.addDevice(in_pipe);
            system.addDevice(out_pipe1);
            system.addDevice(out_pipe2);
            
            system.configure();
            system.solve();
            
            /*in_pipe.configure();
            in_pipe.calculateModel();
            
            out_pipe1.configure();
            out_pipe1.calculateModel();
            
            out_pipe2.configure();
            out_pipe2.calculateModel();*/
            
            //System.out.println(crossPoint);
            
        } catch (ConfigurationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CalculationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(in_pipe.printEnergyResults());
        System.out.println(crossPoint.printEnergyResults());
        System.out.println(out_pipe1.printEnergyResults());
        System.out.println(out_pipe2.printEnergyResults());
    }
    
    
    private static void doTurbineExtraction() {
        
        DummyDevice dummy1 = new DummyDevice();
        DummyDevice dummy2 = new DummyDevice();
        DummyDevice dummy3 = new DummyDevice();
        SteamTurbine turbine = new SteamTurbine();
        turbine.defineIsentropicEfficiency(0.87);
        
        CrossPoint crossPoint = new CrossPoint();
        
        Pipe src_pipe = createPipe(dummy1, turbine);
        Pipe in_pipe = createPipe(turbine, crossPoint);
        Pipe out_pipe1 = createPipe(crossPoint, dummy2);
        Pipe out_pipe2 = createPipe(crossPoint, dummy3);
        
        try {
            
            src_pipe.defineMassRate(15);
            src_pipe.setFluid(Water.getInstance());
            src_pipe.getEndState().definePressure(15000);
            src_pipe.getEndState().defineTemperature(898.15);
            
            out_pipe1.setFluid(Water.getInstance());
            out_pipe1.getStartState().definePressure(1200);
            out_pipe1.defineMassRate(3.00);
            //out_pipe1.getStartState().defineTemperature(313.4705433668645);
            //out_pipe1.getStartState().defineEntropy(0.5764265006207172);
            
            //out_pipe2.defineMassRate(15.0);
            //out_pipe2.getStartState().definePressure(1200);
            //out_pipe2.getStartState().defineTemperature(461.1146416213004);
            //out_pipe2.getStartState().defineEntropy(2.2162961195070965);
            
            ThermalSystem system = new ThermalSystem();
            
            system.addDevice(turbine);
            system.addDevice(crossPoint);
            
            system.addDevice(src_pipe);
            system.addDevice(in_pipe);
            system.addDevice(out_pipe1);
            system.addDevice(out_pipe2);
            
            system.configure();
            system.solve();
            
            /*in_pipe.configure();
            in_pipe.calculateModel();
            
            out_pipe1.configure();
            out_pipe1.calculateModel();
            
            out_pipe2.configure();
            out_pipe2.calculateModel();*/
            
            //System.out.println(crossPoint);
            
        } catch (ConfigurationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CalculationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println(src_pipe);
        System.out.println(turbine);
        System.out.println(in_pipe);
        System.out.println(crossPoint);
        System.out.println(out_pipe1);
        System.out.println(out_pipe2);
    }

    private static void doFeedWaterExtraction() {
        
        DummyDevice dummy1 = new DummyDevice();
        DummyDevice dummy2 = new DummyDevice();
        DummyDevice dummy3 = new DummyDevice();
        DummyDevice dummy4 = new DummyDevice();
        
        OpenFeedWaterHeater feedHeater = new OpenFeedWaterHeater();        
        SteamTurbine turbine = new SteamTurbine();
        turbine.defineIsentropicEfficiency(0.87);
        CrossPoint crossPoint = new CrossPoint();
        
        Pipe src_pipe = createPipe(dummy1, turbine);
        Pipe in_pipe = createPipe(turbine, crossPoint);
        Pipe out_pipe1 = createPipe(crossPoint, feedHeater);
        Pipe out_pipe2 = createPipe(crossPoint, dummy2);
        
        Pipe fd_in_pipe = createPipe(dummy4, feedHeater);
        Pipe fd_out_pipe = createPipe(feedHeater, dummy3);
        
        try {
            
            src_pipe.defineMassRate(15);
            src_pipe.setFluid(Water.getInstance());
            src_pipe.getEndState().definePressure(15000);
            src_pipe.getEndState().defineTemperature(898.15);
            
            fd_in_pipe.getEndState().definePressure(1200);
            fd_in_pipe.getEndState().defineTemperature(313.4705433668645);
            fd_in_pipe.getEndState().defineEntropy(0.5764265006207172);
            
            fd_out_pipe.defineMassRate(15.0);
            fd_out_pipe.getStartState().definePressure(1200);
            
            ThermalSystem system = new ThermalSystem();
            
            system.addDevice(feedHeater);
            system.addDevice(turbine);
            system.addDevice(crossPoint);
            
            system.addDevice(src_pipe);
            system.addDevice(in_pipe);
            system.addDevice(out_pipe1);
            system.addDevice(out_pipe2);
            system.addDevice(fd_in_pipe);
            system.addDevice(fd_out_pipe);
            
            system.configure();
            system.solve();
            
            /*in_pipe.configure();
            in_pipe.calculateModel();
            
            out_pipe1.configure();
            out_pipe1.calculateModel();
            
            out_pipe2.configure();
            out_pipe2.calculateModel();*/
            
            //System.out.println(crossPoint);
            
        } catch (ConfigurationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
            
            for(ConfigurationException.Error e:ex.getErrorsList()){
                
                System.out.println(e.description());
            }
            
        } catch (CalculationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println(src_pipe);
        System.out.println(turbine);
        System.out.println(in_pipe);
        System.out.println(crossPoint);
        System.out.println(out_pipe2);
        
        System.out.println(out_pipe1);
        System.out.println(fd_in_pipe);
        System.out.println(feedHeater);
        System.out.println(fd_out_pipe);
    }

    private static void doClosedFeedWaterHeaterTest() {
        
        
        DummyDevice dummy1 = new DummyDevice();
        DummyDevice dummy2 = new DummyDevice();
        DummyDevice dummy3 = new DummyDevice();
        DummyDevice dummy4 = new DummyDevice();
        ClosedFeedWaterHeater closedFeedHeater = new ClosedFeedWaterHeater();
        
        Pipe in_pipe1 = createPipe(dummy1, closedFeedHeater);
        Pipe in_pipe2 = createPipe(dummy2, closedFeedHeater);
        Pipe out_pipe1 = createPipe(closedFeedHeater, dummy3);
        Pipe out_pipe2 = createPipe(closedFeedHeater, dummy4);
        
        try {
            
            in_pipe1.setFluid(Water.getInstance());
            in_pipe1.defineMassRate(11);
            in_pipe1.getEndState().definePressure(15000);
            in_pipe1.getEndState().defineEnthalpy(900);
            
            in_pipe2.setFluid(Water.getInstance());
            in_pipe2.defineMassRate(4);
            in_pipe2.getEndState().definePressure(4000);
            in_pipe2.getEndState().defineTemperature(680);
            
            closedFeedHeater.configure();
            closedFeedHeater.calculateModel();
            
            //System.out.println(feedHeater);
            
        } catch (ConfigurationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CalculationException ex) {
            Logger.getLogger(SteamCycleTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println(in_pipe1);
        System.out.println(in_pipe2);
        System.out.println(closedFeedHeater);
        System.out.println(out_pipe1);
        System.out.println(out_pipe2);
    }
    
}
