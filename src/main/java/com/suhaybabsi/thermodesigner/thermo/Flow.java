/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo;

import com.suhaybabsi.thermodesigner.core.Property;
import com.suhaybabsi.thermodesigner.devices.SteadyFlowDevice;
import com.suhaybabsi.thermodesigner.thermo.fluid.FlowState;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import com.suhaybabsi.thermodesigner.thermo.fluid.Gas;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class Flow {
    
    private SteadyFlowDevice start;
    private SteadyFlowDevice end;

    public void setStart(SteadyFlowDevice start) {
        
        this.start = start;
    }

    public SteadyFlowDevice getStart() {
        return start;
    }

    public void setEnd(SteadyFlowDevice end) {
        this.end = end;
    }

    public SteadyFlowDevice getEnd() {
        return end;
    }
    
    
    private Fluid fluid;
    private Property massRate = new Property("massRate");
    private final Property pressure = new Property("pressure");
    private final Property temperature = new Property("temperature");
    private final Property velocity = new Property("velocity");
    private final Property elevation = new Property("elevation");
    private final Property enthalpy = new Property("enthalpy");
    private final Property entropy = new Property("entropy");
    private final Property vapourFraction = new Property("vapour fraction");
    private final Property exergy = new Property("Exergy");

    private final List<Property> properties;

    public Flow() {
        properties = new ArrayList<Property>();
        properties.add(pressure);
        properties.add(temperature);
        properties.add(velocity);
        properties.add(elevation);
        properties.add(enthalpy);
        properties.add(entropy);
        properties.add(vapourFraction);
        
        enthalpy.setMin(-400);
        enthalpy.setMax(10000);
        
        massRate.setMin(0);
        massRate.setMax(Double.MAX_VALUE);
        
        temperature.setMin(0);
        temperature.setMax(2200);
        
        pressure.setMin(1);
        pressure.setMax(100000);
        
        entropy.setMin(-1.53);
        entropy.setMax(10.53);
        
        vapourFraction.setMin(0);
        vapourFraction.setMax(1);
        
        elevation.setValue(0.0);
    }

    public void reset() {
        for (Property p : properties) {
            if (p.isCalculated()) {
                p.setValue(Double.NaN);
            }
        }
    }
    
    public void defineMassRate(double massRate) {
        this.massRate.define(massRate);
    }
    public void definePressure(double pressure) {
        this.pressure.define(pressure);
    }
    public void defineElevation(double elevation) {
        this.elevation.define(elevation);
    }
    public void defineVelocity(double velocity) {
        this.velocity.define(velocity);
    }
    public void defineTemperature(double temperature) {
        this.temperature.define(temperature);
    }
    public void defineVapourFraction(double vapourFraction) {
        this.vapourFraction.define(vapourFraction);
    }
    public void defineEntropy(double entropy) {
        this.entropy.define(entropy);
    }
    public void defineEnthalpy(double enthalpy) {
        this.enthalpy.define(enthalpy);
    }
    
    public void setFluid(Fluid fluid) {
        this.fluid = fluid;
    }
    public void setMassRate(double massRate) {
        this.massRate.setValue(massRate);
    }
    public void setPressure(double pressure) {
        this.pressure.setValue(pressure);
    }
    public void setTemperature(double temperature) {
        this.temperature.setValue(temperature);
    }
    public void setVelocity(double velocity) {
        this.velocity.setValue(velocity);
    }
    public void setElevation(double elevation) {
        this.elevation.setValue(elevation);
    }
    public void setEntropy(double entropy) {
        this.entropy.setValue(entropy);
    }
    public void setEnthalpy(double enthalpy) {
        this.enthalpy.setValue(enthalpy);
    }
    public void setVapourFraction(double fraction) {
        this.vapourFraction.setValue(fraction);
    }
    public void setMassRateProp(Property massRate) {
        this.massRate = massRate;
    }
    
    public Property getTemperatureProp() {
        return temperature;
    }
    public Property getPressureProp() {
        return pressure;
    }
    public Property getMassRateProp() {
        return massRate;
    }
    public Property getEnthalpyProp() {
        return enthalpy;
    }
    public Property getVelocityProp() {
        return velocity;
    }
    public Property getElevationProp() {
        return elevation;
    }
    public Property getEntropyProp() {
        return entropy;
    }
    public Property getVapourFractionProp() {
        return vapourFraction;
    }
    
    public Property searchUnknownProperty(){
        
        if(massRate.hasValue() == false){
            return massRate;
        }
        if(enthalpy.hasValue() == false){
            return enthalpy;
        }
        if(velocity.hasValue() == false){
            return velocity;
        }
        if(elevation.hasValue() == false){
            return elevation;
        }
        return null;
    }
    
    public Fluid getFluid() {
        return fluid;
    }
    public double getExergy(){
        return exergy.getValue();
    }
    public double getMassRate() {
        return massRate.getValue();
    }
    public double getPressure() {
        return pressure.getValue();
    }
    public double getTemperature() {
        return temperature.getValue();
    }
    public double getVelocity() {
        return velocity.getValue();
    }
    public double getElevation() {
        return elevation.getValue();
    }
    public double getEnthalpy() {
        return enthalpy.getValue();
    }
    public double getVapourFraction() {
        return vapourFraction.getValue();
    }
    public double getEntropy() {
        return this.entropy.getValue();
    }
    
    public void defineVolumetricRate(double V) {
        
        FlowState state = new FlowState();
        state.temperature(getTemperature());
        state.pressure(getPressure());
        
        double specificVolume = getFluid().specificVolume(state);
        defineMassRate(V / specificVolume);
    }
    
    public double calculateExergy(double To, double Po){
        double x = fluid.exergy(createState(), To, Po);
        
        //Kinetic Energy
        if( Double.isNaN(getVelocity()) == false){
            x += Math.pow(getVelocity(), 2.0) / 2 /1000;
        }
        
        //Potential Energy
        x += getElevation() * 9.81 / 1000;
        
        exergy.setValue(x);
        return x;
    }
    
    public FlowState createState(){
        
        FlowState state = new FlowState();
        if(this.pressure.hasValue()){
            state.pressure(pressure.getValue());
        }
        if(this.temperature.hasValue()){
            state.temperature(temperature.getValue());
        }
        if(this.vapourFraction.hasValue()){
            state.vapourFraction(vapourFraction.getValue());
        }
        if(this.entropy.hasValue()){
            state.entropy(entropy.getValue());
        }
        if(this.enthalpy.hasValue()){
            state.enthalpy(enthalpy.getValue());
        }
        return state;
    }
    
    public double calculateEntropy() {
        FlowState state = createState();
        return fluid.entropy(state);
    }
    public double calculateEnthalpy() {
        FlowState state = createState();
        return fluid.enthalpy(state);
    }
    public double calculateTemperature() {
        FlowState state = createState();
        return fluid.temperature(state);
    }
    public void calculate() {
        
        updateEnthalpy();
        updateEntropy();
        updateTemperature();
        updateVapourFraction();
        updatePressure();
    }

    public double updatePressure() {
        
        if(getPressureProp().isDefined()){
            
            return getPressure();
        }else{
        
            double p = calculatePressure();
            setPressure(p);
            return p;
        }
    }
    public double updateEnthalpy() {
        
        if(getEnthalpyProp().isDefined()){
            
            return getEnthalpy();
        }else{
        
            double h = calculateEnthalpy();
            setEnthalpy(h);
            return h;
        }
    }
    public double updateEntropy() {
        
        if(getEntropyProp().isDefined()){
            
            return getEntropy();
        }else{
        
            double s = calculateEntropy();
            setEntropy(s);
            return s;
        }
    }
    public double updateVapourFraction() {
        
        if(getVapourFractionProp().isDefined()){
            
            return getVapourFraction();
        }else{
        
            double s = calculateVapourFraction();
            setVapourFraction(s);
            return s;
        }
    }

    public double calculateVapourFraction() {
        FlowState state = createState();
        return fluid.vapourFraction(state);
    }

    public double updateTemperature() {
        if(getTemperatureProp().isDefined()){
            
            return getTemperature();
        }else{
        
            double t = calculateTemperature();
            setTemperature(t);
            return t;
        }
    }

    public List<Property> getKnownProperties() {
        List<Property> knowns = new ArrayList<Property>();
        for(Property p:properties){
            if(p.hasValue()){
                knowns.add(p);
            }
        }
        return knowns;
    }

    public void updateProperties(List<Property> knowns) {
        
        for(Property kp: knowns){
            for(Property p : properties){
                if(kp.getName().equals(p.getName()) ){
                    p.setValue(kp.getValue());
                }
            }
        }
    }
    
    public double calculateProperty(Property p){
    
        if(p.equals(entropy) ){
            return calculateEntropy();
        }
        if(p.equals(enthalpy) ){
            return calculateEnthalpy();
        }
        if(p.equals(temperature) ){
            return calculateTemperature();
        }
        if(p.equals(vapourFraction) ){
            return calculateVapourFraction();
        }
        if(p.equals(pressure) ){
            return calculatePressure();
        }
        return Double.NaN;
    }
    public double calculatePressure() {
        FlowState state = createState();
        return fluid.pressure(state);
    }

    public void updateProperty(String prop, double value) {
        
        Property p = getProperty(prop);
        if(p != null){
            p.setValue(value);
        }
    }
    public Property getProperty(String prop) {
        
        for(Property p:properties){
            if(p.getName().equals(prop)){
                return p;
            }
        }
        return null;
    }
}
