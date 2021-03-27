import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GitUtil {

//    static String projectPath;
    public static long runRollBackGitShell(String projectPath) {
        try {
            long startTime =  System.currentTimeMillis();
            long endTime, usedTime;
            System.out.println("开始执行命令:git reset --hard HEAD~1");// 注意这里必须是HEAD~1而不是HEAD^
            Process proc = Runtime.getRuntime().exec("cmd.exe /c cd " + projectPath + "&& git reset --hard HEAD~1");

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

    public static void runDiffGitShell(String projectPath) {
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
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
