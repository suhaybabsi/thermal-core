/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.core;

/**
 *
 * @author suhaybal-absi
 */
public class Property {
    
    private String name;
    private double min = 0;
    private double max = Double.MAX_VALUE;
    public Property(String name) {
        this.name = name;
        this.type = PropertyType.CALCULATED;
    }

    public String getName() {
        return name;
    }
    
    public static enum PropertyType{CALCULATED, DEFINED}
    private PropertyType type = PropertyType.CALCULATED;

    public void setMin(double min) {
        this.min = min;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    protected double value = Double.NaN;
    
    public PropertyType getType() {
        return type;
    }
    public void setType(PropertyType type) {
        this.type = type;
    }
    
    public double getValue(){
        return value;
    }
    public void setValue(double value) {
        this.value = value;
    }
    public boolean isCalculated(){
        return this.type == PropertyType.CALCULATED;
    }
    public boolean isDefined(){
        return this.type == PropertyType.DEFINED;
    }
    public boolean hasValue(){
        return Double.isNaN(this.value) == false;
    }

    @Override
    public String toString() {
        return "Property {name: "+name+", value: "+getValue()+"}";
    }
    public void define(double val){
        setValue(val);
        setType(PropertyType.DEFINED);
    }
}
