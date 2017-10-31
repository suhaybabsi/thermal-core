/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.tables.water;

import com.suhaybabsi.thermodesigner.core.Utils;
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
public class SaturatedTemperatureTable implements SaturatedTable {

    public static enum TableProperty {

        TEMP(0), P_SAT(1), V_F(2), V_G(3), U_F(4), U_FG(5), U_G(6), H_F(7), H_FG(8), H_G(9), S_F(10), S_FG(11), S_G(12);

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
        recordLines = Utils.getTableLines("water_saturated_t_props.csv");
        
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
        return getPropertyValue(p, TableProperty.TEMP, value);
    }

    private double getPropertyValue(TableProperty p, final TableProperty g, double value) {

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

    private SaturatedTemperatureTable() {
        readData();
    }

    private static SaturatedTemperatureTable _instance;

    public static SaturatedTemperatureTable getInstance() {

        if (_instance == null) {

            _instance = new SaturatedTemperatureTable();
        }
        return _instance;
    }

    public double getPressureSaturated(double temp) {
        return getPropertyValue(TableProperty.P_SAT, temp);
    }

    @Override
    public double getLiquidVolume(double temp) {
        return getPropertyValue(TableProperty.V_F, temp);
    }

    @Override
    public double getEvapVolume(double temp) {
        double v_l = getLiquidVolume(temp);
        double v_g = getVaporVolume(temp);
        return v_g - v_l;
    }

    @Override
    public double getVaporVolume(double temp) {
        return getPropertyValue(TableProperty.V_G, temp);
    }

    @Override
    public double getLiquidInternalEnergy(double temp) {
        return getPropertyValue(TableProperty.U_F, temp);
    }

    @Override
    public double getEvapInternalEnergy(double temp) {
        return getPropertyValue(TableProperty.U_FG, temp);
    }

    @Override
    public double getVaporInternalEnergy(double temp) {
        return getPropertyValue(TableProperty.U_G, temp);
    }

    @Override
    public double getLiquidEnthalpy(double temp) {
        return getPropertyValue(TableProperty.H_F, temp);
    }

    @Override
    public double getEvapEnthalpy(double temp) {
        return getPropertyValue(TableProperty.H_FG, temp);
    }

    @Override
    public double getVaporEnthalpy(double temp) {
        return getPropertyValue(TableProperty.H_G, temp);
    }

    @Override
    public double getLiquidEntropy(double temp) {
        return getPropertyValue(TableProperty.S_F, temp);
    }

    @Override
    public double getEvapEntropy(double temp) {
        return getPropertyValue(TableProperty.S_FG, temp);
    }

    @Override
    public double getVaporEntropy(double temp) {
        return getPropertyValue(TableProperty.S_G, temp);
    }

    @Override
    public double getVolume(double temp, double x) {

        if (isSatPercentValid(x) == false) {
            return Double.NaN;
        }

        double v_l = getLiquidVolume(temp);
        double v_evap = getEvapVolume(temp);
        return v_l + x * v_evap;
    }

    @Override
    public double getInternalEnergy(double temp, double x) {

        if (isSatPercentValid(x) == false) {
            return Double.NaN;
        }

        double u_l = getLiquidInternalEnergy(temp);
        double u_evap = getEvapInternalEnergy(temp);
        return u_l + x * u_evap;
    }

    @Override
    public double getEnthalpy(double temp, double x) {

        if (isSatPercentValid(x) == false) {
            return Double.NaN;
        }

        double h_l = getLiquidEnthalpy(temp);
        double h_evap = getEvapEnthalpy(temp);
        return h_l + x * h_evap;
    }

    @Override
    public double getEntropy(double temp, double x) {

        if (isSatPercentValid(x) == false) {
            return Double.NaN;
        }

        double s_l = getLiquidEntropy(temp);
        double s_evap = getEvapEntropy(temp);
        return s_l + x * s_evap;
    }

    private boolean isSatPercentValid(double x) {
        return x >= 0.0 && x <= 1.0;
    }

    @Override
    public double getSaturationRatio_volume(double temp, double volume) {

        double v_l = getLiquidVolume(temp);
        double v_evap = getEvapVolume(temp);
        return (volume - v_l) / v_evap;
    }

    @Override
    public double getSaturationRatio_enthalpy(double temp, double enthalpy) {

        double h_l = getLiquidEnthalpy(temp);
        double h_evap = getEvapEnthalpy(temp);
        return (enthalpy - h_l) / h_evap;
    }

    @Override
    public double getSaturationRatio_energy(double temp, double energy) {

        double u_l = getLiquidInternalEnergy(temp);
        double u_evap = getEvapInternalEnergy(temp);
        return (energy - u_l) / u_evap;
    }

    @Override
    public double getSaturationRatio_entropy(double temp, double entropy) {

        double s_l = getLiquidEntropy(temp);
        double s_evap = getEvapEntropy(temp);
        return (entropy - s_l) / s_evap;
    }
}
