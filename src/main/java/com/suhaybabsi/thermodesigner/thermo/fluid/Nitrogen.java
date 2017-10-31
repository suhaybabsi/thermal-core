/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.fluid;

import com.suhaybabsi.thermodesigner.thermo.equations.IdealGasHeatEquation;
import com.suhaybabsi.thermodesigner.thermo.tables.GasChemicalTable;

/**
 *
 * @author suhaybal-absi
 */
public class Nitrogen extends Gas {
    
    private static Nitrogen _instance;
    public static Nitrogen getInstance(){
        if(_instance == null){
            _instance = new Nitrogen();
        }
        return _instance;
    }
    
    private IdealGasHeatEquation heatEquation;
    private final GasChemicalTable.GasChemicalRecord chemicalRecord;
    private Nitrogen() {
        super("nitrogen");
        chemicalRecord = GasChemicalTable.readGasChemicalRecord("N2");
        heatEquation = new IdealGasHeatEquation(28.9, -0.1571e-2, 0.8081e-5, -2.873e-9);
        heatEquation.setRange(273, 1800);
        heatEquation.setPercentErrorMax(0.59);
        heatEquation.setPercentErrorAvg(0.34);
        heatEquation.setGasConstant(chemicalRecord.getGasConstant());
        heatEquation.setMolarMass(chemicalRecord.getMolarMass());
    }

    @Override
    public double specificHeatCp(double temperature) {
        return heatEquation.getHeatAtConstPressure(temperature);
    }

    @Override
    public double specificHeatRatio(double temperature) {
        return heatEquation.getHeatRatio(temperature);
    }

    @Override
    public double specificHeatCv(double temperature) {
        return heatEquation.getHeatAtConstVolume(temperature);        
    }

    @Override
    public double gasConstant() {
        return chemicalRecord.getGasConstant();
    }
    
    
}
