/*
 * Gas to gas heat exchanger
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.Property;
import com.suhaybabsi.thermodesigner.core.ThermalEquation;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class GGHeatExchanger extends SteadyFlowDevice {
    
    private static class PressureLossEquation extends ThermalEquation {
        
        private Property pi_p;
        private Property po_p;
        private Property dp_p;
        private PressureLossEquation(Property pi, Property po, Property dp){
            super(pi, po, dp);
            setName("Pressure Loss Equation");
            this.pi_p = pi;
            this.po_p = po;
            this.dp_p = dp;
        }
        
        @Override
        protected double evaluate() {
            
            double pi = pi_p.getValue();
            double po = po_p.getValue();
            double dp = dp_p.getValue();
            
            return po - pi * ( 1 - dp);
        }
    }
    private static class ConitinuityEquation extends ThermalEquation {

        private final Property mip;
        private final Property mop;
        private ConitinuityEquation(Property mi, Property mo) {
            super(mi, mo);
            setName("Conitinuity Equation");
            this.mip = mi;
            this.mop = mo;
        }
        @Override
        protected double evaluate() {

            double mi = mip.getValue();
            double mo = mop.getValue();
            return mi - mo;
        }
    }
    private static class EffectivenessEquation extends ThermalEquation {

        private final Property eff;
        private final Property Ti1;
        private final Property To1;
        private final Property Ti2;
        
        private EffectivenessEquation(Property eff, Property Ti1, Property To1, Property Ti2) {
            super(eff, Ti1, To1, Ti2);
            setName("Effectiveness Equation");
            this.eff = eff;
            this.Ti1 = Ti1;
            this.To1 = To1;
            this.Ti2 = Ti2;
        }

        @Override
        protected double evaluate() {
            
            double to1 = To1.getValue();
            double ti1 = Ti1.getValue();
            double ti2 = Ti2.getValue();
            double _eff = eff.getValue();
            
            return _eff - (to1 - ti1) / (ti2 - ti1);
        }
        
    }
    private static class EnergyConservationEquation extends ThermalEquation {

        private final Property mi1;
        private final Property mi2;
        private final Property Ti1;
        private final Property To1;
        private final Property Ti2;
        private final Property To2;
        private final Fluid fd1;
        private final Fluid fd2;
        
        private EnergyConservationEquation(Property mi1, Property mi2, Property Ti1, Property To1, Property Ti2, Property To2, Fluid f1, Fluid f2) {
            super(mi1, mi2, Ti1, To1, Ti2, To2);
            setName("Energy Conservation Equation");
            this.mi1 = mi1;
            this.mi2 = mi2;
            this.Ti1 = Ti1;
            this.To1 = To1;
            this.Ti2 = Ti2;
            this.To2 = To2;
            this.fd1 = f1;
            this.fd2 = f2;
        }

        @Override
        protected double evaluate() {
            
            double m1 = mi1.getValue();
            double m2 = mi2.getValue();
            
            double ti1 = Ti1.getValue();
            double to1 = To1.getValue();
            double ti2 = Ti2.getValue();
            double to2 = To2.getValue();
            
            double cp1 = fd1.averageHeatCp(ti1, to1);
            double cp2 = fd2.averageHeatCp(ti2, to2);
            
            //System.out.println("Ti1: "+ti1+", To1: "+to1+", Ti2: "+ti2+", To2: "+to2);
            //System.out.println("Cp1: "+cp1+", Cp2: "+cp2);
            return m1 * cp1 * (to1 - ti1) + m2 * cp2 * (to2 - ti2);
        }
        
    }

    private final Property effectiveness = new Property("Effectiveness");
    private final Property side1PressureLoss = new Property("Side 1 Pressure Loss");
    private final Property side2PressureLoss = new Property("Side 2 Pressure Loss");

    private Flow fi1;
    private Flow fi2;
    private Flow fo1;
    private Flow fo2;
    protected GGHeatExchanger(String name, String jsonType) {
        super(name, jsonType);
    }
    public GGHeatExchanger() {
        super("Heat Exchanger", "heat_exchanger");
        effectiveness.setMin(0);
        effectiveness.setMax(1);

        side1PressureLoss.setMin(0);
        side1PressureLoss.setMax(1);
        
        side2PressureLoss.setMin(0);
        side2PressureLoss.setMax(1);
    }
    
    @Override
    protected void configureEquations() throws ConfigurationException {
        
        int inNum = getFlowManager().getInFlowsNum();
        int outNum = getFlowManager().getOutFlowsNum();
        
        boolean onlyValid = (inNum == outNum && outNum == 2);
        if(onlyValid == false){
            
            throw new ConfigurationException(this, "Not enough flows defined.");
        }
        
        fi1 = getFlowManager().getIn(0);
        fi2 = getFlowManager().getIn(1);
        fo1 = getFlowManager().getOut(0);
        fo2 = getFlowManager().getOut(1);
        
        Fluid fd1 = (fi1.getFluid() != null) ? fi1.getFluid() : fo1.getFluid();
        Fluid fd2 = (fi2.getFluid() != null) ? fi2.getFluid() : fo2.getFluid();
        
        fi1.setFluid(fd1);
        fo1.setFluid(fd1);
        fi2.setFluid(fd2);
        fo2.setFluid(fd2);
        
        if(fd1 == null || fd2 == null){
            throw new ConfigurationException(this, "Fluid isn't defined properly.");
        }
        
        Fluid f1 = fo1.getFluid();
        Fluid f2 = fo2.getFluid();
        
        Property Ti1 = fi1.getTemperatureProp();
        Property To1 = fo1.getTemperatureProp();
        Property Ti2 = fi2.getTemperatureProp();
        Property To2 = fo2.getTemperatureProp();
        
        Property Pi1 = fi1.getPressureProp();
        Property Po1 = fo1.getPressureProp();
        Property Pi2 = fi2.getPressureProp();
        Property Po2 = fo2.getPressureProp();
        
        Property mi1 = fi1.getMassRateProp();
        Property mo1 = fo1.getMassRateProp();
        Property mi2 = fi2.getMassRateProp();
        Property mo2 = fo2.getMassRateProp();
        
        addEquation(new EnergyConservationEquation(mi1, mi2, Ti1, To1, Ti2, To2, f1, f2));
        addEquation(new ConitinuityEquation(mi1, mo1));
        addEquation(new ConitinuityEquation(mi2, mo2));
        addEquation(new EffectivenessEquation(effectiveness, Ti1, To1, Ti2));
        
        addEquation(new PressureLossEquation(Pi1, Po1, side1PressureLoss));
        addEquation(new PressureLossEquation(Pi2, Po2, side2PressureLoss));
        
    }

    public void defineEffectiveness(double eff) {
        this.effectiveness.setValue(eff);
        this.effectiveness.setType(Property.PropertyType.DEFINED);
    }
    public double getEffectiveness() {
        return effectiveness.getValue();
    }
    
    public void defineSide1PressureLoss(double dp) {
        this.side1PressureLoss.setValue(dp);
        this.side1PressureLoss.setType(Property.PropertyType.DEFINED);
    }

    public double getSide1PressureLoss() {
        return side1PressureLoss.getValue();
    }
    
    public void defineSide2PressureLoss(double dp) {
        this.side2PressureLoss.setValue(dp);
        this.side2PressureLoss.setType(Property.PropertyType.DEFINED);
    }

    public double getSide2PressureLoss() {
        return side2PressureLoss.getValue();
    }
    
    private final Property exergyDestruction = new Property("Exergy Destruction");
    private final Property secondLawEfficiency = new Property("Second Law Efficiency");
    @Override
    public void calculateExergy(){
        
        super.calculateExergy();
        
        double mi1 = fi1.getMassRate();
        double mo1 = fo1.getMassRate();
        
        double mi2 = fi2.getMassRate();
        double mo2 = fo2.getMassRate();
        
        double xi1 = fi1.getExergy();
        double xo1 = fo1.getExergy();
        
        double xi2 = fi2.getExergy();
        double xo2 = fo2.getExergy();

        double Xin = mi1 * xi1 + mi2 * xi2;
        double Xout = mo1 * xo1 + mo2 * xo2;
        
        double Xdest = Xin - Xout;
        double Xeff = (Xin - Xdest) / Xin;

        exergyDestruction.setValue(Xdest);
        secondLawEfficiency.setValue(Xeff);
    }
    
    @Override
    public List<FieldResult> getEnergyOutputResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("m1", fi1.getMassRate() ));
        res.add(FieldResult.create("m2", fi2.getMassRate() ));
        
        res.add(FieldResult.create("Ti1", fi1.getTemperature() ));
        res.add(FieldResult.create("Pi1", fi1.getPressure() ));
        
        res.add(FieldResult.create("To1", fo1.getTemperature() ));
        res.add(FieldResult.create("Po1", fo1.getPressure() ));
        
        res.add(FieldResult.create("Ti2", fi2.getTemperature() ));
        res.add(FieldResult.create("Pi2", fi2.getPressure() ));
        
        res.add(FieldResult.create("To2", fo2.getTemperature() ));
        res.add(FieldResult.create("Po2", fo2.getPressure() ));
        
        res.add(FieldResult.create("Pl1", getSide1PressureLoss() ));
        res.add(FieldResult.create("Pl2", getSide2PressureLoss() ));
        
        return res;
    }
    
    @Override
    public List<FieldResult> getExergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Xdest", "Exergy Destruction", exergyDestruction.getValue())  );
        res.add(FieldResult.create("Xeff", "Second Law Efficiency", secondLawEfficiency.getValue()) );
        
        return res; 
    }
}
