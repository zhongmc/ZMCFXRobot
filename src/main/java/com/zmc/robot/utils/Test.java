package com.zmc.robot.utils;

import java.text.DecimalFormat;

public class Test {

    public static void main(String[] args) {
        
        double fval = 10.04, fv2 = 100.00024;

        System.out.println(String.format("%.04f,%f,%.4f,%f", fval, fval, fv2, fv2));

        DecimalFormat fmt = new DecimalFormat("0.####"); 
        System.out.println(String.format("%s,%s", fmt.format(fval), fmt.format(fv2)));

    }


}