package net.yandex.taskmanager.services;

import net.yandex.taskmanager.model.EpicTask;
import net.yandex.taskmanager.model.SubTask;
import net.yandex.taskmanager.model.Task;
import net.yandex.taskmanager.model.TaskTypes;

import java.io.IOException;
import java.util.List;

public class HTTPTaskManager extends FileBackedTasksManager implements TaskManager {

    private final KVTaskClient client;
    private String keySaving = "default";
    private final String TASKS = "tasks";
    private final String EPICS = "epics";
    private final String SUBTASKS = "subtasks";
    private final String HISTORY = "history";

    public HTTPTaskManager(String address, boolean check) {
            client = new KVTaskClient(address);
            if (check) load();
    }

    public HTTPTaskManager(String address) {
        this(address, false);
    }

    public HTTPTaskManager() {
        this("http://localhost:8078", false);
    }

    public String getKeySaving() {
        return keySaving;
    }

    public void setKeySaving(String keySaving) {
        this.keySaving = keySaving;
    }

    @Override
    public void save() {
            StringBuilder saving = new StringBuilder(); // Собираю весь менеджер в строку
            saving.append(getTemplateSave()); // Сначала шаблон
            for (Task task : getTasks()) {
                saving.append(toString(task)).append("\n");
            }
            for (EpicTask task : getEpics()) {
                saving.append(toString(task)).append("\n");
            }
            for (SubTask task : getSubTasks()) {
                saving.append(toString(task)).append("\n");
            }
            saving.append(toString(getHistoryManager())).append(" ");
            client.put(getKeySaving(), saving.toString());
    }

    public void load() {
        String loadedManager = client.load(keySaving);
        if (loadedManager.isBlank()) {
            System.out.println("Загрузка с сервера не удалась. Возможно, менеджер ещё не сохранялся.");
            return;
        }
        // Очистка всех мап, чтобы полностью обновить менеджер
        clearEpics();
        clearSubTasks();
        clearTasks();

        String[] lineManager = loadedManager.split("\n");
        int newId = 0; // Для поиска последнего использованного id.

        //System.out.println(Arrays.toString(lineManager));
        // Задачи записаны в промежутке [1] - [length()-2] // История на [length()-1]
        for (int i = 1; i <= (lineManager.length - 2); i++) {
            String[] record = lineManager[i].split(",");

            if (Integer.parseInt(record[0]) > newId) {
                newId = Integer.parseInt(record[0]);
            } // запоминаем последнее присвоенное значение ID

            switch (TaskTypes.valueOf(record[1])) {
                case EPIC:
                    getEpicsMap().put(Integer.parseInt(record[0]), (EpicTask) fromString(lineManager[i]));
                    continue;
                case SUBTASK:
                    SubTask subTask = (SubTask) fromString(lineManager[i]);
                    if (subTask == null) {
                        throw new ManagerSaveException("Ошибка в загрузке SubTask");
                    }
                    getSubTasksMap().put(subTask.getId(), subTask);
                    getEpicsMap().get(subTask.getEpicID())
                            .addSubTaskID(subTask.getId());
                    continue;
                case TASK:
                    getTasksMap().put(Integer.parseInt(record[0]), fromString(lineManager[i]));
            }
        }

        if (!lineManager[lineManager.length - 1].isBlank()){
            List<Integer> historyId = historyFromString(lineManager[lineManager.length - 1]); // Получаем историю
            for (Integer id : historyId) {
                if (getEpicsMap().containsKey(id)) {
                    getHistoryManager().add(getEpicsMap().get(id));
                } else if (getSubTasksMap().containsKey(id)) {
                    getHistoryManager().add(getSubTasksMap().get(id));
                } else if (getTasksMap().containsKey(id)) {
                    getHistoryManager().add(getTasksMap().get(id));
                } else {
                    throw new ManagerSaveException("Ошибка в загрузке истории просмотров.");
                }
            }
        }

        setID(newId);
        fullUpdateSortedTasks();
        for (EpicTask epic : getEpics()) {
            checkEpicForDone(epic.getId());
        }
    }

}
