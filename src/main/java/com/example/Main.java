/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.suhaybabsi.thermodesigner.ModelCalculation;
import com.suhaybabsi.thermodesigner.core.CalculationException;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.ThermalSystem;
import com.suhaybabsi.thermodesigner.core.Utils;
import com.suhaybabsi.thermodesigner.json.JsonWork;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.sql.DataSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping("/")
  String index() {
    return "index";
  }

  private static final String template = "Hello, %s!";
  private final AtomicLong counter = new AtomicLong();

  @RequestMapping(value = "/sayHello", method = RequestMethod.GET)
  @ResponseBody
  public Greeting sayHello(@RequestParam(value = "name", required = false, defaultValue = "Stranger") String name) {
    return new Greeting(counter.incrementAndGet(), String.format(template, name));
  }

  @CrossOrigin
  @RequestMapping(value = "/calculate", method = RequestMethod.POST, produces = "application/json")
  public ResponseEntity<String> calculate(@RequestBody String rawModel) {

    final HttpHeaders httpHeaders= new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    
    try {

      System.out.println("MODEL RECEIVED:");
      System.out.println(rawModel);

      JSONObject jsonModel = new JSONObject(rawModel);
      ThermalSystem system = JsonWork.parseModel(jsonModel);

      system.configure();
      system.solve();
      system.calculateExergy();

      String responseJSON = JsonWork.generateJsonResults(system).toString();
      return new ResponseEntity<String>(responseJSON, httpHeaders, HttpStatus.OK);

    } catch (ConfigurationException ex) {
      ex.printStackTrace();
      Logger.getLogger(ModelCalculation.class.getName()).log(Level.SEVERE, null, ex);
    } catch (CalculationException ex) {
      ex.printStackTrace();
      Logger.getLogger(ModelCalculation.class.getName()).log(Level.SEVERE, null, ex);
    } catch (JSONException ex) {
      ex.printStackTrace();
      Logger.getLogger(ModelCalculation.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NullPointerException ex) {
      ex.printStackTrace();
      Logger.getLogger(ModelCalculation.class.getName()).log(Level.SEVERE, null, ex);
    }

    String errorJSON = "{\"error\": \"Invalid system !\"}";
    return new ResponseEntity<String>(errorJSON, httpHeaders, HttpStatus.OK);
  }

  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
      ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        output.add("Read from DB: " + rs.getTimestamp("tick"));
      }

      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }
}

/* For testing
{"devices":[{"type":"intake","props":{"p":101.325,"t":288,"m":1,"g":"air"}},{"type":"compressor","props":{"r":4}},{"type":"burner","props":{"et":1100,"pl":0.03,"nb":0.99,"fl":"diesel"}},{"type":"gas_turbine"},{"type":"exhaust","props":{"p":102}},{"type":"generator"}],"flows":[{"from":{"d":0,"o":0},"to":{"d":1,"o":0}},{"from":{"d":1,"o":1},"to":{"d":2,"o":0}},{"from":{"d":2,"o":1},"to":{"d":3,"o":0}},{"from":{"d":3,"o":1},"to":{"d":4,"o":0}}],"shafts":[[{"d":1,"c":0},{"d":3,"c":0},{"d":5,"c":0}]]}
*/