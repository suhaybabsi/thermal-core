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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 *
 * @author suhaybal-absi
 */
public class GasChemicalTable {

    private final static double UNIVERSAL_GAS_CONSTANT = 8.31447; // [kJ/kmol.K]

    public static class CriticalPoint {
        private double pressure; //[K]
        private double temperature; //[MPa]
        private double volume; //[m3/kmol]

        private CriticalPoint() {
        }

        public double getPressure() {
            return pressure;
        }

        public double getTemperature() {
            return temperature;
        }

        public double getVolume() {
            return volume;
        }

        @Override
        public String toString() {
            return getTemperature() + "\t" + getPressure() + "\t" + getVolume();
        }
    }

    public static class GasChemicalRecord {

        public static GasChemicalRecord create(String[] values) {

            int j = 0;
            double[] numerics = new double[4];
            for (int i = 2; i < values.length; i++) {
                numerics[j++] = NumberUtils.toDouble(values[i]);
            }

            GasChemicalRecord r = new GasChemicalRecord();

            r.substance = values[0];
            r.formula = values[1];
            r.molarMass = numerics[0];
            r.gasConstant = UNIVERSAL_GAS_CONSTANT / r.molarMass;
            r.criticalPoint = new CriticalPoint();
            r.criticalPoint.temperature = numerics[1];
            r.criticalPoint.pressure = numerics[2];
            r.criticalPoint.volume = numerics[3];

            return r;
        }

        @Override
        public String toString() {
            String r = "";
            r += this.substance;
            r += "\t" + this.formula;
            r += "\t" + this.molarMass;
            r += "\t" + this.gasConstant;
            r += "\t" + this.criticalPoint;
            return r;
        }

        private GasChemicalRecord() {
        }

        private String substance;
        private String formula;
        private double molarMass;

        public String getSubstance() {
            return substance;
        }

        public String getFormula() {
            return formula;
        }

        public double getMolarMass() {
            return molarMass;
        }

        public double getGasConstant() {
            return gasConstant;
        }

        public CriticalPoint getCriticalPoint() {
            return criticalPoint;
        }

        private double gasConstant;
        private CriticalPoint criticalPoint;

    }

    private static List<GasChemicalRecord> gasesChemicalData;

    private static List<GasChemicalRecord> getGasesChemicalData() {

        if (gasesChemicalData == null) {

            List<String> recordLines;
            recordLines = Utils.getTableLines("gases_chemical_props.csv");

            if (recordLines != null) {
                gasesChemicalData = new ArrayList<GasChemicalRecord>();
                for (String line : recordLines) {

                    String[] values = line.split(",");
                    gasesChemicalData.add(GasChemicalRecord.create(values));
                }
            }
        }

        return gasesChemicalData;
    }

    public static void printChemicalData() {

        for (GasChemicalRecord rec : getGasesChemicalData()) {
            System.out.println(rec);
        }
    }

    public static GasChemicalRecord readGasChemicalRecord(String formula) {

        for (GasChemicalRecord r : getGasesChemicalData()) {

            if (r.getFormula().trim().equals(formula)) {
                return r;
            }
        }
        return null;
    }
}
