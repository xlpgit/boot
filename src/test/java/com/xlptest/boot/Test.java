package com.xlptest.boot;

/**
 * @author twjitm
 */
public class Test {
    public static void main(String[] args) {
        String data ="" ;

        String lan[] = data.split("\n");
        StringBuilder result= new StringBuilder();
        for (int i = 0; i < lan.length; i++) {
            String[] d = lan[i].split(",");
            String fpid = d[0];
            String v = d[2];
               String  r="[\n" +
                        "        'fpid' => "+fpid+",\n" +
                        "        'reward_val' => ["+v+"]\n" +
                        "    ],\n";

            result.append(r);

        }

        System.out.println(result.toString());
    }
}
