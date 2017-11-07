package me.xujichang.plugin.java.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

/**
 *
 * @author developerXu
 */
public class AndroidArchivesTask extends DefaultTask {
    @Optional
    private String message;

    @TaskAction
    public void action() {

    }

}
