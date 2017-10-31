/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.core;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class PropertiesUtils {
    
    
    public static List<Property> searchForUnknowns(List<Property> props){
        
        List<Property> unknowns = new ArrayList<Property>();
        for(Property p : props){ 
            if(p.hasValue() == false){
                unknowns.add(p);
            }
        }
        return unknowns;
    }
    public static int getNumberOfUnknowns(List<Property> props){
        int unknownNum = 0; 
        for(Property p : props){ 
            if(p.hasValue() == false ){
                unknownNum++;
            }
        }
        return unknownNum;
    }
    public static List<Property> searchForCalculated(List<Property> props){
        
        List<Property> calcs = new ArrayList<Property>();
        for(Property p : props){ 
            if(p.isCalculated()){
                calcs.add(p);
            }
        }
        return calcs;
    }
    
}
