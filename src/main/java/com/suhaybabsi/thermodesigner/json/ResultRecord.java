/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.json;

import com.suhaybabsi.thermodesigner.core.TDJSONObject;
import com.suhaybabsi.thermodesigner.devices.Device;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

/**
 *
 * @author suhaybal-absi
 */
public class ResultRecord {
    
    private final JSONObject src;
    private final JSONObject res;
    private final Device dvc;
    public ResultRecord(Device device) {
        this.dvc = device;
        this.src = (JSONObject) device.getData();
        this.res = new TDJSONObject(); 
    }
    public void add(String p, double value) throws JSONException{
        if(src.isNull(p)){
            res.put(p, value);
        }
    }
    public void add(String p, String value) throws JSONException{
        if(src.isNull(p)){
            res.put(p, value);
        }
    }
    public JSONObject wrap() throws JSONException{
        if(res.length() > 0){
            JSONObject json = new JSONObject();
            json.put("type", dvc.getJsonType());
            json.put("res", this.res);
            return json;
        }
        return null;
    }
}
