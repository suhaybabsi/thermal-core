/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.tables.water;

import com.suhaybabsi.thermodesigner.core.Utils;
import com.suhaybabsi.thermodesigner.thermo.fluid.FlowState;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 *
 * @author suhaybal-absi
 */
public class SaturatedPressureTable implements SaturatedTable {

    public static enum TableProperty {

        PRESS(0), T_SAT(1), V_F(2), V_G(3), U_F(4), U_FG(5), U_G(6), H_F(7), H_FG(8), H_G(9), S_F(10), S_FG(11), S_G(
                12);

        private final int index;

        private TableProperty(int i) {
            this.index = i;
        }

        public int index() {
            return index;
        }
    }

    public static class SaturatedRecord {

        private final double[] values;

        public static SaturatedRecord create(String[] vals) {

            double[] numerics = new double[TableProperty.values().length];
            for (int i = 0; i < vals.length; i++) {

                numerics[i] = NumberUtils.toDouble(vals[i]);
            }
            return new SaturatedRecord(numerics);
        }

        private SaturatedRecord(double[] values) {
            this.values = values;
        }

        public double get(TableProperty p) {
            return values[p.index()];
        }
    }

    private List<SaturatedRecord> recordList;

    public List<SaturatedRecord> getRecordList() {
        return recordList;
    }

    private void readData() {

        List<String> recordLines;
        recordLines = Utils.getTableLines("water_saturated_p_props.csv");
        
        if (recordLines != null) {
            recordList = new ArrayList<SaturatedRecord>();
            for (String line : recordLines) {

                String[] values = line.split(",");
                recordList.add(SaturatedRecord.create(values));
            }
        }
    }

    private Object searchRecords(TableProperty g, double val) {

        SaturatedRecord pr = null, nr = null;
        for (SaturatedRecord r : getRecordList()) {

            if (r.get(g) == val) {

                return r;
            } else if (r.get(g) < val) {
                pr = r;
            } else if (r.get(g) > val) {
                nr = r;
            }

            if (pr != null && nr != null) {
                return new SaturatedRecord[] { pr, nr };
            }
        }

        return null;
    }

    private double getPropertyValue(TableProperty p, double value) {
        return getPropertyValue(p, TableProperty.PRESS, value);
    }

    public double getPropertyValue(TableProperty p, final TableProperty g, double value) {

        Collections.sort(getRecordList(), new Comparator<SaturatedRecord>() {
            @Override
            public int compare(SaturatedRecord o1, SaturatedRecord o2) {
                if (o1.get(g) == o2.get(g)) {
                    return 0;
                }
                return o1.get(g) < o2.get(g) ? -1 : 1;
            }
        });

        Object res = searchRecords(g, value);

        if (res == null) {
            return Double.NaN;
        }

        if (res instanceof SaturatedRecord) {

            return ((SaturatedRecord) res).get(p);
        } else {
            SaturatedRecord pr = ((SaturatedRecord[]) res)[0];
            SaturatedRecord nr = ((SaturatedRecord[]) res)[1];

            double p1 = pr.get(p);
            double p2 = nr.get(p);

            double g1 = pr.get(g);
            double g2 = nr.get(g);

            return (p2 - p1) / (g2 - g1) * (value - g1) + p1;
        }
    }

    private SaturatedPressureTable() {
        readData();
    }

    private static SaturatedPressureTable _instance;

    public static SaturatedPressureTable getInstance() {

        if (_instance == null) {

            _instance = new SaturatedPressureTable();
        }
        return _instance;
    }

    public double getTemperatureSaturated(double pressure) {
        return getPropertyValue(TableProperty.T_SAT, pressure);
    }

    @Override
    public double getLiquidVolume(double pressure) {
        return getPropertyValue(TableProperty.V_F, pressure);
    }

    @Override
    public double getEvapVolume(double pressure) {
        double v_l = getLiquidVolume(pressure);
        double v_g = getVaporVolume(pressure);
        return v_g - v_l;
    }

    @Override
    public double getVaporVolume(double pressure) {
        return getPropertyValue(TableProperty.V_G, pressure);
    }

    @Override
    public double getLiquidInternalEnergy(double pressure) {
        return getPropertyValue(TableProperty.U_F, pressure);
    }

    @Override
    public double getEvapInternalEnergy(double pressure) {
        return getPropertyValue(TableProperty.U_FG, pressure);
    }

    @Override
    public double getVaporInternalEnergy(double pressure) {
        return getPropertyValue(TableProperty.U_G, pressure);
    }

    @Override
    public double getLiquidEnthalpy(double pressure) {
        return getPropertyValue(TableProperty.H_F, pressure);
    }

    @Override
    public double getEvapEnthalpy(double pressure) {
        return getPropertyValue(TableProperty.H_FG, pressure);
    }

    @Override
    public double getVaporEnthalpy(double pressure) {
        return getPropertyValue(TableProperty.H_G, pressure);
    }

    @Override
    public double getLiquidEntropy(double pressure) {
        return getPropertyValue(TableProperty.S_F, pressure);
    }

    @Override
    public double getEvapEntropy(double pressure) {
        return getPropertyValue(TableProperty.S_FG, pressure);
    }

    @Override
    public double getVaporEntropy(double pressure) {
        return getPropertyValue(TableProperty.S_G, pressure);
    }

    @Override
    public double getVolume(double pressure, double x) {

        if (isSatPercentValid(x) == false) {
            return Double.NaN;
        }

        double v_l = getLiquidVolume(pressure);
        double v_evap = getEvapVolume(pressure);
        return v_l + x * v_evap;
    }

    @Override
    public double getInternalEnergy(double pressure, double x) {

        if (isSatPercentValid(x) == false) {
            return Double.NaN;
        }

        double u_l = getLiquidInternalEnergy(pressure);
        double u_evap = getEvapInternalEnergy(pressure);
        return u_l + x * u_evap;
    }

    @Override
    public double getEnthalpy(double pressure, double x) {

        if (isSatPercentValid(x) == false) {
            return Double.NaN;
        }

        double h_l = getLiquidEnthalpy(pressure);
        double h_evap = getEvapEnthalpy(pressure);
        return h_l + x * h_evap;
    }

    @Override
    public double getEntropy(double pressure, double x) {

        if (isSatPercentValid(x) == false) {
            return Double.NaN;
        }

        double s_l = getLiquidEntropy(pressure);
        double s_evap = getEvapEntropy(pressure);
        return s_l + x * s_evap;
    }

    private boolean isSatPercentValid(double x) {
        return x >= 0.0 && x <= 1.0;
    }

    @Override
    public double getSaturationRatio_volume(double press, double volume) {

        double v_l = getLiquidVolume(press);
        double v_evap = getEvapVolume(press);
        return (volume - v_l) / v_evap;
    }

    @Override
    public double getSaturationRatio_enthalpy(double press, double enthalpy) {

        double h_l = getLiquidEnthalpy(press);
        double h_evap = getEvapEnthalpy(press);
        return (enthalpy - h_l) / h_evap;
    }

    @Override
    public double getSaturationRatio_energy(double press, double energy) {

        double u_l = getLiquidInternalEnergy(press);
        double u_evap = getEvapInternalEnergy(press);
        return (energy - u_l) / u_evap;
    }

    @Override
    public double getSaturationRatio_entropy(double press, double entropy) {

        double s_l = getLiquidEntropy(press);
        double s_evap = getEvapEntropy(press);
        return (entropy - s_l) / s_evap;
    }

}
