/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.tables;

import com.suhaybabsi.thermodesigner.core.Utils;
import com.suhaybabsi.thermodesigner.thermo.Atmosphere;
import com.suhaybabsi.thermodesigner.thermo.fluid.GasHeatEvaluator;
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
public class IdealGasHeatTable implements GasHeatEvaluator {

    public static enum HeatProperty {
        TEMPERATURE(0), HEAT_CONST_PRESSURE(1), HEAT_CONST_VOLUME(2), HEAT_RATIO(3);

        private int index;

        private HeatProperty(int i) {
            this.index = i;
        }
    }

    public static class SpecificHeatRecord {

        private double[] values;

        private SpecificHeatRecord(double[] vals) {
            this.values = vals;
        }

        public static SpecificHeatRecord create(String[] values) {

            double[] numerics = new double[4];
            for (int i = 0; i < values.length; i++) {

                numerics[i] = NumberUtils.toDouble(values[i]);
            }
            return new SpecificHeatRecord(numerics);
        }

        public double get(HeatProperty p) {
            return values[p.index];
        }

        @Override
        public String toString() {
            String r = "";
            r += get(HeatProperty.TEMPERATURE);
            r += "\t" + get(HeatProperty.HEAT_CONST_PRESSURE);
            r += "\t" + get(HeatProperty.HEAT_CONST_VOLUME);
            r += "\t" + get(HeatProperty.HEAT_RATIO);
            return r;
        }

        private double getTemperature() {
            return get(HeatProperty.TEMPERATURE);
        }
    }

    private String tableName;
    private List<SpecificHeatRecord> heatRecordList;

    public IdealGasHeatTable(String t) {
        this.tableName = t;
        readData();
    }

    private void readData() {

        List<String> recordLines;
        recordLines = Utils.getTableLines(tableName);
        
        if (recordLines != null) {

            heatRecordList = new ArrayList<SpecificHeatRecord>();
            for (String line : recordLines) {

                String[] values = line.split(",");
                heatRecordList.add(SpecificHeatRecord.create(values));
            }
        }
    }

    public List<SpecificHeatRecord> getHeatRecordList() {
        return heatRecordList;
    }

    public void printData() {

        System.out.println("\nGas Heat Table Properties");
        for (SpecificHeatRecord r : getHeatRecordList()) {
            System.out.println(r);
        }
    }

    private static final Comparator<SpecificHeatRecord> temperatureComparator = new Comparator<SpecificHeatRecord>() {
        @Override
        public int compare(SpecificHeatRecord o1, SpecificHeatRecord o2) {
            if (o1.getTemperature() == o2.getTemperature()) {
                return 0;
            }
            return o1.getTemperature() < o2.getTemperature() ? -1 : 1;
        }
    };

    private Object searchHeatRecords(double temperature) {
        SpecificHeatRecord pr = null, nr = null;
        for (SpecificHeatRecord r : getHeatRecordList()) {

            if (r.getTemperature() == temperature) {

                return r;
            } else if (r.getTemperature() < temperature) {
                pr = r;
            } else if (r.getTemperature() > temperature) {
                nr = r;
            }

            if (pr != null && nr != null) {
                return new SpecificHeatRecord[] { pr, nr };
            }
        }
        return null;
    }

    private double getHeatPropertyValue(HeatProperty p, double temperature) {

        Collections.sort(getHeatRecordList(), temperatureComparator);

        Object res = searchHeatRecords(temperature);
        if (res == null) {
            return Double.NaN;
        }

        if (res instanceof SpecificHeatRecord) {
            return ((SpecificHeatRecord) res).get(p);
        } else {
            SpecificHeatRecord pr = ((SpecificHeatRecord[]) res)[0];
            SpecificHeatRecord nr = ((SpecificHeatRecord[]) res)[1];

            double c1 = pr.get(p);
            double c2 = nr.get(p);

            double t1 = pr.getTemperature();
            double t2 = nr.getTemperature();

            return (c2 - c1) / (t2 - t1) * (temperature - t1) + c1;
        }
    }

    @Override
    public double getHeatAtConstPressure(double temperature) {
        return getHeatPropertyValue(HeatProperty.HEAT_CONST_PRESSURE, temperature);
    }

    @Override
    public double getHeatAtConstVolume(double temperature) {
        return getHeatPropertyValue(HeatProperty.HEAT_CONST_VOLUME, temperature);
    }

    @Override
    public double getHeatRatio(double temperature) {
        return getHeatPropertyValue(HeatProperty.HEAT_RATIO, temperature);
    }
}
