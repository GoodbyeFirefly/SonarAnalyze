import java.io.*;
import java.util.ArrayList;

public class GitUtil {

    public static long runRollBackGitShell(String projectPath, int num) {
        try {
            long startTime =  System.currentTimeMillis();
            long endTime, usedTime;
            System.out.println("开始执行命令:git reset --hard HEAD~" + num);// 注意这里必须是HEAD~1而不是HEAD^
            String comm = "cmd.exe /c cd " + projectPath + "&& git reset --hard HEAD~" + num;
            Process proc = Runtime.getRuntime().exec(comm);
            clearStream(proc.getInputStream());
            clearStream(proc.getErrorStream());
            int processCode = proc.waitFor();
            System.out.println("processCode: " + processCode);
            if(processCode == 0) {
                System.out.println("命令执行完成");
                endTime =  System.currentTimeMillis();
                // 获取扫描时间
                usedTime = (endTime - startTime) / 1000;
                System.out.println("更新commit数据用时" + usedTime + "s");
                System.out.println("-----------------------------");
                return usedTime;
            } else {
                System.out.println("Error in GitUtil[23]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static String runDiffGitShell(ArrayList<ProjectIssue> lastProjIssues, ArrayList<ProjectIssue> curProjIssues, String projectPath) {
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec("cmd.exe /c cd " + projectPath + "&& git diff HEAD~1");
            InputStream in = process.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);
            StringBuffer sb = new StringBuffer();
            String message = null, lastMessage = null;
            String fileName = null;
            int index = 0;
            boolean flag = false;// 标识接下来的内容是否为diff报告中的内容
            while((message = br.readLine()) != null) {
                if(message.length() > 10 && message.substring(0, 10) == "diff --git") {
                    flag = false;
                    continue;// 跳出这次循环
                }
                if(message.length() > 2 && message.substring(0, 2).equals("@@")) {
                    // 获取当前diff报告属于哪个文件夹
                    fileName = lastMessage.substring(6);
                    String[] split = message.split(" |,");
                    // 获取当前diff报告开始的行数
                    index = Integer.valueOf(split[3]);
                    flag = true;

                    System.out.println("fileName: " + fileName);

                    continue;// 跳出这次循环
                }
                if(flag) {

                    System.out.println("Line " + index + ": " + message);
                    getIssuesInfo(lastProjIssues, curProjIssues, fileName, index);
                    index++;
                }
//                sb.append(message + "\n");
                // 记录上一行数据，当检测到@@时，用其来判断当前改动的代码属于哪个文件夹
                lastMessage = message;
            }

            System.out.println(sb);
            return sb.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        run.exit(0);
        return null;
    }

    public static void copyFolderByCMD(String scrFolder, String desFolder) {
        Runtime run = Runtime.getRuntime();
        try {
            Process proc = run.exec("cmd.exe /c " + "xcopy " + scrFolder + " " + desFolder + " /s /f /h");
            clearStream(proc.getInputStream());
            clearStream(proc.getErrorStream());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void deleteFile(File file) {
        //判断文件不为null或文件目录存在
        if (file == null || !file.exists()){
            return;
        }
        //取得这个目录下的所有子文件对象
        File[] files = file.listFiles();
        //遍历该目录下的文件对象
        for (File f: files){
            //打印文件名
            String name = file.getName();
            //判断子目录是否存在子目录,如果是文件则删除
            if (f.isDirectory()){
                deleteFile(f);
            }else {
                f.delete();
            }
        }
        //删除空文件夹  for循环已经把上一层节点的目录清空。
        file.delete();
    }

    public static void refreshWorkspaceByCMD(String path, String projectFileName) {
        try {
            File f = new File(path + "\\" + projectFileName + "-workspace");
            deleteFile(f);// 删除该目录下所有文件夹和文件
            f = new File(path + "\\" + projectFileName + "-workspace");
            // 在项目所在目录下创建"项目名-workspace"文件夹
            f.mkdir();
            f = new File(path + "\\" + projectFileName + "-workspace" + "\\" + projectFileName);
            // 在"项目名-workspace"文件夹中创建与项目同名的文件夹
            if(f.mkdir()) {
                String srcFolder = path + "\\" + projectFileName;
                String desFolder = path + "\\" + projectFileName + "-workspace" + "\\" + projectFileName;
                copyFolderByCMD(srcFolder, desFolder);
            } else {
                System.out.println("Error");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearStream(InputStream stream) {
        String line = null;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(stream));) {
            while ((line = in.readLine()) != null) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getIssuesInfo(ArrayList<ProjectIssue> lastProjIssues, ArrayList<ProjectIssue> curProjIssues, String fileName, int index) {
        for(ProjectIssue pi : curProjIssues) {
            String s = pi.getComponent();
//            System.out.println("fileName: " + fileName);
//            System.out.println("index: " + index);
            if(s.equals(fileName) && pi.getLine() != null && Integer.valueOf(pi.getLine()) == index) {
                boolean findFlag = false;
                for(ProjectIssue tem : lastProjIssues) {
                    if (tem.getComponent().equals(pi.getComponent()) &&
                    tem.getKey().equals(pi.getKey()) &&
                    tem.getHash().equals(pi.getHash())) {
                        // 找到相同的缺陷
                        findFlag = true;

                        if(tem.getStatus().equals("OPEN") && pi.getStatus().equals("OPEN")){
                            System.out.println("延续的缺陷：");
                            System.out.println(pi.getMessage());
                        } else if(tem.getStatus().equals("CLOSED") && pi.getStatus().equals("CLOSED")) {

                        } else if(tem.getStatus().equals("OPEN") && pi.getStatus().equals("CLOSED")) {
                            System.out.println("修复的缺陷：");
                            System.out.println(pi.getMessage());
                        }
                    }
                }
                if(findFlag == false) {
                    System.out.println("新增的缺陷：");
                    System.out.println(pi.getMessage());
                }

            }
        }
        return null;
    }

}
