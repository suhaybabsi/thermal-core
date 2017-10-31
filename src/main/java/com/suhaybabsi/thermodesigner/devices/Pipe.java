package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.CommonEquations;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.Property;
import com.suhaybabsi.thermodesigner.core.ThermalEquation;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public class Pipe extends SteadyFlowDevice {
    
    public static class FlowPropertyEquation extends ThermalEquation{
        private final Flow flow;
        private final Property prop;
        public FlowPropertyEquation(Flow flow, Property p){
            
            super();
            this.flow = flow;
            this.prop = p;
            setName("Flow Property Equation: "+ p.getName());
        }
        @Override
        protected double evaluate() {
            
            if(prop.hasValue()){
                return 0;
            }else{
            
                double v = flow.calculateProperty(prop);
                boolean found = Double.isNaN(v) == false;
                if(found){
                    prop.setValue(v);
                    return 0;
                }
            }
            return -1;
        }
    }
    public static class EnthalpyLossEquation extends ThermalEquation {

        private final Property h1_p;
        private final Property h2_p;
        private final Property dh_p;

        protected EnthalpyLossEquation(Property h1, Property h2, Property dh) {
            super(h1, h2, dh);
            setName("Enthalpy Loss Equation");
            this.h1_p = h1;
            this.h2_p = h2;
            this.dh_p = dh;
        }

        @Override
        protected double evaluate() {

            double h1 = h1_p.getValue();
            double h2 = h2_p.getValue();
            double dh = dh_p.getValue();
            
//            System.out.println("h1: "+h1+", h2:"+h2+", dh: "+dh);
            return h2 - h1 * (1 - dh);
        }
    }
    
    private final Property pressureLoss = new Property("Pressure Loss");
    private final Property enthalpyLoss = new Property("Enthalpy Loss");
    
    public Pipe() {
        super("Pipe", "pipe");
        
        pressureLoss.define(0.0);
        enthalpyLoss.define(0.0);
    }
    
    public Flow getStartState() {
        return flowManager.getIn(0);
    }
    
    public Flow getEndState() {
        return flowManager.getOut(0);
    }
    
    public void setFluid(Fluid fluid) {
        
        getStartState().setFluid(fluid);
        getEndState().setFluid(fluid);
    }
    
    @Override
    protected void configureEquations() throws ConfigurationException {
        
        insureSingleFlowFluids();
        Flow f1 = flowManager.getIn(0);
        Flow f2 = flowManager.getOut(0);
        
        addEquation(new CommonEquations.MassConservationEquation(
                        f1.getMassRateProp(), 
                        f2.getMassRateProp()));
        
        addEquation(new CommonEquations.PressureLossEquation(
                        f1.getPressureProp(), 
                        f2.getPressureProp(), pressureLoss));
        
        addEquation(new EnthalpyLossEquation(
                        f1.getEnthalpyProp(), 
                        f2.getEnthalpyProp(), enthalpyLoss));
        
        Flow[] flows = new Flow[]{f1, f2};
        for(Flow f:flows){
        
            addEquation(new FlowPropertyEquation(f, f.getEnthalpyProp()));
            addEquation(new FlowPropertyEquation(f, f.getTemperatureProp()));
            addEquation(new FlowPropertyEquation(f, f.getPressureProp()));  //To Be Investigated
            addEquation(new FlowPropertyEquation(f, f.getEntropyProp()));
            addEquation(new FlowPropertyEquation(f, f.getVapourFractionProp()));
        }
    }

    public void definePressureLoss(double value) {
        this.pressureLoss.define(value);
    }
    public void defineEnthalpyLoss(double value) {
        this.enthalpyLoss.define(value);
    }
    
    public double getMassFlowRate() {
        return flowManager.getIn(0).getMassRate();
    }
    
    public void defineMassRate(double value) {
        getStartState().defineMassRate(value);
        getEndState().defineMassRate(value);
    }
    
    private SteadyFlowDevice inletDevice;
    private SteadyFlowDevice outletDevice;
    
    public SteadyFlowDevice getInletDevice() {
        return inletDevice;
    }
    public void setInletDevice(SteadyFlowDevice inletDevice) {
        this.inletDevice = inletDevice;
    }

    public SteadyFlowDevice getOutletDevice() {
        return outletDevice;
    }
    public void setOutletDevice(SteadyFlowDevice outletDevice) {
        this.outletDevice = outletDevice;
    }

    @Override
    protected List<FieldResult> getEnergyOutputResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        Flow f1 = flowManager.getIn(0);
        Flow f2 = flowManager.getOut(0);
        Fluid fl = f1 != null && f1.getFluid() != null ? f1.getFluid() : null;
        fl = fl == null && f2 != null ? f2.getFluid() : fl;
        
        String fluid = fl != null ? fl.getName() : "N/A";
        
        res.add(FieldResult.create("Scheme", getInletDevice().getName()+" --> "+getOutletDevice().getName()) );
        res.add(FieldResult.create("Fluid", fluid) );
        res.add(FieldResult.create("Pressure Loss", this.pressureLoss.getValue()) );
        res.add(FieldResult.create("Enthalpy Loss", this.enthalpyLoss.getValue()) );
        res.add(FieldResult.create("Mass Rate",  this.getMassFlowRate()) );
        
        res.add(FieldResult.create("Pressure (1)",  this.getStartState().getPressure()) );
        res.add(FieldResult.create("Temperature (1)",  this.getStartState().getTemperature()) );
        res.add(FieldResult.create("Enthalpy (1)",  this.getStartState().getEnthalpy()) );
        res.add(FieldResult.create("Entropy (1)",  this.getStartState().getEntropy()) );
        res.add(FieldResult.create("Vapour Fraction (1)",  this.getStartState().getVapourFraction()) );
        
        res.add(FieldResult.create("Pressure (2)",  this.getEndState().getPressure()) );
        res.add(FieldResult.create("Temperature (2)",  this.getEndState().getTemperature()) );
        res.add(FieldResult.create("Enthalpy (2)",  this.getEndState().getEnthalpy()) );
        res.add(FieldResult.create("Entropy (2)",  this.getEndState().getEntropy()) );
        res.add(FieldResult.create("Vapour Fraction (2)",  this.getEndState().getVapourFraction()) );
        
        return res;
    }

    @Override
    public List<FieldResult> getExergyResults() {
        
        List<FieldResult> res = new ArrayList<FieldResult>();
        Flow f1 = getStartState();
        Flow f2 = getEndState();

        res.add(FieldResult.create("Exergy (1)", f1.getExergy()) );
        res.add(FieldResult.create("Exergy (2)",  f2.getExergy()) );
        
        return res;
    }
}
