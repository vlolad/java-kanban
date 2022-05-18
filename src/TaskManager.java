import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

         ArrayList<Task> getTasks();
         ArrayList<EpicTask> getEpics();
         ArrayList<SubTask> getSubTasks();

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

         ArrayList<SubTask> getEpicSubTasks(int epicID);
         HistoryManager getHistoryManager();
    }
