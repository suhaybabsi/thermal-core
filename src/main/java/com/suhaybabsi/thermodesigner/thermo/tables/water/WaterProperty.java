/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.tables.water;

/**
 *
 * @author suhaybal-absi
 */
public class WaterProperty {
    
    public static final WaterProperty PRESSURE = new WaterProperty(
            SaturatedPressureTable.TableProperty.PRESS, 
            null, null, null, 
            SaturatedTemperatureTable.TableProperty.P_SAT, 
            null, null, null, 
            null, null, true);
    
    public static final WaterProperty TEMPERATURE = new WaterProperty(
            SaturatedPressureTable.TableProperty.T_SAT, 
            null, null, null, 
            SaturatedTemperatureTable.TableProperty.TEMP, 
            null, null, null, 
            SubcooledTable.SubcooledProperty.TEMPERATURE, 
            SuperheatedTable.SuperheatedProperty.TEMPERATURE, true);
    
    public static final WaterProperty ENTROPY = new WaterProperty(
            null, 
            SaturatedPressureTable.TableProperty.S_F, 
            SaturatedPressureTable.TableProperty.S_FG, 
            SaturatedPressureTable.TableProperty.S_G, 
            null, 
            SaturatedTemperatureTable.TableProperty.S_F, 
            SaturatedTemperatureTable.TableProperty.S_FG, 
            SaturatedTemperatureTable.TableProperty.S_G, 
            SubcooledTable.SubcooledProperty.ENTROPY, 
            SuperheatedTable.SuperheatedProperty.ENTROPY, false);
    
    public static final WaterProperty ENTHALPY = new WaterProperty(
            null, 
            SaturatedPressureTable.TableProperty.H_F, 
            SaturatedPressureTable.TableProperty.H_FG, 
            SaturatedPressureTable.TableProperty.H_G, 
            null, 
            SaturatedTemperatureTable.TableProperty.H_F, 
            SaturatedTemperatureTable.TableProperty.H_FG, 
            SaturatedTemperatureTable.TableProperty.H_G, 
            SubcooledTable.SubcooledProperty.ENTHALPY, 
            SuperheatedTable.SuperheatedProperty.ENTHALPY, false);
    
    public static final WaterProperty VOLUME = new WaterProperty(
            null, 
            SaturatedPressureTable.TableProperty.V_F, 
            null, 
            SaturatedPressureTable.TableProperty.V_G, 
            null, 
            SaturatedTemperatureTable.TableProperty.V_F, 
            null, 
            SaturatedTemperatureTable.TableProperty.V_G, 
            SubcooledTable.SubcooledProperty.VOLUME, 
            SuperheatedTable.SuperheatedProperty.VOLUME, false);
    
    public static final WaterProperty INTERNAL_ENERGY = new WaterProperty(
            null, 
            SaturatedPressureTable.TableProperty.U_F, 
            SaturatedPressureTable.TableProperty.U_FG, 
            SaturatedPressureTable.TableProperty.U_G, 
            null, 
            SaturatedTemperatureTable.TableProperty.U_F, 
            SaturatedTemperatureTable.TableProperty.U_FG, 
            SaturatedTemperatureTable.TableProperty.U_G, 
            SubcooledTable.SubcooledProperty.INTERNAL_ENERGY, 
            SuperheatedTable.SuperheatedProperty.INTERNAL_ENERGY, false);
    
    
    private final SaturatedPressureTable.TableProperty satPressProp;
    private final SaturatedPressureTable.TableProperty satPressPropF;
    private final SaturatedPressureTable.TableProperty satPressPropFG;
    private final SaturatedPressureTable.TableProperty satPressPropG;
    
    private final SaturatedTemperatureTable.TableProperty satTempProp;
    private final SaturatedTemperatureTable.TableProperty satTempPropF;
    private final SaturatedTemperatureTable.TableProperty satTempPropFG;
    private final SaturatedTemperatureTable.TableProperty satTempPropG;
    
    private final SubcooledTable.SubcooledProperty subcooledProp;
    private final SuperheatedTable.SuperheatedProperty superheatedProp;
    
    private final boolean _single;
    private WaterProperty(
            SaturatedPressureTable.TableProperty satPressProp, 
            SaturatedPressureTable.TableProperty satPressPropF, 
            SaturatedPressureTable.TableProperty satPressPropFG, 
            SaturatedPressureTable.TableProperty satPressPropG, 
            SaturatedTemperatureTable.TableProperty satTempProp, 
            SaturatedTemperatureTable.TableProperty satTempPropF, 
            SaturatedTemperatureTable.TableProperty satTempPropFG, 
            SaturatedTemperatureTable.TableProperty satTempPropG, 
            SubcooledTable.SubcooledProperty subcooledProp, 
            SuperheatedTable.SuperheatedProperty superheatedProp, boolean single) {
        
        this.satPressProp = satPressProp;
        this.satPressPropF = satPressPropF;
        this.satPressPropFG = satPressPropFG;
        this.satPressPropG = satPressPropG;
        this.satTempProp = satTempProp;
        this.satTempPropF = satTempPropF;
        this.satTempPropFG = satTempPropFG;
        this.satTempPropG = satTempPropG;
        this.subcooledProp = subcooledProp;
        this.superheatedProp = superheatedProp;
        this._single = single;
    }

    public SaturatedPressureTable.TableProperty getSatPressProp() {
        return satPressProp;
    }

    public SaturatedPressureTable.TableProperty getSatPressPropF() {
        return satPressPropF;
    }

    public SaturatedPressureTable.TableProperty getSatPressPropFG() {
        return satPressPropFG;
    }

    public SaturatedPressureTable.TableProperty getSatPressPropG() {
        return satPressPropG;
    }

    public SaturatedTemperatureTable.TableProperty getSatTempProp() {
        return satTempProp;
    }

    public SaturatedTemperatureTable.TableProperty getSatTempPropF() {
        return satTempPropF;
    }

    public SaturatedTemperatureTable.TableProperty getSatTempPropFG() {
        return satTempPropFG;
    }

    public SaturatedTemperatureTable.TableProperty getSatTempPropG() {
        return satTempPropG;
    }

    public SubcooledTable.SubcooledProperty getSubcooledProp() {
        return subcooledProp;
    }

    public SuperheatedTable.SuperheatedProperty getSuperheatedProp() {
        return superheatedProp;
    }
     
    public boolean isHeader(){
        return this.equals(WaterProperty.PRESSURE);
    }
    public boolean isSingle() {
        return _single;
    } 
}
