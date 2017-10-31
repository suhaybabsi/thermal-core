package com.suhaybabsi.thermodesigner.thermo;

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
public class Atmosphere {

    public static final double STAGNATION_TEMP = 298.0;
    public static final double STAGNATION_PRESS = 101.325;

    public static enum AtmosphereProperty {

        ALTITUDE(0), TEMPERATURE(1), PRESSURE(2), GRAVITY(3), SOUND_SPEED(4), DENSITY(5), VISCOSITY(
                6), THERMAL_CONDUCTIVITY(7);

        private final int index;

        private AtmosphereProperty(int i) {
            this.index = i;
        }

        public int index() {
            return index;
        }
    }

    public static class AtmosphereRecord {

        private final double[] values;

        public static AtmosphereRecord create(String[] vals) {

            double[] numerics = new double[Atmosphere.AtmosphereProperty.values().length];
            for (int i = 0; i < vals.length; i++) {

                numerics[i] = NumberUtils.toDouble(vals[i]);
            }
            return new AtmosphereRecord(numerics);
        }

        private AtmosphereRecord(double[] values) {
            this.values = values;
        }

        public double get(Atmosphere.AtmosphereProperty p) {
            return values[p.index()];
        }
    }

    private static Object searchRecords(Atmosphere.AtmosphereProperty g, double val) {

        Atmosphere.AtmosphereRecord pr = null, nr = null;
        for (Atmosphere.AtmosphereRecord r : getRecordList()) {

            if (r.get(g) == val) {

                return r;
            } else if (r.get(g) < val) {
                pr = r;
            } else if (r.get(g) > val) {
                nr = r;
            }

            if (pr != null && nr != null) {
                return new Atmosphere.AtmosphereRecord[] { pr, nr };
            }
        }

        return null;
    }

    private static double getPropertyValue(Atmosphere.AtmosphereProperty p, double value) {
        return getPropertyValue(p, Atmosphere.AtmosphereProperty.ALTITUDE, value);
    }

    public static double getPropertyValue(Atmosphere.AtmosphereProperty p, final Atmosphere.AtmosphereProperty g,
            double value) {

        Collections.sort(getRecordList(), new Comparator<Atmosphere.AtmosphereRecord>() {
            @Override
            public int compare(Atmosphere.AtmosphereRecord o1, Atmosphere.AtmosphereRecord o2) {
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

        if (res instanceof Atmosphere.AtmosphereRecord) {

            return ((Atmosphere.AtmosphereRecord) res).get(p);
        } else {
            Atmosphere.AtmosphereRecord pr = ((Atmosphere.AtmosphereRecord[]) res)[0];
            Atmosphere.AtmosphereRecord nr = ((Atmosphere.AtmosphereRecord[]) res)[1];

            double p1 = pr.get(p);
            double p2 = nr.get(p);

            double g1 = pr.get(g);
            double g2 = nr.get(g);

            return (p2 - p1) / (g2 - g1) * (value - g1) + p1;
        }
    }

    private static List<AtmosphereRecord> recordList;

    private static List<AtmosphereRecord> getRecordList() {

        if (recordList == null) {

            List<String> recordLines = Utils.getTableLines("atmosphere_props.csv");
            if (recordLines != null) {
                recordList = new ArrayList<AtmosphereRecord>();
                for (String line : recordLines) {

                    String[] values = line.split(",");
                    recordList.add(AtmosphereRecord.create(values));
                }
            }
        }

        return recordList;
    }

    public static double getPressureAtAltitude(double altitude) {
        return getPropertyValue(AtmosphereProperty.PRESSURE, altitude);
    }

    public static double getTemperatureAtAltitude(double altitude) {
        return getPropertyValue(AtmosphereProperty.TEMPERATURE, altitude);
    }

    public static double getSoundSpeedAtAltitude(double altitude) {
        return getPropertyValue(AtmosphereProperty.SOUND_SPEED, altitude);
    }

    public static void printData() {

        for (AtmosphereRecord rec : getRecordList()) {
            System.out.println(rec);
        }
    }

}
