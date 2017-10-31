/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.Property;
import com.suhaybabsi.thermodesigner.core.ThermalEquation;
import com.suhaybabsi.thermodesigner.core.Work;
import com.suhaybabsi.thermodesigner.core.WorkType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class Generator extends Device implements MechanicalMachine {

    @Override
    public boolean isShaftConnected() {
        return this.shaft != null;
    }
    
    private static class WorkDeliveryEquation extends ThermalEquation {

        private final Work inwork;
        private final Work outwork;
        private WorkDeliveryEquation(Work inWork, Work outWork) {
            super(inWork, outWork);
            setName("Work Delivery Equation");
            this.inwork = inWork;
            this.outwork = outWork;
        }
        @Override
        protected double evaluate() {
            double w_in = inwork.getValue();
            double w_out = outwork.getValue();
            return w_in - w_out;
        }   
    }
    
    private final Work deliveredWork;
    private final Work work;
    public Generator() {
        super("Generator", "generator");
        this.deliveredWork = new Work(WorkType.PRODUCED);
        this.work = new Work(WorkType.CONSUMED);
    }
    
    private Shaft shaft;
    public void connectShaft(Shaft shaft) {
        if(this.shaft != null){
            disconnectShaft();
        }
        
        this.shaft = shaft;
        shaft.addWork(work);
    }
    public void disconnectShaft(){
        if(this.shaft != null){
            this.shaft.removeWork(work);
            this.shaft = null;
        }
    }
    public double getConsumedWork() {
        return work.getValue();
    }
    
    @Override
    protected void configureEquations() throws ConfigurationException {
        addEquation(new WorkDeliveryEquation(work, deliveredWork));
    }

    public void defineConsumedWork(double w) {
        this.work.setValue(w);
        this.work.setType(Property.PropertyType.DEFINED);
    }

    @Override
    public List<FieldResult> getEnergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>(); 
        
        res.add(FieldResult.create("w", "Consumed Work", getConsumedWork()) );
        
        return res;
    }
}
