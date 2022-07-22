package net.yandex.taskmanager.services;

import java.io.File;

public class Managers {

    public static HTTPTaskManager getDefault() {
        return new HTTPTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedTasksManager getFileBacked(String csvFileName) {
        return new FileBackedTasksManager(new File(csvFileName + ".csv"));
    }

    public static InMemoryTaskManager getInMemoryTaskManager() {
        return new InMemoryTaskManager();
    }
}
