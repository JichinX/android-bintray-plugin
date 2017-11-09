package me.xujichang.plugin.java.bean;


import java.io.File;

/**
 * des:
 *
 * @author xjc
 * created at 2017/11/9 18:05
 */
public class Artifact {
    private String name;
    private String groupId;
    private String version;
    private String type;
    private String classifier;
    private File file;
    private String path;
    private String signedExtension;
    private String extension;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSignedExtension() {
        return signedExtension;
    }

    public void setSignedExtension(String signedExtension) {
        this.signedExtension = signedExtension;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artifact artifact = (Artifact) o;

        if (!name.equals(artifact.name)) return false;
        if (!groupId.equals(artifact.groupId)) return false;
        if (!version.equals(artifact.version)) return false;
        if (!type.equals(artifact.type)) return false;
        if (!classifier.equals(artifact.classifier)) return false;
        if (!file.equals(artifact.file)) return false;
        if (!path.equals(artifact.path)) return false;
        if (!signedExtension.equals(artifact.signedExtension)) return false;
        return extension.equals(artifact.extension);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
        result = 31 * result + (file != null ? file.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (signedExtension != null ? signedExtension.hashCode() : 0);
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        return result;
    }
}
