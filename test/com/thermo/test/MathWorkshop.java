/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thermo.test;

import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.FunctionUtils;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.function.Add;
import org.apache.commons.math3.analysis.function.Constant;
import org.apache.commons.math3.analysis.function.Divide;
import org.apache.commons.math3.analysis.function.Minus;
import org.apache.commons.math3.analysis.function.Pow;
import org.apache.commons.math3.analysis.function.Power;
import org.apache.commons.math3.analysis.function.Sin;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

/* Secure password storage

byte[] salt = new byte[16];
random.nextBytes(salt);
KeySpec spec = new PBEKeySpec("password".toCharArray(), salt, 65536, 128);
SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
byte[] hash = f.generateSecret(spec).getEncoded();
Base64.Encoder enc = Base64.getEncoder();
System.out.printf("salt: %s%n", enc.encodeToString(salt));
System.out.printf("hash: %s%n", enc.encodeToString(hash));

*/




/**
 *
 * @author suhaybal-absi
 */
public class MathWorkshop {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        equationsTest();
       //exampleTest();
       //practicalTest();
        
    }
    
    public static void practicalTest(){
        
         final BivariateFunction biFunc = new BivariateFunction() {
            @Override
            public double value(double x, double y) {
                return 6*x*x - 10*y*y + 5*y - 20;
            }
        };
        double x,y;
        
        x = 2.10554824; y =1.1;
        System.out.printf("Eval (%f, %f): %f", x, y, biFunc.value(x, y) );
        System.out.println();
        
        x = 1.9282879848295131; y= 0.7916925273621022;
        System.out.printf("Eval (%f, %f): %f", x, y, biFunc.value(x, y) );
        System.out.println();
        
        x = 4.316902090472318; y = 3.2903759884692008;
        System.out.printf("Eval (%f, %f): %f", x, y, biFunc.value(x, y) );
        System.out.println();
        
        
        MultivariateFunction squaredFunc = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double val = biFunc.value(point[0], point[1]);
                return val*val;
            }
        };
        
        ObjectiveFunction objFunc = new ObjectiveFunction(squaredFunc);
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        
        //The following equation could be used for solving energy equation
        //since it is a linear multi-variate function.
        
        //SimplexSolver optimizer = new SimplexSolver();
        //optimizer.optimize(optData)
        
        
        PointValuePair pair = optimizer.optimize(
                objFunc,
                new MaxIter(100), new MaxEval(100),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{2.0, 1.0}),
                new SimpleBounds(new double[]{2.0, 1.0}, new double[]{2.5, 1.5}));
        
        // Now, let's tell the user about it:
	double[] point = pair.getPoint();
	System.out.println("\nRoots for Two-variables function\n"
		+ "----------------------------------\n\n"
		+ "Minimum found at (" + point[0] + ", " + point[1] + ")\n"
		+ "with value " + pair.getValue());
        
    }
    public static void exampleTest(){
        
	MultivariateFunction function = new MultivariateFunction() {
		@Override
		public double value(double[] point) {
			double x = point[0];
			double y = point[1];
			return (1 - x) * (1 - x) + 100 * (y - x * x) * (y - x * x);
		}
	};
        
        ObjectiveFunction objectiveFunc = new ObjectiveFunction(function);
        NelderMeadSimplex simplex = new NelderMeadSimplex(new double[] { 0.2, 0.2 });
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-5, 1e-6);
        PointValuePair pair = optimizer.optimize(
                simplex, 
                new MaxIter(10000),
                new MaxEval(1000),
                objectiveFunc, 
                GoalType.MINIMIZE, new InitialGuess(new double[] { 0, 0 }));
        
	// Now, let's tell the user about it:
	double[] point = pair.getPoint();
	System.out.println("\nMinimization (Rosenbrock function)\n"
		+ "----------------------------------\n\n"
		+ "Minimum found at (" + point[0] + ", " + point[1] + ")\n"
		+ "with value " + pair.getValue());
    }

    private static void equationsTest() {
        
        /*double val =  ( Cpa1 * (T1 - Tref) - Cpa2 * (T2 - Tref) ) 
                        / ( Cpg2 * (T2 - Tref) - HV ) 
                        / nb;
        
        
        FunctionUtils.compose(new Minus(), new Constant(42700));
        */
    }
}
