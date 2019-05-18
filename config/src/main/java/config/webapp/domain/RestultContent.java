package config.webapp.domain;

/**
 * 返回消息实体
 */
public class RestultContent {
    /*
    * SUCCESS or ERROR
    * */
    private Integer status;
    private Object data;
    private String errorMsg;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
