package net.yandex.taskmanager.services;

import java.io.File;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedTasksManager getFileBacked(String csvFileName){
        return new FileBackedTasksManager(new File(csvFileName + ".csv"));
    }
}
