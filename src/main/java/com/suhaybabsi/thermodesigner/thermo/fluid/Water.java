/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.fluid;

import com.hummeling.if97.IF97;
import com.hummeling.if97.OutOfRangeException;
import com.suhaybabsi.thermodesigner.thermo.equations.IdealGasHeatEquation;
import com.suhaybabsi.thermodesigner.thermo.tables.GasChemicalTable;
import com.suhaybabsi.thermodesigner.thermo.tables.water.SaturatedPressureTable;
import com.suhaybabsi.thermodesigner.thermo.tables.water.SaturatedTemperatureTable;
import com.suhaybabsi.thermodesigner.thermo.tables.water.SubcooledTable;
import com.suhaybabsi.thermodesigner.thermo.tables.water.SuperheatedTable;

/**
 *
 * @author suhaybal-absi
 */
public class Water extends Fluid {

    private static Water _instance;

    public static Water getInstance() {
        if (_instance == null) {
            _instance = new Water();
        }
        return _instance;
    }
    private final IdealGasHeatEquation heatEquation;
    private final GasChemicalTable.GasChemicalRecord chemicalRecord;
    private final SubcooledTable subcooledTable;
    private final SaturatedTemperatureTable saturatedTemperatureTable;
    private final SaturatedPressureTable saturatedPressureTable;
    private final SuperheatedTable superheatedTable;
    private final IF97 if97;

    private Water() {
        super("water");
        chemicalRecord = GasChemicalTable.readGasChemicalRecord("H2O");
        heatEquation = new IdealGasHeatEquation(32.24, 0.1923e-2, 1.055e-5, -3.595e-9);
        heatEquation.setRange(273, 1800);
        heatEquation.setPercentErrorAvg(0.24);
        heatEquation.setPercentErrorMax(0.53);
        heatEquation.setGasConstant(chemicalRecord.getGasConstant());
        heatEquation.setMolarMass(chemicalRecord.getMolarMass());

        if97 = new IF97();

        subcooledTable = SubcooledTable.getInstance();
        saturatedTemperatureTable = SaturatedTemperatureTable.getInstance();
        saturatedPressureTable = SaturatedPressureTable.getInstance();
        superheatedTable = SuperheatedTable.getInstance();
    }

    @Override
    public double specificHeatRatio(double temperature) {
        return heatEquation.getHeatRatio(temperature);
    }

    @Override
    public double specificHeatCp(double temperature) {
        return heatEquation.getHeatAtConstPressure(temperature);
    }

    @Override
    public double specificHeatCv(double temperature) {
        return heatEquation.getHeatAtConstVolume(temperature);
    }

    @Override
    public double entropy(FlowState state) {
        
        try {
            double value = Double.NaN;
            if (state.isPressureValid() && state.isEnthalpyValid()) {

                double P = state.pressure() / 1000;
                double h = state.enthalpy();
                value = if97.specificEntropyPH(P, h);
                

            } else if (state.isPressureValid() && state.isTemperatureValid()) {

                double P = state.pressure() / 1000;
                double T = state.temperature();
                double X = state.vapourFraction();
                double Tsat = if97.saturationTemperatureP(P);
                
                if(Math.abs(T-Tsat) <= 0.005 && Double.isNaN(X) == false){
                    
                    value = if97.specificEntropyPX(P, X);
                }else{
                
                    value = if97.specificEntropyPT(P, T);
                }
                
            } else if (state.isPressureValid() && state.isVapourFractionValid()) {

                double P = state.pressure() / 1000;
                double X = state.vapourFraction();
                value = if97.specificEntropyPX(P, X);

            } else if (state.isTemperatureValid() && state.isVapourFractionValid()) {

                double T = state.temperature();
                double X = state.vapourFraction();
                value = if97.specificEntropyTX(T, X);
            }

            return value;

        } catch (OutOfRangeException e) {
            return Double.NaN;
        }
    }

    @Override
    public double enthalpy(FlowState state) {
        try {
            double value = Double.NaN;
            if (state.isPressureValid() && state.isEntropyValid()) {

                double P = state.pressure() / 1000;
                double s = state.entropy();
                value = if97.specificEnthalpyPS(P, s);

            } else if (state.isPressureValid() && state.isTemperatureValid()) {

                double P = state.pressure() / 1000;
                double T = state.temperature();
                double X = state.vapourFraction();
                double Tsat = if97.saturationTemperatureP(P);
                
                if(Math.abs(T-Tsat) <= 0.005 && Double.isNaN(X) == false){
                    
                    value = if97.specificEnthalpyPX(P, X);
                }else{
                
                    value = if97.specificEnthalpyPT(P, T);
                }

            } else if (state.isPressureValid() && state.isVapourFractionValid()) {

                double P = state.pressure() / 1000;
                double X = state.vapourFraction();
                value = if97.specificEnthalpyPX(P, X);

            } else if (state.isTemperatureValid() && state.isVapourFractionValid()) {

                double T = state.temperature();
                double X = state.vapourFraction();
                value = if97.specificEnthalpyTX(T, X);
            }

            return value;

        } catch (OutOfRangeException e) {
            return Double.NaN;
        }
    }

