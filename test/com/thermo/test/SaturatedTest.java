/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thermo.test;

import com.suhaybabsi.thermodesigner.thermo.tables.water.SaturatedPressureTable;
import com.suhaybabsi.thermodesigner.thermo.tables.water.SaturatedTemperatureTable;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author suhaybal-absi
 */
public class SaturatedTest {
    
    private static SaturatedTemperatureTable tempTable;
    private static SaturatedPressureTable pressTable;
    @BeforeClass
    public static void setUpBeforeClass() {
        tempTable = SaturatedTemperatureTable.getInstance();
        pressTable = SaturatedPressureTable.getInstance();
    }
    
    @Test
    public void testPressureVolume_Temp() {
        
        double T = 280;
        assertEquals(tempTable.getPressureSaturated(T), 6416.6, 0.0);
        assertEquals(tempTable.getLiquidVolume(T), 0.001333, 0.0);
        assertEquals(tempTable.getVaporVolume(T), 0.030153, 0.0);
        
        T = 2;
        assertEquals(tempTable.getPressureSaturated(T), 0.7157, 0.00001);
        assertEquals(tempTable.getLiquidVolume(T), 0.001, 0.0);
        assertEquals(tempTable.getVaporVolume(T), 182.483, 0.001);
        
    }

    @Test
    public void testEnthalpy_Temp() {
        
        double T = 150;
        assertEquals(tempTable.getLiquidEnthalpy(T), 632.18, 0.0);
        assertEquals(tempTable.getEvapEnthalpy(T), 2113.8, 0.0);
        assertEquals(tempTable.getVaporEnthalpy(T), 2745.9, 0.0);
        
        T = 353;
        assertEquals(tempTable.getLiquidEnthalpy(T), 1696.88, 0.0);
        assertEquals(tempTable.getEvapEnthalpy(T), 844.82, 0.0);
        assertEquals(tempTable.getVaporEnthalpy(T), 2541.7, 0.0001);
        
    }
    
    @Test
    public void testEntropy_Temp() {
        
        double T = 15;
        assertEquals(tempTable.getLiquidEntropy(T), 0.2245, 0.0);
        assertEquals(tempTable.getEvapEntropy(T), 8.5559, 0.0);
        assertEquals(tempTable.getVaporEntropy(T), 8.7803, 0.0);
        
        
        T = 134;
        assertEquals(tempTable.getLiquidEntropy(T), 1.67668, 0.0);
        assertEquals(tempTable.getEvapEntropy(T), 5.31046, 0.0);
        assertEquals(tempTable.getVaporEntropy(T), 6.98714, 0.0);
        
    }
    
    @Test
    public void testInternalEnergy_Temp() {
        
        double T = 233;
        assertEquals(tempTable.getLiquidInternalEnergy(T), 1000.7, 0.01);
        assertEquals(tempTable.getEvapInternalEnergy(T), 1602.4, 0.1);
        assertEquals(tempTable.getVaporInternalEnergy(T), 2603.08, 0.0);
        
        T = 70;
        assertEquals(tempTable.getLiquidInternalEnergy(T), 293.04, 0.0);
        assertEquals(tempTable.getEvapInternalEnergy(T), 2175.8, 0.0);
        assertEquals(tempTable.getVaporInternalEnergy(T), 2468.9, 0.0);
        
    }
    
    /*
     *  Pressure Tests !!
     */
    
    
    @Test
    public void testPressureVolume_Press() {
        
        double P = 1.0;
        assertEquals(pressTable.getTemperatureSaturated(P), 6.97, 0.0);
        assertEquals(pressTable.getLiquidVolume(P), 0.001000, 0.0);
        assertEquals(pressTable.getVaporVolume(P), 129.19, 0.0);
        
        P = 23;
        assertEquals(pressTable.getTemperatureSaturated(P), 63, 0.0);
        assertEquals(pressTable.getLiquidVolume(P), 0.001019, 0.00001);
        assertEquals(pressTable.getVaporVolume(P), 6.7813, 0.0001);
        
    }
    
    @Test
    public void testEnthalpy_Press() {
        
        double P = 100;
        assertEquals(pressTable.getLiquidEnthalpy(P), 417.51, 0.0);
        assertEquals(pressTable.getEvapEnthalpy(P), 2257.5, 0.0);
        assertEquals(pressTable.getVaporEnthalpy(P), 2675.0, 0.0);
        
        P = 17500;
        assertEquals(pressTable.getLiquidEnthalpy(P), 1711.25, 0.0);
        assertEquals(pressTable.getEvapEnthalpy(P), 817.6, 0.01);
        assertEquals(pressTable.getVaporEnthalpy(P), 2528.85, 0.0);
        
    }
    
    @Test
    public void testEntropy_Press() {
        
        double P = 500;
        assertEquals(pressTable.getLiquidEntropy(P), 1.8604 , 0.0);
        assertEquals(pressTable.getEvapEntropy(P), 4.9603, 0.0);
        assertEquals(pressTable.getVaporEntropy(P), 6.8207, 0.0);
        
        
        P = 730;
        assertEquals(pressTable.getLiquidEntropy(P), 2.008, 0.001);
        assertEquals(pressTable.getEvapEntropy(P), 4.6846, 0.0001);
        assertEquals(pressTable.getVaporEntropy(P), 6.6931, 0.0001);
         
    }
    
    @Test
    public void testInternalEnergy_Press() {
        
        double P = 300;
        assertEquals(pressTable.getLiquidInternalEnergy(P), 561.11, 0.01);
        assertEquals(pressTable.getEvapInternalEnergy(P), 1982.1, 0.1);
        assertEquals(pressTable.getVaporInternalEnergy(P), 2543.2, 0.0);
        
        P = 440;
        assertEquals(pressTable.getLiquidInternalEnergy(P), 618.964, 0.001);
        assertEquals(pressTable.getEvapInternalEnergy(P), 1937.38, 0.0);
        assertEquals(pressTable.getVaporInternalEnergy(P), 2556.3, 0.01);
        
    }
}
