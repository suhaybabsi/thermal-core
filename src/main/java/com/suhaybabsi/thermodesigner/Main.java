package com.suhaybabsi.thermodesigner;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.suhaybabsi.thermodesigner.core.CalculationException;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.core.ThermalSystem;
import com.suhaybabsi.thermodesigner.json.JsonWork;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class Main {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping("/")
  String index() {
    return "index";
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
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    } catch (CalculationException ex) {
      ex.printStackTrace();
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    } catch (JSONException ex) {
      ex.printStackTrace();
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NullPointerException ex) {
      ex.printStackTrace();
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }

    String errorJSON = "{\"error\": \"Invalid system !\"}";
    return new ResponseEntity<String>(errorJSON, httpHeaders, HttpStatus.OK);
  }
}