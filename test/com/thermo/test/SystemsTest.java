/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thermo.test;

import com.suhaybabsi.thermodesigner.core.CalculationException;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.ThermalSystem;
import com.thermo.test.systems.MultiShaftParallel;
import com.thermo.test.systems.MultiShaftSeriesNoReheat;
import com.thermo.test.systems.MultiShaftSeriesReheat;
import com.thermo.test.systems.SingleShaftIntercooling;
import com.thermo.test.systems.SingleShaftRegenerate;
import com.thermo.test.systems.SingleShaftSeriesReheat;
import com.thermo.test.systems.SingleShaftSimple;
import com.thermo.test.systems.SteamTurbineRegenrative;
import com.thermo.test.systems.SteamTurbineRegenrative2;
import com.thermo.test.systems.SteamTurbineSimple;
import com.thermo.test.systems.Turbojet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author suhaybal-absi
 */
public class SystemsTest {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        ThermalSystem simple = new SingleShaftSimple();
        ThermalSystem parallel = new MultiShaftParallel();
        ThermalSystem seriesReheat = new MultiShaftSeriesReheat();
        ThermalSystem seriesNoReheat = new MultiShaftSeriesNoReheat();
        ThermalSystem regenerate = new SingleShaftRegenerate();
        ThermalSystem intercooling = new SingleShaftIntercooling();
        ThermalSystem singleSeriesReheat = new SingleShaftSeriesReheat();
        ThermalSystem steamTurbineSimple = new SteamTurbineSimple();
        ThermalSystem steamTurbineRegenerative = new SteamTurbineRegenrative();
        ThermalSystem steamTurbineRegenerative2 = new SteamTurbineRegenrative2();
        ThermalSystem turbojet = new Turbojet();
        
        try {
//            simple.configure();
//            simple.solve();
//            simple.calculateExergy();
//            simple.writeResultsToPath("output/simple.txt");
//            
//            parallel.configure();
//            parallel.solve();
//            parallel.writeResultsToPath("output/parallel.txt");
////        
//            seriesReheat.configure();
//            seriesReheat.solve();
//            seriesReheat.writeResultsToPath("output/series_reheat.txt");
////            
//            seriesNoReheat.configure();
//            seriesNoReheat.solve();
//            seriesNoReheat.writeResultsToPath("output/series_noreheat.txt");
////            
//            regenerate.configure();
//            regenerate.solve();
//            regenerate.writeResultsToPath("output/preheat.txt");
////            
//            intercooling.configure();
//            intercooling.solve();
//            intercooling.writeResultsToPath("output/intercooling.txt");
////            
//            singleSeriesReheat.configure();
//            singleSeriesReheat.solve();
//            singleSeriesReheat.writeResultsToPath("output/single_series_reheat.txt");
//            
//            steamTurbineSimple.configure();
//            steamTurbineSimple.solve();
//            //steamTurbineSimple.calculateExergy();
//            steamTurbineSimple.writeResultsToPath("output/steam_turbine_simple.txt");
            
//            steamTurbineRegenerative.configure();
//            steamTurbineRegenerative.solve();
//            //steamTurbineRegenerative.calculateExergy();
//            steamTurbineRegenerative.writeResultsToPath("output/steam_turbine_regenerative.txt");
//            
            steamTurbineRegenerative2.configure();
            steamTurbineRegenerative2.solve();
            steamTurbineRegenerative2.calculateExergy();
            steamTurbineRegenerative2.writeResultsToPath("output/steam_turbine_regenerative2.txt");
////            
//            turbojet.configure();
//            turbojet.solve();
//            turbojet.writeResultsToPath("output/turbojet.txt");
            
            
            /*double ri = 1.1;
            double rf = 26.7;
            double dr = (rf-ri)/70;
            String results = "";
            for (double i = 0; i < 71; i++) {
                
                double r = ri + dr*i;
                ThermalSystem cycle = new SingleShaftSimple(r);
                cycle.configure();
                cycle.solve();
                results += r + "\t" + cycle.getEfficiency() + "\n";

            }
            System.out.println(results);
            */ 
        } catch (ConfigurationException ex) {
            Logger.getLogger(SystemsTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CalculationException ex) {
            Logger.getLogger(SystemsTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
//        System.out.println(steamTurbineSimple.getFullResults());
//        System.out.println(steamTurbineRegenerative.getFullResults());
//        System.out.println(steamTurbineRegenerative2.getFullResults());
    }
    
}
