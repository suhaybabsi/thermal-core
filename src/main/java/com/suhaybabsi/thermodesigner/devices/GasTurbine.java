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
import com.suhaybabsi.thermodesigner.core.WorkProducingDevice;
import com.suhaybabsi.thermodesigner.core.WorkType;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class GasTurbine extends TurboMachine implements WorkProducingDevice {

    static class ExpansionEquation extends ThermalEquation {

        private final Property pr;
        private final Property press1;
        private final Property press2;

        public ExpansionEquation(Property r, Property p1, Property p2) {

            super(r, p1, p2);
            setName("Expansion Equation");
            this.pr = r;
            this.press1 = p1;
            this.press2 = p2;
        }

        @Override
        protected double evaluate() {

            double p1 = press1.getValue();
            double p2 = press2.getValue();
            double rt = pr.getValue();
            
            //System.out.println("p1: "+p1 + ", p2: " +p2+", rt: "+rt );

            return rt - p1 / p2;
        }
    }

    static class PolytropicTemperatureEquation extends ThermalEquation {

        private final Property t1;
        private final Property t2;
        private final Property rt_p;
        private final Fluid gas;
        private final Property nt_p;

        public PolytropicTemperatureEquation(Property t1, Property t2, Property rt_p, Property nt_p, Fluid gas) {

            super(t1, t2, rt_p);
            setName("Temperature Equation");
            this.gas = gas;
            this.t1 = t1;
            this.t2 = t2;
            this.rt_p = rt_p;
            this.nt_p = nt_p;
        }

        @Override
        protected double evaluate() {

            double T1 = t1.getValue();
            double T2 = t2.getValue();
            double rt = rt_p.getValue();
            double g = gas.averageHeatRatio(T1, T2, Double.NaN);

            double nt;
            if (nt_p.isDefined()) {
                nt = nt_p.getValue();
            } else {
                nt = 0.90 - (rt - 1) / 250;
                nt_p.setValue(nt);
            }
            
            double power = nt * (g - 1) / g;
            //System.out.println("(n-1)/n_t: "+power);
            return T1 - T2 - T1 * (1 - Math.pow(1 / rt, power));
        }
    }

    static class WorkEquation extends ThermalEquation {

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

            double mt = massRate.getValue();
            double T2 = t2.getValue();
            double T1 = t1.getValue();
            double Wt = work.getValue();
            double cpg = gas.averageHeatCp(T1, T2);

            return Wt - mt * cpg * (T1 - T2);
        }
    }

    static class MassConservationEquation extends ThermalEquation {

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

    private final Property pressureRatio = new Property("Pressure Ratio");
    private final Property polytropicEfficiency = new Property("Polytropic Efficiency");

    public GasTurbine() {
        super("Gas Turbine", "gas_turbine");
        work = new Work(WorkType.PRODUCED);
        work.setMin(0);
        work.setMax(Double.MAX_VALUE);

        pressureRatio.setMin(0);
        pressureRatio.setMax(200);

        polytropicEfficiency.setMin(0);
        polytropicEfficiency.setMax(1);
    }

    @Override
    protected void configureEquations() throws ConfigurationException {

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

        addEquation(new ExpansionEquation(pressureRatio, P1, P2));
        addEquation(new PolytropicTemperatureEquation(T1, T2, pressureRatio, polytropicEfficiency, gas));
        addEquation(new WorkEquation(work, massRateIn, T1, T2, gas));
        addEquation(new MassConservationEquation(massRateIn, massRateOut));

        outFlow.setFluid(inFlow.getFluid());
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
        double m2 = f2.getMassRate();
        double x1 = f1.getExergy();
        double x2 = f2.getExergy();
        
        double Wct = work.getValue();
        
        double Xin = m1 * x1;
        double Xout = m2 * x2 + Wct;
        
        double Xdest = Xin - Xout;
        double Wrev = m1 * (x1 - x2);
        double Xeff = Wct / Wrev;
        
        reversibleWork.setValue(Wrev);
        exergyDestruction.setValue(Xdest);
        secondLawEfficiency.setValue(Xeff);
    }

    public void definePressureRatio(double r) {
        pressureRatio.define(r);
    }

    public void defineWorkProduced(double wc) {
        work.define(wc);
    }

    public void definePolytropicEfficiency(double nt) {
        polytropicEfficiency.define(nt);
    }

    public double getPressureRatio() {
        return pressureRatio.getValue();
    }

    public double getPolytropicEfficiency() {
        return polytropicEfficiency.getValue();
    }
    
    @Override
    public double getWorkProduced() {
        return work.getValue();
    }
    
    @Override
    public List<FieldResult> getEnergyOutputResults() {
        
        Flow flow2 = flowManager.getOut(0);
        List<FieldResult> res = new ArrayList<FieldResult>();  
        
        res.add(FieldResult.create("Mass Rate (2): ", flow2.getMassRate() ));
        res.add(FieldResult.create("Pressure Ratio: ", getPressureRatio() ));
        res.add(FieldResult.create("Work Produced: ", getWorkProduced() ));
        res.add(FieldResult.create("Polytropic Efficiency: ", getPolytropicEfficiency() ));
        res.add(FieldResult.create("P2: ", flow2.getPressure() ));
        res.add(FieldResult.create("T2: ", flow2.getTemperature() ));   

        return res;
    }

    @Override
    public List<FieldResult> getEnergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();  
        
        res.add(FieldResult.create("pr", "Pressure Ratio", getPressureRatio()) );
        res.add(FieldResult.create("wt", "Work Produced", getWorkProduced()) );
        res.add(FieldResult.create("nt", "Polytropic Efficiency", getPolytropicEfficiency()) );
        
        return res;
    }

    @Override
    public List<FieldResult> getExergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();        
        
        res.add(FieldResult.create("Wrev", "Reversible Work", reversibleWork.getValue()) );
        res.add(FieldResult.create("Xdest", "Exergy Destruction", exergyDestruction.getValue()) );
        res.add(FieldResult.create("Xeff", "Second Law Efficiency", secondLawEfficiency.getValue()) );
         
        return res;
    }
    
}