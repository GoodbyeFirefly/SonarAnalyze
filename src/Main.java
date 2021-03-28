import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.*;



public class Main {
    public static Scanner input = new Scanner(System.in);
    public static int numOfCommit = 2;

    public static void main(String[] args) {
        // 输入配置信息
        System.out.println("输入待扫描项目地址：");
//        String projectPath = input.nextLine();
//        String projectPath = "D://news";// 暂时统一，避免重复输入
//        String projectPath = "D:\\TestProjects\\spring-boot";// 暂时统一，避免重复输入
//        String projectPath = "D:\\TestProjects\\interesting-python";// 暂时统一，避免重复输入//        String projectPath = "D:\\TestProjects\\interesting-python";// 暂时统一，避免重复输入
        String projectPath = "D:\\TestProjects\\untitled";// 暂时统一，避免重复输入


        String fileName = "sonar-project.properties";// 配置文件名称

        System.out.println("输入项目名称：");
//        String projectName = input.nextLine();
//        String projectName = "news";// 暂时统一，避免重复输入
//        String projectName = "SpringBoot";// 暂时统一，避免重复输入
//        String projectName = "PythonProject";// 暂时统一，避免重复输入
        String projectName = "bugTest";
        String projectVersion = "1.0";
        String sources = "./";
        String binaries = "./";


//        HashMap<String, ArrayList<String>>measures = new HashMap<>();
//        ArrayList<String> scanTime = new ArrayList<>();
        ArrayList<ProjectIssue> projReports = new ArrayList<>();
        System.out.println("输入commit数目：");
//      numOfCommit = input.nextInt();

        SonarUtil sonar = new SonarUtil(numOfCommit, projectPath, fileName, projectName, projectVersion, sources, binaries);


        // 创建配置文件
        sonar.createConfFile(projectPath.concat("/"), fileName, projectName, projectVersion, sources, binaries);
//
//        for(int i = 0; i < numOfCommit; i++) {
//            System.out.println("第" + i + "个版本");
//
//            // 运行命令行
//            String time = runSonarShell(projectPath);
//            scanTime.add(0, time);
//
//            // 获得该版本代码的报告
//            while (getTasksNumInQueue(projectName) != 0){
//                try {
//                    Thread.currentThread().sleep(1000);// 暂停一段时间再发送请求
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            projReports.add(getProjReport(projectName));
//            System.out.println("projReport: " + getProjReport(projectName));

            // 回滚为上个commit版本
//            runRollBackGitShell(projectPath);
//        }
//        measures.put("scan_time", scanTime);

        // 从sonarQube获取项目分析数据
//        getJsonData(projectName, measures);
//        showMeasures(measures);
//        showProjInfo(projReports);
//        runDiffGitShell(projectPath);

        System.out.println(sonar.getProjReport(projectName));
        System.out.println(GitUtil.runDiffGitShell(projectPath));
    }











}

