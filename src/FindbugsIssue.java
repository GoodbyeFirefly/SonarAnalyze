public class FindbugsIssue {
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
