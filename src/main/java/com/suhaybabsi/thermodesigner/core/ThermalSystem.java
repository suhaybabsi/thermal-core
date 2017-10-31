/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.core;

import com.suhaybabsi.thermodesigner.devices.AirFlow;
import com.suhaybabsi.thermodesigner.devices.Boiler;
import com.suhaybabsi.thermodesigner.devices.Burner;
import com.suhaybabsi.thermodesigner.devices.Compressor;
import com.suhaybabsi.thermodesigner.devices.Condenser;
import com.suhaybabsi.thermodesigner.devices.Device;
import com.suhaybabsi.thermodesigner.devices.Diffuser;
import com.suhaybabsi.thermodesigner.devices.Exhaust;
import com.suhaybabsi.thermodesigner.devices.GasTurbine;
import com.suhaybabsi.thermodesigner.devices.Generator;
import com.suhaybabsi.thermodesigner.devices.Intake;
import com.suhaybabsi.thermodesigner.devices.Nozzle;
import com.suhaybabsi.thermodesigner.devices.Pipe;
import com.suhaybabsi.thermodesigner.devices.Pump;
import com.suhaybabsi.thermodesigner.devices.SteadyFlowDevice;
import com.suhaybabsi.thermodesigner.devices.SteamTurbine;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author suhaybal-absi
 */
public class ThermalSystem {

    
    private final List<Device> deviceList = new ArrayList<Device>();
    public ThermalSystem() {}
    public void configure() throws ConfigurationException {

        List<Device> toBeConfigured = new ArrayList<Device>();
        toBeConfigured.addAll(deviceList);

        while (toBeConfigured.size() > 0) {

            List<Device> configured = new ArrayList<Device>();
            List<ConfigurationException.Error> configurationErrors 
                    = new ArrayList<ConfigurationException.Error>();
            for (Device device : toBeConfigured) {
                try {
                    
                    device.configure();
                    configured.add(device);
                } catch (ConfigurationException e) {
                    configurationErrors.addAll(e.getErrorsList());
                }
            }
            
            if (configured.isEmpty()) {
                throw new ConfigurationException(configurationErrors);
            } else {
                toBeConfigured.removeAll(configured);
            }
        }
    }
    
    public void solve() throws CalculationException {

        List<ThermalEquation> allEquations = new ArrayList<ThermalEquation>();
        HashMap<ThermalEquation, CalculationError> errorsMap = 
                new HashMap<ThermalEquation, CalculationError>();
        
        
        //HashSet<Property> allProperties = new HashSet<Property>();
        final HashSet<Property> allUnknowns = new HashSet<Property>();
        
        for (Device device : getDeviceList()) {
            allEquations.addAll(device.getEquationsList());
            
            /*System.out.println("Device: " + device.getName());
            for (ThermalEquation eq : device.getEquationsList()) {
                
                allProperties.addAll(eq.getProperties());
                allUnknowns.addAll(eq.getCurrentUnknowns());
                
                int num = eq.getUnknownNum();
                System.out.println(eq + " -> " + num);
            }
            
            System.out.println("---");*/
        }
        
        /*
        System.out.println("Number of Unknowns: "+allUnknowns.size());
        System.out.println("Number of Properties: "+allProperties.size());
        System.out.println("Number of Equations: "+allEquations.size());
        */
        
        ArrayList<Device> solvedDevices = new ArrayList<Device>();
        ArrayList<ThermalEquation> toBeSolved = new ArrayList<ThermalEquation>();
        toBeSolved.addAll(allEquations);
        outerloop:while(toBeSolved.size() > 0){
        
            for(ThermalEquation eq:toBeSolved){
                //System.out.println(eq + " -> " + eq.getUnknownNum());
                if(eq.isSolvable()){
                    
                    CalculationError error = eq.solve();
                    
                    if(error == null){
                        
                        toBeSolved.remove(eq);
                        
                        if( eq.getDevice().isSolved() 
                                && !solvedDevices.contains(eq.getDevice()) ){
                            
                            solvedDevices.add( eq.getDevice() );
                            
                            if(eq.getDevice() instanceof Pipe){
                                
                                Pipe pipe = (Pipe) eq.getDevice();
                                System.out.println("Pipe solved:      " + pipe.getInletDevice().getJsonType() + " -> " + pipe.getOutletDevice().getJsonType() );
                            }else {
                                System.out.println("Device solved:    " + eq.getDevice().getJsonType() );
                            }
                        }
                        
                        continue outerloop;
                    }else{
                        
                        errorsMap.put(eq, error);
                    }
                }
            }
            break;
        }
        /*
        
        System.out.println();
        System.out.println("**********************************************");
        System.out.println("************ AFTER CALCULATION ***************");
        System.out.println("**********************************************");
        System.out.println();
        allUnknowns.clear();
        */
        
        for (Device device : getDeviceList()) {
            
            //System.out.println("Device: " + device.getName());
            
            for (ThermalEquation eq : device.getEquationsList()) {
                allUnknowns.addAll(eq.getCurrentUnknowns());
             
                /*if (eq.isSolved()) {
                    System.out.println(eq + " -> SOLVED");
                } else {
                    int num = eq.getUnknownNum();
                    System.out.println(eq + " -> " + num);
                }*/
            }
            
            //System.out.println("---");
        }
        
        
        for(Device device : getDeviceList()){
            
            boolean t1 = false;
            for(ThermalEquation eq : toBeSolved){
                
                if( eq.getDevice().equals(device) ){
                    
                    if(!t1){
                        t1 = true;
                        System.out.println();
                        
                        if (eq.getDevice() instanceof Pipe) {

                            Pipe pipe = (Pipe) eq.getDevice();
                            System.out.println("Unsolved Pipe:      " + pipe.getInletDevice().getJsonType() + " -> " + pipe.getOutletDevice().getJsonType());
                        } else {
                            System.out.println("Unsolved Device: " + device.getJsonType());
                        }
                        
                    }
                    
                    boolean t2 = false;
                    for(Property prop:eq.getCurrentUnknowns()){
                        
                        if (!t2) {
                            t2 = true;
                            System.out.println("  - " + eq);
                            System.out.println("     Unknowns:");
                        }
                        
                        System.out.println("     * " + prop);
                    }
                    
                    if (t2) {
                        List<Property> knowns = new ArrayList<Property>();
                        knowns.addAll(eq.getProperties());
                        knowns.removeAll(eq.getCurrentUnknowns());

                        System.out.println("     Knowns:");
                        for (Property prop : knowns) {
                            System.out.println("     * " + prop);
                        }
                    }
                }
            }
        }
        
        System.out.println("Number of solved devices: "+solvedDevices.size());
        System.out.println("Number of Unknowns: "+allUnknowns.size());
        System.out.println("Number of Unsolved Equations: "+toBeSolved.size());
        
        CalculationError error;
        if(toBeSolved.size() == allUnknowns.size() && allUnknowns.size() > 0){
            
            error = new CalculationError(null, "There are MANY solutions for this system");
            throw new CalculationException(error);
        }else if(toBeSolved.size() > 0){
            
            error = new CalculationError(null, "Indeterminate system - Not enough parameters are defined.");
            throw new CalculationException(error);
        }
        
    }
    public void addDevice(Device device) {
        
        this.deviceList.add(device);
    }
    
