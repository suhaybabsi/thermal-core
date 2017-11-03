 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.devices;

import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.thermo.Atmosphere;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import java.util.List;

/**
 *
 * @author suhaybal-absi
 */
public abstract class SteadyFlowDevice extends Device {
    protected FlowManager flowManager = new FlowManager(this);

    protected SteadyFlowDevice(String name, String jsonType) {
        super(name, jsonType);
    }
    public FlowManager getFlowManager() {
        return flowManager;
    }

    @Override
    public void calculateExergy() {
        for (Flow flow : flowManager.getAllFlows()) {
            flow.calculateExergy(
                    Atmosphere.STAGNATION_TEMP,
                    Atmosphere.STAGNATION_PRESS);
        }
    }
    
    protected void insureSingleFlowFluids() throws ConfigurationException{
    
        Flow flowIn = flowManager.getIn(0);
        Flow flowOut = flowManager.getOut(0);
        
        insureFluids(flowIn, flowOut);
    }
    
    protected void insureFluids(Flow f1, Flow f2) throws ConfigurationException{
        
        Fluid fld1 = f1.getFluid();
        Fluid fld2 = f2.getFluid();
        
        Fluid fluid = (fld1 != null) ? fld1 : fld2;
        if(fluid == null){
            throw new ConfigurationException(this, "Incompatible Flow");
        }
        
        f1.setFluid(fluid);
        f2.setFluid(fluid);
    }

    protected void insureFluids() throws ConfigurationException{ 
        
        this.insureFluids(null);
    }

    protected void insureFluids(Fluid suggestFluid) throws ConfigurationException{
        
        List<Flow> allFlows = flowManager.getAllFlows();
        Fluid fluid = null;
        for(Flow f:allFlows){
            
            if(f.getFluid() != null){
                fluid = f.getFluid();
                break;
            }
        }

        if(fluid == null){
            fluid = suggestFluid;
        }
        
        if(fluid == null){
            throw new ConfigurationException(this, "Incompatible Fluid !");
        } else {
            for(Flow f:allFlows){
                f.setFluid(fluid);
            }
        }        
    }
}
