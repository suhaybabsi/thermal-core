/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.ThermalEquation;
import com.suhaybabsi.thermodesigner.core.Work;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class Shaft extends Device {
    
    private static class EnergyBalanceEquation extends ThermalEquation {

        private final List<Work> works;
        private final double efficiency;

        private EnergyBalanceEquation(List<Work> works, double eff) {
            
            super(works.toArray(new Work[works.size()]));
            setName("Energy Balance Equation");
            this.works = works;
            this.efficiency = eff;
        }

        @Override
        protected double evaluate() {

            double inputWork = 0;
            double outputWork = 0;
            for (Work w : works) {
                if(w.isProduced()){
                    inputWork += w.getValue();
                }else{
                    outputWork += w.getValue();
                }
            }
            return outputWork - inputWork * efficiency;
        }
    }
    
    private double mechanicalEfficiency = 0.99;
    private final List<Work> workList;
    public Shaft() {
        super("Shaft", "shaft");
        workList = new ArrayList<Work>();
    }

    @Override
    protected void configureEquations() throws ConfigurationException {
        
        addEquation(new EnergyBalanceEquation(workList, mechanicalEfficiency));
    }
    
    
    public void addWork(Work work) {
        workList.add(work);
    }
    public void removeWork(Work work) {
        workList.remove(work);
    }

    public void setMechanicalEfficiency(double mechanicalEfficiency) {
        this.mechanicalEfficiency = mechanicalEfficiency;
    }
    public double getMechanicalEfficiency() {
        return mechanicalEfficiency;
    }
    public double getOutputWork() {
        double work = 0;
        for(Work w:workList){
            
            if(w.isConsumed()){
                work += w.getValue();
            }
        }
        return work;
    }
    public double getInputWork() {
        double work = 0;
        for(Work w: workList){
            
            if(w.isProduced()){
                work += w.getValue();
            }
        }
        return work;
    } 
    public List<Work> getWorkList() {
        return workList;
    }
    
    @Override
    protected List<FieldResult> getEnergyOutputResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Input Work", getInputWork() ));
        res.add(FieldResult.create("Output Work", getOutputWork() ));
        
        return res;
    }
    
}
