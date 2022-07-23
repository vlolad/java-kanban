package net.yandex.taskmanager.services;

import java.io.File;

public class Managers {

    public static TaskManager getDefault() {
        return new HTTPTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getFileBacked(String csvFileName) {
        return new FileBackedTasksManager(new File(csvFileName + ".csv"));
    }

    public static TaskManager getInMemoryTaskManager() {
        return new InMemoryTaskManager();
    }
}
