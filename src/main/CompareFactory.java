package main;

import Saika_Output.ExStreamer;
import calc_map.Diagram;
import calc_map.Vertex;
import file_core.CodeFile;
import file_core.FileStreamer;
import file_core.FolderScanner;
import graphViz.GraphVizTest;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Saika on 2019/1/12.
 */
public abstract  class CompareFactory {
    private static DecimalFormat df = new DecimalFormat("#.00");

    public static ArrayList<String> suffixList=new ArrayList<>();
    public static boolean createDiagram=false;
    public static boolean byLines=false;
    public static boolean bySize=false;

    private static double adj_dis=1;
    public static double pow_dis=1;
    public static double edge_weight =1;
    public static double check_threshold =0.6;
    public static double threshold=0.6;

//    private static final boolean COMMENT=true;
    private static final double LOW_INDEX=1;
    private static final double BAS_DIS=1;

    private static final String dictionary_path="/home/hjs/code_compare/src/dictionary";
//    private static final String target_path="/media/hjs/KINGSTON/check/";
    private static final String path0="/media/hjs/KINGSTON/check/jsp-server";
    private static final String path1="/media/hjs/KINGSTON/check/jsp-lab";

    private static final String exPath="/home/hjs/下载/";
    private static final String graphVizPath="C:\\Users\\Saika\\Desktop\\output";
    private static final String dotPath="C:\\Program Files (x86)\\Graphviz2.38\\bin\\dot.exe";

    public static void init()
    {
        createDiagram=false;
        byLines=false;
        bySize=false;
        adj_dis=1;
        pow_dis=1;
        edge_weight =1;
        check_threshold =0.6;
        suffixList=new ArrayList<>();
        suffixList.add("java");
        threshold=0.6;
    }

    public static String getGraphVizPath() {
        return graphVizPath;
    }

    public static String getDotPath() {
        return dotPath;
    }

    public static String getExPath() {
        return exPath;
    }

    private static double dis(int times) {
        return BAS_DIS+adj_dis/Math.pow(times,pow_dis);
    }

    public static String test_compare2(){
        init();
        int projectSize = 2;
        String[] paths = new String[projectSize];
        paths[0]=path0;
        paths[1]=path1;
        double result = compare(paths[0], paths[1]);
        ////System.out.println();
        ////System.out.println("Result->" + df.format(result * 100) + "%");
        return (df.format(result * 100) + "%");
    }


//
//    public String test_compare()
//    {
//
//        return (compare(,0,1));
//    }

    public static boolean compare(String path0,String path1,double w,double p,double ch,double th)
    {
        pow_dis=p;
        check_threshold=ch;
        edge_weight =w;
        threshold=th;
        return compare(path0,path1)>threshold;
    }

    public static double compare(String path0, String path1)//1-1
    {
        int projectSize = 2;
        String[] paths = new String[projectSize];
        paths[0] = path0;
        paths[1] = path1;
        Diagram[] projects = new Diagram[projectSize];
        for (int i = 0; i < projectSize; i++) {
            if (createDiagram)
                check_draw(paths[i]);
            projects[i] = check(paths[i]);
        }

        if (projects[0] != null) {
            if (projects[1] != null) {
                return compareDiagram(projects[0],projects[1]);
            }
        }
        return 0;
    }
    public static double compare_oneToGroup(String path1,String path2)//1-N
    {
        double similar = 0;
        String[] paths=new File(path2).list();
        if (paths==null) return 0;
        for(String Scanner2:paths) {
            if (createDiagram)
                check_draw(Scanner2);
            similar = Math.max(compare(path1, Scanner2),similar);
        }
        return similar;
    }


    public static String compare_inGroup(String path)//N
    {
        return compare_betweenGroup(path,path);
    }

