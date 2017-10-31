/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.CalculationError;
import com.suhaybabsi.thermodesigner.core.CalculationException;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.ThermalEquation;
import com.suhaybabsi.thermodesigner.json.ResultRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

/**
 *
 * @author suhaybal-absi
 */
public abstract class Device {
   
    private String name;
    private String jsonType;
    private Object data;

    protected Device(String name, String jsonType) {
        this.name = name;
        this.jsonType = jsonType;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
    
    public void calculateExergy(){}
    
    private List<ThermalEquation> equationsList = null;
    protected abstract void configureEquations() throws ConfigurationException;
    public void calculateModel() throws CalculationException{
        ArrayList<ThermalEquation> toBeSolved = new ArrayList<ThermalEquation>();
        HashMap<ThermalEquation, CalculationError> errorsMap = 
                new HashMap<ThermalEquation, CalculationError>();
        
        toBeSolved.addAll(equationsList);
        outerLoop:while(toBeSolved.size() > 0){
            
            for(ThermalEquation eq:toBeSolved){
           
                int unknownNum = eq.getUnknownNum();
                //System.out.println("eq: "+ eq);
                //System.out.println("un: "+ unknownNum);
                if (unknownNum <= 1) {
                    
                    CalculationError error = eq.solve();
                    //System.out.println("error: "+ (error != null));
                    if(error == null){
                        
                        toBeSolved.remove(eq);
                        continue outerLoop;
                    }else{
                        errorsMap.put(eq, error);
                    }
                }
            }
            break;
        }
        
        
        if(toBeSolved.size() > 0){
            
            String equationsText = "";
            for(ThermalEquation eq:toBeSolved){
                
                
                equationsText += "\t" + eq + ", For:\n";
                if(eq.getCurrentUnknowns().size() > 0){
                    equationsText += "\t\t" + eq.getCurrentUnknowns() + "\n";
                }
                
                CalculationError error = errorsMap.get(eq);
                if(error != null){
                    equationsText += "\t\t" + error.getMessage() + "\n";
                }
                
            }
            
            String message = "Device couldn't be solved{\n\tEquations:\n"+ equationsText +"\n}";
            throw new CalculationException(this, message);
        }
    }
    protected boolean configuredProperly = false;
    public void configure() throws ConfigurationException{
        if(configuredProperly == false){
            equationsList = new ArrayList<ThermalEquation>();
            configureEquations();
            configuredProperly = true;
        }
    }
    protected void addEquation(ThermalEquation eq){
        eq.setDevice(this);
        equationsList.add(eq);
    }
    public void reset() {
        for(ThermalEquation eq:equationsList){
            eq.reset();
        }
    }
    public boolean isSolved() {
        
        int solvedNum = 0;
        for (ThermalEquation eq : equationsList) {
            int unknownNum = eq.getUnknownNum();
            if (unknownNum == 0) {
                solvedNum++;
            }
        }
        return solvedNum == equationsList.size();
    }
    public boolean hasMathematicalModel(){ 
        return equationsList == null || equationsList.isEmpty();
    }
    public List<ThermalEquation> getEquationsList() {
        return equationsList;
    }

    public String getJsonType() {
        return jsonType;
    }

    protected void setJsonType(String type) {
        this.jsonType = type;
    }
    
    public static class FieldResult {
        String abbrev;
        String name;
        Object value;
        
        public static FieldResult create(String name, Object value){
            
            FieldResult rf = new FieldResult();
            
            rf.name = name;
            rf.value = value;
            
            return rf;
        }
        public static FieldResult create(String abbrev, String name, Object value){
            
            FieldResult rf = new FieldResult();
            
            rf.abbrev = abbrev;
            rf.name = name;
            rf.value = value;
            
            return rf;
        }
        private FieldResult(){}
    }
    
    protected List<FieldResult> getEnergyOutputResults(){return getEnergyResults();};
    public List<FieldResult> getEnergyResults(){return null;};
    public List<FieldResult> getExergyResults(){return null;};
    
    public JSONObject getJSONResults() throws JSONException{
        
        return getJSONResults(false);
    }
    public JSONObject getJSONResults(boolean exergy) throws JSONException{
        
        ResultRecord res = new ResultRecord(this);
        
        List<FieldResult> energyResults = getEnergyResults();
        energyResults = (energyResults == null) ? new ArrayList<FieldResult>() : energyResults;
        
        if(exergy){
            
            List<FieldResult> exergyResults = getExergyResults();
            if(exergyResults != null){
                energyResults.addAll(exergyResults);
            }
        }
        
        for(FieldResult field:energyResults){
            
            //System.out.println("Json: "+field.abbrev+", Name: "+field.name+", Value: "+field.value);
            
            if (field.value == null) {

                res.add(field.abbrev, "N/A");
            } else if(field.value instanceof Double){
                
                res.add(field.abbrev, (Double) field.value);
            } else {
                
                res.add(field.abbrev, field.value.toString());
            }
        }
        
        return res.wrap();
    }
    
    public String printEnergyResults(){
        return printResults(getEnergyOutputResults());
    }
    public String printExergyResults(){
        return printResults(getExergyResults());
    }
    
    private String printResults(List<FieldResult> results){
        
        if (results == null) {return "";}
        
        String str = "";
        for(FieldResult rf : results){
        
            str += "\n\t" + rf.name + ": " + rf.value;
        }        
        return str;
    }
    
    @Override
    public String toString() {
        return "{ "+getName()+" - "+ printEnergyResults() + "\n}";
    }
}
