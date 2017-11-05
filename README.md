# Thermal-core

Java framework for modelling and solving thermal systems. It was inspired by the work described in the following papers:

* [Thermoeconomic Optimization for Green Multi-Shaft Gas Turbine Engines](http://www.sciencedirect.com/science/article/pii/S0360544213003186?lipi=urn%3Ali%3Apage%3Ad_flagship3_profile_view_base%3BN9UJoSUcSweCJut%2BCQ5y2A%3D%3D)
* [Exergy Analysis for Greener Gas Turbine Engine Arrangements](https://link.springer.com/article/10.1134%2FS1810232813030090?lipi=urn%3Ali%3Apage%3Ad_flagship3_profile_view_base%3BN9UJoSUcSweCJut%2BCQ5y2A%3D%3D)
* [Sustainable Gas Turbine Power Generation by Adopting Green Control Technologies](http://www.tandfonline.com/doi/full/10.1080/19397038.2013.865812?lipi=urn%3Ali%3Apage%3Ad_flagship3_profile_view_base%3BN9UJoSUcSweCJut%2BCQ5y2A%3D%3D&#.UtRhA2QW3HI)

The framewok is exposed to web through a REST API. Currently, it is used by application [Thermal-visualizer](https://github.com/suhaybabsi/thermal-visualizer) to demonstrate framework full capabilities.

[Try The Visualizer](https://thermal-visualizer.herokuapp.com/visualizer)

## Running Locally

Make sure you have Java and Maven installed.

```sh
$ git clone https://github.com/suhaybabsi/thermal-core.git
$ cd thermal-core
$ mvn install
$ java -jar target/thermal-core-1.0.jar
```

The app should now be running on [localhost:5000](http://localhost:5000/).

## Adding More Devices

All devices are subclasses of [Device](https://github.com/suhaybabsi/thermal-core/blob/master/src/main/java/com/suhaybabsi/thermodesigner/devices/Device.java). You can subclass [SteadyFlowDevice](https://github.com/suhaybabsi/thermal-core/blob/master/src/main/java/com/suhaybabsi/thermodesigner/devices/SteadyFlowDevice.java) if your device have flows of mass streaming in and out (Steady State, Steady Flow "SSSF").

Devices that consume or produce work must implement either [WorkConsumingDevice](https://github.com/suhaybabsi/thermal-core/blob/master/src/main/java/com/suhaybabsi/thermodesigner/core/WorkConsumingDevice.java) or [WorkProducingDevice](https://github.com/suhaybabsi/thermal-core/blob/master/src/main/java/com/suhaybabsi/thermodesigner/core/WorkProducingDevice.java) interface, respectively. Likewise, devices that produce heat must implement [HeatProducingDevice](https://github.com/suhaybabsi/thermal-core/blob/master/src/main/java/com/suhaybabsi/thermodesigner/core/HeatProducingDevice.java).

### Properties

`Property` class is used to define all themophysical properties included in the mathematical model. Use it to define all device-related properties.

For example, the property "compression ratio" of Compressors can defined as follows:

```java
private final Property compressionRatio = new Property("Compression Ratio");
```

### Flows

`Flow` class is used to store all thermodynamics properties related to fluid/gas flow. Devices that have flows streaming in and out of them can access those properties by the use of `FlowManager` class property:

```java
Flow inFlow = flowManager.getIn(0);
Flow outFlow = flowManager.getOut(0);

Property massRateIn = inFlow.getMassRateProp();
Property massRateOut = outFlow.getMassRateProp();
```

### Work

Use `Work` class to store the amount of work, and to define weather the device is consuming or producing that amount.

```java
public Compressor() {
    super("Compressor", "compressor");
    work = new Work(WorkType.CONSUMED);
    ...
```

### Governing Equations

The mathematical model of device should be defined using `ThermalEquation` inteface. See below example for mass conversation.

```java
private static class MassConservationEquation extends ThermalEquation {
    private final Property massIn;
    private final Property massOut;
    public MassConservationEquation(
        Property massIn, Property massOut) {
        super(massIn, massOut);
        setName("Mass Conservation Equation");
        this.massIn = massIn;
        this.massOut = massOut;
    }
    
    @Override
    protected double evaluate() {
        
        double m_in = massIn.getValue();
        double m_out = massOut.getValue();
        return m_in - m_out;
    }   
}
```

After you define all equations classes, override `configureEquations()`  method to create and add instances of those equations to the device model.

```java
@Override
protected void configureEquations() throws ConfigurationException  {
    
    insureSingleFlowFluids();
    
    Flow inFlow = flowManager.getIn(0);
    Flow outFlow = flowManager.getOut(0);
    
    Property massRateIn = inFlow.getMassRateProp();
    Property massRateOut = outFlow.getMassRateProp();
    Fluid gas = inFlow.getFluid();
    
    Property P1 = inFlow.getPressureProp();
    Property P2 = outFlow.getPressureProp();
    
    Property T1 = inFlow.getTemperatureProp();
    Property T2 = outFlow.getTemperatureProp();

    addEquation(new CompressionEquation(compressionRatio, P1, P2));
    addEquation(new PolytropicTemperatureEquation(T1, T2, compressionRatio, polytropicEfficiency, gas));
    addEquation(new WorkEquation(this.work, massRateIn, T1, T2, gas));
    addEquation(new MassConservationEquation(massRateIn, massRateOut));
    
    outFlow.setFluid(Air.getInstance());
}
```

### Contributing

If you'd like to introduce your device into the framework, please consider making a [pull request](https://help.github.com/articles/creating-a-pull-request/)