    public static String compare_betweenGroup(String path1, String path2)//N-N
    {
        if (!path1.endsWith("File.separator")) path1 += File.separator;
        if (!path2.endsWith("File.separator")) path2 += File.separator;
        String[] paths1=new File(path1).list();
        String[] paths2=new File(path2).list();
        StringBuilder result= new StringBuilder();
        if (paths1==null) return null;
        if (paths2==null) return null;
        for(String Scanner1:paths1)
        {
            if (createDiagram) {
                check_draw(Scanner1);
            }

            for(String Scanner2:paths2) {
                if (!Scanner1.equals(Scanner2)) {
                    double similar = compare(path1 + Scanner1, path2 + Scanner2);
                    if (similar > threshold)
                        result.append(Scanner1).append("-").append(Scanner2).append(":").append(similar).append(";<br/>");
                }
            }
        }
        return result.toString();
    }

    private static double compareDiagram(Diagram m1, Diagram m2)
    {
        //思路：匹配点 比较边权值  或者比较边后匹配点？
        // 得出结论 单向匹配z
        Set<Vertex> v1=m1.getVertexList();
        Set<Vertex> v2=m2.getVertexList();
        //匹配点
        Map<Vertex,Vertex> likely=new HashMap<>();
        double sumSimilar=0;
        int index=0;
        double weightIndex=0;
        String[][] values=new String[v1.size()+1][4];
        for (Vertex Scanner:v1)
        {
            index++;


            ////System.out.println("Looking For->"+Scanner.info() +"====="+index+"/"+v1.size());
            Vertex result=null;
            double max_similar=0;

            for (Vertex Scanner2:v2) {
                double this_similar = Scanner.similar(Scanner2);
                if (this_similar > max_similar) {
                    if (this_similar> check_threshold)
                    result = Scanner2;
                    max_similar = this_similar;
                    if (max_similar == 1) break;
                }
            }
//            if (result!=null) {
//                ////System.out.println("Result:" + result.info() + "-" + df.format(max_similar * 100) + "%");
//            }
//            else
//            {
//                ////System.out.println("Result:NULL");
//            }
            likely.put(Scanner, result);
            values[index - 1][0] = Scanner.info();
            if (result != null) {
                values[index - 1][1] = result.info();
            }
            else
            {
                values[index - 1][1] ="NULL";
            }

            values[index - 1][2] = df.format(max_similar * 100) + "%";

            if (byLines) weightIndex+=Scanner.getCf().getLines();
            if (bySize) weightIndex+=Scanner.getCf().getSize();
            weightIndex++;

            if (byLines) sumSimilar+=Scanner.getCf().getLines()*max_similar;
            if (bySize) sumSimilar+=Scanner.getCf().getSize()*max_similar;
            sumSimilar += max_similar;
                //sumSimilar+=Math.pow(max_similar,2);
        }
        if (weightIndex<=0) weightIndex=1;
        double similar1=sumSimilar/weightIndex;
        double sumSimilar2=0;

        index=0;
        weightIndex=0;
        double max_similar2;
        for (Vertex Scanner:v1)
        {
            index++;
            Vertex like=likely.get(Scanner);
//            try {
//                ////System.out.println(Scanner.info() + "=" + like.info());
//            }catch (Exception ignored) {
//
//            }
            if (like!=null)
            {
                Set relates=Scanner.getTo();
                double sum=0;
                for (Object Scanner2:relates)
                {
                    try {
                        double similar;
                        Vertex v = (Vertex) Scanner2;
                        double distance1 = m1.getDistance(Scanner,v,LOW_INDEX);
                        Vertex like2 = likely.get(v);
                        double distance2=m2.getDistance(like,like2,LOW_INDEX);
                        ////System.out.println(Scanner.info() +"_"+distance1+"_"+distance2);
                        if (distance2==-1) similar=0;
                        else
                        {
                            double diff=Math.abs(distance1-distance2);
                            double sumDis=distance1+distance2;
                            similar=1-(diff)/(sumDis);

//                            if (diff<=sumDis*0.5+1) similar=0.6;
//                            if (diff<=sumDis*0.25+1) similar=0.7;
//                            if (diff<=sumDis*0.1+1) similar=0.8;
//                            if (diff<=sumDis*0.03+1) similar=0.9;
//                            if (diff<=sumDis*0.01+1) similar=1;
                            ////System.out.println("S="+similar);

                        }
                        sum+=similar;//*(Scanner.similar(like));//+((Vertex)Scanner2).similar(like2)

                    }catch (Exception e)
                    {
                        //System.out.println(e);
                    }
                }
//                double edge_MaxS=1d;
                if ((Scanner.getTo().size()>0)||(like.getTo().size()>0))
                {
                    if (relates.size()>0) max_similar2=sum/relates.size();
                    else max_similar2=0;
//                    if (max_similar>edge_MaxS)
//                        max_similar=edge_MaxS;
                    //System.out.println(max_similar2);
                    values[index - 1][3] = df.format(max_similar2 * 100) + "%";

                    if (byLines)  weightIndex+=Scanner.getCf().getLines();
                    if (bySize)  weightIndex+=Scanner.getCf().getSize();
                    weightIndex++;

                    if (byLines) sumSimilar2+=Scanner.getCf().getLines()*max_similar2;
                    if (bySize) sumSimilar2+=Scanner.getCf().getSize()*max_similar2;
                    sumSimilar2+=max_similar2;
                }
                else
                {
                    values[index - 1][3] = "NaN";

                }

            }
//            else
//            {
//                //nan++;
//            }
        }

       // //System.out.println(attrs.size());
    //    //System.out.println(sumSimilar2+"_"+weightIndex);

        double similar2;
        if (weightIndex<=0) weightIndex=1;
         //double sizes=v1.size();
            similar2=sumSimilar2/(weightIndex);
//        else
//            similar2=0;

        if (similar2>1) similar2=1;

        //System.out.println("SS:"+similar1+"-|-"+similar2);
        double result=(similar1+similar2* edge_weight)/(1+ edge_weight);
//        values[v1.size()][2]= String.valueOf(similar1);
//        values[v1.size()][3]= String.valueOf(similar2);
//        values[v1.size()][4]= String.valueOf(result);

        ArrayList<String> attrs=new ArrayList<>();
        //System.out.println(similar1+"_"+similar2);
        attrs.add("origin");
        attrs.add("similar_target");
        attrs.add("vertex_Similar");
        attrs.add("edge_Similar");
      //  attrs.add("Similar");

        new ExStreamer("S:"+result+"["+m1.getName()+"-"+m2.getName()+"].xls").excelOut(v1.size(),attrs,values);

        return result;
    }
    private static Diagram check(String path) {
        String[] temp=path.split("/");
        String name=temp[temp.length-1];
        Diagram m=new Diagram(name);
//        FolderScanner fs=new FolderScanner();
        FolderScanner.init();
        if (suffixList.size()==0)
        {
            return null;
        }

        FolderScanner.setSuffixList(suffixList);
//        if (!COMMENT) fs.disableComment();
        try {
            FolderScanner.find(path,1);
        } catch (IOException ignored) {

        }
        for (CodeFile Scanner:FolderScanner.getCodeFiles()) {
            Vertex v=new Vertex(Scanner);
            m.addVertex(v);
            Scanner.setIndex(0);
        }
        for (CodeFile Scanner:FolderScanner.getCodeFiles()) {
//            String nowPackage=Scanner.getPackageName();
            //System.out.println("["+nowPackage+"]"+Scanner.getFileName());
            String code=Scanner.getCode();
            for (CodeFile Scanner2:FolderScanner.getCodeFiles()) {
                int times= scanFolder(Scanner, code, Scanner2);
                if (times>0) {
                    double weight=dis(times);//距离权重 1-1.25
                    m.getVertex(Scanner.getFileName()).Relate(m.getVertex(Scanner2.getFileName()),weight);
               //     //System.out.println("MS"+m.getVertex(Scanner.getFileName()).getTo().size());
                }
            }
            //System.out.println();
        }
        return m;

    }

