package me.xujichang.plugin.java.task;

import me.xujichang.plugin.java.bean.Artifact;
import org.gradle.api.internal.file.CopyActionProcessingStreamAction;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal;
import org.gradle.api.internal.tasks.SimpleWorkResult;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.WorkResult;

import java.io.File;

/**
 * des:
 *
 * @author xjc
 * created at 2017/11/9 18:34
 */
public class RecordingCopyTask extends Copy {
    public static String NAME = "_bintrayRecordingCopy";
    private Artifact artifact;

    @SkipWhenEmpty
    @Optional
    @Override
    public File getDestinationDir() {
        return getProject().getBuildDir();
    }

    @Override
    protected CopyAction createCopyAction() {
        super.createCopyAction();
        String intoDir = getProject().relativePath(getRootSpec().getDestinationDir());

        CopyAction copyAction = new CopyAction() {
            @Override
            public WorkResult execute(CopyActionProcessingStream stream) {
                CopyStreamAction action = new CopyStreamAction(intoDir);
                stream.process(action);
                return new SimpleWorkResult(getDidWork());
            }
        };
        return copyAction;
    }

    class CopyStreamAction implements CopyActionProcessingStreamAction {
        private String intoDir;

        public CopyStreamAction(String intoDir) {
            this.intoDir = intoDir;
        }

        @Override
        public void processFile(FileCopyDetailsInternal details) {
            if (!details.isDirectory()) {
                String destRelPath = intoDir != null ? (intoDir + '/' + details.getPath()) : details.getPath();
                artifact = new Artifact();
                artifact.setFile(details.getFile());
                artifact.setPath(destRelPath);
            }
            setDidWork(true);
        }
    }
}
