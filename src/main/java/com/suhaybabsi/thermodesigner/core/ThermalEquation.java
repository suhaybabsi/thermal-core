/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.core;

import com.suhaybabsi.thermodesigner.devices.Device;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.UnivariateSolverUtils;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.NullArgumentException;

/**
 *
 * @author suhaybal-absi
 */
public abstract class ThermalEquation implements UnivariateFunction {

    public static double TOLERANCE = 1e-6;
    private Property unknown;
    private final List<Property> properties;
    private String name;
    protected final void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public ThermalEquation(Property... props) {
        properties = Arrays.asList(props);
    }
    @Override
    public double value(double x) {
        if (unknown != null) {
            unknown.setValue(x);
        }
        return evaluate();
    }

    protected abstract double evaluate();
    public CalculationError solve() {  
        
        addressUnknowns();
        if (unknown != null) {
            
            try{
                
                UnivariateSolverUtils.solve(this, unknown.getMin(), unknown.getMax());
                return null;
            }catch(NullArgumentException ex){
                
                String errorText = unknown.getName() + ": "+ ex.getMessage();
                return new CalculationError(getDevice(), errorText);
            } catch (NoBracketingException ex) {
                
                String errorText = unknown.getName() + ": "+ ex.getMessage();
                return new CalculationError(getDevice(), errorText);
            }
        } else if(properties.isEmpty()){
            
            try{
                
                UnivariateSolverUtils.solve(this, -2, 2);
                return null;
            }catch(NullArgumentException ex){
                
                return new CalculationError(getDevice(), ex.getMessage());
            } catch (NoBracketingException ex) {
                
                return new CalculationError(getDevice(), ex.getMessage());
            }
            
        } else {

            double val = Math.abs(evaluate());
            double tol = getAcceptableTolerance();
            if (val > tol) {
                
                String errorText = this.getName() + ": Invalid Evaluation !";
                return new CalculationError(getDevice(), errorText);
            }
        }
        return null;
    }

    public int getUnknownNum() {
        return PropertiesUtils.getNumberOfUnknowns(properties);
    }

    @Override
    public String toString() {
        if(this.name == null){
            return super.toString(); 
        }else{
            return "Thermal Eq. {"+name+"}";
        }
    }

    private void addressUnknowns() {
        
        unknown = null;
        List<Property> unknowns = PropertiesUtils.searchForUnknowns(properties);
        
        if (unknowns.size() == 1) {
            unknown = unknowns.get(0);
        } else if (unknowns.size() > 1){
            System.out.println("Invalid Equation !! -> "+unknowns.size());
        }
    }

    public void reset() {
        for(Property p:properties){
            if(p.isCalculated() && p.hasValue()){
                p.setValue(Double.NaN);
            }
        }
    }

    public List<Property> getCurrentUnknowns() {
        
        return PropertiesUtils.searchForUnknowns(properties);
    }
    public List<Property> getProperties() {
        return properties;
    }
    
    public boolean isSolvable(){
        int unknownNum = getUnknownNum();
        return unknownNum <= 1;
    }
    public boolean isSolved(){
        int unknownNum = getUnknownNum();
        return unknownNum == 0;
    }

    private double getAcceptableTolerance() {
        
        double defined = Double.NaN;
        for(Property p : properties){ 
            if(p.isDefined()){
                defined = p.getValue();
            }
        }
        
        double ten = Math.log10(defined);
        
        if(ten > 7){
            
            return 10;
        }else if(ten > 2){
            
            return 1;
        }else if(ten > 0){
            
            return 0.001;
        }else{
        
            return TOLERANCE;
        }
    }
    
    private Device device;
    public void setDevice(Device dvc) {
        this.device = dvc;
    }

    public Device getDevice() {
        return device;
    }

    protected Property getUnknown() {
        return unknown;
    }
    
    
}
