package me.xujichang.plugin.java;

import me.xujichang.plugin.java.extension.BintrayExtensions;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.Date;

/**
 * @author developerXu
 * java 版本的插件
 */
public class UploadPluginImpl implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        BintrayExtensions bintrayExtensions = target.getExtensions().create("BintrayExtensions", BintrayExtensions.class);
        target.task("time").doLast(new Action<Task>() {
            @Override
            public void execute(Task task) {

            }
        }).doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {

            }
        });
    }
}