    private static int scanFolder(CodeFile Scanner, String code, CodeFile Scanner2) {
        String target="[^a-zA-Z0-9]"+Scanner2.getName()+"[^a-zA-Z0-9]";
        if (Scanner.getName().equals(Scanner2.getName()))
            target=Scanner2.getName();
        int times=appearNumber(code,target);
        if (Scanner.getName().equals(Scanner2.getName()))
        {
            times=0;
        }
        return times;
    }


    private static void check_draw(String path) {

        ArrayList<String> Edges=new ArrayList<>();
      //  ArrayList<String> Edges2=new ArrayList<>();
//        FolderScanner fs=new FolderScanner();
        FolderScanner.init();
        try {
            FolderScanner.find(path,1);
        } catch (IOException ignored) {

        }
        for (CodeFile Scanner:FolderScanner.getCodeFiles()) {
            Scanner.setIndex(0);
        }
        for (CodeFile Scanner:FolderScanner.getCodeFiles()) {
//            String nowPackage=Scanner.getPackageName();
            //System.out.println("["+nowPackage+"]"+Scanner.getFileName());
            String code=Scanner.getCode();
            for (CodeFile Scanner2:FolderScanner.getCodeFiles()) {
                int times=scanFolder(Scanner, code, Scanner2);
                if (times>0) {
                    double weight=dis(times);//权重 1-1.25
                    Edges.add("\"" + Scanner.getFileName() + "\"" + "->" +
                            "\"" + Scanner2.getFileName() + "\"" + " " +
                            "[label=\"" + weight + "\"]");
//                    Edges2.add("\"" + Scanner.getFileName() + "\"" + "->" +
//                            "\"" + Scanner2.getFileName() + "\"" + " " +
//                            "[label=\"" + type + "\"]");
                }
            }
            //System.out.println();
        }



        String[] temp=path.split("/");
        String name=temp[temp.length-1];
        GraphVizTest gvt=new GraphVizTest();
        gvt.draw(Edges,"Diagram_"+name);
//        GraphVizTest gvt2=new GraphVizTest();
//        gvt2.draw(Edges2,"relation_"+i);

    }



