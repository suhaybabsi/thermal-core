package com.suhaybabsi.thermodesigner.thermo.fluid;

import com.suhaybabsi.thermodesigner.core.Utils;
import com.suhaybabsi.thermodesigner.thermo.equations.IdealGasHeatEquation;
import com.suhaybabsi.thermodesigner.thermo.equations.ModifiedAirHeatEquation;
import com.suhaybabsi.thermodesigner.thermo.tables.GasChemicalTable;
import com.suhaybabsi.thermodesigner.thermo.tables.IdealGasHeatTable;
import com.suhaybabsi.thermodesigner.thermo.tables.IdealGasPropertiesTable;
import java.io.File;

/**
 *
 * @author suhaybal-absi
 */
public class Air extends Gas {
    
    private IdealGasPropertiesTable propertiesTable;
    private IdealGasHeatTable heatTable;
    private IdealGasHeatEquation heatEquation;
    private ModifiedAirHeatEquation modifiedHeatEquation;
    private GasChemicalTable.GasChemicalRecord chemicalRecord;
    private GasHeatEvaluator heatEvaluater;
    private Air() {
        super("air");
        chemicalRecord = GasChemicalTable.readGasChemicalRecord("—");
        propertiesTable = new IdealGasPropertiesTable("air_idealgas_props.csv");
        heatTable = new IdealGasHeatTable("air_heat_props.csv");
        heatEquation = new IdealGasHeatEquation(28.11, 0.1967e-2, 0.4802e-5, -1.966e-9);
        heatEquation.setRange(273, 1800);
        heatEquation.setPercentErrorMax(0.72);
        heatEquation.setPercentErrorAvg(0.33);
        heatEquation.setGasConstant(chemicalRecord.getGasConstant());
        heatEquation.setMolarMass(chemicalRecord.getMolarMass());
        
        modifiedHeatEquation = new ModifiedAirHeatEquation();
        heatEvaluater = modifiedHeatEquation;
    }
    public void printGasData() {
        propertiesTable.printData();
        heatTable.printData();
    }
    private static Air _instance;
    public static Air getInstance(){
        if(_instance == null){
            _instance = new Air();
        }
        return _instance;
    }

    public IdealGasPropertiesTable getPropertiesTable() {
        return propertiesTable;
    }
    
    

    @Override
    public double specificHeatCp(double temperature) {
        return heatEvaluater.getHeatAtConstPressure(temperature);
    }

    @Override
    public double specificVolume(FlowState state) {
        double T = state.temperature(), P = state.pressure();
        double R = chemicalRecord.getGasConstant();
        return R*T/P;
    }

    @Override
    public double specificHeatRatio(double temperature) {
        return heatEvaluater.getHeatRatio(temperature);
    }
    
    @Override
    public double enthalpy(FlowState state) {
        
        return propertiesTable.getGasEnthalpy(state.temperature());
    }
    @Override
    public double temperature(FlowState state) {
        
        return propertiesTable.getGasTemperature(state.enthalpy());
    }
    @Override
    public double gasConstant() {
        return chemicalRecord.getGasConstant();
    }

    @Override
    public double entropy(FlowState state) {
        
        double t = state.temperature();
        double s = propertiesTable.getGasEntropy(t);
        return s;
    }
}