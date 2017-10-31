/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.CommonEquations;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.Property;
import com.suhaybabsi.thermodesigner.core.ThermalEquation;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import com.suhaybabsi.thermodesigner.thermo.fluid.Gas;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class Nozzle extends SteadyFlowDevice {
    
    private final Property pressureLoss = new Property("Pressure Loss");
    private final Property exhaustStaticPressure = new Property("Exhaust Static Pressure");
    private final Property exhaustMachNumber = new Property("Exhaust Mach Number");
    private final Property exhaustStaticTemperature = new Property("Exhaust Static Temperature");
    private final Property heatLoss = new Property("Heat Loss");
    
    
    public void adiapatic(){
        heatLoss.define(0.0);
    }
    public void definePressureLoss(double value) {
        this.pressureLoss.define(value);
    }
    public void defineExhaustStaticPressure(double value) {
        this.exhaustStaticPressure.define(value);
    }
    public void defineExhaustStaticTemperature(double value) {
        this.exhaustStaticTemperature.define(value);
    }
    public void defineExhaustVelocity(double value) {
        Flow flowOut = flowManager.getOut(0);
        flowOut.getVelocityProp().define(value);
    }
    public void defineExhaustMachNumber(double value) {
        this.exhaustMachNumber.define(value);
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
    public double getExhaustMachNumber() {
        return exhaustMachNumber.getValue();
    }
    public double getExhaustStaticPressure() {
        return exhaustStaticPressure.getValue();
    }
    public double getExhaustStaticTemperature() {
        return exhaustStaticTemperature.getValue();
    }
    public double getExhaustVelocity() {
        return exitFlow.getVelocityProp().getValue();
    }
    
    private static class TotalPressureEquation extends ThermalEquation {

        private final Property P_p;
        private final Property Pt_p;
        private final Property M_p;
        private final Fluid fluid;
        private final Property Tt_p;
        
        private TotalPressureEquation(Property P_p, Property Pt_p, Property Tt_p,Property M_p, Fluid f){
            
            super(P_p, Pt_p, Tt_p, M_p);
            this.P_p = P_p;
            this.Pt_p = Pt_p;
            this.M_p = M_p;
            this.Tt_p = Tt_p;
            this.fluid = f;
            
        }

        @Override
        protected double evaluate() {
            
            double P = P_p.getValue();
            double Pt = Pt_p.getValue();
            double Tt = Tt_p.getValue();
            double M = M_p.getValue();
            
            double g = fluid.specificHeatRatio(Tt);
            
            return Pt -  P * Math.pow(1 + (g-1) / 2 * Math.pow(M, 2), g / (g-1));
        }
    }
    
    private static class TotalTemperatureEquation extends ThermalEquation {

        private final Property T_p;
        private final Property Tt_p;
        private final Property M_p;
        private final Fluid fluid;

       
        private TotalTemperatureEquation(Property T_p, Property Tt_p,Property M_p, Fluid f){
            
            super(T_p, Tt_p, Tt_p, M_p);
            this.T_p = T_p;
            this.Tt_p = Tt_p;
            this.M_p = M_p;
            this.fluid = f;
        }

        @Override
        protected double evaluate() {
            
            double T = T_p.getValue();
            double Tt = Tt_p.getValue();
            double M = M_p.getValue();
            
            double g = fluid.specificHeatRatio(Tt);
            
            return Tt -  T * (1 + (g-1) / 2 * Math.pow(M, 2));
        }
    }
    
    private static class VelocityEquation extends ThermalEquation {

        private final Property u_p;
        private final Property M_p;
        private final Property T_p;
        private final Property Tt_p;
        private final Gas gas;
    
        
        private VelocityEquation(Property u_p, Property M_p, Property T_p, Property Tt_p, Gas gas){
            
            super(u_p, M_p, T_p, Tt_p);
            this.u_p = u_p;
            this.M_p = M_p;
            this.T_p = T_p;
            this.Tt_p = Tt_p;
            this.gas = gas;
        }

        @Override
        protected double evaluate() {
            
            double u = u_p.getValue();
            double M = M_p.getValue();
            double T = T_p.getValue();
            double Tt = Tt_p.getValue();
            
            double R = gas.gasConstant() * 1000;
            double g = gas.specificHeatRatio(Tt);
            //System.out.println("g: "+g+", R:"+R);
            return u - M * Math.sqrt(g * R * T);
        }
    }
    
    private final Flow exitFlow = new Flow();
    public Flow getExitFlow() {
        return exitFlow;
    }
    
    public Nozzle() {
        super("Nozzle", "nozzle");
        flowManager.disableOutlet();
    }

    @Override
    protected void configureEquations() throws ConfigurationException {
        
        Flow flowIn = flowManager.getIn(0);
        Flow flowOut = exitFlow;
        insureFluids(flowIn, flowOut);
        
        Property massIn = flowIn.getMassRateProp();
        Property massOut = flowOut.getMassRateProp();
        
        Property p1 = flowIn.getPressureProp();
        Property p2 = flowOut.getPressureProp();
        
        Property t1 = flowIn.getTemperatureProp();
        Property t2 = flowOut.getTemperatureProp();
        Fluid f = flowIn.getFluid();
        
        addEquation(new CommonEquations.MassConservationEquation(massIn, massOut));
        addEquation(new CommonEquations.PressureLossEquation(p1, p2, pressureLoss));
        addEquation(new TotalPressureEquation(exhaustStaticPressure, p2, t2, 
                exhaustMachNumber, f));
        addEquation(new TotalTemperatureEquation(exhaustStaticTemperature, t2, 
                exhaustMachNumber, f));
        addEquation(new CommonEquations.GasHeatLossEquation(heatLoss, massIn, t1, t2, f));
        
        Property u2 = flowOut.getVelocityProp();
        addEquation(new VelocityEquation(u2, exhaustMachNumber, exhaustStaticTemperature, t2, (Gas) f));
    }
    
    @Override
    public List<FieldResult> getEnergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("pl", "Pressure Loss", getPressureLoss()) );
        res.add(FieldResult.create("esp", "Exhaust Static Pressure", getExhaustStaticPressure()) );
        res.add(FieldResult.create("est", "Exhaust Static Temperature", getExhaustStaticTemperature()) );
        res.add(FieldResult.create("em", "Mach Number", getExhaustMachNumber()) );
        res.add(FieldResult.create("hl", "Heat Loss", getHeatLoss()) );
        
        return res;
    }
    
    @Override
    protected List<FieldResult> getEnergyOutputResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Exit Static Pressure", exhaustStaticPressure.getValue() ));
        res.add(FieldResult.create("Exit Total Pressure", exitFlow.getPressure() ));
        res.add(FieldResult.create("Exit Static Temperature", exhaustStaticTemperature.getValue() ));
        res.add(FieldResult.create("Exit Total Temperature", exitFlow.getTemperature() ));
        res.add(FieldResult.create("Exit Mach Number", exhaustMachNumber.getValue() ));
        res.add(FieldResult.create("Exit Velocity", exitFlow.getVelocity() ));
        
        return res;
    }
}
