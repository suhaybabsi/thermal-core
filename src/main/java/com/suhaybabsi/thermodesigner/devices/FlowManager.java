/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.Property;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class FlowManager {
    
    private List<Flow> inFlows = new ArrayList<Flow>();
    private List<Flow> outFlows = new ArrayList<Flow>();

    private SteadyFlowDevice device;
    public FlowManager(SteadyFlowDevice dvc) {
        this.device = dvc;   
    }
    public void addIn(Flow f){
        
        if(inletDisabled){
            return;
        }
        
        f.setEnd(device);
        inFlows.add(f);
    }
    
    public void addOut(Flow f){
        
        if(outletDisabled){
            return;
        }
        
        f.setStart(device);
        outFlows.add(f);
    }
    public Flow getIn(int i) {
        if (i < inFlows.size()) {
            return inFlows.get(i);
        }
        return null;
    }
    public Flow getOut(int i){
        if (i < outFlows.size()){
            return outFlows.get(i) ;
        }
        return null;
    }
    public List<Property> getOutMassRates(){
        
        List<Property> massRateProps = new ArrayList<Property>();
        for(Flow f:outFlows){
            massRateProps.add(f.getMassRateProp());
        }
        return massRateProps;
    }
    public List<Property> getInMassRates(){
        
        List<Property> massRateProps = new ArrayList<Property>();
        for(Flow f:inFlows){
            massRateProps.add(f.getMassRateProp());
        }
        return massRateProps;
    }
    
    public List<Flow> getAllFlows(){
        
        List<Flow> allFlows = new ArrayList<Flow>();
        for(Flow f:inFlows){
            allFlows.add(f);
        }
        for(Flow f:outFlows){
            allFlows.add(f);
        }
        
        return allFlows;
    }

    private boolean inletDisabled = false;
    void disableInlet() {
        inletDisabled = true;
        
    }
    private boolean outletDisabled = false;
    void disableOutlet() {
        outletDisabled = true;
    }

    int getInFlowsNum() {
        return inFlows.size();
    }

    int getOutFlowsNum() {
        return outFlows.size();
    }
    
    public void removeAll(){
        
        inFlows.clear();
        outFlows.clear();
    }
}
