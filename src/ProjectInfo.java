import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

// 支持序列化存储
public class ProjectInfo implements Serializable {
    HashMap<String, String> measures;
    ArrayList<ProjectIssue> issues;

    public ProjectInfo(HashMap<String, String> measures, ArrayList<ProjectIssue> issues) {
        this.measures = measures;
        this.issues = issues;
    }

    public ProjectInfo() {
    }

    public HashMap<String, String> getMeasures() {
        return measures;
    }

    public void setMeasures(HashMap<String, String> measures) {
        this.measures = measures;
    }

    public ArrayList<ProjectIssue> getIssues() {
        return issues;
    }

    public void setIssues(ArrayList<ProjectIssue> issues) {
        this.issues = issues;
    }

    @Override
    public String toString() {
        return "ProjectInfo{" +
                "measures=" + measures +
                ", issues=" + issues +
                '}';
    }
}
