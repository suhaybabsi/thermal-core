/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suhaybabsi.thermodesigner;

import com.suhaybabsi.thermodesigner.core.CalculationException;
import com.suhaybabsi.thermodesigner.core.ConfigurationException;
import com.suhaybabsi.thermodesigner.json.JsonWork;
import com.suhaybabsi.thermodesigner.core.ThermalSystem;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

/**
 *
 * @author suhaybal-absi
 */
@WebServlet(name = "ModelCalculation", urlPatterns = {"/calculate"}, initParams = {
    @WebInitParam(name = "model", value = "Value")})
public class ModelCalculation extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String rawModel = request.getParameter("model");
        //System.out.println("res: \n"+rawModel+"\n\n");
        
        Object errorMessage= null;
        JSONObject result = new JSONObject();
        
        try {
            
            result = new JSONObject();
            result.put("error", "Incorrect system configuration");
            
            JSONObject jsonModel = new JSONObject(rawModel);
            ThermalSystem system = JsonWork.parseModel(jsonModel);
            
            String root = System.getProperty("user.dir") + "output/";
            
            system.configure();
            system.writeResultsToPath(root + "last_calculated_prev.txt");
            system.writeResultsToPath(root + "last_calculated_post.txt");
            
            system.solve();
            system.calculateExergy();
            
            system.writeResultsToPath(root + "last_calculated_post.txt");
            
            result = JsonWork.generateJsonResults(system);
            System.out.println(result);
            
        } catch (ConfigurationException ex) {
            errorMessage = ex.getJsonMessage();
            Logger.getLogger(ModelCalculation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CalculationException ex) {
            errorMessage = ex.getJsonMessage();
            Logger.getLogger(ModelCalculation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            errorMessage = ex.getMessage();
            Logger.getLogger(ModelCalculation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            errorMessage = ex.getMessage();
            Logger.getLogger(ModelCalculation.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        if(errorMessage != null){
            result = new JSONObject();
            try {
                result.put("error", errorMessage);
            } catch (JSONException ex) {
                Logger.getLogger(ModelCalculation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            //System.out.println(result.toString());
            out.print(result.toString());
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