    @Override
    public double specificVolume(FlowState state) {
        try {
            double value = Double.NaN;
            if (state.isPressureValid() && state.isEntropyValid()) {

                double P = state.pressure() / 1000;
                double s = state.entropy();
                value = if97.specificVolumePS(P, s);

            } else if (state.isPressureValid() && state.isTemperatureValid()) {

                double P = state.pressure() / 1000;
                double T = state.temperature();
                double X = state.vapourFraction();
                double Tsat = if97.saturationTemperatureP(P);
                
                if(Math.abs(T-Tsat) <= 0.005 && Double.isNaN(X) == false){
                    
                    value = if97.specificVolumePX(P, X);
                }else{
                
                    value = if97.specificVolumePT(P, T);
                }

            } else if (state.isPressureValid() && state.isVapourFractionValid()) {

                double P = state.pressure() / 1000;
                double X = state.vapourFraction();
                value = if97.specificVolumePX(P, X);

            } else if (state.isTemperatureValid() && state.isVapourFractionValid()) {

                double T = state.temperature();
                double X = state.vapourFraction();
                value = if97.specificVolumeTX(T, X);
            }

            return value;

        } catch (OutOfRangeException e) {
            return Double.NaN;
        }
    }

    @Override
    public double vapourFraction(FlowState state) {

        try {
            double value = Double.NaN;
            if (state.isPressureValid() && state.isEntropyValid()) {

                double P = state.pressure() / 1000;
                double s = state.entropy();
                value = if97.vapourFractionPS(P, s);

            } else if (state.isPressureValid() && state.isEnthalpyValid()) {

                double P = state.pressure() / 1000;
                double h = state.enthalpy();
                value = if97.vapourFractionPH(P, h);

            } else if (state.isEnthalpyValid() && state.isEntropyValid()) {

                double h = state.enthalpy();
                double s = state.entropy();
                value = if97.vapourFractionHS(h, s);

            } else if (state.isTemperatureValid() && state.isEntropyValid()) {

                double T = state.temperature();
                double s = state.entropy();
                value = if97.vapourFractionTS(T, s);
            }

            return value;

        } catch (OutOfRangeException e) {
            return Double.NaN;
        }
    }

    @Override
    public double saturatedTemperature(double pressure) {
        return if97.saturationTemperatureP(pressure / 1000);
    }

    @Override
    public double saturatedPressure(double temperature) {
        return if97.saturationPressureT(temperature);
    }

    @Override
    public double temperature(FlowState state) {
        
        try {
            double value = Double.NaN;
            if (state.isPressureValid() && state.isEntropyValid()) {

                double P = state.pressure() / 1000;
                double s = state.entropy();
                value = if97.temperaturePS(P, s);
                //System.err.println(">>>>> " + P + ", "+ s+", "+ value);
            } else if (state.isPressureValid() && state.isEnthalpyValid()) {

                double P = state.pressure() / 1000;
                double h = state.enthalpy();
                value = if97.temperaturePH(P, h);

            } else if (state.isEnthalpyValid() && state.isEntropyValid()) {

                double h = state.enthalpy();
                double s = state.entropy();
                value = if97.temperatureHS(h, s);
            }

            return value;

        } catch (OutOfRangeException e) {
            return Double.NaN;
        }
    }
    
    @Override
    public double pressure(FlowState state) {
        
        try {
            double value = Double.NaN;
            double h = Double.NaN, s = Double.NaN;
            if (state.isTemperatureValid() && state.isEntropyValid()) {
                
                h = enthalpy(state);
                s = state.entropy();
                
            } else if (state.isTemperatureValid() && state.isEnthalpyValid()) {
                
                h = state.enthalpy();
                s = entropy(state);
                
            } else if (state.isEnthalpyValid() && state.isEntropyValid()) {
                
                h = state.enthalpy();
                s = state.entropy();
            }
            
            return if97.pressureHS(h, s) * 1000;
            
        } catch (OutOfRangeException e) {
            return Double.NaN;
        }
    }
}
