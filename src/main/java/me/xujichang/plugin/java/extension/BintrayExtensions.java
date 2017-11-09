package me.xujichang.plugin.java.extension;

import groovy.lang.Closure;
import me.xujichang.plugin.java.task.RecordingCopyTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.specs.Spec;
import org.gradle.util.ConfigureUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * des:
 *
 * @author developerXu
 * created by 2017/11/8 0:05
 */
public class BintrayExtensions {

    private Project project;

    private String apiUrl;

    private String user;

    private String key;

    private PackageConfig pkg = new PackageConfig();

    private String[] configurations;

    private String[] publications;

    private RecordingCopyTask filesSpec;

    private boolean publish;

    private boolean override;

    private boolean dryRun;

    public BintrayExtensions(Project project) {
        this.project = project;
    }

    public void pkg(Closure closure) {
        ConfigureUtil.configure(closure, pkg);
    }

    public void filesSpec(Closure closure) {
        Map<String, Class<? extends AbstractTask>> options = new HashMap<>();
        options.put("type", RecordingCopyTask.class);
        filesSpec = (RecordingCopyTask) project.task(options, RecordingCopyTask.NAME);
        ConfigureUtil.configure(closure, filesSpec);
        filesSpec.getOutputs().upToDateWhen(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task element) {
                return false;
            }
        });
    }

    /**
     * 对包的相关配置属性
     */
    class PackageConfig {
        private String repo;
        /**
         * An alternative user for the package
         */
        private String userOrg;
        private String name;
        private String desc;
        private String websiteUrl;
        private String issueTrackerUrl;
        private String vcsUrl;
        private String githubRepo;
        private String githubReleaseNotesFile;
        private boolean publicDownloadNumbers;
        private String[] licenses;
        private String[] labels;
        private Map attributes;
        private VersionConfig version = new VersionConfig();
        private DebianConfig debian = new DebianConfig();

        public void debian(Closure closure) {
            ConfigureUtil.configure(closure, debian);
        }

        public void version(Closure closure) {
            ConfigureUtil.configure(closure, version);
        }

        public String getRepo() {
            return repo;
        }

        public void setRepo(String repo) {
            this.repo = repo;
        }

        public String getUserOrg() {
            return userOrg;
        }

        public void setUserOrg(String userOrg) {
            this.userOrg = userOrg;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getWebsiteUrl() {
            return websiteUrl;
        }

        public void setWebsiteUrl(String websiteUrl) {
            this.websiteUrl = websiteUrl;
        }

        public String getIssueTrackerUrl() {
            return issueTrackerUrl;
        }

        public void setIssueTrackerUrl(String issueTrackerUrl) {
            this.issueTrackerUrl = issueTrackerUrl;
        }

        public String getVcsUrl() {
            return vcsUrl;
        }

        public void setVcsUrl(String vcsUrl) {
            this.vcsUrl = vcsUrl;
        }

        public String getGithubRepo() {
            return githubRepo;
        }

        public void setGithubRepo(String githubRepo) {
            this.githubRepo = githubRepo;
        }

        public String getGithubReleaseNotesFile() {
            return githubReleaseNotesFile;
        }

        public void setGithubReleaseNotesFile(String githubReleaseNotesFile) {
            this.githubReleaseNotesFile = githubReleaseNotesFile;
        }

        public boolean isPublicDownloadNumbers() {
            return publicDownloadNumbers;
        }

        public void setPublicDownloadNumbers(boolean publicDownloadNumbers) {
            this.publicDownloadNumbers = publicDownloadNumbers;
        }

        public String[] getLicenses() {
            return licenses;
        }

        public void setLicenses(String[] licenses) {
            this.licenses = licenses;
        }

        public String[] getLabels() {
            return labels;
        }

        public void setLabels(String[] labels) {
            this.labels = labels;
        }

        public Map getAttributes() {
            return attributes;
        }

        public void setAttributes(Map attributes) {
            this.attributes = attributes;
        }

        public VersionConfig getVersion() {
            return version;
        }

        public void setVersion(VersionConfig version) {
            this.version = version;
        }

        public DebianConfig getDebian() {
            return debian;
        }

        public void setDebian(DebianConfig debian) {
            this.debian = debian;
        }
    }

    class DebianConfig {
        private String distribution;
        private String component;
        private String architecture;

        public String getDistribution() {
            return distribution;
        }

        public void setDistribution(String distribution) {
            this.distribution = distribution;
        }

        public String getComponent() {
            return component;
        }

        public void setComponent(String component) {
            this.component = component;
        }

        public String getArchitecture() {
            return architecture;
        }

        public void setArchitecture(String architecture) {
            this.architecture = architecture;
        }

    }

    class VersionConfig {
        private String name;
        private String desc;
        private String released;
        private String vcsTag;
        private Map attributes;
        private GpgConfig gpg = new GpgConfig();
        private MavenCentralSyncConfig mavenCentralSync = new MavenCentralSyncConfig();

        public void gpg(Closure closure) {
            ConfigureUtil.configure(closure, gpg);
        }

        public void mavenCentralSync(Closure closure) {
            ConfigureUtil.configure(closure, mavenCentralSync);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getReleased() {
            return released;
        }

        public void setReleased(String released) {
            this.released = released;
        }

        public String getVcsTag() {
            return vcsTag;
        }

        public void setVcsTag(String vcsTag) {
            this.vcsTag = vcsTag;
        }

        public Map getAttributes() {
            return attributes;
        }

        public void setAttributes(Map attributes) {
            this.attributes = attributes;
        }
    }

    class GpgConfig {
        private boolean sign;
        private String passphrase;

        public boolean isSign() {
            return sign;
        }

        public void setSign(boolean sign) {
            this.sign = sign;
        }

        public String getPassphrase() {
            return passphrase;
        }

        public void setPassphrase(String passphrase) {
            this.passphrase = passphrase;
        }
    }

    class MavenCentralSyncConfig {
        private Boolean sync;
        private String user;
        private String password;
        private String close;

        public Boolean getSync() {
            return sync;
        }

        public void setSync(Boolean sync) {
            this.sync = sync;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getClose() {
            return close;
        }

        public void setClose(String close) {
            this.close = close;
        }
    }
}