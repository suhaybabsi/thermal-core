/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.fluid;

/**
 *
 * @author suhaybal-absi
 */
public class FlowState {
    
    public static FlowState create(double temperature, double pressure){
        FlowState state = new FlowState();
        state.pressure(pressure);
        state.temperature(temperature);
        return state;
    }
    
    private double pressure = Double.NaN;
    private double temperature = Double.NaN;
    private double enthalpy = Double.NaN;
    private double entropy = Double.NaN;
    private double energy = Double.NaN;
    private double vapourFraction = Double.NaN;
    private double volume = Double.NaN;

    public double pressure() {
        return pressure;
    }

    public void pressure(double pressure) {
        this.pressure = pressure;
    }

    public double temperature() {
        return temperature;
    }

    public void temperature(double temperature) {
        this.temperature = temperature;
    }

    public double enthalpy() {
        return enthalpy;
    }

    public void enthalpy(double enthalpy) {
        this.enthalpy = enthalpy;
    }

    public double entropy() {
        return entropy;
    }

    public void entropy(double entropy) {
        this.entropy = entropy;
    }

    public double energy() {
        return energy;
    }

    public void energy(double energy) {
        this.energy = energy;
    }

    public double volume() {
        return volume;
    }

    public void volume(double volume) {
        this.volume = volume;
    }
    
    
    public double vapourFraction() {
        return this.vapourFraction;
    }

    public void vapourFraction(double percent) {
        this.vapourFraction = percent;
    }
    
    
    
    public boolean isPressureValid() {
        return Double.isNaN(this.pressure) == false;
    }
    public boolean isTemperatureValid() {
        return Double.isNaN(this.temperature) == false;
    }
    public boolean isVapourFractionValid() {
        return Double.isNaN(this.vapourFraction) == false;
    }
    public boolean isEnthalpyValid() {
        return Double.isNaN(this.enthalpy) == false;
    }
    public boolean isEntropyValid() {
        return Double.isNaN(this.entropy) == false;
    }
    public boolean isVolumeValid() {
        return Double.isNaN(this.volume) == false;
    }
    public boolean isEnergyValid() {
        return Double.isNaN(this.energy) == false;
    }
    
    @Override
    public String toString() {
        String output = "State {";
        output += "\n\tPressure: " + this.pressure;
        output += "\n\tTemperature: " + this.temperature;
        output += "\n\tVolume: " + this.volume;
        output += "\n\tInternal Energy: " + this.energy;
        output += "\n\tEntropy: " + this.entropy;
        output += "\n\tEnthalpy: " + this.enthalpy;
        output += "\n\tSaturation Percent: " + this.vapourFraction;
        output += "\n}";
        return output;
    }
    
    public FlowState copy(){
        
        FlowState newState = new FlowState();
        newState.pressure = this.pressure;
        newState.temperature = this.temperature;
        newState.volume = this.volume;
        newState.energy = this.energy;
        newState.enthalpy = this.enthalpy;
        newState.entropy = this.entropy;
        newState.vapourFraction = this.vapourFraction;
        
        return newState;
    }

    public void reset() {
        this.pressure = Double.NaN;
        this.temperature = Double.NaN;
        this.enthalpy = Double.NaN;
        this.entropy = Double.NaN;
        this.energy = Double.NaN;
        this.vapourFraction = Double.NaN;
        this.volume = Double.NaN;
    }
}
