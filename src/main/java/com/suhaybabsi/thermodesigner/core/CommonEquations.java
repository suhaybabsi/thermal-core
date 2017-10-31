/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.core;

import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;

/**
 *
 * @author suhaybal-absi
 */
public class CommonEquations {
    
    public static class PropertyMatchEquation extends ThermalEquation {

        private final Property p1;
        private final Property p2;
        public PropertyMatchEquation(Property p1, Property p2){
            
            super(p1, p2);
            this.p1 = p1;
            this.p2 = p2;
        }
        @Override
        protected double evaluate() {
            return p1.getValue() - p2.getValue();
        }
    }
    
    public static class MassConservationEquation extends ThermalEquation {
        
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
    
    public static abstract class PropertyLossEquation extends ThermalEquation {

        private final Property p1_p;
        private final Property p2_p;
        private final Property dp_p;

        protected PropertyLossEquation(Property p1, Property p2, Property dp) {
            super(p1, p2, dp);
            this.p1_p = p1;
            this.p2_p = p2;
            this.dp_p = dp;
        }

        @Override
        protected double evaluate() {

            double p1 = p1_p.getValue();
            double p2 = p2_p.getValue();
            double dp = dp_p.getValue();

            return p2 - p1 * (1 - dp);
        }
    }
    public static class PressureLossEquation extends PropertyLossEquation {

        public PressureLossEquation(Property p1, Property p2, Property dp) {
            super(p1, p2, dp);
            setName("Pressure Loss Equation");
        }
    }
    
    
    public static class GasHeatLossEquation extends ThermalEquation {

        private final Property t1_p;
        private final Property m_p;
        private final Property q_p;
        private final Property t2_p;
        private final Fluid fluid;
        
        public GasHeatLossEquation(Property q_p, Property m_p, Property t1_p, Property t2_p, Fluid f){
            
            super(q_p, m_p, t1_p, t2_p);
            this.q_p = q_p;
            this.m_p = m_p;
            this.t1_p = t1_p;
            this.t2_p = t2_p;
            this.fluid = f;
        }
        @Override
        protected double evaluate() {
            
            double t1 = t1_p.getValue();
            double t2 = t2_p.getValue();
            double m = m_p.getValue();
            double q = q_p.getValue();
            
            double cp = fluid.averageHeatCp(t1, t2);
            
            return q - m * cp * (t1 - t2);
        }
    }
}
