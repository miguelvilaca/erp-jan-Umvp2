package cronapi.chatbot.methods;

public class WatsonAssistantOptions {
    private String workspaceId;
    private String userName;
    private String password;
    private String versionDate = "2017-05-26";

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVersionDate() {
        return versionDate;
    }
}
