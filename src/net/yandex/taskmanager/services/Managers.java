package net.yandex.taskmanager.services;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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

    public static HTTPTaskManager getHTTPManager(URL address) throws IOException, InterruptedException {
        return new HTTPTaskManager(address);
    }
}
