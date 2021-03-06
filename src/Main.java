import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.*;



public class Main {
    public static Scanner input = new Scanner(System.in);
    public static int numOfCommit = 1;

    public static void main(String[] args) {
        // 输入配置信息
        System.out.println("输入待扫描项目地址：");

        String path = "D:\\TestProjects";

        String projectFileName = "untitled";
//        String projectFileName = "interesting-python";
//        String projectFileName = "bootstrap";
//        String projectFileName = "checkstyle";
//        String projectFileName = "JooLun-wx";
//        String projectFileName = "RxJava";
//        String projectFileName = "SpongeAPI";
//        String projectFileName = "spring-boot";
//        String projectFileName = "springfox";
//        String projectFileName = "jeecg-boot";
//        String projectFileName = "litemall";
//        String projectFileName = "spring-boot-demo";
//        String projectFileName = "spring-boot-old";
//        String projectFileName = "TestForFindbugs";
//        String projectFileName = "TestForSonar";

        String projectPath = path + "\\" + projectFileName;// 暂时统一，避免重复输入
        String newProjectPath = path + "\\" + projectFileName + "-workspace" + "\\" + projectFileName;
        String conFileName = "sonar-project.properties";// 配置文件名称

        System.out.println("输入项目名称：");
//        String projectName = input.nextLine();

//        String projectName = "news";// 暂时统一，避免重复输入
//        String projectName = "PythonProject";// 暂时统一，避免重复输入
        String projectName = "bugTest";
//        String projectName = "PythonProject";
//        String projectName = "bootstrap";
//        String projectName = "checkstyle";
//        String projectName = "JooLun-wx";
//        String projectName = "RxJava";
//        String projectName = "SpongeAPI";
//        String projectName = "SpringBoot";
//        String projectName = "springfox";
//        String projectName = "jeecg-boot";
//        String projectName = "litemall";
//        String projectName = "spring-boot-demo";
//        String projectName = "spring-boot-old";

        String projectVersion = "1.0";
        String sources = "./";
        String binaries = "./";


        ArrayList<ArrayList<sonarProjectIssue>> projReports = new ArrayList<>();
//        System.out.println("输入commit数目：");
//        numOfCommit = input.nextInt();

//        SonarUtil sonar = new SonarUtil(numOfCommit, path, projectFileName, projectName, projectVersion, sources, binaries);

        FindbugsUtil findbugs = new FindbugsUtil(path, projectFileName, projectName);
//        findbugs.getProjInfo(2);
        GitUtil.runFindbugsDiffGitShell(findbugs.getProjInfo(1), findbugs.getProjInfo(0), path, projectFileName, 1,0);
//        System.out.println(findbugs.getFindbugsIssues());
        // 创建配置文件
//        sonar.createConfFile();
//        File file = new File("D:\\TestProjects\\TestForFindbugs-workspace\\TestForFindbugs");
//        GitUtil.compailJavaToClass(file, "D:\\TestProjects\\TestForFindbugs\\classes");

//        ArrayList<FindbugsIssue> issues = FindbugsUtil.getFindbugsIssues("D:\\TestProjects\\spring-boot-demo-workspace\\findbugs.html");

//        System.out.println(issues.size());
//        FindbugsUtil.runFindbugsShell(path, projectFileName);


//        ProjectInfo pi1 = sonar.getProjInfo(1);
//        ProjectInfo pi2 = sonar.getProjInfo(0);

//        sonar.showMeasures(pi2.getMeasures());

//        GitUtil.runSonarDiffGitShell(pi1.getIssues(), pi2.getIssues(), projectPath, 1);

//        GitUtil.getAllSonarCompareIssuesInfo(pi1.getIssues(), pi2.getIssues());

//        GitUtil.refreshWorkspaceByCMD(path, projectFileName);
//        GitUtil.runRollBackGitShell(newProjectPath, 5000);
//        String time = sonar.runSonarShell(newProjectPath);

//        System.out.println("projReport: " + sonar.getProjReport(projectName));
//        HashMap<String, String> measures = sonar.combineSonarMeasures(projectName);
//        System.out.println(measures);
//        System.out.println("***********************************");
//        System.out.println("scantime: " + time);

//        pi = sonar.getProjReport(projectName);
//        projReports.add(pi);

//        GitUtil.refreshWorkspaceByCMD(path, projectFileName);
//        GitUtil.runRollBackGitShell(newProjectPath, 0);
//        sonar.runSonarShell(newProjectPath);
//        pi = sonar.getProjIssuesReportFromWeb();
//        projReports.add(pi);


    }
}

