# Thermal-core

Java framework for modelling and solving thermal systems. Exposed to web through REST API. 

Currently the REST API is used in application [Thermal-visualizer](https://github.com/suhaybabsi/thermal-visualizer) to demonstrate framework capabilities.

## Running Locally

Make sure you have Java and Maven installed.

```sh
$ git clone https://github.com/suhaybabsi/thermal-core.git
$ cd thermal-core
$ mvn install
$ java -jar target/thermal-core-1.0.jar
```

The app should now be running on [localhost:5000](http://localhost:5000/).