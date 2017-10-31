/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author suhaybal-absi
 */
public class Utils {

    public static File getTableFile(String name) {
        return getResource("tables/" + name);
    }

    public static List<String> getTableLines(String name) {
        return getResourceLines("tables/" + name);
    }
    
    // Spring way of getting resources
    public static List<String> getResourceLines(String resPath) {

        Resource resource = new ClassPathResource("classpath:" + resPath);

        try {
            InputStream resourceInputStream = resource.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(resourceInputStream));

            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();

            System.out.println("TABLE READ - " + resPath + ": " +lines.size());

            return lines;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static File getResource(String path) {

        ClassLoader classLoader = Utils.class.getClassLoader();

        System.out.println(classLoader);

        return new File(classLoader.getResource(path).getFile());
    }

    public static double getValidNumber(double... numbers) {

        for (double number : numbers) {

            if (Double.isNaN(number) == false) {
                return number;
            }
        }

        return Double.NaN;
    }

}
