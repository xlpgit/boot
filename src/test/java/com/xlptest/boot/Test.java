package com.xlptest.boot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author twjitm
 */
public class Test {
    static class Data {

        private String fpid;
        private String equipment;
        private Map<String, Integer> equip_gems;
        private Map<String, Integer> cost;

        public Data() {
            equip_gems = new HashMap<>();
            cost = new HashMap<>();
        }

        public String getFpid() {
            return fpid;
        }

        public void setFpid(String fpid) {
            this.fpid = fpid;
        }

        public String getEquipment() {
            return equipment;
        }

        public void setEquipment(String equipment) {
            this.equipment = equipment;
        }

        public Map<String, Integer> getEquip_gems() {
            return equip_gems;
        }

        public void setEquip_gems(Map<String, Integer> equip_gems) {
            this.equip_gems = equip_gems;
        }

        public Map<String, Integer> getCost() {
            return cost;
        }

        public void setCost(Map<String, Integer> cost) {
            this.cost = cost;
        }
    }

    public static void main(String[] args) throws Exception {
        String data = "";
        String lan[] = data.split("\n");
        BufferedReader fileReader = new BufferedReader(new FileReader(new File("")));
        String dd;
        Map<String, Data> map = new HashMap<>();
        StringBuilder result = new StringBuilder();
        while (!("").equals(dd = fileReader.readLine())) {
            if (dd == null) {
                break;
            }
            //for (String s : lan) {
            String[] d = dd.split(",");
            String fpid = d[0];
            String equip = d[1];
            String key = fpid + equip;
            Data dt = map.getOrDefault(key, new Data());
            dt.setFpid(fpid);
            dt.setEquipment(d[1]);

            Map<String, Integer> gems = dt.getEquip_gems();
            Integer gn = gems.getOrDefault(d[2], 0);
            gn += Integer.parseInt(d[3]);
            gems.put(d[2], gn);

            Map<String, Integer> costmap = dt.getCost();
            Integer cn = costmap.getOrDefault(d[4], 0);
            cn += Integer.parseInt(d[5]);
            costmap.put(d[4], cn);
            cn = costmap.getOrDefault(d[6], 0);

            cn += Integer.parseInt(d[7]);
            costmap.put(d[6], cn);
            map.put(key, dt);
        }


        map.forEach((fp, v) -> {
            Map<String, Integer> costs = v.getCost();
            Map<String, Integer> gems = v.getEquip_gems();
            StringBuilder c = new StringBuilder();
            for (Map.Entry<String, Integer> en : costs.entrySet()) {
                c.append("'").append(en.getKey()).append("' => ").append(en.getValue()).append(",\n");
            }

            StringBuilder g = new StringBuilder();
            for (Map.Entry<String, Integer> en : gems.entrySet()) {
                g.append("'").append(en.getKey()).append("' => ").append(en.getValue()).append(",\n");
            }
            String r = "[\n" +
                    "    'fpid' => " + v.getFpid() + ",\n" +
                    "    'equipment' => '" + v.getEquipment() + "',\n" +
                    "    'equip_gems' => [\n" + g + "\n],\n" +
                    "    'cost' => [\n" + c + "]\n" +
                    "],";

            result.append(r);
        });

        System.out.println(result.toString());
    }
}
