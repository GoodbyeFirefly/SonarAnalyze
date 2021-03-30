import java.io.*;

public class GitUtil {

    public static long runRollBackGitShell(String projectPath, int num) {
        try {
            long startTime =  System.currentTimeMillis();
            long endTime, usedTime;
            System.out.println("开始执行命令:git reset --hard HEAD~" + num);// 注意这里必须是HEAD~1而不是HEAD^
            String comm = "cmd.exe /c cd " + projectPath + "&& git reset --hard HEAD~" + num;
            Process proc = Runtime.getRuntime().exec(comm);

            int processCode = proc.waitFor();
            if(processCode == 0) {
                System.out.println("命令执行完成");
                endTime =  System.currentTimeMillis();
                // 获取扫描时间
                usedTime = (endTime - startTime) / 1000;
                System.out.println("更新commit数据用时" + usedTime + "s");
                System.out.println("-----------------------------");
                return usedTime;
            } else {
                System.out.println("执行失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String runDiffGitShell(String projectPath) {
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec("cmd.exe /c cd " + projectPath + "&& git diff HEAD~1");
            InputStream in = process.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);
            StringBuffer sb = new StringBuffer();
            String message;
            while((message = br.readLine()) != null) {
                sb.append(message + "\n");
            }
            System.out.println(sb);
            return sb.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    //复制文件夹
    public static void copyFolder(File srcFolder, File desFolder) throws IOException {
        //遍历原始文件夹里面的所有文件及文件夹
        File[] files = srcFolder.listFiles();
        for (File srcFile : files) {
            //如果是文件夹
            if (srcFile.isDirectory()){
                //在新的文件夹内创建一个和srcFile文件夹同名文件夹，然后再递归调用，判断文件夹里面的情况，然后做出相应处理
                String srcFileName = srcFile.getName();
                File newFolder = new File(desFolder, srcFileName);
                if (!newFolder.exists()){
                    newFolder.mkdir();
                    copyFolder(srcFile,newFolder);
                }
                //如果是文件
            }else {
                String srcFileName = srcFile.getName();
                File desFile = new File(desFolder, srcFileName);
                copyFile(srcFile,desFile);
            }
        }
    }

    public static void copyFolderByCMD(String scrFolder, String desFolder) {
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec("cmd.exe /c " + "xcopy " + scrFolder + " " + desFolder + " /s /f /h");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //复制文件
    public static void copyFile (File srcFile, File desFile) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile),"utf-8"));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(desFile),"utf-8"));
//        char[] chars = new char[1024];
//        int len;

        String temp = null;
        while((temp = br.readLine())!=null){
            bw.write(temp + "\n");
            bw.flush();
        }
//        while ((len = br.read(chars)) != -1) {
//            bw.write(chars,0,len);
//            //bw.flush();
//        }
        br.close();
        bw.close();
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

    public static void refreshWorkspace(String path, String projectFileName) {
        try {
            File f = new File(path + "\\" + projectFileName + "-workspace");
            deleteFile(f);// 删除该目录下所有文件夹和文件
            f = new File(path + "\\" + projectFileName + "-workspace");
            f.mkdir();// 重新创建目录
            File desFolder = new File(path + "\\" + projectFileName + "-workspace" + "\\" + projectFileName);
            File srcFolder = new File(path + "\\" + projectFileName);
            if(desFolder.mkdir()) {
                copyFolder(srcFolder, desFolder);
            } else {
                System.out.println("Error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

}
