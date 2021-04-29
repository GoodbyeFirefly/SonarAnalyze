import java.io.*;
import java.util.ArrayList;
import java.util.regex.*;

public class GitUtil {

    private static Runtime r = Runtime.getRuntime();

    public static long runRollBackGitShell(String projectPath, int num) {
        if(num == 0) {
            return 0;
        }
        try {
            long startTime =  System.currentTimeMillis();
            long endTime, usedTime;
            System.out.println("开始执行命令:git reset --hard HEAD~" + num);// 注意这里必须是HEAD~1而不是HEAD^
            String comm = "cmd.exe /c cd " + projectPath + "&& git reset --hard HEAD~" + num;
            Process proc = Runtime.getRuntime().exec(comm);
            clearStream(proc.getInputStream());
            clearStream(proc.getErrorStream());
            int processCode = proc.waitFor();
//            System.out.println("processCode: " + processCode);
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

    public static String runSonarDiffGitShell(ArrayList<sonarProjectIssue> lastProjIssues, ArrayList<sonarProjectIssue> curProjIssues, String projectPath, int num) {
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec("cmd.exe /c cd " + projectPath + "&& git diff HEAD~" + num);
            InputStream in = process.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);
            StringBuffer sb = new StringBuffer();
            String message = null;
            String fileName = null;
            int oldLineIndex = 0, newLineIndex = 0;
            boolean flag = false;// 标识接下来的内容是否为diff报告中的内容
            while((message = br.readLine()) != null) {
                if(message.length() > 10 && message.substring(0, 10).equals("diff --git")) {
                    flag = false;
//                    System.out.println("message: " + message);
                    String[] split = message.split(" ");
                    fileName = split[2].substring(2);
                    System.out.println("\nfileName: " + fileName);
                    continue;// 跳出这次循环
                }
                if(message.length() > 2 && message.substring(0, 2).equals("@@")) {
                    // 获取当前diff报告属于哪个文件夹
//                    fileName = lastMessage.substring(6);
                    String[] split = message.split(" |,");
                    // 获取当前diff报告开始的行数
                    oldLineIndex = 0 - Integer.valueOf(split[1]);
                    newLineIndex = Integer.valueOf(split[3]);
                    flag = true;

                    continue;// 跳出这次循环
                }
                if(flag) {

                    System.out.println(oldLineIndex + " " + newLineIndex + ": " + message);

                    if(message.substring(0, 1).equals("+")) {
                        getSonarCompareIssuesInfo(lastProjIssues, curProjIssues, fileName, newLineIndex, true);
                        newLineIndex++;
                    } else if(message.substring(0, 1).equals("-")) {
                        getSonarCompareIssuesInfo(lastProjIssues, curProjIssues, fileName, oldLineIndex, false);
                        oldLineIndex++;
                    } else {
                        getSonarCompareIssuesInfo(lastProjIssues, curProjIssues, fileName, newLineIndex, true);
                        newLineIndex++;
                        getSonarCompareIssuesInfo(lastProjIssues, curProjIssues, fileName, oldLineIndex, false);
                        oldLineIndex++;
                    }
                }
//                sb.append(message + "\n");

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

    public static String runFindbugsDiffGitShell(ArrayList<FindbugsIssue> lastProjIssues, ArrayList<FindbugsIssue> curProjIssues, String projectPath, int num) {
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec("cmd.exe /c cd " + projectPath + "&& git diff HEAD~" + num);
            InputStream in = process.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);
            StringBuffer sb = new StringBuffer();
            String message = null;
            String fileName = null;
            int oldLineIndex = 0, newLineIndex = 0;
            boolean flag = false;// 标识接下来的内容是否为diff报告中的内容
            while((message = br.readLine()) != null) {
                if(message.length() > 10 && message.substring(0, 10).equals("diff --git")) {
                    flag = false;
//                    System.out.println("message: " + message);
                    String[] split = message.split(" ");
                    fileName = split[2].substring(2);
                    System.out.println("\nfileName: " + fileName);
                    continue;// 跳出这次循环
                }
                if(message.length() > 2 && message.substring(0, 2).equals("@@")) {

                    String[] split = message.split(" |,");
                    // 获取当前diff报告开始的行数
                    oldLineIndex = 0 - Integer.valueOf(split[1]);
                    newLineIndex = Integer.valueOf(split[3]);
                    flag = true;

                    continue;// 跳出这次循环
                }
                if(flag) {

                    System.out.println(oldLineIndex + " " + newLineIndex + ": " + message);

                    if(message.substring(0, 1).equals("+")) {
                        getFindbugsCompareIsuesInfo(lastProjIssues, curProjIssues, fileName, oldLineIndex, newLineIndex, 1);
                        newLineIndex++;
                    } else if(message.substring(0, 1).equals("-")) {
                        getFindbugsCompareIsuesInfo(lastProjIssues, curProjIssues, fileName, oldLineIndex, newLineIndex, 2);
                        oldLineIndex++;
                    } else {
                        getFindbugsCompareIsuesInfo(lastProjIssues, curProjIssues, fileName, oldLineIndex, newLineIndex, 0);
                        newLineIndex++;
                        oldLineIndex++;
                    }
                }
//                sb.append(message + "\n");

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
            File f = new File(path + "\\" + projectFileName + "-workspace" + "\\" + projectFileName);
            deleteFile(f);// 删除该目录下所有文件夹和文件
            f = new File(path + "\\" + projectFileName + "-workspace");
//             在项目所在目录下创建"项目名-workspace"文件夹
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

            // 创建projectInfo文件夹，用于存放不同commit的扫描信息
            f = new File(path.concat("\\").concat(projectFileName).concat("-workspace").concat("\\").concat("ProjectInfo"));
            if(f.exists()) {}
            else f.mkdir();


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

    /**
     * 根据文件名和对应行数，查找新版本中对应的缺陷信息，进而对比旧版本中缺陷信息，
     * 查看缺陷的状况（新增、修复、重现、延续）
     * @param lastProjIssues
     * @param curProjIssues
     * @param fileName
     * @param index flag为true时表示当前版本所要验证的代码行，false表示历史版本
     * @param flag true：以当前版本为基准，遍历历史版本；false：以历史版本为基准，遍历当前版本
     * @return
     */
    public static String getSonarCompareIssuesInfo(ArrayList<sonarProjectIssue> lastProjIssues, ArrayList<sonarProjectIssue> curProjIssues, String fileName, int index, boolean flag) {
        // 寻找消失的缺陷
        if(flag == false) {
            for (sonarProjectIssue pi : lastProjIssues) {
                String s = pi.getComponent();
                if(s.equals(fileName) && pi.getLine() != null && Integer.valueOf(pi.getLine()) == index) {
                    boolean findFlag = false;
                    for(sonarProjectIssue tem : curProjIssues) {
                        if (tem.getComponent().equals(pi.getComponent()) &&
                                tem.getKey().equals(pi.getKey()) &&
                                tem.getHash().equals(pi.getHash())) {
                            // 找到了，不需要处理
                            findFlag = true;
                            break;
                        }
                    }
                    if(findFlag == false) {
                        System.out.println("***************************************");
                        System.out.println("* 消失的缺陷：");
                        System.out.println("* " + pi.getMessage());
                        System.out.println("***************************************");
                    }
                }
            }
            return null;
        }

        for(sonarProjectIssue pi : curProjIssues) {
            String s = pi.getComponent();
//            System.out.println("fileName: " + fileName);
//            System.out.println("index: " + index);
            if(s.equals(fileName) && pi.getLine() != null && Integer.valueOf(pi.getLine()) == index) {
                boolean findFlag = false;
                for(sonarProjectIssue tem : lastProjIssues) {
                    if (tem.getComponent().equals(pi.getComponent()) &&
                    tem.getKey().equals(pi.getKey()) &&
                    tem.getHash().equals(pi.getHash())) {
                        // 找到相同的缺陷
                        findFlag = true;

                        if(tem.getStatus().equals("OPEN") && pi.getStatus().equals("OPEN")){
                            System.out.println("***************************************");
                            System.out.println("* 延续的缺陷：");
                            System.out.println("* " + pi.getMessage());
                            System.out.println("***************************************");
                        } else if(tem.getStatus().equals("CLOSED") && pi.getStatus().equals("CLOSED")) {

                        } else if(tem.getStatus().equals("OPEN") && pi.getStatus().equals("CLOSED")) {
                            System.out.println("***************************************");
                            System.out.println("* 修复的缺陷：");
                            System.out.println("* " + pi.getMessage());
                            System.out.println("***************************************");
                        }
                    }
                }
                if(findFlag == false) {
                    System.out.println("***************************************");
                    System.out.println("* 新增的缺陷：");
                    System.out.println("* " + pi.getMessage());
                    System.out.println("***************************************");
                }

            }
        }
        return null;
    }

    /**
     *
     * @param lastProjIssues
     * @param curProjIssues
     * @param fileName
     * @param oldLineIndex
     * @param newLineIndex
     * @param flag 0：共有代码，1：新增代码，2：删除代码
     * @return
     */
    public static String getFindbugsCompareIsuesInfo(ArrayList<FindbugsIssue> lastProjIssues, ArrayList<FindbugsIssue>  curProjIssues, String fileName, int oldLineIndex, int newLineIndex, int flag) {
        // 表示此行代码为新增代码
        if(flag == 1) {
            for( FindbugsIssue fi : curProjIssues) {
                if(fileName.endsWith(fi.getComponent()) && Integer.valueOf(fi.getLine()) == newLineIndex) {
                    System.out.println("***************************************");
                    System.out.println("* 新增的缺陷：");
                    System.out.println("* " + fi.getMessage());
                    System.out.println("***************************************");
                }
            }
        }
        // 表示此行代码在新版本中已经删除
        else if(flag == 2) {
            for(FindbugsIssue fi : lastProjIssues) {
                if(fileName.endsWith(fi.getComponent()) && Integer.valueOf(fi.getLine()) == oldLineIndex) {
                    System.out.println("***************************************");
                    System.out.println("* 消失的缺陷：");
                    System.out.println("* " + fi.getMessage());
                    System.out.println("***************************************");
                }
            }
        }
        // 表示此行代码本身未变动，但具体的位置可能发生改变，且可能会对缺陷产生影响
        else {
            boolean findInOldIssues = false;// 标记是否在旧版本代码中找到对应缺陷
            boolean findInNewIssues = false;// 标记是否在新版本代码中找到对应缺陷
            FindbugsIssue oldIssue = null;
            FindbugsIssue newIssue = null;
            for(FindbugsIssue fi : lastProjIssues) {
                if(fileName.endsWith(fi.getComponent()) && Integer.valueOf(fi.getLine()) == oldLineIndex) {
                    findInOldIssues = true;
                    oldIssue = fi;
                }
            }
            for(FindbugsIssue fi : curProjIssues) {
                if(fileName.endsWith(fi.getComponent()) && Integer.valueOf(fi.getLine()) == newLineIndex) {
                    findInNewIssues = true;
                    newIssue = fi;
                }
            }
            if(findInOldIssues == true && findInNewIssues == true) {
                if(oldIssue.getMessage().equals(newIssue.getMessage())) {
                    System.out.println("***************************************");
                    System.out.println("* 延续的缺陷：");
                    System.out.println("* " + newIssue.getMessage());
                    System.out.println("***************************************");
                } else {
                    System.out.println("***************************************");
                    System.out.println("* 原来的缺陷：");
                    System.out.println("* " + oldIssue.getMessage());
                    System.out.println("* 新出的缺陷：");
                    System.out.println("* " + newIssue.getMessage());
                    System.out.println("***************************************");
                }
            } else if(findInOldIssues == false && findInNewIssues == true) {
                System.out.println("***************************************");
                System.out.println("* 新增的缺陷：");
                System.out.println("* " + newIssue.getMessage());
                System.out.println("***************************************");
            } else if(findInOldIssues == true && findInNewIssues == false) {
                System.out.println("***************************************");
                System.out.println("* 修复的缺陷：");
                System.out.println("* " + oldIssue.getMessage());
                System.out.println("***************************************");
            }
        }

        return null;
    }

    /**
     * 对比两次扫描的全部缺陷信息，找出有变化的缺陷信息
     * @param lastProjIssues
     * @param curProjIssues
     * @return
     */
    public static String getAllSonarCompareIssuesInfo(ArrayList<sonarProjectIssue> lastProjIssues, ArrayList<sonarProjectIssue> curProjIssues) {
        int numOfBug = 0;
        int numOfVul = 0;
        int numOfNew = 0;
        int numOfDelete = 0;
        int numOfFix = 0;
        int numOfReopen = 0;

        int bugOfNew = 0;
        int vulOfNew = 0;
        int bugOfDelete = 0;
        int vulOfDelete = 0;
        int bugOfFix = 0;
        int vulOfFix = 0;
        int bugOfReopen = 0;
        int vulOfReopen = 0;
        for(sonarProjectIssue pi : curProjIssues) {
            boolean findFlag = false;
            for(sonarProjectIssue tem : lastProjIssues) {
                if (tem.getComponent().equals(pi.getComponent()) &&
                        tem.getKey().equals(pi.getKey()) &&
                        tem.getHash().equals(pi.getHash()) &&
                        tem.getMessage().equals(pi.getMessage())) {
                    // 找到相同的缺陷
                    findFlag = true;

                    if(tem.getStatus().equals("OPEN") && pi.getStatus().equals("OPEN")){
//                        System.out.println("***************************************");
//                        System.out.println("* 延续的缺陷：");
//                        System.out.println("* " + pi.getType());
//                        System.out.println("* " + pi.getComponent());
//                        System.out.println("* " + pi.getLine());
//                        System.out.println("* " + pi.getMessage());
//                        System.out.println("***************************************");
                    } else if(tem.getStatus().equals("CLOSED") && pi.getStatus().equals("CLOSED")) {

                    } else if(tem.getStatus().equals("OPEN") && pi.getStatus().equals("CLOSED")) {
                        System.out.println("***************************************");
                        System.out.println("* 修复的缺陷：");
                        System.out.println("* " + pi.getType());
                        if(pi.getType().equals("BUG")) {
                            numOfBug++;
                            bugOfFix++;
                        }else {
                            numOfVul++;
                            vulOfFix++;
                        }
                        numOfFix++;
                        System.out.println("* " + pi.getComponent());
                        System.out.println("* " + pi.getLine());
                        System.out.println("* " + pi.getMessage());
                        System.out.println("***************************************");
                    } else {
                        System.out.println("***************************************");
                        System.out.println("* 重现的缺陷：");
                        System.out.println("* " + pi.getType());
                        if(pi.getType().equals("BUG")) {
                            numOfBug++;
                            bugOfReopen++;
                        }else {
                            numOfVul++;
                            vulOfReopen++;
                        }
                        numOfReopen++;
                        System.out.println("* " + pi.getComponent());
                        System.out.println("* " + pi.getLine());
                        System.out.println("* " + pi.getMessage());
                        System.out.println("***************************************");
                    }
                }
            }
            if(findFlag == false) {
                System.out.println("***************************************");
                System.out.println("* 新增的缺陷：");
                System.out.println("* " + pi.getType());
                if(pi.getType().equals("BUG")) {
                    numOfBug++;
                    bugOfNew++;
                }else {
                    numOfVul++;
                    vulOfNew++;
                }
                numOfNew++;
                System.out.println("* " + pi.getComponent());
                System.out.println("* " + pi.getLine());
                System.out.println("* " + pi.getMessage());
                System.out.println("***************************************");
            }
        }

        for(sonarProjectIssue pi : lastProjIssues) {
            boolean findFlag = false;
            for(sonarProjectIssue tem : curProjIssues) {
                if (tem.getComponent().equals(pi.getComponent()) &&
                        tem.getKey().equals(pi.getKey()) &&
                        tem.getHash().equals(pi.getHash()) &&
                        tem.getMessage().equals(pi.getMessage())) {
                    // 找到相同的缺陷
                    findFlag = true;
                }
            }
            if(findFlag == false) {
                System.out.println("***************************************");
                System.out.println("* 消失的缺陷：");
                System.out.println("* " + pi.getType());
                if(pi.getType().equals("BUG")) {
                    numOfBug++;
                    bugOfDelete++;
                }else {
                    numOfVul++;
                    vulOfDelete++;
                }
                numOfDelete++;
                System.out.println("* " + pi.getComponent());
                System.out.println("* " + pi.getLine());
                System.out.println("* " + pi.getMessage());
                System.out.println("***************************************");
            }
        }
//        System.out.println("numOfBug: " + numOfBug);
//        System.out.println("numOfVul: " + numOfVul);
//        System.out.println("修复的缺陷" + numOfFix);
//        System.out.println("重现的缺陷" + numOfReopen);
//        System.out.println("新增的缺陷" + numOfNew);
//        System.out.println("消失的缺陷" + numOfDelete);


        System.out.println("新增的bug缺陷：" + bugOfNew);
        System.out.println("新增的vul缺陷：" + vulOfNew + "\n");

        System.out.println("消失的bug缺陷：" + bugOfDelete);
        System.out.println("消失的vul缺陷：" + vulOfDelete + "\n");

        System.out.println("修复的vul缺陷：" + bugOfFix);
        System.out.println("修复的bug缺陷：" + vulOfFix + "\n");

        System.out.println("重现的vul缺陷：" + bugOfReopen);
        System.out.println("重现的bug缺陷：" + vulOfReopen + "\n");


        return null;
    }

//    /**
//     * 将sourceFile所指文件夹中（包括子文件夹）所有java文件编译为class文件
//     * @param sourceFile
//     * @param targetPath
//     */
//    public static void compailJavaToClass(File sourceFile, String targetPath) {
//        try {
//            // 判断文件不为null或文件目录存在
//            if (sourceFile == null || !sourceFile.exists()){
//                return;
//            }
//            // 取得这个目录下的所有子文件对象
//            File[] files = sourceFile.listFiles();
//            // 遍历该目录下的文件对象
//            for (File f: files){
//
//                if (f.isDirectory()){
//////                    String name = f.getName();
////                    String path = f.getParentFile().getPath();
////
////                    // .：任意字符，*：任意次数
////                    // 这里表示匹配任意结尾为java的文件
////                    String pattern = ".*java";
//////                    System.out.println("path: " + path);
////                    String cmd = "javac -encoding utf-8 -d " + targetPath + " " + path + "\\*.java";
////                    System.out.println(cmd);
////                    Process proc = Runtime.getRuntime().exec(cmd);
////                    int processCode = proc.waitFor();
////                    if(processCode != 0) {
////                        System.out.println("转化失败[GitUtil:406]");
////                    }
////                    proc.destroy();
//                    compailJavaToClass(f, targetPath);
//                }else {
////                     打印文件名
//                    String name = f.getName();
//                    String path = f.getPath();
//
//                    // .：任意字符，*：任意次数
//                    // 这里表示匹配任意结尾为java的文件
//                    String pattern = ".*java";
////                    System.out.println("path: " + path);
//                    if(Pattern.matches(pattern, name)) {
//                        String cmd = "javac -encoding utf-8 -d " + targetPath + " " + path;
//                        System.out.println(cmd);
//                        Process proc = Runtime.getRuntime().exec(cmd);
//                        int processCode = proc.waitFor();
//                        if(processCode != 0) {
//                            System.out.println("转化失败[GitUtil:406]");
//                        }
//                        proc.destroy();
//                    }
//                }
//            }
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

//    public static void compailJavaToClass(File sourceFile) {
//        try {
//            // 判断文件不为null或文件目录存在
//            if (null != sourceFile){
//                if(sourceFile.isDirectory()) {
//                    File[] files = sourceFile.listFiles();
//                    for(File f : files) {
//                        compailJavaToClass(f);
//                    }
//                } else if(sourceFile.isFile() && sourceFile.getName().endsWith(".java")) {
//                    r.exec("javac " + sourceFile.getAbsolutePath());
//
//                    System.out.println("正在编译：" + sourceFile.getAbsolutePath());
//                }
//            }
//
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
}
