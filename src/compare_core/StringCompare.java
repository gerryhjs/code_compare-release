package compare_core;

import java.util.ArrayList;

public class StringCompare {
    private String s1;
    private String s2;


    public StringCompare(String s1,String s2)
    {
        this.s1=s1.replace("*","");
        this.s2=s2.replace("*","");
    }
    public double work(int mode) {
        if ((s1.length()<10)||(s2.length()<10)) return 0;
        if (mode == 0)//机械容差比较
         /*
            abc=bca=cba dif=0
         */ {
            //int keyNum=0;
            ArrayList<String> keys = new ArrayList<>();
            int[] index = new int[s1.length() + s2.length()];
            // int[] index2=new int[s2.length()];
            for (int i = 0; i < s1.length(); i++) {
                int place = keys.indexOf(String.valueOf(s1.charAt(i)));
                if (place >= 0) {
                    index[place]++;
                } else {
                    place = keys.size();
                    keys.add(String.valueOf(s1.charAt(i)));
                    index[place]++;
                }
            }
//            for (int i=0;i<keys.size();i++) {
//                //System.out.println(keys.get(i)+":"+index[i]);
//            }
            for (int i = 0; i < s2.length(); i++) {
                int place = keys.indexOf(String.valueOf(s2.charAt(i)));
                if (place >= 0) {
                    index[place]--;
                } else {
                    place = keys.size();
                    keys.add(String.valueOf(s2.charAt(i)));
                    index[place]--;
                }
            }
            double diff = 0;
            for (int i = 0; i < keys.size(); i++) {
                ////System.out.println(keys.get(i) + ":" + index[i]);
                diff += Math.abs(index[i]);
            }
            return 1-(diff)/Math.max(s1.length(), s2.length());
            //-Math.abs(s1.length()-s2.length())
        }
        if (mode == 1) //前后近似度比较
        /*
            abc-abd dif=1
            增加 删除 变换字符视为一次操作
         */ {
            double[][] diff = new double[s1.length() + 1][s2.length() + 1];
            for (int i = 0; i < s1.length(); i++)
                diff[i][0] = i;
            for (int j = 0; j < s2.length(); j++)
                diff[0][j] = j;
            for (int i = 1; i <= s1.length(); i++)
                for (int j = 1; j <= s2.length(); j++) {
                    double temp = 0;
                    if (s1.charAt(i - 1) != s2.charAt(j - 1))
                        temp = 1;
                    diff[i][j] = min(diff[i - 1][j] + 1, diff[i][j - 1] + 1, diff[i - 1][j - 1] + temp);
                }
            return 1-(diff[s1.length()][s2.length()])/Math.min(s1.length(), s2.length());
            //return 1-(diff[s1.length()][s2.length()])/Math.min(s1.length(), s2.length());//-Math.abs(s1.length()-s2.length())
        }
        if (mode == 2)
        {
        //...
        }
        return -1;
    }


    public double min(double... input)
    {
        double ans=Double.MAX_VALUE;
        for (double Scanner:input)
            ans=Math.min(ans,Scanner);
        return ans;
    }


}
