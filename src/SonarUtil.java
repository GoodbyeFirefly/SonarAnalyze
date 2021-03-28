import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class SonarUtil {
    int numOfCommit;
    String projectPath;
    String fileName;
    String projectName;
    String projectVersion = "1.0";
    String sources = "./";
    String binaries = "./";

    public SonarUtil(int numOfCommit, String projectPath, String fileName, String projectName, String projectVersion, String sources, String binaries) {
        this.numOfCommit = numOfCommit;
        this.projectPath = projectPath;
        this.fileName = fileName;
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.sources = sources;
        this.binaries = binaries;
    }

    /**
     * 创建配置文件
     * @param projectPath
     * @param fileName
     * @param projectName
     * @param projectVersion
     * @param sources
     * @param binaries
     */
    public void createConfFile(String projectPath, String fileName,String projectName,
                                  String projectVersion, String sources, String binaries) {

        // 创建配置文件
        File file = new File(projectPath, fileName);
        if(file.exists()) {
            System.out.println("配置文件已存在，开始更新配置");
        } else {
            try {
                file.createNewFile();
                System.out.println("配置文件创建成功，开始更新配置");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 向文件中添加配置信息
        FileWriter fw;
        try {
            fw = new FileWriter(projectPath + fileName);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("sonar.projectKey=" + projectName + "\n");
            bw.write("sonar.projectName=" + projectName + "\n");
            bw.write("sonar.projectVersion=" + projectVersion + "\n");
            bw.write("sonar.sources=" + sources + "\n");
            bw.write("sonar.java.binaries=" + binaries + "\n");
            bw.write("sonar.sourceEncoding=UTF-8\n");

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(2);
        }
    }

    /**
     * 打开命令行，切换到对应目录，执行sonar-scanner指令
     * @param projectPath
     */
    public String runSonarShell (String projectPath) {
        long usedTime = 0;
        try {
//            Process proc = Runtime.getRuntime().exec("cmd.exe /c copy D:\\tmp\\my.txt D:\\tmp\\my_by_only_cmd.txt");
            long startTime =  System.currentTimeMillis();
            System.out.println("开始执行命令");
            Process proc = Runtime.getRuntime().exec("cmd.exe /c cd " + projectPath + "&& sonar-scanner");

            InputStream is1 = proc.getInputStream();
            new Thread(() -> {
                BufferedReader br = new BufferedReader(new InputStreamReader(is1));
                try{
                    while(br.readLine() != null) ;
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }).start();
            InputStream is2 = proc.getErrorStream();
            BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
            while(br2.readLine() != null){}

            int processCode = proc.waitFor();
            if(processCode == 0) {
                System.out.println("扫描完成");
                long endTime =  System.currentTimeMillis();
                // 获取扫描时间
                usedTime = (endTime - startTime) ;

//                System.out.println("扫描用时" + usedTime + "s");
                System.out.println("-----------------------------");
            } else {
                System.out.println("扫描失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return (usedTime) + "ms";

    }

    /**
     * 根据项目名称获取sonarqube扫描结果（bugs、codeSmells、vulnerabilities）
     * @param projectName
     */
    public HashMap<String, ArrayList<String>> getAllJsonData(String projectName) {
        HashMap<String, ArrayList<String>> measures = new HashMap<>();
        if(taskFinished(projectName)) {
            System.out.println("开始接收数据");

//        String paramOfSize = "component=" + projectName + "&metrics=accessors,classes,directories,files,functions,lines,ncloc,comment_lines,comment_lines_density,comment_lines_data,generated_lines,new_lines,generated_ncloc,ncloc_language_distribution,ncloc_data";
            String paramOfSize = "classes,directories,files,functions,lines,ncloc,comment_lines,comment_lines_density,comment_lines_data,generated_lines,new_lines,generated_ncloc,ncloc_language_distribution,ncloc_data";
            String paramOfDocumentation = "commented_out_code_lines";
            String paramOfComplexity = "complexity,cognitive_complexity,class_complexity,file_complexity,complexity_in_classes,complexity_in_functions,file_complexity_distribution,class_complexity_distribution,function_complexity_distribution";
            String paramOfCoverage = "branch_coverage,conditions_to_cover,it_branch_coverage,it_coverage,it_conditions_to_cover,it_line_coverage,line_coverage,it_lines_to_cover,lines_to_cover,overall_conditions_to_cover,coverage,overall_branch_coverage,new_branch_coverage,new_conditions_to_cover,new_it_branch_coverage,new_it_coverage,new_it_conditions_to_cover,new_it_line_coverage,new_line_coverage,new_it_lines_to_cover,new_lines_to_cover,new_overall_conditions_to_cover,new_coverage,new_overall_branch_coverage,new_overall_coverage,overall_coverage,conditions_by_line,coverage_line_hits_data,covered_conditions_by_line,overall_conditions_by_line,overall_coverage_line_hits_data,overall_covered_conditions_by_line,overall_line_coverage,executable_lines_data,it_conditions_by_line,it_coverage_line_hits_data,it_covered_conditions_by_line,it_uncovered_conditions,it_uncovered_lines";
            String paramOfDuplications = "duplicated_blocks,duplicated_lines,duplicated_lines_density,new_duplicated_blocks,new_duplicated_lines,new_duplicated_lines_density,duplicated_files,duplications_data";
            String paramOfIssues = "blocker_violations,critical_violations,info_violations,violations,major_violations,minor_violations,new_blocker_violations,new_critical_violations,new_info_violations,new_violations,new_major_violations,new_minor_violations,open_issues,confirmed_issues,false_positive_issues";
            String paramOfMaintainability = "code_smells,new_code_smells,new_technical_debt,new_maintainability_rating,effort_to_reach_maintainability_rating_a,sqale_rating";
            String paramOfReliability = "bugs,new_bugs";
            String paramOfSecurity = "vulnerabilities,new_vulnerabilities";
            String paramOfSCM = "last_commit_date";
            String paramOfManagement = "burned_budget,business_value";

            getAllSonarMeasures(projectName, paramOfSize, measures);
            getAllSonarMeasures(projectName, paramOfDocumentation, measures);
            getAllSonarMeasures(projectName, paramOfComplexity, measures);
            getAllSonarMeasures(projectName, paramOfCoverage, measures);
            getAllSonarMeasures(projectName, paramOfDuplications, measures);
            getAllSonarMeasures(projectName, paramOfIssues, measures);
            getAllSonarMeasures(projectName, paramOfMaintainability, measures);
            getAllSonarMeasures(projectName, paramOfReliability, measures);
            getAllSonarMeasures(projectName, paramOfSecurity, measures);
            getAllSonarMeasures(projectName, paramOfSCM, measures);
            getAllSonarMeasures(projectName, paramOfManagement, measures);

//            System.out.println(measures);
//        System.out.println(measures.get("last_commit_date"));

        }
        return measures;

    }

    /**
     * 根据项目名称获取sonarqube扫描结果（bugs、codeSmells、vulnerabilities）
     * @param projectName
     */
    public HashMap<String, String> getJsonData(String projectName) {
        HashMap<String, String> measures = new HashMap<>();
        if(taskFinished(projectName)) {
            System.out.println("开始接收数据");

//        String paramOfSize = "component=" + projectName + "&metrics=accessors,classes,directories,files,functions,lines,ncloc,comment_lines,comment_lines_density,comment_lines_data,generated_lines,new_lines,generated_ncloc,ncloc_language_distribution,ncloc_data";
            String paramOfSize = "classes,directories,files,functions,lines,ncloc,comment_lines,comment_lines_density,comment_lines_data,generated_lines,new_lines,generated_ncloc,ncloc_language_distribution,ncloc_data";
            String paramOfDocumentation = "commented_out_code_lines";
            String paramOfComplexity = "complexity,cognitive_complexity,class_complexity,file_complexity,complexity_in_classes,complexity_in_functions,file_complexity_distribution,class_complexity_distribution,function_complexity_distribution";
            String paramOfCoverage = "branch_coverage,conditions_to_cover,it_branch_coverage,it_coverage,it_conditions_to_cover,it_line_coverage,line_coverage,it_lines_to_cover,lines_to_cover,overall_conditions_to_cover,coverage,overall_branch_coverage,new_branch_coverage,new_conditions_to_cover,new_it_branch_coverage,new_it_coverage,new_it_conditions_to_cover,new_it_line_coverage,new_line_coverage,new_it_lines_to_cover,new_lines_to_cover,new_overall_conditions_to_cover,new_coverage,new_overall_branch_coverage,new_overall_coverage,overall_coverage,conditions_by_line,coverage_line_hits_data,covered_conditions_by_line,overall_conditions_by_line,overall_coverage_line_hits_data,overall_covered_conditions_by_line,overall_line_coverage,executable_lines_data,it_conditions_by_line,it_coverage_line_hits_data,it_covered_conditions_by_line,it_uncovered_conditions,it_uncovered_lines";
            String paramOfDuplications = "duplicated_blocks,duplicated_lines,duplicated_lines_density,new_duplicated_blocks,new_duplicated_lines,new_duplicated_lines_density,duplicated_files,duplications_data";
            String paramOfIssues = "blocker_violations,critical_violations,info_violations,violations,major_violations,minor_violations,new_blocker_violations,new_critical_violations,new_info_violations,new_violations,new_major_violations,new_minor_violations,open_issues,confirmed_issues,false_positive_issues";
            String paramOfMaintainability = "code_smells,new_code_smells,new_technical_debt,new_maintainability_rating,effort_to_reach_maintainability_rating_a,sqale_rating";
            String paramOfReliability = "bugs,new_bugs";
            String paramOfSecurity = "vulnerabilities,new_vulnerabilities";
            String paramOfSCM = "last_commit_date";
            String paramOfManagement = "burned_budget,business_value";

            getSonarMeasures(projectName, paramOfSize, measures);
            getSonarMeasures(projectName, paramOfDocumentation, measures);
            getSonarMeasures(projectName, paramOfComplexity, measures);
            getSonarMeasures(projectName, paramOfCoverage, measures);
            getSonarMeasures(projectName, paramOfDuplications, measures);
            getSonarMeasures(projectName, paramOfIssues, measures);
            getSonarMeasures(projectName, paramOfMaintainability, measures);
            getSonarMeasures(projectName, paramOfReliability, measures);
            getSonarMeasures(projectName, paramOfSecurity, measures);
            getSonarMeasures(projectName, paramOfSCM, measures);
            getSonarMeasures(projectName, paramOfManagement, measures);

//            System.out.println(measures);
//        System.out.println(measures.get("last_commit_date"));

        }
        return measures;

    }


    /**
     * 根据接口地址和参数，返回获取到的JSONObject
     * @param api
     * @param param
     * @return
     */
    public JSONObject getJsonObjectFromApi(String api, String param) {
        JSONObject jsonObject = null;
        PrintWriter out = null;
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
//            URL url = new URL(api);
            String urlStr = api.concat(URLEncoder.encode(param,"utf-8"));
//            System.out.println("urlStr" + urlStr);
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            System.out.println("ResponseCode" + connection.getResponseCode());
            connection.setRequestMethod("GET");// 这里先默认只用get方式

            // 发送参数
//            connection.setDoOutput(true);
//            out = new PrintWriter(connection.getOutputStream());
//            out.print(param);
//            out.flush();

            // 接受结果
            is = connection.getInputStream();
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            // 通过流读取结果
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            // 解析json数据
            String backJson = sb.toString();                                    // 获得json字符串
            jsonObject = JSONObject.parseObject(backJson);           // 将字符串转换为JSONObject对象

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(is != null) is.close();
                if(br != null) br.close();
                if(out != null) out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    /**
     * 返回当前sonarqube中该项目的任务队列里任务数目
     * @param projectName
     * @return
     */
    public int getTasksNumInQueue (String projectName){
        String apiOfTasks = "http://localhost:9000/api/ce/component?component=";
        String param = projectName;
        JSONObject jsonObject = getJsonObjectFromApi(apiOfTasks, param);
        JSONArray queueAry = jsonObject.getJSONArray("queue");
        return queueAry.size();
    }

    /**
     * 根据参数获得相应的指标
     * @param param 向接口发送的参数
     * @return 各种参数的值
     */
    public void getAllSonarMeasures(String projectName, String param, HashMap<String, ArrayList<String>>measures) {
        try {
//            String apiOfTasks = "http://localhost:9000/api/ce/component?";
//            JSONObject jsonObjectOfTasks = getJsonObjectFromApi(apiOfTasks, )

            String apiOfMeasures = "http://localhost:9000/api/measures/search_history?component="+projectName+"&metrics=";
            JSONObject jsonObjectOfMeasures = getJsonObjectFromApi(apiOfMeasures, param);
//            JSONObject componentObj = jsonObjectOfMeasures.getJSONObject("component");    // 获取component的JSONObject对象

            if(jsonObjectOfMeasures != null) {
//                System.out.println("jsonObjectOfMeasures:" + jsonObjectOfMeasures);
                JSONArray measuresAry = jsonObjectOfMeasures.getJSONArray("measures"); // 由于是数组形式，先获取measures的JSONArray对象
//                System.out.println("measuresAry:" + measuresAry);

                for(int i = 0; i < measuresAry.size(); i++) {
                    JSONObject measuresObj = measuresAry.getJSONObject(i);       // 获取measures的JSONObject对象
                    String key = measuresObj.getString("metric");
                    JSONArray historyAry = measuresObj.getJSONArray("history");
                    ArrayList<String> valueList = new ArrayList<>();// 存放该指标中的所有历史数据
                    for(int j = historyAry.size() - 1; j >= historyAry.size() - numOfCommit; j--) {
                        JSONObject valueObj = historyAry.getJSONObject(j);
                        String value = valueObj.getString("value");
                        valueList.add(value);
                    }
                    measures.put(key, valueList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据参数获得相应的指标
     * @param param 向接口发送的参数
     * @return 各种参数的值
     */
    public void getSonarMeasures(String projectName, String param, HashMap<String, String>measures) {
        try {
            String apiOfMeasures = "http://localhost:9000/api/measures/search_history?component="+projectName+"&metrics=";
            JSONObject jsonObjectOfMeasures = getJsonObjectFromApi(apiOfMeasures, param);
            if(jsonObjectOfMeasures != null) {
                JSONArray measuresAry = jsonObjectOfMeasures.getJSONArray("measures"); // 由于是数组形式，先获取measures的JSONArray对象
                for(int i = 0; i < measuresAry.size(); i++) {
                    JSONObject measuresObj = measuresAry.getJSONObject(i);       // 获取measures的JSONObject对象
                    String key = measuresObj.getString("metric");
                    JSONArray historyAry = measuresObj.getJSONArray("history");
                    // 获取最新一次的数据
                    int j = historyAry.size() - 1;
                    JSONObject valueObj = historyAry.getJSONObject(j);
                    String value = valueObj.getString("value");
                    measures.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 展示从sonar获得的数据结果
     * @param measures
     */
    public void showMeasures(HashMap<String, ArrayList<String>>measures) {
        System.out.println("***************ScanTime***************");
        System.out.println("scan_time:" + measures.get("scan_time"));
        System.out.println();

        System.out.println("***************Size***************");
        System.out.println("accessors:" + measures.get("accessors"));
        System.out.println("classes:" + measures.get("classes"));
        System.out.println("directories:" + measures.get("directories"));
        System.out.println("files:" + measures.get("files"));
        System.out.println("functions:" + measures.get("functions"));
        System.out.println("lines:" + measures.get("lines"));
        System.out.println("ncloc:" + measures.get("ncloc"));
        System.out.println("comment_lines:" + measures.get("comment_lines"));
        System.out.println("comment_lines_density:" + measures.get("comment_lines_density"));
        System.out.println("comment_lines_data:" + measures.get("comment_lines_data"));
        System.out.println("generated_lines:" + measures.get("generated_lines"));
        System.out.println("new_lines:" + measures.get("new_lines"));
        System.out.println("generated_ncloc:" + measures.get("generated_ncloc"));
        System.out.println("ncloc_language_distribution:" + measures.get("ncloc_language_distribution"));
        System.out.println("ncloc_data:" + measures.get("ncloc_data"));

        System.out.println();
        System.out.println("***************Complexity***************");
        System.out.println("complexity:" + measures.get("complexity"));
        System.out.println("cognitive_complexity:" + measures.get("cognitive_complexity"));
        System.out.println("class_complexity:" + measures.get("class_complexity"));
        System.out.println("file_complexity:" + measures.get("file_complexity"));
        System.out.println("complexity_in_classes:" + measures.get("complexity_in_classes"));
        System.out.println("complexity_in_functions:" + measures.get("complexity_in_functions"));
        System.out.println("file_complexity_distribution:" + measures.get("file_complexity_distribution"));
        System.out.println("class_complexity_distribution:" + measures.get("class_complexity_distribution"));
        System.out.println("function_complexity_distribution:" + measures.get("function_complexity_distribution"));

        System.out.println();
        System.out.println("***************Duplications***************");
        System.out.println("duplicated_blocks:" + measures.get("duplicated_blocks"));
        System.out.println("duplicated_lines:" + measures.get("duplicated_lines"));
        System.out.println("duplicated_lines_density:" + measures.get("duplicated_lines_density"));
        System.out.println("new_duplicated_blocks:" + measures.get("new_duplicated_blocks"));
        System.out.println("new_duplicated_lines:" + measures.get("new_duplicated_lines"));
        System.out.println("new_duplicated_lines_density:" + measures.get("new_duplicated_lines_density"));
        System.out.println("duplicated_files:" + measures.get("duplicated_files"));
        System.out.println("duplications_data:" + measures.get("duplications_data"));

        System.out.println();
        System.out.println("***************Documentation***************");
        System.out.println("commented_out_code_lines:" + measures.get("commented_out_code_lines"));

        System.out.println();
        System.out.println("***************Coverage***************");
        System.out.println("branch_coverage:" + measures.get("branch_coverage"));
        System.out.println("conditions_to_cover:" + measures.get("conditions_to_cover"));
        System.out.println("it_branch_coverage:" + measures.get("it_branch_coverage"));
        System.out.println("it_coverage:" + measures.get("it_coverage"));
        System.out.println("it_conditions_to_cover:" + measures.get("it_conditions_to_cover"));
        System.out.println("it_line_coverage:" + measures.get("it_line_coverage"));
        System.out.println("line_coverage:" + measures.get("line_coverage"));
        System.out.println("it_lines_to_cover:" + measures.get("it_lines_to_cover"));
        System.out.println("lines_to_cover:" + measures.get("lines_to_cover"));
        System.out.println("overall_conditions_to_cover:" + measures.get("overall_conditions_to_cover"));
        System.out.println("coverage:" + measures.get("coverage"));
        System.out.println("overall_branch_coverage:" + measures.get("overall_branch_coverage"));
        System.out.println("new_branch_coverage:" + measures.get("new_branch_coverage"));
        System.out.println("new_conditions_to_cover:" + measures.get("new_conditions_to_cover"));
        System.out.println("new_it_branch_coverage:" + measures.get("new_it_branch_coverage"));
        System.out.println("new_it_coverage:" + measures.get("new_it_coverage"));
        System.out.println("new_it_conditions_to_cover:" + measures.get("new_it_conditions_to_cover"));
        System.out.println("new_it_line_coverage:" + measures.get("new_it_line_coverage"));
        System.out.println("new_line_coverage:" + measures.get("new_line_coverage"));
        System.out.println("new_it_lines_to_cover:" + measures.get("new_it_lines_to_cover"));
        System.out.println("new_lines_to_cover:" + measures.get("new_lines_to_cover"));
        System.out.println("new_overall_conditions_to_cover:" + measures.get("new_overall_conditions_to_cover"));
        System.out.println("new_coverage:" + measures.get("new_coverage"));
        System.out.println("new_overall_branch_coverage:" + measures.get("new_overall_branch_coverage"));
        System.out.println("new_overall_coverage:" + measures.get("new_overall_coverage"));
        System.out.println("overall_coverage:" + measures.get("overall_coverage"));
        System.out.println("conditions_by_line:" + measures.get("conditions_by_line"));
        System.out.println("coverage_line_hits_data:" + measures.get("coverage_line_hits_data"));
        System.out.println("covered_conditions_by_line:" + measures.get("covered_conditions_by_line"));
        System.out.println("overall_conditions_by_line:" + measures.get("overall_conditions_by_line"));
        System.out.println("overall_coverage_line_hits_data:" + measures.get("overall_coverage_line_hits_data"));
        System.out.println("overall_covered_conditions_by_line:" + measures.get("overall_covered_conditions_by_line"));
        System.out.println("overall_line_coverage:" + measures.get("overall_line_coverage"));
        System.out.println("executable_lines_data:" + measures.get("executable_lines_data"));
        System.out.println("it_conditions_by_line:" + measures.get("it_conditions_by_line"));
        System.out.println("it_coverage_line_hits_data:" + measures.get("it_coverage_line_hits_data"));
        System.out.println("it_covered_conditions_by_line:" + measures.get("it_covered_conditions_by_line"));
        System.out.println("it_uncovered_conditions:" + measures.get("it_uncovered_conditions"));
        System.out.println("it_uncovered_lines:" + measures.get("it_uncovered_lines"));

        System.out.println();
        System.out.println("***************Issues***************");
        System.out.println("blocker_violations:" + measures.get("blocker_violations"));
        System.out.println("critical_violations:" + measures.get("critical_violations"));
        System.out.println("info_violations:" + measures.get("info_violations"));
        System.out.println("violations:" + measures.get("violations"));
        System.out.println("major_violations:" + measures.get("major_violations"));
        System.out.println("minor_violations:" + measures.get("minor_violations"));
        System.out.println("new_blocker_violations:" + measures.get("new_blocker_violations"));
        System.out.println("new_critical_violations:" + measures.get("new_critical_violations"));
        System.out.println("new_info_violations:" + measures.get("new_info_violations"));
        System.out.println("new_violations:" + measures.get("new_violations"));
        System.out.println("new_major_violations:" + measures.get("new_major_violations"));
        System.out.println("new_minor_violations:" + measures.get("new_minor_violations"));
        System.out.println("open_issues:" + measures.get("open_issues"));
        System.out.println("confirmed_issues:" + measures.get("commented"));
        System.out.println("commented:" + measures.get("confirmed_issues"));
        System.out.println("false_positive_issues:" + measures.get("false_positive_issues"));

        System.out.println();
        System.out.println("***************Maintainability***************");
        System.out.println("code_smells:" + measures.get("code_smells"));
        System.out.println("new_code_smells:" + measures.get("new_code_smells"));
        System.out.println("new_technical_debt:" + measures.get("new_technical_debt"));
        System.out.println("new_maintainability_rating:" + measures.get("new_maintainability_rating"));
        System.out.println("effort_to_reach_maintainability_rating_a:" + measures.get("effort_to_reach_maintainability_rating_a"));
        System.out.println("sqale_rating:" + measures.get("sqale_rating"));

        System.out.println();
        System.out.println("***************Reliability***************");
        System.out.println("bugs:" + measures.get("bugs"));
        System.out.println("new_bugs:" + measures.get("new_bugs"));

        System.out.println();
        System.out.println("***************Security***************");
        System.out.println("vulnerabilities:" + measures.get("vulnerabilities"));
        System.out.println("new_vulnerabilities:" + measures.get("new_vulnerabilities"));

        System.out.println();
        System.out.println("***************SCM***************");
        System.out.println("last_commit_date:" + measures.get("last_commit_date"));

        System.out.println();
        System.out.println("***************Management***************");
        System.out.println("burned_budget:" + measures.get("burned_budget"));
        System.out.println("business_value:" + measures.get("business_value"));


    }

    public ArrayList<ProjectIssue> getProjReport(String projectName) {
        ArrayList<ProjectIssue> projReport = new ArrayList<>();
        try {
            String apiOfIssues = "http://localhost:9000/api/issues/search?componentKeys=";
            JSONObject jsonObjectOfMeasures = getJsonObjectFromApi(apiOfIssues, projectName);
//            JSONObject componentObj = jsonObjectOfMeasures.getJSONObject("component");    // 获取component的JSONObject对象

            String total = jsonObjectOfMeasures.getString("total");
//            String total = totalObj.getString("total");
//            projReport.put("total", (JSONObject) total);
            System.out.println("total: " + total);

            JSONArray issuesAry = jsonObjectOfMeasures.getJSONArray("issues"); // 数组形式
            for(int i = 0; i < issuesAry.size(); i++) {
                JSONObject jsonObj = issuesAry.getJSONObject(i);
                JSONObject textRangeObj = jsonObj.getJSONObject("textRange");
                ProjectIssue pi = new ProjectIssue(jsonObj.getString("key"),
                        jsonObj.getString("rule"),
                        jsonObj.getString("severity"),
                        jsonObj.getString("component"),
                        jsonObj.getString("project"),
                        jsonObj.getString("line"),
                        jsonObj.getString("hash"),
                        jsonObj.getString("status"),
                        jsonObj.getString("message"),
                        jsonObj.getString("type"),
                        textRangeObj);
                projReport.add(pi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return projReport;
    }

    /**
     * 展示所有版本的report
     * @param projReports
     */
    public void showProjInfo(ArrayList<ArrayList<ProjectIssue>> projReports) {
        System.out.println();
        for (int i = 0; i < projReports.size(); i++) {

            System.out.println("version: " + i);
            ArrayList<ProjectIssue> projInfo = projReports.get(i);

//            Collection jsonObj = projInfo.values();
//            for(Iterator iterator = jsonObj.iterator(); iterator.hasNext();) {
//                Object obj = iterator.next();
//                System.out.println("obj:" + obj);
//            }

            System.out.println(projInfo);
            System.out.println();
        }
    }

    public boolean taskFinished(String projectName) {
        int numOfTasksInQueue = getTasksNumInQueue(projectName);
        while (numOfTasksInQueue != 0) {
            try {
                Thread.currentThread().sleep(5000);// 暂停一段时间再发送请求
            } catch (Exception e) {
                e.printStackTrace();
            }
            numOfTasksInQueue = getTasksNumInQueue(projectName);
            System.out.println("待处理任务个数：" + numOfTasksInQueue);
        }
        return true;
    }
}
