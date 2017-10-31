/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class Intake extends SteadyFlowDevice {
    
    private Fluid fluid;
    private double massRate;
    private double pressure;
    private double temperature;
    
    public Intake() {
        super("Intake", "intake");
        flowManager.disableInlet();
        massRate = Double.NaN;
        pressure = Double.NaN;
        temperature = Double.NaN;
    }
    @Override
    protected void configureEquations() throws ConfigurationException {
        
        Flow flowOut = getFlowManager().getOut(0); 
        if (flowOut.getFluid() == null) {
            flowOut.setFluid(fluid);
        } else {
            fluid = flowOut.getFluid();
        }
        
        if(Double.isNaN(massRate) == false){
            flowOut.defineMassRate(massRate);
        }
        
        if(Double.isNaN(pressure) == false){
            flowOut.definePressure(pressure);
        }
        
        if(Double.isNaN(temperature) == false){
            flowOut.defineTemperature(temperature);
        }
    }
    public void definePressure(double press) {
        this.pressure = press;
    }
    public void defineTemperature(double temp) {
        this.temperature = temp;
    }
    public void defineMassRate(double mass) {
        this.massRate = mass;
    }

    public void setFluid(Fluid fluid) {
        this.fluid = fluid;
    }
    public Fluid getFluid() {
        return fluid;
    }

    public double getPressure() {
        Flow flow = flowManager.getOut(0);
        return flow.getPressure();
    }

    public double getTemperature() {
        Flow flow = flowManager.getOut(0);
        return flow.getTemperature();
    }

    public double getMassRate() {
        Flow flow = flowManager.getOut(0);
        return flow.getMassRate();
    }
    
    public String getFluidName() {
        return (getFluid() == null) ? "null" : getFluid().getName();
    }

    @Override
    public List<FieldResult> getEnergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>(); 
        
        res.add(FieldResult.create("m", "Mass Rate", getMassRate()) );
        res.add(FieldResult.create("p", "Pressure", getPressure()) );
        res.add(FieldResult.create("t", "Temperature", getTemperature()) );
        res.add(FieldResult.create("f", "Gas", getFluidName()) );
        
        return res;
    }
}
