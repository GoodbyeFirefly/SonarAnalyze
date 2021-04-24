import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

// 支持序列化存储
public class ProjectInfo implements Serializable {
    HashMap<String, String> sonarMeasures;
    ArrayList<sonarProjectIssue> sonarIssues;



    public ProjectInfo() {
    }

    public HashMap<String, String> getMeasures() {
        return sonarMeasures;
    }

    public void setMeasures(HashMap<String, String> measures) {
        this.sonarMeasures = measures;
    }

    public ArrayList<sonarProjectIssue> getIssues() {
        return sonarIssues;
    }

    public void setIssues(ArrayList<sonarProjectIssue> issues) {
        this.sonarIssues = issues;
    }

    @Override
    public String toString() {
        return "ProjectInfo{" +
                "measures=" + sonarMeasures +
                ", issues=" + sonarIssues +
                '}';
    }
}