    public void addAllDevices(List<? extends Device> dvcList) {
        this.deviceList.addAll(dvcList);
    }

    public List<Device> getDeviceList() {
        return deviceList;
    }
    
    public double getNetWork(){
        
        double work = 0;
        for(Device device:deviceList){
            if(device instanceof Generator){
                work += ((Generator) device).getConsumedWork();
            }
        }
        
        for(Device device:deviceList){
            
            if(device instanceof WorkConsumingDevice){
                
                WorkConsumingDevice machine = (WorkConsumingDevice) device;
                if(machine.isShaftConnected() == false){
                    work -= machine.getWorkConsumed();
                } 
            }
            
            if(device instanceof WorkProducingDevice){
                
                WorkProducingDevice machine = (WorkProducingDevice) device;
                if(machine.isShaftConnected() == false){
                    work += machine.getWorkProduced();
                } 
            }
        }
        
        return work;
    }
    public double getFuelConsumption(){
        
        double mfuel = 0;
        for(Device device:deviceList){
            
            if(device instanceof Burner){
                mfuel += ((Burner) device).getFuelMassRate();
            }
        }
        return mfuel;
    }
    public double getSFC(){
    
        double mfuel = getFuelConsumption();
        double WN = getNetWork();
        double SFC = 3600 * mfuel / WN;
        return SFC;
    }
    public double getTotalInputEnergy(){
        
        double energy = 0;
        for(Device device:deviceList){
            
            if(device instanceof HeatProducingDevice){
                energy += ((HeatProducingDevice) device).getHeatProduced();
            }
        }
        return energy;    
    }
    public double getEfficiency(){
        
        double energyInput = getTotalInputEnergy();
        double usefulWork = getNetWork();
        
        return usefulWork/energyInput;
    }
    
    private ThermalSystemType type;
    
