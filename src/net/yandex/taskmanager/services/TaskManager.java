package net.yandex.taskmanager.services;

import net.yandex.taskmanager.model.*;

import java.util.List;

public interface TaskManager {

         List<Task> getTasks();
         List<EpicTask> getEpics();
         List<SubTask> getSubTasks();

         void clearTasks();
         void clearEpics();
         void clearSubTasks();

         Task getTaskByID(int id);
         Task getEpicByID(int id);
         Task getSubTaskByID(int id);

         void createTask(Task task);
         void createEpic(EpicTask epic);
         void createSubTask(SubTask subTask);

         void deleteTaskByID(int id);
         void deleteEpicByID(int id);
         void deleteSubTaskByID(int id);

         void updateTask(Task newTask);
         void updateEpic(EpicTask newEpic);
         void updateSubTask(SubTask newSubTask);

         List<SubTask> getEpicSubTasks(int epicID);
         HistoryManager getHistoryManager();
    }
