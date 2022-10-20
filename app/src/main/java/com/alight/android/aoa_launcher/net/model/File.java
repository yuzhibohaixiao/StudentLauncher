package com.alight.android.aoa_launcher.net.model;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
import java.text.Collator;
import java.util.Date;
import java.util.Locale;

@Table(name = "download_file")
public class File implements Serializable, Comparable<File> {
    //下载完成
    public static final int DOWNLOAD_COMPLETE = 4;
    //准备下载
    public static final int DOWNLOAD_REDYA = 0;
    //下载进行中
    public static final int DOWNLOAD_PROCEED = 1;
    //暂停
    public static final int DOWNLOAD_PAUSE = 2;
    //出错
    public static final int DOWNLOAD_ERROR = 3;

    @Column(name = "id", isId = true)
    private String id;
    /**
     * 文件名
     */
    @Column(name = "file_name")
    private String fileName;
    /**
     * 1-下载 2-下载完成
     */
    @Column(name = "type")
    private int type;
    @Column(name = "url")
    private String url;
    /**
     * 文件类型
     */
    @Column(name = "file_type")
    private String fileType;
    /**
     * 保存路径
     */
    @Column(name = "path")
    private String path;
    /**
     * 文件大小
     */
    @Column(name = "size")
    private Long size;
    @Column(name = "size_str")
    private String sizeStr;
    /**
     * 下载状态
     */
    @Column(name = "status")
    private int status;
    /**
     * 下载进度
     */
    @Column(name = "progress")
    private int progress;
    /**
     * 下载速度
     */
    private String speed;
    @Column(name = "create_time")
    private Date createTime;

    private boolean checked;


    private boolean installed;

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    private int versionCode;

    private String packName;

    private Integer topFlag;

    public boolean isToBeUpdated() {
        return toBeUpdated;
    }

    public void setToBeUpdated(boolean toBeUpdated) {
        this.toBeUpdated = toBeUpdated;
    }

    //待更新
    private boolean toBeUpdated;

    public Integer getTopFlag() {
        return topFlag;
    }

    public void setTopFlag(Integer topFlag) {
        this.topFlag = topFlag;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    private int format;

    public int getIconState() {
        return iconState;
    }

    public void setIconState(int iconState) {
        this.iconState = iconState;
    }

    /**
     * 1为显示 0为未显示
     */
    private int iconState = 0;

    /**
     * 是否显示单选框
     */
    private boolean show;

    private int seq;

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getSizeStr() {
        return sizeStr;
    }

    public void setSizeStr(String sizeStr) {
        this.sizeStr = sizeStr;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public String toString() {
        return "File{" +
                "id='" + id + '\'' +
                ", fileName='" + fileName + '\'' +
                ", type=" + type +
                ", url='" + url + '\'' +
                ", fileType='" + fileType + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", sizeStr='" + sizeStr + '\'' +
                ", status=" + status +
                ", progress=" + progress +
                ", speed='" + speed + '\'' +
                ", createTime=" + createTime +
                ", checked=" + checked +
                ", show=" + show +
                '}';
    }

    @Override
    public int compareTo(File o) {
        if (this.getFormat() > o.getFormat()) {
            return 1;
        } else if (this.getFormat() < o.getFormat()) {
            return -1;
        } else {
            return Collator.getInstance(Locale.CHINESE).compare(this.fileName, o.fileName);
        }
    }
}
