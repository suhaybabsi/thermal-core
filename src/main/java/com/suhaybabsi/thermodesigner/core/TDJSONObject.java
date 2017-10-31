/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.core;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

/**
 *
 * @author suhaybal-absi
 */
public class TDJSONObject extends JSONObject {
    private static boolean isFinite(double value){
        return Double.isNaN(value) == false 
                && Double.isInfinite(value) == false;
    }
    @Override
    public JSONObject put(String key, double value) throws JSONException {
        Object cvalue = isFinite(value) ? value : "N/A";
        return super.put(key, cvalue); 
    }
}
