/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.thermo.Atmosphere;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Air;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class AirFlow extends SteadyFlowDevice {
    
    private double altitude = Double.NaN;
    private double machNumber = Double.NaN;
    private double massFlowRate = Double.NaN;
    private double soundSpeed = Double.NaN;

    public void defineMassFlowRate(double massFlowRate) {
        this.massFlowRate = massFlowRate;
    }
    public void defineMachNumber(double machNumber) {
        this.machNumber = machNumber;
    }
    public void defineAltitude(double altitude) {
        this.altitude = altitude;
    }
    
    public double getMassFlowRate() {
        Flow flowout = getFlowManager().getOut(0);
        return flowout.getMassRate();
    }
    
    public double getAltitude() {
        return altitude;
    }
    public double getMachNumber() {
        return machNumber;
    }
    public double getVelocity() {
        Flow flowout = getFlowManager().getOut(0);
        return flowout.getVelocity();
    }
    
    
    public AirFlow() {
        super("Air Flow", "air_flow");
        flowManager.disableInlet();
    }

    @Override
    protected void configureEquations() throws ConfigurationException {
        
        if(flowManager.getOutFlowsNum() == 0){
            throw new ConfigurationException(this, "Exit flow isn't defined");
        }
        
        Air air = Air.getInstance();
        Flow flowout = getFlowManager().getOut(0);
        flowout.setFluid(air);
        
        if(Double.isNaN(altitude) == false 
                && Double.isNaN(machNumber) == false){
            
            double Ta, Pa, a;
            Ta = Atmosphere.getTemperatureAtAltitude(altitude)+ 273.15;
            Pa = Atmosphere.getPressureAtAltitude(altitude);
            a = Atmosphere.getSoundSpeedAtAltitude(altitude);
            
            //System.out.println("Ta: "+Ta+", Pa: "+Pa+", a: "+a);
            
            double u = machNumber * a;
            double Tta = calculateTotalTemperature(Ta, machNumber, air);
            double Pta = calculateTotalPressure(Pa, machNumber, Tta, air);
            
            this.soundSpeed = a;
            flowout.setPressure(Pta);
            flowout.setTemperature(Tta);
            flowout.setVelocity(u);
        }
        if(Double.isNaN(massFlowRate) == false){
            flowout.defineMassRate(massFlowRate);
        }
    }
    
    private double calculateTotalTemperature(double Ta, double Ma, Air air) {
        
        double Tta = Ta;
        double Tta_c, g;
        double maxIterations = 30;
        
        for(int i = 0 ; i < maxIterations; i++){
            
            Tta_c = Tta;
            g = air.specificHeatRatio(Tta_c);
            Tta = Ta * (1 + (g-1) / 2 * Math.pow(Ma, 2));
            
            if(Math.abs(Tta - Tta_c) < 10e-7){
                break;
            }
        }
        return Tta;
    }

    private double calculateTotalPressure(double Pa, double Ma, double Tta, Air air) {
        
        double g = air.specificHeatRatio(Tta);
        double Pta = Pa * Math.pow(1 + (g-1) / 2 * Math.pow(Ma, 2), g / (g-1));
        return Pta;
    }

    public double getSoundSpeed() {
        return soundSpeed;
    }

    @Override
    public List<FieldResult> getEnergyResults() {
        
        Flow flowOut = flowManager.getOut(0);
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("p", "Pressure", flowOut.getPressure()) );
        res.add(FieldResult.create("t", "Temperature", flowOut.getTemperature()) );
        
        res.add(FieldResult.create("v", "Velocity", getVelocity()) );
        res.add(FieldResult.create("m", "Mass Flow Rate", getMassFlowRate()) );
        res.add(FieldResult.create("mach", "Mach Number", getMachNumber()) );
        res.add(FieldResult.create("alt", "Altitude", getAltitude()) );
        
        return res;
    }
}
