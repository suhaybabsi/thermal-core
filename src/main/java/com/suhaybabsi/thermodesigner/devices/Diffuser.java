/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.CommonEquations;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.Property;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class Diffuser extends SteadyFlowDevice {
    
    private final Property pressureLoss = new Property("Pressure Loss");
    private final Property heatLoss = new Property("Heat Loss");
    
    public void definePressureLoss(double value) {
        this.pressureLoss.define(value);
    }
    public void defineHeatLoss(double value) {
        this.heatLoss.define(value);
    }
    
    public double getPressureLoss() {
        return pressureLoss.getValue();
    }
    public double getHeatLoss() {
        return heatLoss.getValue();
    }
    
    public void adiapatic(){
        heatLoss.define(0.0);
    }
    
    public Diffuser() {
        super("Diffuser", "diffuser");
    }
    @Override
    protected void configureEquations() throws ConfigurationException {
        
        insureSingleFlowFluids();
        
        Flow flowIn = flowManager.getIn(0);
        Flow flowOut = flowManager.getOut(0);
        
        Property massIn = flowIn.getMassRateProp();
        Property massOut = flowOut.getMassRateProp();
        
        Property p1 = flowIn.getPressureProp();
        Property p2 = flowOut.getPressureProp();
        
        Property t1 = flowIn.getTemperatureProp();
        Property t2 = flowOut.getTemperatureProp();
        
        Fluid f = flowIn.getFluid();
        
        addEquation(new CommonEquations.MassConservationEquation(massIn, massOut));
        addEquation(new CommonEquations.PressureLossEquation(p1, p2, pressureLoss));
        addEquation(new CommonEquations.GasHeatLossEquation(heatLoss, massIn, t1, t2, f));
    }

    @Override
    public List<FieldResult> getEnergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("pl", "Pressure Loss", getPressureLoss()) );
        res.add(FieldResult.create("hl", "Heat Loss", getHeatLoss()) );
        
        return res;
    }

    @Override
    protected List<FieldResult> getEnergyOutputResults() {
        
        Flow flowIn = flowManager.getIn(0);
        Flow flowOut = flowManager.getOut(0);
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Pressure (1)", flowIn.getPressure() ));
        res.add(FieldResult.create("Temperature (1)", flowIn.getTemperature() ));
        
        res.add(FieldResult.create("Pressure (2)", flowOut.getPressure() ));
        res.add(FieldResult.create("Temperature (2)", flowOut.getTemperature() ));
        
        return res; 
    }
}
