import java.io.Serializable;

public class FindbugsIssue implements Serializable {
    String warningType;// 警告类型
    String waringCode;// 警告码
    String message;// 报错信息
    String component;// 文件位置
    String line;// 具体行数

    public FindbugsIssue(String warningType, String waringCode, String message, String component, String line) {
        this.warningType = warningType;
        this.waringCode = waringCode;
        this.message = message;
        this.component = component;
        this.line = line;
    }

    public String getWarningType() {
        return warningType;
    }

    public void setWarningType(String warningType) {
        this.warningType = warningType;
    }

    public String getWaringCode() {
        return waringCode;
    }

    public void setWaringCode(String waringCode) {
        this.waringCode = waringCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return "FindbugsIssue{" +
                "warningType='" + warningType + '\'' +
                ", waringCode='" + waringCode + '\'' +
                ", message='" + message + '\'' +
                ", component='" + component + '\'' +
                ", line='" + line + '\'' +
                '}';
    }


}