    public String getFullResults() {
        return getFullResults(false);
    }
    public String getFullResults(boolean includeExergy) {
        
        recognizeSystemType();

        String data = "";
        for(Device device: getDeviceList()){
            
            data += "{";
            data += " - " + device.getName();
            data += device.printEnergyResults();
            
            if(includeExergy){
                data += device.printExergyResults();
            }
            
            data += "\n}\n";
        }
        
        switch (this.type) {
            case GAS_TURBINE:
            case STEAM_TURBINE:
                data += getGasTurbinePerformance();
                break;
                
            case TURBOJET:
                data += getTurbojetPerformance();
                break;
        }

        return data;
    }
    public String getTurbojetPerformance() {

        String data;
        data = "\nPerformance {\n";
        data += "\n\tThrust: " + getThrust();
        data += "\n\tTSFC: " + getTSFC();
        data += "\n}";
        return data;
    }
    public String getGasTurbinePerformance() {

        String data;
        data = "\nPerformance {\n";
        data += "\n\tNet Work: " + getNetWork();
        data += "\n\tTotal Fuel Consumption: " + getFuelConsumption();
        data += "\n\tSFC: " + getSFC();
        data += "\n\tThermal Efficiency: " + FormatUtils.format(100 * getEfficiency()) + "%";
        data += "\n}";

        return data;
    }
    public String getCharts() {
        
        return "";
    }
    public void writeResultsToPath(String path){
        
        try {
            
            File f = new File(path);
            FileUtils.writeStringToFile(f, getFullResults(isExergyEnabled()));
            
        } catch (IOException ex) {
            Logger.getLogger(ThermalSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private final List<Flow> flowList = new ArrayList<Flow>();
    public Flow createFlow(SteadyFlowDevice dvc1, SteadyFlowDevice dvc2){
        
        Flow flow = new Flow();
        
        dvc1.getFlowManager().addOut(flow);
        dvc2.getFlowManager().addIn(flow);
        
        flowList.add(flow);
        
        return flow;
    }
    public List<Flow> getFlowList() {
        return flowList;
    }
    
    private final List<Pipe> pipeList = new ArrayList<Pipe>();
    public Pipe createPipe(SteadyFlowDevice dvc1, SteadyFlowDevice dvc2){
        
        Pipe pipe = new Pipe();
        
        Flow f1 = new Flow();
        dvc1.getFlowManager().addOut(f1);
        pipe.getFlowManager().addIn(f1);
        
        Flow f2 = new Flow();
        pipe.getFlowManager().addOut(f2);
        dvc2.getFlowManager().addIn(f2);
        
        pipe.setInletDevice(dvc1);
        pipe.setOutletDevice(dvc2);
        
        pipeList.add(pipe);
        
        return pipe;
    }
    public List<Pipe> getPipeList() {
        return pipeList;
    }
    
    public double getThrust(){
        
        AirFlow airFlow = (AirFlow) searchForDeviceType(AirFlow.class);
        Nozzle nozzle = (Nozzle) searchForDeviceType(Nozzle.class);
        
        double F = Double.NaN;
        if(airFlow != null && nozzle != null){

            double m = airFlow.getMassFlowRate();
            double ua = airFlow.getVelocity();
            double ue = nozzle.getExhaustVelocity();
            
            F = m * (ue - ua);
        }
        return F;
    }
    public double getTSFC(){
        
        double F = getThrust();
        double mf = getFuelConsumption(); 
        return 3600 * mf / F;
    }
    
    private Device searchForDeviceType(Class deviceType) {
        
        for(Device device : deviceList){
            if(deviceType.isInstance(device)){
                return device;
            }
        }
        return null;
    }
    
    private void recognizeSystemType(){
        
        boolean isGasTurbine = 
            hasDeviceType(Intake.class) &&
            hasDeviceType(Compressor.class) &&
            hasDeviceType(Burner.class) &&
            hasDeviceType(GasTurbine.class) &&
            hasDeviceType(Exhaust.class);
        
        boolean isSteamTurbine = 
            hasDeviceType(Boiler.class) &&
            hasDeviceType(Pump.class) &&
            hasDeviceType(SteamTurbine.class) && 
            hasDeviceType(Condenser.class);
        
        boolean isTurbojet = 
            hasDeviceType(AirFlow.class) &&
            hasDeviceType(Diffuser.class) &&
            hasDeviceType(Compressor.class) && 
            hasDeviceType(Burner.class) && 
            hasDeviceType(GasTurbine.class)  && 
            hasDeviceType(Nozzle.class);
        
        if(isGasTurbine && isSteamTurbine){
        
            this.type = ThermalSystemType.COMBINED_CYCLE;
        }else if(isGasTurbine){
        
            this.type = ThermalSystemType.GAS_TURBINE;
        }else if(isSteamTurbine){
        
            this.type = ThermalSystemType.STEAM_TURBINE;
        }else if(isTurbojet){
        
            this.type = ThermalSystemType.TURBOJET;
        }
    }

    private boolean hasDeviceType(Class deviceType) {
        
        for(Device device:deviceList){
            
            if(deviceType.isInstance(device)){            
                return true;
            }
        }
        return false;
    }

    public ThermalSystemType getType() {
        if(type == null){
            recognizeSystemType();
        }
        return type;
    }
    
    private boolean exergyEnabled = false;
    public boolean isExergyEnabled(){
        return exergyEnabled;
    }
    public void calculateExergy(){
        
        exergyEnabled = true;
        for(Device device:getDeviceList()){
            device.calculateExergy();
        }        
    }
}
