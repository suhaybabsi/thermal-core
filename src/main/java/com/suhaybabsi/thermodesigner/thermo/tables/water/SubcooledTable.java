/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.thermo.tables.water;

import com.suhaybabsi.thermodesigner.core.Utils;
import com.suhaybabsi.thermodesigner.thermo.fluid.FlowState;
import com.suhaybabsi.thermodesigner.thermo.tables.Range;
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
public class SubcooledTable {

    public static enum SubcooledProperty {

        TEMPERATURE(0), VOLUME(1), INTERNAL_ENERGY(2), ENTHALPY(3), ENTROPY(4);
        private int index = -1;

        private SubcooledProperty(int i) {
            this.index = i;
        }

        public int index() {
            return index;
        }
    }

    public static class SubcooledRecord {

        private double[] values;

        public static SubcooledRecord create(String[] vals) {
            double[] numerics = new double[5];
            for (int i = 0; i < vals.length; i++) {
                numerics[i] = NumberUtils.toDouble(vals[i]);
            }
            return new SubcooledRecord(numerics);
        }

        public SubcooledRecord(double[] values) {
            this.values = values;
        }

        public double get(SubcooledProperty p) {
            return values[p.index];
        }

        public double getTemperature() {
            return values[SubcooledProperty.TEMPERATURE.index];
        }
    }

    public static class SubcooledSubTable {

        private double pressure;
        private List<SubcooledRecord> recordList = new ArrayList<SubcooledRecord>();

        public double getPressure() {
            return pressure;
        }

        public SubcooledSubTable(double pressure) {
            this.pressure = pressure;
        }

        public void addRecord(SubcooledRecord r) {
            recordList.add(r);
        }

        public List<SubcooledRecord> getRecordList() {
            return recordList;
        }

        private Object searchRecords(SubcooledProperty g, double val) {

            SubcooledRecord pr = null, nr = null;
            for (SubcooledRecord r : getRecordList()) {

                if (r.get(g) == val) {

                    return r;
                } else if (r.get(g) < val) {
                    pr = r;
                } else if (r.get(g) > val) {
                    nr = r;
                }

                if (pr != null && nr != null) {
                    return new SubcooledRecord[] { pr, nr };
                }
            }

            return null;
        }

        private double getPropertyValue(SubcooledProperty p, final SubcooledProperty g, double value) {

            Collections.sort(getRecordList(), new Comparator<SubcooledRecord>() {
                @Override
                public int compare(SubcooledRecord o1, SubcooledRecord o2) {
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

            if (res instanceof SubcooledRecord) {

                return ((SubcooledRecord) res).get(p);
            } else {
                SubcooledRecord pr = ((SubcooledRecord[]) res)[0];
                SubcooledRecord nr = ((SubcooledRecord[]) res)[1];

                double p1 = pr.get(p);
                double p2 = nr.get(p);

                double g1 = pr.get(g);
                double g2 = nr.get(g);

                return (p2 - p1) / (g2 - g1) * (value - g1) + p1;
            }
        }

    }

    private static SubcooledTable _instance;

    public static SubcooledTable getInstance() {
        if (_instance == null) {
            _instance = new SubcooledTable();
        }
        return _instance;
    }

    private List<SubcooledSubTable> pressureTableList;

    private SubcooledTable() {
        readData();
    }

    public void readData() {

        List<String> fileLines = Utils.getTableLines("water_subcooled_props.csv");
        
        if (fileLines != null) {

            pressureTableList = new ArrayList<SubcooledSubTable>();
            SubcooledSubTable currentSubTable = null;

            for (String line : fileLines) {

                if (line.toUpperCase().trim().startsWith("P")) {

                    String rawPressure = line.split("=")[1].trim();
                    double pressure = NumberUtils.toDouble(rawPressure);
                    currentSubTable = new SubcooledSubTable(pressure * 1000);
                    pressureTableList.add(currentSubTable);

                } else if (line.trim().isEmpty()) {

                    currentSubTable = null;
                } else if (currentSubTable != null) {

                    String[] vals = line.split(",");
                    currentSubTable.addRecord(SubcooledRecord.create(vals));
                }
            }
        }
    }

    public List<SubcooledSubTable> getPressureTableList() {

        return pressureTableList;
    }

    public double getPropertyGivenPressure(double pressure, SubcooledProperty p, SubcooledProperty given,
            double value) {

        Collections.sort(getPressureTableList(), new Comparator<SubcooledSubTable>() {
            @Override
            public int compare(SubcooledSubTable o1, SubcooledSubTable o2) {
                if (o1.getPressure() == o2.getPressure()) {
                    return 0;
                }
                return o1.getPressure() < o2.getPressure() ? -1 : 1;
            }
        });

        SubcooledSubTable pt = null, nt = null;
        for (SubcooledSubTable subTable : getPressureTableList()) {

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
        return getPropertyGivenPressure(pressure, SubcooledProperty.ENTHALPY, SubcooledProperty.TEMPERATURE,
                temperature);
    }

    public double getEntropy(double pressure, double temperature) {

        return getPropertyGivenPressure(pressure, SubcooledProperty.ENTROPY, SubcooledProperty.TEMPERATURE,
                temperature);
    }

    public double getVolume(double pressure, double temperature) {
        return getPropertyGivenPressure(pressure, SubcooledProperty.VOLUME, SubcooledProperty.TEMPERATURE, temperature);
    }

    public double getEnthalpy(FlowState state) {

        return getProperty(SubcooledProperty.ENTHALPY, state);
    }

    public double getEntropy(FlowState state) {

        return getProperty(SubcooledProperty.ENTROPY, state);
    }

    public double getVolume(FlowState state) {

        return getProperty(SubcooledProperty.VOLUME, state);
    }

    public double getTemperature(FlowState state) {

        return getProperty(SubcooledProperty.TEMPERATURE, state);
    }

    private double getProperty(SubcooledProperty target_prop, FlowState state) {

        double pressure = state.pressure();

        SubcooledProperty given_prop = null;
        double given_value = Double.NaN;
        if (state.isTemperatureValid()) {

            given_prop = SubcooledProperty.TEMPERATURE;
            given_value = state.temperature();

        } else if (state.isEnergyValid()) {

            given_prop = SubcooledProperty.INTERNAL_ENERGY;
            given_value = state.energy();

        } else if (state.isEnthalpyValid()) {

            given_prop = SubcooledProperty.ENTHALPY;
            given_value = state.enthalpy();

        } else if (state.isEntropyValid()) {

            given_prop = SubcooledProperty.ENTROPY;
            given_value = state.entropy();

        } else if (state.isVolumeValid()) {

            given_prop = SubcooledProperty.VOLUME;
            given_value = state.volume();
        }

        if (given_prop != null && Double.isNaN(given_value) == false) {

            return getPropertyGivenPressure(pressure, target_prop, given_prop, given_value);
        }
        return Double.NaN;
    }

    public static Range getTemperatureRange() {
        return new Range(0, 380);
    }

}
