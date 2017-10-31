/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.tables.water;

import com.suhaybabsi.thermodesigner.core.Utils;
import com.suhaybabsi.thermodesigner.thermo.fluid.FlowState;
import java.io.File;
import java.io.FileNotFoundException;
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
public class SuperheatedTable {

    public static enum SuperheatedProperty {

        TEMPERATURE(0), VOLUME(1), INTERNAL_ENERGY(2), ENTHALPY(3), ENTROPY(4);
        private int index = -1;

        private SuperheatedProperty(int i) {
            this.index = i;
        }

        public int index() {
            return index;
        }
    }

    public static class SuperheatedRecord {

        private double[] values;

        public static SuperheatedRecord create(String[] vals) {
            double[] numerics = new double[5];
            for (int i = 0; i < vals.length; i++) {
                numerics[i] = NumberUtils.toDouble(vals[i]);
            }
            return new SuperheatedRecord(numerics);
        }

        public SuperheatedRecord(double[] values) {
            this.values = values;
        }

        public double get(SuperheatedProperty p) {
            return values[p.index];
        }

        public double getTemperature() {
            return values[SuperheatedProperty.TEMPERATURE.index];
        }
    }

    public static class SuperheatedSubTable {

        private double pressure;
        private List<SuperheatedRecord> recordList = new ArrayList<SuperheatedRecord>();

        public double getPressure() {
            return pressure;
        }

        public SuperheatedSubTable(double pressure) {
            this.pressure = pressure;
        }

        public void addRecord(SuperheatedRecord r) {
            recordList.add(r);
        }

        public List<SuperheatedRecord> getRecordList() {
            return recordList;
        }

        private Object searchRecords(SuperheatedProperty g, double val) {

            SuperheatedRecord pr = null, nr = null;
            for (SuperheatedRecord r : getRecordList()) {

                if (r.get(g) == val) {

                    return r;
                } else if (r.get(g) < val) {
                    pr = r;
                } else if (r.get(g) > val) {
                    nr = r;
                }

                if (pr != null && nr != null) {
                    return new SuperheatedRecord[] { pr, nr };
                }
            }

            return null;
        }

        private double getPropertyValue(SuperheatedProperty p, final SuperheatedProperty g, double value) {

            Collections.sort(getRecordList(), new Comparator<SuperheatedRecord>() {
                @Override
                public int compare(SuperheatedRecord o1, SuperheatedRecord o2) {
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

            if (res instanceof SuperheatedRecord) {

                return ((SuperheatedRecord) res).get(p);
            } else {
                SuperheatedRecord pr = ((SuperheatedRecord[]) res)[0];
                SuperheatedRecord nr = ((SuperheatedRecord[]) res)[1];

                double p1 = pr.get(p);
                double p2 = nr.get(p);

                double g1 = pr.get(g);
                double g2 = nr.get(g);

                return (p2 - p1) / (g2 - g1) * (value - g1) + p1;
            }
        }

    }

    private static SuperheatedTable _instance;

    public static SuperheatedTable getInstance() {
        if (_instance == null) {
            _instance = new SuperheatedTable();
        }
        return _instance;
    }

    private List<SuperheatedSubTable> pressureTableList;

    private SuperheatedTable() {

        readData();
    }

    public void readData() {

        List<String> fileLines = Utils.getTableLines("water_superheated_props.csv");
        
        if (fileLines != null) {
            pressureTableList = new ArrayList<SuperheatedSubTable>();

            SuperheatedSubTable currentSubTable = null;
            for (String line : fileLines) {

                if (line.toUpperCase().trim().startsWith("P")) {

                    String rawPressure = line.split("=")[1].trim();
                    double pressure = NumberUtils.toDouble(rawPressure);
                    currentSubTable = new SuperheatedSubTable(pressure * 1000);
                    pressureTableList.add(currentSubTable);

                } else if (line.trim().isEmpty()) {

                    currentSubTable = null;
                } else if (currentSubTable != null) {

                    String[] vals = line.split(",");
                    currentSubTable.addRecord(SuperheatedRecord.create(vals));
                }
            }
        }
    }

    public List<SuperheatedSubTable> getPressureTableList() {

        return pressureTableList;
    }

    public double getPropertyGivenPressure(double pressure, SuperheatedProperty p, SuperheatedProperty given,
            double value) {

        Collections.sort(getPressureTableList(), new Comparator<SuperheatedSubTable>() {
            @Override
            public int compare(SuperheatedSubTable o1, SuperheatedSubTable o2) {
                if (o1.getPressure() == o2.getPressure()) {
                    return 0;
                }
                return o1.getPressure() < o2.getPressure() ? -1 : 1;
            }
        });

        SuperheatedSubTable pt = null, nt = null;
        for (SuperheatedSubTable subTable : getPressureTableList()) {

            if (subTable.getPressure() == pressure) {

                return subTable.getPropertyValue(p, given, value);
            } else if (subTable.getPressure() < pressure) {

                pt = subTable;
            } else if (subTable.getPressure() > pressure) {

                nt = subTable;
            }

            if (pt != null && nt != null) {

                double v1 = pt.getPropertyValue(p, given, value);
                double v2 = nt.getPropertyValue(p, given, value);

                double p1 = pt.getPressure();
                double p2 = nt.getPressure();

                return (v2 - v1) / (p2 - p1) * (pressure - p1) + v1;
            }
        }

        return Double.NaN;
    }

    public double getEnthalpy(double pressure, double temperature) {
        return getPropertyGivenPressure(pressure, SuperheatedProperty.ENTHALPY, SuperheatedProperty.TEMPERATURE,
                temperature);
    }

    public double getEntropy(double pressure, double temperature) {
        return getPropertyGivenPressure(pressure, SuperheatedProperty.ENTROPY, SuperheatedProperty.TEMPERATURE,
                temperature);
    }

    public double getVolume(double pressure, double temperature) {
        return getPropertyGivenPressure(pressure, SuperheatedProperty.VOLUME, SuperheatedProperty.TEMPERATURE,
                temperature);
    }

    public double getEnthalpy(FlowState state) {

        return getProperty(SuperheatedProperty.ENTHALPY, state);
    }

    public double getEntropy(FlowState state) {

        return getProperty(SuperheatedProperty.ENTROPY, state);
    }

    public double getVolume(FlowState state) {

        return getProperty(SuperheatedProperty.VOLUME, state);
    }

    public double getTemperature(FlowState state) {

        return getProperty(SuperheatedProperty.TEMPERATURE, state);
    }

    private double getProperty(SuperheatedProperty target_prop, FlowState state) {

        double pressure = state.pressure();

        SuperheatedProperty given_prop = null;
        double given_value = Double.NaN;
        if (state.isTemperatureValid()) {

            given_prop = SuperheatedProperty.TEMPERATURE;
            given_value = state.temperature();

        } else if (state.isEnergyValid()) {

            given_prop = SuperheatedProperty.INTERNAL_ENERGY;
            given_value = state.energy();

        } else if (state.isEnthalpyValid()) {

            given_prop = SuperheatedProperty.ENTHALPY;
            given_value = state.enthalpy();

        } else if (state.isEntropyValid()) {

            given_prop = SuperheatedProperty.ENTROPY;
            given_value = state.entropy();

        } else if (state.isVolumeValid()) {

            given_prop = SuperheatedProperty.VOLUME;
            given_value = state.volume();
        }

        if (given_prop != null && Double.isNaN(given_value) == false) {

            return getPropertyGivenPressure(pressure, target_prop, given_prop, given_value);
        }
        return Double.NaN;
    }

}
