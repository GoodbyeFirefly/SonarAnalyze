import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class FindbugsUtil {

//    public static String runFindbugsShell (String projectPath, String projectFileName) {
//        long usedTime = 0;
//        // findbugs安装位置
//        String findbugsPath = "C:\\Progra~1\\findbugs-3.0.1\\bin";
//        // 生成html报告的位置
//        String targetFile = projectPath + "\\" + projectFileName + "-workspace" + "\\findbugs.html";
//        // 原项目中所有java文件编译为class文件后存放的位置
//        String sourceClassesPath = projectPath + "\\" + projectFileName + "-workspace" + "\\classes";
//        System.out.println("sourceClassesPath: " + sourceClassesPath);
//        // 确保存放class的文件夹classes存在
//        File file = new File(sourceClassesPath);
//        if(!file.exists()) {
//            file.mkdir();
//        }
//        file = new File(projectPath + "\\" + projectFileName + "-workspace\\" + projectFileName);
//        GitUtil.compailJavaToClass(file);
//        System.out.println("文件编译完成");
//
//        String getHtmlCmd = ".\\findbugs -textui -html -low -outputFile " + targetFile + " " + sourceClassesPath;
//        // 这里用cmd.exe会提示文件名、卷名错误
//        String cmd = "powershell.exe /c cd " + findbugsPath + " ; " + getHtmlCmd;
////        String cmd = getHtmlCmd;
//
//        try {
//            long startTime =  System.currentTimeMillis();
//            System.out.println("开始执行命令: .\\findbugs -textui -html -low -outputFile PathA PathB");
//            System.out.println("cmd: " + cmd);
//
////            Runtime.getRuntime().exec("cmd.exe /c cd " + findbugsPath).waitFor();
//            Process proc = Runtime.getRuntime().exec(cmd);
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getErrorStream(), Charset.forName("gb2312")));
//            String line = null;
//            while ((line = in.readLine()) != null) {
//                System.out.println(line);
//            }
//            in.close();
//
//
////            GitUtil.clearStream(proc.getInputStream());
////            GitUtil.clearStream(proc.getErrorStream());
//
//            int processCode = proc.waitFor();
//            if(processCode == 0) {
//                System.out.println("命令执行完成");
//                long endTime =  System.currentTimeMillis();
//                // 获取扫描时间
//                usedTime = (endTime - startTime) ;
////                System.out.println("扫描用时" + usedTime + "s");
//                System.out.println("-----------------------------");
//            } else {
//                System.out.println("Error in FindbugsUtil[113]");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return (usedTime) + "ms";
//    }

    /**
     * 根据html文档进行分析，获得所有缺陷的详细信息
     * @param filename
     * @return
     */
    public static ArrayList<FindbugsIssue> getFindbugsIssues(String filename) {
        ArrayList<FindbugsIssue> issues = new ArrayList<>();
        // 获取html的document对象
        Document doc = Jsoup.parse(readHtml(filename));
        // 获取所有h2标签中的元素
        Elements elements = doc.select("h2");
        // 遍历所有h2标签
        for (Element e : elements) {
            // 获取h2标签中的内容
            String type = e.select("a").text();
            // 如果h2标签中内容以Warnings结尾
            if (type != null && type.endsWith("Warnings")) {
                // 获得缺陷详细信息的dom对象
                Element table = e.nextElementSibling();
                // tablerows中包含缺陷的描述、错误码
                Elements tablerows = table.select(".tablerow1,.tablerow0");
                // detailrows中包含缺陷的位置、行数
                Elements detailrows = table.select(".detailrow1,.detailrow0");
                // 遍历每个类型下的所有缺陷
                for(int i = 0; i < tablerows.size(); i++) {
                    Element tablerow = tablerows.get(i);
                    Element detailrow = detailrows.get(i);

                    Elements tdList = tablerow.select("td");
                    String waringCode = tdList.get(0).select("span").text();
                    String message = tdList.get(1).text();
                    tdList = detailrow.select("td");

                    // 以空格进行分割，获得字符串数组
                    String[] tem = tdList.get(1).text().split(" ");
                    int componentIndex = 8;// 文件所在位置
                    int lineIndex = tem.length - 1;// 行数所在位置
                    String component = tem[componentIndex].replace(".", "/").concat(".java");
                    String line = tem[lineIndex].split("-")[0];

                    issues.add(new FindbugsIssue(type, waringCode, message, component, line));

                }
            }
        }

        return  issues;
    }

    /**
     * 通过文件名获取html的字符流形式
     * @param filename
     * @return
     */
    public static String readHtml(String filename) {
        StringBuffer sb = new StringBuffer();
        try(FileInputStream fis = new FileInputStream(filename)) {
            byte[] bytes = new byte[1024];
            while (fis.read(bytes) != -1) {
                sb.append(new String(bytes));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}