    //    String type="";
//                if ((code.indexOf("new " + Scanner2.getName() + "[^a-zA-Z0-9]") > 0)) {
//                    Scanner.create(Scanner2);
//                    type="CREATE";
//                   //times*=5;
//                }
//                else if ((code.indexOf("[^a-zA-Z0-9]"+Scanner2.getName()  + ".") > 0)
//                        ||(!Scanner2.getFileName().equals(Scanner.getFileName()))) {
//                    type="use";
//                    Scanner.use(Scanner2);
//                    //times*=2;
//                }
//                else if ((times > 0))
//                {
//                    type="relate";
//                    Scanner.relate(Scanner2);
//                }



//    private static String selectFolder()
//    {
//        JFileChooser fileChooser = new JFileChooser("");
//        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//        int returnVal = fileChooser.showOpenDialog(fileChooser);
//        if(returnVal == JFileChooser.APPROVE_OPTION){
//            return  fileChooser.getSelectedFile().getAbsolutePath();
//        }
//        return null;
//    }

    private static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }

    public static String[] getDictionary() {
        String s= FileStreamer.input(new File(dictionary_path));
        //System.out.println(s);
        if (s==null) return null;
        return s.split(",");
    }

//
//    public void setByLines(boolean byLines) {
//        CompareFactory.byLines = byLines;
//    }
//
//    public void setBySize(boolean bySize) {
//        CompareFactory.bySize = bySize;
//    }
//
//    public void setSuffixList(ArrayList<String> suffixList)
//    {
//        CompareFactory.suffixList =suffixList;
//    }
//    public  void setCreateDiagram(boolean createDiagram) {
//        CompareFactory.createDiagram = createDiagram;
//    }
//


}
