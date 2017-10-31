/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo;

import com.suhaybabsi.thermodesigner.core.Property;

/**
 *
 * @author suhaybal-absi
 */
public class Heat extends Property {
    public static class Simple extends Heat{
        public Simple(double val) {
            this.value = val;
        }
    }

    public Heat() {
        super("heat");
    }
    
    
}
