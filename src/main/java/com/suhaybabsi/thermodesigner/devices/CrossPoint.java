/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public class CrossPoint extends SteadyFlowDevice {

    private static class MatchStateEquation extends ThermalEquation {
        private final List<Flow> allFlows;
        private MatchStateEquation(List<Flow> allFlows){
            this.allFlows = allFlows;
        }
        @Override
        protected double evaluate() {

            double temp = Double.NaN;
            double press = Double.NaN;
            double entrop = Double.NaN;
            double enthlp = Double.NaN;
            double vapour = Double.NaN;
            
            int validCount = 0;
            for (Flow f : allFlows) {
                
                temp = f.getTemperatureProp().hasValue() ? f.getTemperature() : temp;
                press = f.getPressureProp().hasValue() ? f.getPressure() : press;
                entrop = f.getEntropyProp().hasValue() ? f.getEntropy() : entrop;
                enthlp = f.getEnthalpyProp().hasValue() ? f.getEnthalpy() : enthlp;
                vapour = f.getVapourFractionProp().hasValue() ? f.getVapourFraction() : vapour;
                
                double[] vals = new double[]{temp, press, entrop, enthlp, vapour};
                
                validCount = 0;
                for(double vl:vals){
                    if(Double.isNaN(vl) == false){
                        validCount++;
                    }
                }
                
                if (validCount == 5) {
                    break;
                }
            }
            
            for (Flow f : allFlows) {
                
                if (Double.isNaN(temp) == false) {
                    f.setTemperature(temp);
                }
                if (Double.isNaN(press) == false) {
                    f.setPressure(press);
                }
                if (Double.isNaN(enthlp) == false) {
                    f.setEnthalpy(enthlp);
                }
                if (Double.isNaN(entrop) == false) {
                    f.setEntropy(entrop);
                }
                if (Double.isNaN(vapour) == false) {
                    f.setVapourFraction(vapour);
                }
            }
            
            return validCount >= 2 ? 0 : -1;
        }
    }
    
    private static class MassConservationEquation extends ThermalEquation {

        private final List<Property> inMass;
        private final List<Property> outMass;
        
        private MassConservationEquation(Property[] allMass, List<Property> inMass, List<Property> outMass){
            super(allMass);
            this.inMass = inMass;
            this.outMass = outMass;
        }

        @Override
        protected double evaluate() {
            
            double massIn = 0;
            for(Property m:inMass){
                massIn += m.getValue();
            }
            
            double massOut = 0;
            for(Property m:outMass){
                massOut += m.getValue();
            }
            return massOut - massIn;
        }
    }
    @Override
    protected void configureEquations() throws ConfigurationException {
        
        List<Property> inMass = flowManager.getInMassRates();
        List<Property> outMass = flowManager.getOutMassRates();
        
        Property[] allMass = new Property[inMass.size() + outMass.size()];
        int i = 0;
        for(Property m:inMass){
            allMass[i++] = m;
        }
        for(Property m:outMass){
            allMass[i++] = m;
        }
        
        insureFluids();
        addEquation(new MassConservationEquation(allMass, inMass, outMass));
        addEquation(new MatchStateEquation(flowManager.getAllFlows()));
        
        for(Flow f:flowManager.getAllFlows()){
            
            addEquation(new Pipe.FlowPropertyEquation(f, f.getEnthalpyProp()));
            addEquation(new Pipe.FlowPropertyEquation(f, f.getTemperatureProp()));
            addEquation(new Pipe.FlowPropertyEquation(f, f.getPressureProp()));  //To Be Investigated
            addEquation(new Pipe.FlowPropertyEquation(f, f.getEntropyProp()));
            addEquation(new Pipe.FlowPropertyEquation(f, f.getVapourFractionProp()));
        }
    }
    
    public CrossPoint() {
        super("Cross Point", "cross_point");
    }
    
    public double getInputMass() {
        double mass = 0;
        for(Property m:flowManager.getInMassRates()){
            mass += m.getValue();
        }
        return mass;
    }

    public double getOutputMass() {
        double mass = 0;
        for(Property m:flowManager.getOutMassRates()){
            mass += m.getValue();
        }
        return mass;
    }

    @Override
    protected List<FieldResult> getEnergyOutputResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Input Mass", getInputMass()) );
        res.add(FieldResult.create("Output Mass", getOutputMass()) );
        
        return res;
    }
}
