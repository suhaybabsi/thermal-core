/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thermo.test;

import com.suhaybabsi.thermodesigner.core.CalculationException;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.devices.GGHeatExchanger;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Air;
import com.suhaybabsi.thermodesigner.thermo.fluid.CombustionGas;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import com.suhaybabsi.thermodesigner.thermo.fluid.Nitrogen;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author suhaybal-absi
 */
public class DevicesTest {
    
    public static void main(String[] args) {

        
        
        Fluid fd = Nitrogen.getInstance();
        System.out.println("Cp: "+fd.specificHeatCp(825));
        System.out.println("Cv: "+fd.specificHeatCv(825));
        System.out.println("g: "+fd.specificHeatRatio(825));
        
        
        scenario1();
        //scenario2();
    }
    private static void scenario1(){
    
        GGHeatExchanger heatExchanger = new GGHeatExchanger();
        heatExchanger.defineEffectiveness(0.80);
        heatExchanger.defineSide1PressureLoss(0.02);
        heatExchanger.defineSide2PressureLoss(0.02);
        
        Flow fi1 = new Flow();
        fi1.setFluid(Air.getInstance());
        fi1.defineMassRate(1.0);
        fi1.defineTemperature(447);
        fi1.definePressure(405);
        
        Flow fi2 = new Flow();
        fi2.setFluid(Nitrogen.getInstance());
        fi2.defineMassRate(1.02);
        fi2.defineTemperature(825);
        fi2.definePressure(102.34);
        
        
        heatExchanger.getFlowManager().addIn(fi1);
        heatExchanger.getFlowManager().addIn(fi2);
        
        heatExchanger.getFlowManager().addOut(new Flow());
        heatExchanger.getFlowManager().addOut(new Flow());
         
        try {
            
            heatExchanger.configure();
            heatExchanger.calculateModel();
            
            System.out.println(heatExchanger);
            
        } catch (ConfigurationException ex) {
            Logger.getLogger(DevicesTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CalculationException ex) {
            Logger.getLogger(DevicesTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static void scenario2(){
    
        GGHeatExchanger heatExchanger = new GGHeatExchanger();
        heatExchanger.defineEffectiveness(0.80);
        heatExchanger.defineSide1PressureLoss(0.02);
        heatExchanger.defineSide2PressureLoss(0.02);
        
        Flow fi1 = new Flow();
        fi1.setFluid(Air.getInstance());
        fi1.defineMassRate(1.0);
        fi1.defineTemperature(447);
        fi1.definePressure(405);
        
        Flow fi2 = new Flow();
        fi2.setFluid(new CombustionGas(0.0178));
        fi2.defineMassRate(1.02);
        fi2.defineTemperature(825);
        fi2.definePressure(102.34);
        
        
        heatExchanger.getFlowManager().addIn(fi1);
        heatExchanger.getFlowManager().addIn(fi2);
        
        heatExchanger.getFlowManager().addOut(new Flow());
        heatExchanger.getFlowManager().addOut(new Flow());
         
        try {
            
            heatExchanger.configure();
            heatExchanger.calculateModel();
            
            System.out.println(heatExchanger);
            
        } catch (ConfigurationException ex) {
            Logger.getLogger(DevicesTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CalculationException ex) {
            Logger.getLogger(DevicesTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
