package config.webapp.domain;

import java.util.Date;

public class CommonFile {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 上传日期
     */
    private Date uploadDate;

    /**
     * 上传人
     */
    private String uploadManId;

    /**
     *  文件类型
     */
    private String fileType;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUploadManId() {
        return uploadManId;
    }

    public void setUploadManId(String uploadManId) {
        this.uploadManId = uploadManId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
