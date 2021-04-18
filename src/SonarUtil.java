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
    String path;
    String projectFileName;
    String projectName;
    String projectVersion = "1.0";
    String sources = "./";
    String binaries = "./";

    /**
     * 构造方法
     * @param numOfCommit
     * @param path
     * @param projectFileName
     * @param projectName
     * @param projectVersion
     * @param sources
     * @param binaries
     */
    public SonarUtil(int numOfCommit, String path, String projectFileName, String projectName, String projectVersion, String sources, String binaries) {
        this.numOfCommit = numOfCommit;
        this.path = path;
        this.projectFileName = projectFileName;
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.sources = sources;
        this.binaries = binaries;
    }

    /**
     * 在原项目目录下创建配置文件
     */
    public void createConfFile() {
        String projectPath = path.concat("\\").concat(projectFileName);
        String projectPathTem = projectPath.concat("/");
        String projectConFileName = "sonar-project.properties";
        // 创建配置文件
        File file = new File(projectPathTem, projectConFileName);
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
            fw = new FileWriter(projectPath.concat("\\").concat(projectConFileName));
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
     * @return 执行sonar-scanner指令过程所需时间的字符串形式
     */
    public String runSonarShell (String projectPath) {
        long usedTime = 0;
        try {
            long startTime =  System.currentTimeMillis();
            System.out.println("开始执行命令: sonar-scanner");
            Process proc = Runtime.getRuntime().exec("cmd.exe /c cd " + projectPath + "&& sonar-scanner");

            InputStream in =proc.getInputStream();
//            InputStream in_ =proc.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
//            BufferedReader br_ = new BufferedReader(new InputStreamReader(in_));
            StringBuffer sb = new StringBuffer();
            String message = null;
//            String message_ = null;
            while((message = br.readLine()) != null) {
                System.out.println(message);
            }

            GitUtil.clearStream(proc.getInputStream());
            GitUtil.clearStream(proc.getErrorStream());

            int processCode = proc.waitFor();
            if(processCode == 0) {
                System.out.println("扫描完成");
                long endTime =  System.currentTimeMillis();
                // 获取扫描时间
                usedTime = (endTime - startTime) ;
//                System.out.println("扫描用时" + usedTime + "s");
                System.out.println("-----------------------------");
            } else {
                System.out.println("Error in SonarUtil[113]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (usedTime) + "ms";
    }

    /**
     * 根据项目名称一次性整合sonarqube中多个版本扫描结果（bugs、codeSmells、vulnerabilities）
     * @param projectName
     */
    public HashMap<String, ArrayList<String>> combineAllSonarMeasures(String projectName) {
        HashMap<String, ArrayList<String>> measures = new HashMap<>();
        waitForTaskFinished();

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

        getAllSonarMeasuresFromWeb(paramOfSize, measures);
        getAllSonarMeasuresFromWeb(paramOfDocumentation, measures);
        getAllSonarMeasuresFromWeb(paramOfComplexity, measures);
        getAllSonarMeasuresFromWeb(paramOfCoverage, measures);
        getAllSonarMeasuresFromWeb(paramOfDuplications, measures);
        getAllSonarMeasuresFromWeb(paramOfIssues, measures);
        getAllSonarMeasuresFromWeb(paramOfMaintainability, measures);
        getAllSonarMeasuresFromWeb(paramOfReliability, measures);
        getAllSonarMeasuresFromWeb(paramOfSecurity, measures);
        getAllSonarMeasuresFromWeb(paramOfSCM, measures);
        getAllSonarMeasuresFromWeb(paramOfManagement, measures);

//            System.out.println(measures);
//        System.out.println(measures.get("last_commit_date"));


        return measures;

    }

    /**
     * 根据项目名称整合sonarqube扫描结果（bugs、codeSmells、vulnerabilities）
     */
    public HashMap<String, String> combineSonarMeasures() {
        HashMap<String, String> measures = new HashMap<>();
        waitForTaskFinished();
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

        getSonarMeasuresFromWeb(paramOfSize, measures);
        getSonarMeasuresFromWeb(paramOfDocumentation, measures);
        getSonarMeasuresFromWeb(paramOfComplexity, measures);
        getSonarMeasuresFromWeb(paramOfCoverage, measures);
        getSonarMeasuresFromWeb(paramOfDuplications, measures);
        getSonarMeasuresFromWeb(paramOfIssues, measures);
        getSonarMeasuresFromWeb(paramOfMaintainability, measures);
        getSonarMeasuresFromWeb(paramOfReliability, measures);
        getSonarMeasuresFromWeb(paramOfSecurity, measures);
        getSonarMeasuresFromWeb(paramOfSCM, measures);
        getSonarMeasuresFromWeb(paramOfManagement, measures);

//            System.out.println(measures);
//        System.out.println(measures.get("last_commit_date"));

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
            String urlStr = api.concat(URLEncoder.encode(param,"utf-8"));// 调整编码格式

            System.out.println("##################urlStr: " + urlStr);

            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
     * @return
     */
    public int getTasksNumInQueue (){
        String apiOfTasks = "http://localhost:9000/api/ce/component?component=";
        String param = this.projectName;
        JSONObject jsonObject = getJsonObjectFromApi(apiOfTasks, param);
        JSONArray queueAry = jsonObject.getJSONArray("queue");
        return queueAry.size();
    }

    /**
     * 根据参数一次性获得多个commit历史相应的指标
     * @param param 参数
     * @param measures 存储结果
     */
    public void getAllSonarMeasuresFromWeb(String param, HashMap<String, ArrayList<String>>measures) {
        try {
            String apiOfMeasures = "http://localhost:9000/api/measures/search_history?component="+projectName+"&metrics=";
            JSONObject jsonObjectOfMeasures = getJsonObjectFromApi(apiOfMeasures, param);

            if(jsonObjectOfMeasures != null) {
                JSONArray measuresAry = jsonObjectOfMeasures.getJSONArray("measures"); // 由于是数组形式，先获取measures的JSONArray对象

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
     * 根据参数获得当次commit对应的指标
     * @param param 参数
     * @param measures 存储结果
     */
    public void getSonarMeasuresFromWeb(String param, HashMap<String, String>measures) {
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
     * 展示measures中数据结果
     * @param measures
     */
    public void showMeasures(HashMap<String, String>measures) {
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

    /**
     * 从web获得issues报告
     * @return
     */
    public ArrayList<ProjectIssue> getProjIssuesReportFromWeb() {
        waitForTaskFinished();
        ArrayList<ProjectIssue> projReport = new ArrayList<>();
        try {
            String apiOfIssues = "http://localhost:9000/api/issues/search?componentKeys=" + projectName + "&types=BUG,VULNERABILITY&ps=100&pageIndex=";
            JSONObject jsonObjectOfIssues = getJsonObjectFromApi(apiOfIssues + 1, "");

            int totalNum = jsonObjectOfIssues.getIntValue("total");
            int curNum = 0;// 设置每次返回100条数据
            int pageIndex = 1;
            System.out.println("total: " + totalNum);

            while (curNum < totalNum) {
                JSONArray issuesAry = jsonObjectOfIssues.getJSONArray("issues"); // 数组形式
                if(issuesAry.size() == 0) {
                    break;// 表示数据已经全部读取完毕
                }

                for(int i = 0; i < issuesAry.size(); i++) {
                    // 获取文件名
                    JSONObject jsonObj = issuesAry.getJSONObject(i);
                    String component = jsonObj.getString("component");
                    String[] split = component.split(":");
                    component = split[1];

                    JSONObject textRangeObj = jsonObj.getJSONObject("textRange");
                    if(textRangeObj == null) {
                        continue;// 若不存在属性：行数，则跳过该缺陷
                    }
                    String line = textRangeObj.getString("endLine");// 记录最后一行

                    ProjectIssue pi = new ProjectIssue(jsonObj.getString("key"),
                            jsonObj.getString("rule"),
                            jsonObj.getString("severity"),
                            component,
                            jsonObj.getString("project"),
                            line,
                            jsonObj.getString("hash"),
                            jsonObj.getString("status"),
                            jsonObj.getString("message"),
                            jsonObj.getString("type"),
                            textRangeObj);
                    projReport.add(pi);
                }

                curNum += 100;// 设置每次返回100条数据
                pageIndex++;

                apiOfIssues = "http://localhost:9000/api/issues/search?componentKeys=" + projectName + "&types=BUG&types=VULNERABILITY&ps=100&pageIndex=";
                jsonObjectOfIssues = getJsonObjectFromApi(apiOfIssues + pageIndex, "");


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

            System.out.println("version: " + (i + 1));
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

    /**
     * 等待sonar后台完成扫描结果的处理
     * @return
     */
    public boolean waitForTaskFinished() {
        int numOfTasksInQueue = getTasksNumInQueue();
        while (numOfTasksInQueue != 0) {
            try {
                Thread.currentThread().sleep(2000);// 暂停一段时间再发送请求
            } catch (Exception e) {
                e.printStackTrace();
            }
            numOfTasksInQueue = getTasksNumInQueue();
            System.out.println("待处理任务个数：" + numOfTasksInQueue);
        }
        return true;
    }


    public ProjectInfo getProjInfo(int num) {
        String projectPath = path.concat("\\").concat(projectFileName);
        String projectInfoPath = path.concat("\\").concat(projectFileName).concat("-workspace").concat("\\").concat("ProjectInfo");
        String newProjectPath = path + "\\" + projectFileName + "-workspace" + "\\" + projectFileName;

        ProjectInfo projInfoTem = new ProjectInfo();
        GitUtil.refreshWorkspaceByCMD(path, projectFileName);
        GitUtil.runRollBackGitShell(newProjectPath, num);

        try {
            File file = new File(projectInfoPath, num + "");
            if(file.exists()) {
                // 通过本地获取扫描信息（issues和measures）
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(projectInfoPath.concat("\\").concat(num+"")));
                projInfoTem = ((ProjectInfo)ois.readObject());
                ois.close();

            } else {
                // 通过api获取扫描信息（issues和measures）
                String time = runSonarShell(newProjectPath);
                HashMap<String, String> measures = combineSonarMeasures();
                measures.put("scan_time", time);// 添加扫描时间指标

                projInfoTem.setMeasures(measures);
                projInfoTem.setIssues(getProjIssuesReportFromWeb());
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(projectInfoPath.concat("\\").concat(num+"")));
                oos.writeObject(projInfoTem);
                oos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return projInfoTem;
    }
}
