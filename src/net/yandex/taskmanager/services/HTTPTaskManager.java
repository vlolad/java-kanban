package net.yandex.taskmanager.services;

import net.yandex.taskmanager.model.EpicTask;
import net.yandex.taskmanager.model.SubTask;
import net.yandex.taskmanager.model.Task;
import net.yandex.taskmanager.model.TaskTypes;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class HTTPTaskManager extends FileBackedTasksManager implements TaskManager {

    private final URL url;
    private static KVTaskClient client;
    private String keySaving = "default";

    public HTTPTaskManager(URL address) throws IOException, InterruptedException {
        this.url = address;
        client = new KVTaskClient(address);

        load();
    }

    public String getKeySaving() {
        return keySaving;
    }

    public void setKeySaving(String keySaving) {
        this.keySaving = keySaving;
    }

    @Override
    public void save() {
        try {
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
            saving.append(toString(getHistoryManager()));
            client.put(getKeySaving(), saving.toString());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void load() throws IOException, InterruptedException {
        String loadedManager = client.load(keySaving);
        if (loadedManager.isBlank()) {
            System.out.println("Загрузка с сервера не удалась. Возможно, менеджер ещё не сохранялся.");
            return;
        }

        String[] lineManager = loadedManager.split("\n");
        int newId = 0; // Для поиска последнего использованного id.

        System.out.println(Arrays.toString(lineManager));
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
                    continue;
            }
        }
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

        setID(newId);
        fullUpdateSortedTasks();
        for (EpicTask epic : getEpics()) {
            checkEpicForDone(epic.getId());
        }
    }

}
