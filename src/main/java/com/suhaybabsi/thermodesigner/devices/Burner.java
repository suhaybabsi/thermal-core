/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.CommonEquations;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.Fuel;
import com.suhaybabsi.thermodesigner.core.HeatProducingDevice;
import com.suhaybabsi.thermodesigner.core.Property;
import com.suhaybabsi.thermodesigner.core.ThermalEquation;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Air;
import com.suhaybabsi.thermodesigner.thermo.fluid.CombustionGas;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class Burner extends SteadyFlowDevice implements HeatProducingDevice {

    public void definePressureLoss(double d) {
        this.pressureLoss.define(d);
    }
    public void defineCombustionEfficiency(double d) {
        this.efficiency.define(d);
    }
    public void defineFuelAirRatio(double v){
        this.fuelAirRatio.define(v);
    }
    public double getFuelAirRatio() {
        return fuelAirRatio.getValue();
    }
    
    private static class CombustionEquation extends ThermalEquation {
        
        private final Property fuelAirRatio;
        private final Property temp1;
        private final Property temp2;
        private final Property effeciency;
        private final Fuel fuel;
        private final Fluid outFd;
        private final Fluid inFd;
        private CombustionEquation(Property f, Property T1, Property T2, Property nb, Fuel fuel, Fluid inFd, Fluid outFd) {
            super(f, T1, T2);
            setName("Combustion Equation");
            this.fuelAirRatio = f;
            this.temp1 = T1;
            this.temp2 = T2;
            this.effeciency = nb;
            this.fuel = fuel;
            this.inFd = inFd;
            this.outFd = outFd;
        }

        @Override
        protected double evaluate() {
            
            
            double Tref = 298;
            double f = fuelAirRatio.getValue();
            double T1 = temp1.getValue();
            double T2 = temp2.getValue();
            
            double Cpa1 = inFd.specificHeatCp(T1);
            double Cpa2 = inFd.specificHeatCp(T2);
            
            ((CombustionGas) outFd).setFuelAirRatio(f);
            
            double Cpg2 = outFd.specificHeatCp(T2);
            double HV = fuel.getHigherHeatingValue();
            double nb = effeciency.getValue();
            
            //System.out.println("T1: "+T1+", T2: "+T2+", rt: "+Cpa1+", nt:"+Cpa2);
            double val = f -  ( Cpa1 * (T1 - Tref) - Cpa2 * (T2 - Tref) ) 
                        / ( Cpg2 * (T2 - Tref) - HV ) 
                        / nb;
            
            return val;
        }
    }
    
    private static class MassConservationEquation extends ThermalEquation {
        
        private Property mrate1;
        private Property mrate2;
        private Property fa_ratio;
        private MassConservationEquation(Property m1, Property m2, Property fa_r){
            super(m1, m2, fa_r);
            setName("Mass Conservation Equation");
            this.mrate1 = m1;
            this.mrate2 = m2;
            this.fa_ratio = fa_r;
        }
        @Override
        protected double evaluate() {
            double m1 = mrate1.getValue();
            double m2 = mrate2.getValue();
            double f = fa_ratio.getValue();
            
            return m1 * (1 + f) - m2;
        }
    }

    @Override
    protected void configureEquations() throws ConfigurationException {
        
        Flow flowIn = flowManager.getIn(0);
        Flow flowOut = flowManager.getOut(0);
        
        if( (flowIn.getFluid() instanceof Air) == false 
               && (flowIn.getFluid() instanceof CombustionGas)  == false ){
            
            throw new ConfigurationException(this, "Incompitable intake fluid type !");
        }
        
        Property T1 = flowIn.getTemperatureProp();
        Property T2 = flowOut.getTemperatureProp();
        
        Property P1 = flowIn.getPressureProp();
        Property P2 = flowOut.getPressureProp();
        
        Property mrate1 = flowIn.getMassRateProp();
        Property mrate2 = flowOut.getMassRateProp();
        
        if(Double.isNaN(exitTemperature) == false){        
            flowOut.defineTemperature(exitTemperature);
        }
        
        flowOut.setFluid(new CombustionGas(0.0));
        
        addEquation(new CombustionEquation(fuelAirRatio, T1, T2, efficiency, fuel, 
                flowIn.getFluid(), flowOut.getFluid()) );
        addEquation(new MassConservationEquation(mrate1, mrate2, fuelAirRatio));
        addEquation(new CommonEquations.PressureLossEquation(P1, P2, pressureLoss));
        
        
    }
    
    private double exitTemperature = Double.NaN;
    private final Property fuelAirRatio = new Property("Fuel Air Ratio");
    private final Property pressureLoss = new Property("Pressure Loss");
    private final Property efficiency = new Property("Efficiency");
    private Fuel fuel = Fuel.DIESEL;
    public void setFuel(Fuel fuel) {
        this.fuel = fuel;
    }
    public Fuel getFuel() {
        return fuel;
    }

    public Burner() {
        super("Burner", "burner");
        efficiency.setMin(0);
        efficiency.setMax(100);
        
        pressureLoss.setMin(0);
        pressureLoss.setMax(100);
        
        fuelAirRatio.setMin(0);
        fuelAirRatio.setMax(1.0);
    }

    public void defineExitTemperature(double temp) {
        this.exitTemperature = temp;
    }
    
    public double getFuelMassRate() {
        Flow flow = flowManager.getIn(0);
        double mf = fuelAirRatio.getValue() * flow.getMassRate();
        return mf;
    }
    public double getExitTemperature() {
        Flow flow2 = flowManager.getOut(0);
        return Double.isNaN(exitTemperature) ? flow2.getTemperature(): exitTemperature;
    }
    public double getPressureLoss() {
        return pressureLoss.getValue();
    } 

    public double getEfficiency() {
        return efficiency.getValue();
    }

    @Override
    public double getHeatProduced() {
        double mfuel = getFuelMassRate();
        return mfuel * getFuel().getHigherHeatingValue();
    }
    
    private final Property exergyDestruction = new Property("Exergy Destruction");
    private final Property secondLawEfficiency = new Property("Second Law Efficiency");
    @Override
    public void calculateExergy(){
        
        super.calculateExergy();
       
        Flow f1 = flowManager.getIn(0);
        Flow f2 = flowManager.getOut(0);

        double m1 = f1.getMassRate();
        double m2 = f2.getMassRate();

        double x1 = f1.getExergy();
        double x2 = f2.getExergy();

        double xf = 1.06 * fuel.getHigherHeatingValue();
        double mf = getFuelMassRate();

        double Xin = m1 * x1 + mf * xf;
        double Xout = m2 * x2;

        double Xdest = Xin - Xout;
        double Xeff = (Xin - Xdest) / Xin;

        exergyDestruction.setValue(Xdest);
        secondLawEfficiency.setValue(Xeff);
    }

    @Override
    protected List<FieldResult> getEnergyOutputResults() {
        
        Flow flow1 = flowManager.getIn(0);
        Flow flow2 = flowManager.getOut(0);
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Mass Rate (1)", flow1.getMassRate()) );
        res.add(FieldResult.create("Fuel Air Ratio", getFuelAirRatio()) );
        res.add(FieldResult.create("P2", flow2.getPressure()) );
        res.add(FieldResult.create("T2", flow2.getTemperature()) );
        
        return res;
    }

    @Override
    public List<FieldResult> getEnergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("et", "Exit Temperature", getExitTemperature()) );
        res.add(FieldResult.create("pl", "Pressure Loss",getPressureLoss()) );
        res.add(FieldResult.create("nb", "Efficiency",getEfficiency()) );
        res.add(FieldResult.create("fa", "Fuel Air Ratio",getFuelAirRatio()) );
        res.add(FieldResult.create("mf", "Fuel Mass Rate",getFuelMassRate()) );
        
        return res;
    }

    @Override
    public List<FieldResult> getExergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Xdest", "Exergy Destruction", exergyDestruction.getValue()) );
        res.add(FieldResult.create("Xeff", "Second Law Efficiency", secondLawEfficiency.getValue()) );
        
        return res; 
    }
    
}
