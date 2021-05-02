import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class FindbugsUtil implements Serializable{
    String path;
    String projectFileName;
    String projectName;

    public FindbugsUtil(String path, String projectFileName, String projectName) {
        this.path = path;
        this.projectFileName = projectFileName;
        this.projectName = projectName;
    }

    /**
     * 扫描workspace中的项目，在projectFileName-workspace中生成findbugs.html结果报告
     * @return
     */
    public String runFindbugsShell () {
        long usedTime = 0;
        // findbugs安装位置
        String findbugsPath = "C:\\Progra~1\\findbugs-3.0.1\\bin";
        // 生成html报告的位置
        String targetFile = path + "\\" + projectFileName + "-workspace" + "\\findbugs.html";
        // 原项目中所有java文件编译为class文件后存放的位置
        String sourceClassesPath = path + "\\" + projectFileName + "-workspace";
        System.out.println("sourceClassesPath: " + sourceClassesPath);

        String getHtmlCmd = ".\\findbugs -textui -html -low -outputFile " + targetFile + " " + sourceClassesPath + "\\" + projectFileName;
        // 这里用cmd.exe会提示文件名、卷名错误
        String cmd = "powershell.exe /c cd " + findbugsPath + " ; " + getHtmlCmd;
//        String cmd = getHtmlCmd;

        try {
            long startTime =  System.currentTimeMillis();
            System.out.println("开始执行命令: .\\findbugs -textui -html -low -outputFile PathA PathB");
            System.out.println("cmd: " + cmd);

//            Runtime.getRuntime().exec("cmd.exe /c cd " + findbugsPath).waitFor();
            Process proc = Runtime.getRuntime().exec(cmd);

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getErrorStream(), Charset.forName("gb2312")));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();

            int processCode = proc.waitFor();
            if(processCode == 0) {
                System.out.println("命令执行完成");
                long endTime =  System.currentTimeMillis();
                // 获取扫描时间
                usedTime = (endTime - startTime) ;
//                System.out.println("扫描用时" + usedTime + "s");
                System.out.println("-----------------------------");
            } else {
                System.out.println("Error in FindbugsUtil[113]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (usedTime) + "ms";
    }

    /**
     * 根据html文档进行分析，获得所有缺陷的详细信息
     * @return
     */
    public ArrayList<FindbugsIssue> getFindbugsIssues() {
        ArrayList<FindbugsIssue> issues = new ArrayList<>();
        // 获取html的document对象
        Document doc = Jsoup.parse(readHtml());
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
//                    System.out.println(tem[lineIndex]);
                    // 目前行数默认为范围中的第一行，如[7-9]默认为7
                    String line = tem[lineIndex].split("-|]")[0];
//                    System.out.println(line);

                    issues.add(new FindbugsIssue(type, waringCode, message, component, line));

                }
            }
        }
        return  issues;
    }

    /**
     * 通过文件名获取html的字符流形式
     * @return
     */
    public String readHtml() {
        StringBuffer sb = new StringBuffer();
        String htmlPath = path + "\\" + projectFileName + "-workspace" + "\\findbugs.html";
        try(FileInputStream fis = new FileInputStream(htmlPath)) {
            byte[] bytes = new byte[1024];
            while (fis.read(bytes) != -1) {
                sb.append(new String(bytes));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     *
     * @param num
     * @return
     */
    public ArrayList<FindbugsIssue> getProjInfo(int num) {
        String projectPath = path.concat("\\").concat(projectFileName);
        String projectInfoPath = path.concat("\\").concat(projectFileName).concat("-workspace").concat("\\").concat("ProjectInfo");
        String newProjectPath = path + "\\" + projectFileName + "-workspace" + "\\" + projectFileName;

        ArrayList<FindbugsIssue> issues = new ArrayList<>();


        try {
            File file = new File(projectInfoPath, "findbugs_" + num);
            if(file.exists()) {
                // 通过本地获取扫描信息（issues和measures）
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(projectInfoPath.concat("\\").concat("findbugs_" + num)));
                issues = (ArrayList<FindbugsIssue>)ois.readObject();
                ois.close();

            } else {
                GitUtil.refreshWorkspaceByCMD(path, projectFileName);
                GitUtil.runRollBackGitShell(path, projectFileName, num);

                // 这里需要手动将项目导入idea，构建，生成class文件
                System.out.println("手动更新【" + projectFileName +  "】classes文件夹，更新完成请输入1");
                Scanner input = new Scanner(System.in);
                if(input.nextInt() == 1) {
                    runFindbugsShell();
                    issues = getFindbugsIssues();

                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(projectInfoPath.concat("\\").concat("findbugs_" + num)));
                    oos.writeObject(issues);
                    oos.close();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return issues;
    }

}


