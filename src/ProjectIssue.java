import com.alibaba.fastjson.JSONObject;

public class ProjectIssue {
    String key;
    String rule;
    String severity;
    String component;
    String project;
    String hash;
    String status;
    String message;
    String type;
    JSONObject textRange;

    public ProjectIssue(String key, String rule, String severity, String component, String project, String hash, String status, String message, String type, JSONObject textRange) {
        this.key = key;
        this.rule = rule;
        this.severity = severity;
        this.component = component;
        this.project = project;
        this.hash = hash;
        this.status = status;
        this.message = message;
        this.type = type;
        this.textRange = textRange;
    }

    @Override
    public String toString() {
        return "key='" + key + '\'' +
                "\nrule='" + rule + '\'' +
                "\nseverity='" + severity + '\'' +
                "\ncomponent='" + component + '\'' +
                "\nproject='" + project + '\'' +
                "\nhash='" + hash + '\'' +
                "\nstatus='" + status + '\'' +
                "\nmessage='" + message + '\'' +
                "\ntype='" + type + '\'' +
                "\ntextRange=" + textRange + "\n\n";
    }

    public String getKey() {
        return key;
    }

    public String getRule() {
        return rule;
    }

    public String getSeverity() {
        return severity;
    }

    public String getComponent() {
        return component;
    }

    public String getProject() {
        return project;
    }

    public String getHash() {
        return hash;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public JSONObject getTextRange() {
        return textRange;
    }
}