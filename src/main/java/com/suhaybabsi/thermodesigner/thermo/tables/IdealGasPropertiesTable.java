/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.tables;

import com.suhaybabsi.thermodesigner.core.Utils;
import com.suhaybabsi.thermodesigner.thermo.Atmosphere;
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
public class IdealGasPropertiesTable {

    public static enum GasProperty {
        TEMPERATURE(0), ENTHALPY(1), RELATIVE_PRESSURE(2), INTERNAL_ENERGY(3), RELATIVE_VOLUME(4), STANDARD_ENTROPY(5);

        private final int index;

        private GasProperty(int i) {
            this.index = i;
        }
    }

    public static class GasPropertiesRecord {
        public static GasPropertiesRecord create(String[] values) {

            double[] numerics = new double[6];
            for (int i = 0; i < values.length; i++) {
                numerics[i] = NumberUtils.toDouble(values[i]);
            }
            return new GasPropertiesRecord(numerics);
        }

        @Override
        public String toString() {
            String r = "" + values[0];
            for (int i = 1; i < values.length; i++) {
                r += "\t" + values[i];
            }
            return r;
        }

        private final double[] values;

        private GasPropertiesRecord(double[] vals) {
            this.values = vals;
        }

        public double get(GasProperty p) {
            return values[p.index];
        }

        private double getTemperature() {
            return values[GasProperty.TEMPERATURE.index];
        }
    }

    private final String tableName;

    public IdealGasPropertiesTable(String f) {
        this.tableName = f;
        readData();
    }

    private List<GasPropertiesRecord> recordList;

    public List<GasPropertiesRecord> getRecordList() {
        return recordList;
    }

    private void readData() {

        List<String> recordLines;
        recordLines = Utils.getTableLines(tableName);
        
        if (recordLines != null) {
            recordList = new ArrayList<GasPropertiesRecord>();
            for (String line : recordLines) {

                String[] values = line.split(",");
                recordList.add(GasPropertiesRecord.create(values));
            }
        }
    }

    public Object searchGasRecords(GasProperty g, double val) {

        GasPropertiesRecord pr = null, nr = null;
        for (GasPropertiesRecord r : getRecordList()) {

            if (r.get(g) == val) {

                return r;
            } else if (r.get(g) < val) {
                pr = r;
            } else if (r.get(g) > val) {
                nr = r;
            }

            if (pr != null && nr != null) {
                return new GasPropertiesRecord[] { pr, nr };
            }
        }

        return null;
    }

    private double getGasPropertyValue(GasProperty p, double value) {
        return getGasPropertyValue(p, GasProperty.TEMPERATURE, value);
    }

    private double getGasPropertyValue(GasProperty p, final GasProperty g, double value) {

        Collections.sort(getRecordList(), new Comparator<GasPropertiesRecord>() {
            @Override
            public int compare(GasPropertiesRecord o1, GasPropertiesRecord o2) {
                if (o1.get(g) == o2.get(g)) {
                    return 0;
                }
                return o1.get(g) < o2.get(g) ? -1 : 1;
            }
        });

        Object res = searchGasRecords(g, value);

        if (res == null) {
            return Double.NaN;
        }

        if (res instanceof GasPropertiesRecord) {

            return ((GasPropertiesRecord) res).get(p);
        } else {
            GasPropertiesRecord pr = ((GasPropertiesRecord[]) res)[0];
            GasPropertiesRecord nr = ((GasPropertiesRecord[]) res)[1];

            double p1 = pr.get(p);
            double p2 = nr.get(p);

            double g1 = pr.get(g);
            double g2 = nr.get(g);

            return (p2 - p1) / (g2 - g1) * (value - g1) + p1;
        }
    }

    public double getGasEnthalpy(double temperature) {

        return getGasPropertyValue(GasProperty.ENTHALPY, temperature);
    }

    public double getGasTemperature(double enthalpy) {
        return getGasPropertyValue(GasProperty.TEMPERATURE, GasProperty.ENTHALPY, enthalpy);
    }

    public double getGasEntropy(double temperature) {
        return getGasPropertyValue(GasProperty.STANDARD_ENTROPY, temperature);
    }

    public void printData() {

        System.out.println("\nGas Table Properties");
        for (GasPropertiesRecord rec : getRecordList()) {
            System.out.println(rec);
        }
    }

}
