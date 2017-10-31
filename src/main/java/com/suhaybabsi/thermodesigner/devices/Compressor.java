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
import com.suhaybabsi.thermodesigner.core.WorkConsumingDevice;
import com.suhaybabsi.thermodesigner.core.WorkType;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Air;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class Compressor extends TurboMachine implements WorkConsumingDevice {


    private static class CompressionEquation extends ThermalEquation {
        
        private Property r;
        private Property p1;
        private Property p2;
        public CompressionEquation(Property r, Property p1, Property p2) {
            super(r, p1, p2);
            setName("Compression Equation");
            this.r = r;
            this.p1 = p1;
            this.p2 = p2;
        }
        @Override
        protected double evaluate() {
            return r.getValue() - p2.getValue()/p1.getValue();
        }   
    }
    private static class PolytropicTemperatureEquation extends ThermalEquation {
        
        private final Property t1;
        private final Property t2;
        private final Property rc_p;
        private final Property nc_p;
        private final Fluid gas;
        public PolytropicTemperatureEquation(Property t1, Property t2, Property rc_p, Property nc_p, Fluid gas) {
            
            super(t1, t2, rc_p);
            setName("Temperature Equation");
            this.gas = gas;
            this.t1 = t1;
            this.t2 = t2;
            this.rc_p = rc_p;
            this.nc_p = nc_p;
        }
        
        @Override
        protected double evaluate() {
            
            double T1 = t1.getValue();
            double T2 = t2.getValue();
            double rc = rc_p.getValue();
            
            double nc;
            if(nc_p.isDefined()){
                nc = nc_p.getValue();
            }else{
                nc = 0.91 - (rc- 1)/300;
                nc_p.setValue(nc);
            }
            
            double g = gas.averageHeatRatio(T1, T2, Double.NaN);
            double power = (g - 1)/(g * nc);
            //System.out.println("(n-1)/n_c: "+power);
            return T2 - T1 - T1*(Math.pow(rc, power) - 1);
        }   
    }
    
    private static class WorkEquation extends ThermalEquation {
        
        private final Property work;
        private final Property massRate;
        private final Property t1;
        private final Property t2;
        private final Fluid gas;
        public WorkEquation(Property work, Property mrate, Property t1, Property t2, Fluid gas) {
            
            super(work, mrate, t1, t2);
            setName("Work Equation");
            
            this.gas = gas;
            this.t1 = t1;
            this.t2 = t2;
            this.massRate = mrate;
            this.work = work;
        }
        
        @Override
        protected double evaluate() {
            
            double mc = massRate.getValue();
            double T2 = t2.getValue();
            double T1 = t1.getValue();
            double Wc = work.getValue();
            double cpa = gas.averageHeatCp(T1, T2);
            
            return Wc - mc * cpa * (T2 - T1); 
        }   
    }
    
    private static class MassConservationEquation extends ThermalEquation {
        
        private final Property massIn;
        private final Property massOut;
        public MassConservationEquation(
            Property massIn, Property massOut) {
            super(massIn, massOut);
            setName("Mass Conservation Equation");
            this.massIn = massIn;
            this.massOut = massOut;
        }
        
        @Override
        protected double evaluate() {
            
            double m_in = massIn.getValue();
            double m_out = massOut.getValue();
            return m_in - m_out;
        }   
    }
    
    private final Property compressionRatio = new Property("Compression Ratio");
    private final Property polytropicEfficiency = new Property("Polytropic Efficiency");

    public Compressor() {
        super("Compressor", "compressor");
        work = new Work(WorkType.CONSUMED);
        work.setMin(0);
        work.setMax(Double.MAX_VALUE);
        
        compressionRatio.setMin(0);
        compressionRatio.setMax(500);
        
        polytropicEfficiency.setMin(0);
        polytropicEfficiency.setMax(1);
    }
    @Override
    protected void configureEquations() throws ConfigurationException  {
        
        insureSingleFlowFluids();
        
        Flow inFlow = flowManager.getIn(0);
        Flow outFlow = flowManager.getOut(0);
        
        Property massRateIn = inFlow.getMassRateProp();
        Property massRateOut = outFlow.getMassRateProp();
        Fluid gas = inFlow.getFluid();
        
        Property P1 = inFlow.getPressureProp();
        Property P2 = outFlow.getPressureProp();
        
        Property T1 = inFlow.getTemperatureProp();
        Property T2 = outFlow.getTemperatureProp();
  
        addEquation(new CompressionEquation(compressionRatio, P1, P2));
        addEquation(new PolytropicTemperatureEquation(T1, T2, compressionRatio, polytropicEfficiency, gas));
        addEquation(new WorkEquation(this.work, massRateIn, T1, T2, gas));
        addEquation(new MassConservationEquation(massRateIn, massRateOut));
        
        outFlow.setFluid(Air.getInstance());
    }
    
    private final Property reversibleWork = new Property("Reversible Work");
    private final Property exergyDestruction = new Property("Exergy Destruction");
    private final Property secondLawEfficiency = new Property("Second Law Efficiency");
    @Override
    public void calculateExergy(){
        
        super.calculateExergy();
        
        Flow f1 = flowManager.getIn(0);
        Flow f2 = flowManager.getOut(0);
        
        double m1 = f1.getMassRate();
        double x1 = f1.getExergy();
        double x2 = f2.getExergy();
        
        double Wc = work.getValue();
        double Wrev = m1 * (x2 - x1);
        double Xdest = Wc - Wrev;
        double Xeff = Wrev / Wc;
        
        reversibleWork.setValue(Wrev);
        exergyDestruction.setValue(Xdest);
        secondLawEfficiency.setValue(Xeff);
    }
    
    public void defineCompressionRatio(double r) {
        this.compressionRatio.define(r);
    }
    public void defineWorkConsumed(double wc) {
        this.work.define(wc);
    }
    public void definePolytropicEfficiency(double nc) {
        this.polytropicEfficiency.define(nc);
    }
    
    public double getCompressionRatio() {
        return compressionRatio.getValue();
    }
    public double getPolytropicEfficiency() {
        return polytropicEfficiency.getValue();
    }
    @Override
    public double getWorkConsumed() {
        return this.work.getValue();
    }

    @Override
    protected List<FieldResult> getEnergyOutputResults() {
        
        Flow flow1 = flowManager.getIn(0);
        Flow flow2 = flowManager.getOut(0);
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Mass Rate (1)", flow1.getMassRate()) );
        res.add(FieldResult.create("Work consumed (Wc)", getWorkConsumed()) );
        res.add(FieldResult.create("Polytropic Efficiency", getPolytropicEfficiency()) );
        res.add(FieldResult.create("P2", flow2.getPressure()) );
        res.add(FieldResult.create("T2", flow2.getTemperature()) );
        
        return res;
    }
    
    @Override
    public List<FieldResult> getEnergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("r", "Compression Ratio", getCompressionRatio()) );
        res.add(FieldResult.create("wc", "Work Consumed", getWorkConsumed()) );
        res.add(FieldResult.create("nc", "Polytropic Efficiency", getPolytropicEfficiency()) );
        
        return res;
    }

    @Override
    public List<FieldResult> getExergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        
        res.add(FieldResult.create("Wrev","Reversible Work", reversibleWork.getValue()) );
        res.add(FieldResult.create("Xdest","Exergy Destruction", exergyDestruction.getValue())  );
        res.add(FieldResult.create("Xeff","Second Law Efficiency", secondLawEfficiency.getValue()) );
        
        return res;
    }
}
