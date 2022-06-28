package net.yandex.taskmanager.services;

import net.yandex.taskmanager.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private static File SAVE;
    private static final String TEMPLATE_SAVE = "id,type,name,status,description,epic\n";

    public FileBackedTasksManager(File file) {
        SAVE = file;
    }

    public static void main(String[] args) {

        FileBackedTasksManager taskManager = new FileBackedTasksManager(new File("save.csv"));

        taskManager.createTask(new Task("Task123", "hehe", TaskStatus.NEW)); // id 1
        taskManager.createEpic(new EpicTask("FirstEpic", "boom")); // id 2
        taskManager.createSubTask(new SubTask("1subtask1", "hehah", TaskStatus.IN_PROGRESS, 2)); // id 3

        taskManager.getEpicByID(2);
        taskManager.getSubTaskByID(3);
        taskManager.getTaskByID(1);
        System.out.println(taskManager.getHistoryManager().getHistory()); // 2,3,1 in history

        FileBackedTasksManager newTaskManager = loadFromFile(SAVE);
        System.out.println(newTaskManager.getHistoryManager().getHistory()); // В истории те же 2,3,1
        System.out.println(newTaskManager.getTasks());
        System.out.println(newTaskManager.getEpics());
        System.out.println(newTaskManager.getSubTasks());
    }

    public void save() {
        try (FileWriter saving = new FileWriter(SAVE)) {
            saving.write(TEMPLATE_SAVE);
            for (Task task : getTasks()) {
                saving.write(toString(task) + "\n");
            }
            for (EpicTask task : getEpics()) {
                saving.write(toString(task) + "\n");
            }
            for (SubTask task : getSubTasks()) {
                saving.write(toString(task) + "\n");
            }
            saving.write("\n");

            saving.write(toString(getHistoryManager()));

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при создании файла сохранения.");
        }
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager newTaskManager = new FileBackedTasksManager(SAVE);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Пропускаем первую строчку
            while (br.ready()) {
                int newId = 0; // для поиска последнего использованного ID
                // Сначала заполняем мапы для разных видов тасков
                String line = br.readLine();
                if (!line.isBlank() || !line.isEmpty()) {

                    String[] record = line.split(",");

                    switch (TaskTypes.valueOf(record[1])) {
                        case EPIC:
                            newTaskManager.getEpicsMap().put(Integer.parseInt(record[0]), (EpicTask) fromString(line));
                            continue;
                        case SUBTASK:
                            SubTask subTask = (SubTask) fromString(line);
                            if (subTask == null) {
                                throw new ManagerSaveException("Ошибка в загрузке SubTask");
                            }
                            newTaskManager.getSubTasksMap().put(subTask.getId(), subTask);
                            newTaskManager.getEpicsMap().get(subTask.getEpicID())
                                    .addSubTaskID(subTask.getId()); // Добавляем в эпик ID его сабтаска
                            continue;
                        case TASK:
                            newTaskManager.getTasksMap().put(Integer.parseInt(record[0]), fromString(line));
                            continue;
                    }
                    // запоминаем последнее присвоенное значение ID
                    if (Integer.parseInt(record[0]) > newId) {
                        newId = Integer.parseInt(record[0]);
                    }
                    //System.out.println("Наибольший ID сейчас: " + newId);
                } else {
                    line = br.readLine(); // Пропускаем пустую строку

                    if (line != null) {
                        List<Integer> historyId = historyFromString(line);
                        for (Integer id : historyId) {
                            if (newTaskManager.getEpicsMap().containsKey(id)) {
                                newTaskManager.getHistoryManager().add(newTaskManager.getEpicsMap().get(id));
                            } else if (newTaskManager.getSubTasksMap().containsKey(id)) {
                                newTaskManager.getHistoryManager().add(newTaskManager.getSubTasksMap().get(id));
                            } else if (newTaskManager.getTasksMap().containsKey(id)) {
                                newTaskManager.getHistoryManager().add(newTaskManager.getTasksMap().get(id));
                            } else {
                                throw new ManagerSaveException("Ошибка в загрузке истории просмотров.");
                            }
                        }
                    }
                }

                newTaskManager.setID(newId);
                for (EpicTask epic : newTaskManager.getEpics()) {
                    newTaskManager.checkEpicForDone(epic.getId());
                }
                return newTaskManager;
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке файла");
        }
        return newTaskManager;
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubTasks() {
        super.clearSubTasks();
        save();
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createEpic(EpicTask epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void createSubTask(SubTask subTask) {
        super.createSubTask(subTask);
        save();
    }

    @Override
    public void deleteTaskByID(int id) {
        super.deleteTaskByID(id);
        save();
    }

    @Override
    public void deleteEpicByID(int id) {
        super.deleteEpicByID(id);
        save();
    }

    @Override
    public void deleteSubTaskByID(int id) {
        super.deleteSubTaskByID(id);
        save();
    }

    @Override
    public void updateTask(Task newTask) {
        super.updateTask(newTask);
        save();
    }

    @Override
    public void updateEpic(EpicTask newEpic) {
        super.updateEpic(newEpic);
        save();
    }

    @Override
    public void updateSubTask(SubTask newSubTask) {
        super.updateSubTask(newSubTask);
        save();
    }

    @Override
    public Task getTaskByID(int id) {
        Task task = super.getTaskByID(id);
        save();
        return task;
    }

    @Override
    public Task getEpicByID(int id) {
        Task task = super.getEpicByID(id);
        save();
        return task;
    }

    @Override
    public Task getSubTaskByID(int id) {
        Task task = super.getSubTaskByID(id);
        save();
        return task;
    }

    private String toString(Task task) {
        String[] line = new String[6];
        line[0] = Integer.toString(task.getId());
        line[2] = task.getName();
        line[3] = String.valueOf(task.getStatus());
        line[4] = task.getDescription();
        // Часть элементов заполняются в зависимости от типа таски.
        // Не смог сделать через instanceof вместе со switch
        if (task instanceof EpicTask) {
            line[1] = String.valueOf(TaskTypes.EPIC);
        } else if (task instanceof SubTask) {
            SubTask newTask = (SubTask) task;
            line[1] = String.valueOf(TaskTypes.SUBTASK);
            line[5] = Integer.toString(newTask.getEpicID());
        } else {
            line[1] = String.valueOf(TaskTypes.TASK);
        }

        return String.join(",", line);
    }

    private static Task fromString(String value) {
        String[] line = value.split(",");
        //assert line[0] != null;
        switch (TaskTypes.valueOf(line[1])) {
            case EPIC:
                return new EpicTask(Integer.parseInt(line[0]), line[2], line[4]);
            case SUBTASK:
                return new SubTask(Integer.parseInt(line[0]), line[2], line[4],
                        TaskStatus.valueOf(line[3]), Integer.parseInt(line[5]));
            case TASK:
                return new Task(Integer.parseInt(line[0]), line[2], line[4], TaskStatus.valueOf(line[3]));
        }
        return null;
    }

    private static String toString(HistoryManager manager) {
        StringJoiner result = new StringJoiner(",");
        for (Task task : manager.getHistory()) {
            result.add(Integer.toString(task.getId()));
        }
        return result.toString();
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> result = new ArrayList<>();
        for (String num : value.split(",")) {
            result.add(Integer.parseInt(num));
        }
        return result;
    }

    private static class ManagerSaveException extends RuntimeException {
        public ManagerSaveException(String message) {
        }
    }
}
