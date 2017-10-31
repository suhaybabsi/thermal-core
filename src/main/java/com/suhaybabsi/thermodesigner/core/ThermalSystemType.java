package com.suhaybabsi.thermodesigner.core;

/**
 *
 * @author suhaybal-absi
 */
public enum ThermalSystemType {
    STEAM_TURBINE("steam_turbine"), GAS_TURBINE("gas_turbine"), 
    TURBOJET("turbojet"), COMBINED_CYCLE("combined_cycle"), 
    REFRIGERATION("refrigeration"), NO_SYSTEM("no_system");
    
    private final String jsonType;
    private ThermalSystemType(String type) {
        this.jsonType = type;
    }
    public String getJson() {
        return jsonType;
    }
}
