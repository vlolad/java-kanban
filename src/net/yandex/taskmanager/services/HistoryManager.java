package net.yandex.taskmanager.services;

import net.yandex.taskmanager.model.Task;

import java.util.List;

public interface HistoryManager {

    void add(Task task);
    void remove(int id);
    List<Task> getHistory();

}
