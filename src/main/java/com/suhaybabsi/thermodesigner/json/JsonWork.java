/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.json;

import com.suhaybabsi.thermodesigner.core.TDJSONObject;
import com.suhaybabsi.thermodesigner.core.ThermalSystem;
import com.suhaybabsi.thermodesigner.core.ThermalSystemType;
import com.suhaybabsi.thermodesigner.core.Work;
import com.suhaybabsi.thermodesigner.devices.AirFlow;
import com.suhaybabsi.thermodesigner.devices.Boiler;
import com.suhaybabsi.thermodesigner.devices.Burner;
import com.suhaybabsi.thermodesigner.devices.ClosedFeedWaterHeater;
import com.suhaybabsi.thermodesigner.devices.Compressor;
import com.suhaybabsi.thermodesigner.devices.Condenser;
import com.suhaybabsi.thermodesigner.devices.CrossPoint;
import com.suhaybabsi.thermodesigner.devices.Device;
import com.suhaybabsi.thermodesigner.devices.Diffuser;
import com.suhaybabsi.thermodesigner.devices.Exhaust;
import com.suhaybabsi.thermodesigner.devices.OpenFeedWaterHeater;
import com.suhaybabsi.thermodesigner.devices.Generator;
import com.suhaybabsi.thermodesigner.devices.GGHeatExchanger;
import com.suhaybabsi.thermodesigner.devices.GasIntercooler;
import com.suhaybabsi.thermodesigner.devices.Intake;
import com.suhaybabsi.thermodesigner.devices.MechanicalMachine;
import com.suhaybabsi.thermodesigner.devices.Shaft;
import com.suhaybabsi.thermodesigner.devices.SteadyFlowDevice;
import com.suhaybabsi.thermodesigner.devices.GasTurbine;
import com.suhaybabsi.thermodesigner.devices.MixingChamber;
import com.suhaybabsi.thermodesigner.devices.Nozzle;
import com.suhaybabsi.thermodesigner.devices.Pipe;
import com.suhaybabsi.thermodesigner.devices.Pump;
import com.suhaybabsi.thermodesigner.devices.SteamTurbine;
import com.suhaybabsi.thermodesigner.thermo.Flow;
import com.suhaybabsi.thermodesigner.thermo.fluid.Fluid;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

/**
 *
 * @author suhaybal-absi
 */
public class JsonWork {

    public static JSONObject generateJsonResults(ThermalSystem system) {

        JSONObject results;

        try {

            results = new JSONObject();
            JSONArray devices = new JSONArray();
            for (int i = 0; i < system.getDeviceList().size(); i++) {

                Device device = system.getDeviceList().get(i);
                if ((device instanceof Shaft) == false && (device instanceof Pipe) == false) {

                    JSONObject jsonDvc = device.getJSONResults(system.isExergyEnabled());

                    if (jsonDvc == null) {
                        jsonDvc = new JSONObject();
                    }

                    devices.put(jsonDvc);
                }
            }

            JSONArray shafts = new JSONArray();
            for (int i = 0; i < system.getDeviceList().size(); i++) {

                Device device = system.getDeviceList().get(i);
                if (device instanceof Shaft) {
                    Shaft shaft = (Shaft) device;

                    JSONArray shaftJson = new JSONArray();
                    for (int w = 0; w < shaft.getWorkList().size(); w++) {

                        Work work = shaft.getWorkList().get(w);
                        int sign = (work.isConsumed()) ? -1 : +1;
                        double val = sign * work.getValue();

                        shaftJson.put(val);
                    }
                    shafts.put(shaftJson);
                }
            }

            //System.out.println(devices);

            JSONArray flows = new JSONArray();
            for (Flow flow : system.getFlowList()) {

                Device dvc1 = flow.getStart();
                Device dvc2 = flow.getEnd();

                int si = system.getDeviceList().indexOf(dvc1);
                int ei = system.getDeviceList().indexOf(dvc2);

                JSONArray id = new JSONArray();
                id.put(si);
                id.put(ei);

                flow.updateEnthalpy();
                flow.updateEntropy();

                //System.out.println("s: "+ flow.getEntropy() + ", h:"+flow.getEnthalpy());

                JSONObject jp = new TDJSONObject();
                jp.put("m", flow.getMassRate());
                jp.put("p", flow.getPressure());
                jp.put("t", flow.getTemperature());
                jp.put("s", flow.getEntropy());
                jp.put("h", flow.getEnthalpy());

                if (system.isExergyEnabled()) {
                    jp.put("x", flow.getExergy());
                }

                JSONObject jf = new JSONObject();
                jf.put("id", id);
                jf.put("type", "stream");
                jf.put("props", jp);
                flows.put(jf);
            }

            for (Pipe pipe : system.getPipeList()) {

                Device dvc1 = pipe.getInletDevice();
                Device dvc2 = pipe.getOutletDevice();

                int si = system.getDeviceList().indexOf(dvc1);
                int ei = system.getDeviceList().indexOf(dvc2);

                JSONArray id = new JSONArray();
                id.put(si);
                id.put(ei);

                JSONObject jp = new TDJSONObject();
                Flow[] ends = { pipe.getStartState(), pipe.getEndState() };
                JSONObject[] jends = new JSONObject[2];

                for (int i = 0; i < ends.length; i++) {
                    Flow end = ends[i];

                    end.updateEnthalpy();
                    end.updateEntropy();

                    JSONObject jend = new TDJSONObject();
                    jend.put("m", end.getMassRate());
                    jend.put("p", end.getPressure());
                    jend.put("t", end.getTemperature());
                    jend.put("vf", end.getVapourFraction());
                    jend.put("s", end.getEntropy());
                    jend.put("h", end.getEnthalpy());

                    if (system.isExergyEnabled()) {
                        jend.put("x", end.getExergy());
                    }

                    jends[i] = jend;
                }

                jp.put("inlet", jends[0]);
                jp.put("outlet", jends[1]);

                JSONObject jf = new JSONObject();
                jf.put("id", id);
                jf.put("type", "flow");
                jf.put("props", jp);

                flows.put(jf);
            }

            results.put("devices", devices);
            results.put("flows", flows);
            results.put("shafts", shafts);

            JSONObject performance = new TDJSONObject();
            ThermalSystemType type = system.getType();
            type = (type == null) ? ThermalSystemType.NO_SYSTEM : type;

            switch (type) {
            case GAS_TURBINE: {
                performance.put("wnet", system.getNetWork());
                performance.put("sfc", system.getSFC());
                performance.put("fc", system.getFuelConsumption());
                performance.put("nth", system.getEfficiency());
            }
                break;
            case TURBOJET: {
                performance.put("thrust", system.getThrust());
                performance.put("tsfc", system.getTSFC());
            }
                break;
            case STEAM_TURBINE: {
                performance.put("wnet", system.getNetWork());
                performance.put("nth", system.getEfficiency());
            }
                break;
            }

            results.put("type", type.getJson());
            results.put("performance", performance);

            return results;
        } catch (JSONException ex) {
            Logger.getLogger(JsonWork.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            results = new JSONObject();
            results.put("error", "System model isn't well represented");
            return results;
        } catch (JSONException ex) {
            Logger.getLogger(JsonWork.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static ThermalSystem parseModel(String rawData) {

        JSONObject obj;
        try {

            obj = new JSONObject(rawData);
            return parseModel(obj);
        } catch (JSONException ex) {
            Logger.getLogger(JsonWork.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static ThermalSystem parseModel(JSONObject obj) {

        ThermalSystem system = new ThermalSystem();

        try {

            JSONArray devices = obj.getJSONArray("devices");
            for (int i = 0; i < devices.length(); i++) {
                JSONObject device = devices.getJSONObject(i);
                Device real_device = parseDevice(device);
                if (real_device != null) {
                    system.addDevice(real_device);
                }

            }

            ArrayList<Device> connectionDevices = new ArrayList<Device>();
            JSONArray flows = obj.getJSONArray("flows");

            for (int i = 0; i < flows.length(); i++) {

                JSONObject jf = flows.getJSONObject(i);
                String type = jf.getString("type");

                JSONObject fromObj = jf.getJSONObject("from");
                JSONObject toObj = jf.getJSONObject("to");

                int si = fromObj.getInt("d");
                int ei = toObj.getInt("d");

                Device dvc1, dvc2;
                dvc1 = system.getDeviceList().get(si);
                dvc2 = system.getDeviceList().get(ei);

                if (type.equals("stream")) {
                    system.createFlow((SteadyFlowDevice) dvc1, (SteadyFlowDevice) dvc2);
                } else {

                    Pipe pipe = system.createPipe((SteadyFlowDevice) dvc1, (SteadyFlowDevice) dvc2);
                    JSONObject input = jf.getJSONObject("props");
                    pipe.setData(input);
                    parsePipeData(pipe, input);
                    connectionDevices.add(pipe);
                }
            }

            JSONArray shafts = obj.getJSONArray("shafts");
            for (int i = 0; i < shafts.length(); i++) {
                JSONArray js = shafts.getJSONArray(i);

                Shaft shaft = new Shaft();
                for (int j = 0; j < js.length(); j++) {

                    JSONObject cple = js.getJSONObject(j);

                    int di = cple.getInt("d");
                    int ci = cple.getInt("c");

                    MechanicalMachine dvc = (MechanicalMachine) system.getDeviceList().get(di);
                    dvc.connectShaft(shaft);
                }
                connectionDevices.add(shaft);
            }

            system.addAllDevices(connectionDevices);
        } catch (JSONException ex) {
            Logger.getLogger(JsonWork.class.getName()).log(Level.SEVERE, null, ex);
        }

        return system;
    }

    private static Device parseDevice(JSONObject dvc) throws JSONException {
        String type = dvc.getString("type");

        System.out.println("PARSING DEVICE: " + type);

        JSONObject prps;
        if (dvc.isNull("props") == false) {
            prps = dvc.getJSONObject("props");
        } else {
            prps = new JSONObject();
        }

        Device device = null;
        if ("intake".equals(type)) {
            device = parseIntake(prps);
        } else if ("compressor".equals(type)) {
            device = parseCompressor(prps);
        } else if ("burner".equals(type)) {
            device = parseBurner(prps);
        } else if ("gas_turbine".equals(type)) {
            device = parseTurbine(prps);
        } else if ("exhaust".equals(type)) {
            device = parseExhaust(prps);
        } else if ("generator".equals(type)) {
            device = parseGenerator(prps);
        } else if ("extraction".equals(type)) {
            device = new CrossPoint();
        } else if ("heat_exchanger".equals(type)) {
            device = parseHeatExchanger(prps);
        } else if ("intercooler".equals(type)) {
            device = parseIntercooler(prps);
        } else if ("pump".equals(type)) {
            device = parsePump(prps);
        } else if ("condenser".equals(type)) {
            device = parseCondenser(prps);
        } else if ("steam_turbine".equals(type)) {
            device = parseSteamTurbine(prps);
        } else if ("boiler".equals(type)) {
            device = parseBoiler(prps);
        } else if ("air_flow".equals(type)) {
            device = parseAirFlow(prps);
        } else if ("diffuser".equals(type)) {
            device = parseDiffuser(prps);
        } else if ("nozzle".equals(type)) {
            device = parseNozzle(prps);
        } else if ("open_feed_heater".equals(type)) {
            device = parseOpenFeedHeater(prps);
        } else if ("closed_feed_heater".equals(type)) {
            device = parseClosedFeedHeater(prps);
        } else if ("mixing_chamber".equals(type)) {
            device = parseMixingChamber(prps);
        }

        if (device != null) {
            device.setData(prps);
        }
        
        return device;
    }

    private static Intake parseIntake(JSONObject jp) throws JSONException {

        Intake intake = new Intake();

        if (jp.isNull("p") == false) {
            intake.definePressure(jp.getDouble("p"));
        }
        if (jp.isNull("t") == false) {
            intake.defineTemperature(jp.getDouble("t"));
        }
        if (jp.isNull("m") == false) {
            intake.defineMassRate(jp.getDouble("m"));
        }
        if (jp.isNull("g") == false) {
            intake.setFluid(Fluid.getFluid(jp.getString("g")));
        }

        return intake;
    }

    private static Device parseCompressor(JSONObject jp) throws JSONException {
        Compressor comp = new Compressor();

        if (jp.isNull("r") == false) {
            comp.defineCompressionRatio(jp.getDouble("r"));
        }
        if (jp.isNull("wc") == false) {
            comp.defineWorkConsumed(jp.getDouble("wc"));
        }
        if (jp.isNull("nc") == false) {
            comp.definePolytropicEfficiency(jp.getDouble("nc"));
        }

        return comp;
    }

    private static Device parseBurner(JSONObject jp) throws JSONException {
        Burner burner = new Burner();

        if (jp.isNull("et") == false) {
            burner.defineExitTemperature(jp.getDouble("et"));
        }
        if (jp.isNull("pl") == false) {
            burner.definePressureLoss(jp.getDouble("pl"));
        }
        if (jp.isNull("nb") == false) {
            burner.defineCombustionEfficiency(jp.getDouble("nb"));
        }
        if (jp.isNull("fa") == false) {
            burner.defineFuelAirRatio(jp.getDouble("fa"));
        }
        if (jp.isNull("mf") == false) {
            /*burner.defineExitTemperature(jp.getDouble("mf"));*/ }

        return burner;
    }

    private static Device parseTurbine(JSONObject jp) throws JSONException {
        GasTurbine turbine = new GasTurbine();

        if (jp.isNull("pr") == false) {
            turbine.definePressureRatio(jp.getDouble("pr"));
        }
        if (jp.isNull("wt") == false) {
            turbine.defineWorkProduced(jp.getDouble("wt"));
        }
        if (jp.isNull("nt") == false) {
            turbine.definePolytropicEfficiency(jp.getDouble("nt"));
        }

        return turbine;
    }

    private static Device parseExhaust(JSONObject jp) throws JSONException {
        Exhaust exhaust = new Exhaust();

        if (jp.isNull("p") == false) {
            exhaust.definePressure(jp.getDouble("p"));
        }
        if (jp.isNull("t") == false) {
            exhaust.defineTemperature(jp.getDouble("t"));
        }
        if (jp.isNull("m") == false) {
            exhaust.defineMassRate(jp.getDouble("m"));
        }

        return exhaust;
    }

    private static Device parseHeatExchanger(JSONObject jp) throws JSONException {
        GGHeatExchanger heatExchanger = new GGHeatExchanger();

        if (jp.isNull("ef") == false) {
            heatExchanger.defineEffectiveness(jp.getDouble("ef"));
        }
        if (jp.isNull("pl1") == false) {
            heatExchanger.defineSide1PressureLoss(jp.getDouble("pl1"));
        }
        if (jp.isNull("pl2") == false) {
            heatExchanger.defineSide2PressureLoss(jp.getDouble("pl2"));
        }

        return heatExchanger;
    }

    private static Device parseIntercooler(JSONObject jp) throws JSONException {
        GasIntercooler intercooler = new GasIntercooler();

        if (jp.isNull("et") == false) {
            intercooler.defineExitFlowTemperature(jp.getDouble("et"));
        }
        if (jp.isNull("pl") == false) {
            intercooler.definePressureLoss(jp.getDouble("pl"));
        }
        if (jp.isNull("he") == false) {
            intercooler.defineHeatExtracted(jp.getDouble("he"));
        }

        return intercooler;
    }

    private static Device parseGenerator(JSONObject jp) throws JSONException {
        Generator generator = new Generator();

        if (jp.isNull("w") == false) {
            generator.defineConsumedWork(jp.getDouble("w"));
        }

        return generator;
    }

    private static Device parsePump(JSONObject jp) throws JSONException {

        Pump pump = new Pump();

        if (jp.isNull("mp") == false) {
            pump.defineMassFlowRate(jp.getDouble("mp"));
        }
        if (jp.isNull("ep") == false) {
            pump.defineExitPressure(jp.getDouble("ep"));
        }
        if (jp.isNull("np") == false) {
            pump.defineIsentropicEfficiency(jp.getDouble("np"));
        }
        if (jp.isNull("wp") == false) {
            pump.defineWorkConsumed(jp.getDouble("wp"));
        }

        return pump;
    }

    private static Device parseCondenser(JSONObject jp) throws JSONException {

        Condenser condenser = new Condenser();

        if (jp.isNull("he") == false) {
            condenser.defineHeatExtracted(jp.getDouble("he"));
        }
        if (jp.isNull("pl") == false) {
            condenser.definePressureLoss(jp.getDouble("pl"));
        }
        if (jp.isNull("sa") == false) {
            condenser.defineSubcoolingAmount(jp.getDouble("sa"));
        }

        return condenser;

    }

    private static Device parseSteamTurbine(JSONObject jp) throws JSONException {

        SteamTurbine turbine = new SteamTurbine();

        if (jp.isNull("ep") == false) {
            turbine.defineExhaustPressure(jp.getDouble("ep"));
        }
        if (jp.isNull("nt") == false) {
            turbine.defineIsentropicEfficiency(jp.getDouble("nt"));
        }
        if (jp.isNull("wt") == false) {
            turbine.defineWorkProduced(jp.getDouble("wt"));
        }

        return turbine;
    }

    private static Device parseBoiler(JSONObject jp) throws JSONException {

        Boiler boiler = new Boiler();

        if (jp.isNull("et") == false) {
            boiler.defineExitTemperature(jp.getDouble("et"));
        }
        if (jp.isNull("hp") == false) {
            boiler.defineHeatProduced(jp.getDouble("hp"));
        }
        if (jp.isNull("pl") == false) {
            boiler.definePressureLoss(jp.getDouble("pl"));
        }

        return boiler;
    }

    private static void parsePipeData(Pipe pipe, JSONObject jp) throws JSONException {

        if (jp.isNull("m") == false) {
            pipe.defineMassRate(jp.getDouble("m"));
        }
        if (jp.isNull("hl") == false) {
            pipe.defineEnthalpyLoss(jp.getDouble("hl"));
        }
        if (jp.isNull("pl") == false) {
            pipe.definePressureLoss(jp.getDouble("pl"));
        }
        if (jp.isNull("f") == false) {
            pipe.setFluid(Fluid.getFluid(jp.getString("f")));
        }

    }

    private static Device parseAirFlow(JSONObject jp) throws JSONException {

        AirFlow airFlow = new AirFlow();

        if (jp.isNull("alt") == false) {
            airFlow.defineAltitude(jp.getDouble("alt"));
        }
        if (jp.isNull("mach") == false) {
            airFlow.defineMachNumber(jp.getDouble("mach"));
        }
        if (jp.isNull("m") == false) {
            airFlow.defineMassFlowRate(jp.getDouble("m"));
        }

        return airFlow;
    }

    private static Device parseDiffuser(JSONObject jp) throws JSONException {

        Diffuser diffuser = new Diffuser();

        if (jp.isNull("hl") == false) {
            diffuser.defineHeatLoss(jp.getDouble("hl"));
        }
        if (jp.isNull("pl") == false) {
            diffuser.definePressureLoss(jp.getDouble("pl"));
        }

        return diffuser;
    }

    private static Device parseNozzle(JSONObject jp) throws JSONException {

        Nozzle nozzle = new Nozzle();

        if (jp.isNull("pl") == false) {
            nozzle.definePressureLoss(jp.getDouble("pl"));
        }
        if (jp.isNull("esp") == false) {
            nozzle.defineExhaustStaticPressure(jp.getDouble("esp"));
        }
        if (jp.isNull("est") == false) {
            nozzle.defineExhaustStaticTemperature(jp.getDouble("est"));
        }
        if (jp.isNull("em") == false) {
            nozzle.defineExhaustMachNumber(jp.getDouble("em"));
        }
        if (jp.isNull("hl") == false) {
            nozzle.defineHeatLoss(jp.getDouble("hl"));
        }

        return nozzle;
    }

    private static Device parseOpenFeedHeater(JSONObject jp) throws JSONException {
        OpenFeedWaterHeater feedHeater = new OpenFeedWaterHeater();

        if (jp.isNull("p") == false) {
            feedHeater.definePressure(jp.getDouble("p"));
        }

        return feedHeater;
    }

    private static Device parseClosedFeedHeater(JSONObject jp) {

        ClosedFeedWaterHeater feedHeater = new ClosedFeedWaterHeater();
        return feedHeater;
    }

    private static Device parseMixingChamber(JSONObject jp) throws JSONException {

        MixingChamber mixingChamber = new MixingChamber();

        if (jp.isNull("p") == false) {
            mixingChamber.definePressure(jp.getDouble("p"));
        }

        return mixingChamber;
    }
}
