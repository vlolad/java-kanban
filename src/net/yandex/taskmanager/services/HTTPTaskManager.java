package net.yandex.taskmanager.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.yandex.taskmanager.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HTTPTaskManager extends FileBackedTasksManager implements TaskManager {

    private final KVTaskClient client;
    private final String TASKS = "tasks";
    private final String EPICS = "epics";
    private final String SUBTASKS = "subtasks";
    private final String HISTORY = "history";
    private static final Gson gson = new Gson();

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

    @Override
    public void save() {
        client.put(TASKS, gson.toJson(new ArrayList<>(getTasks())));
        client.put(EPICS, gson.toJson(new ArrayList<>(getEpics())));
        client.put(SUBTASKS, gson.toJson(new ArrayList<>(getSubTasks())));

        String jsonHistory = gson.toJson(
                getHistory().stream().map(Task::getId).collect(Collectors.toList())
        );
        client.put(HISTORY, jsonHistory);
    }

    public void load() {
        List<String> keys = new ArrayList<>(List.of(TASKS, EPICS, SUBTASKS));

        for (String key : keys) {
            String loaded = client.load(key);
            if ("[]".equals(loaded)) {
                System.out.println("Загрузка " + key + " с сервера не удалась. Возможно, менеджер ещё не сохранялся.");
                continue;
            }

            switch (key) {
                case TASKS:
                    ArrayList<Task> tasks = gson.fromJson(loaded,
                            new TypeToken<ArrayList<Task>>() {
                            }.getType());
                    fillMaps(tasks);
                    continue;
                case EPICS:
                    ArrayList<EpicTask> epics = gson.fromJson(loaded,
                            new TypeToken<ArrayList<EpicTask>>() {
                            }.getType());
                    fillMaps(epics);
                    continue;
                case SUBTASKS:
                    ArrayList<SubTask> subtasks = gson.fromJson(loaded,
                            new TypeToken<ArrayList<SubTask>>() {
                            }.getType());
                    fillMaps(subtasks);
            }
        }

        String loadedHistory = client.load(HISTORY);
        if (!loadedHistory.isBlank()) {
            fillHistory(loadedHistory);
        }

        for (EpicTask epic : getEpics()) {
            checkEpicForDone(epic.getId());
        }
        fullUpdateSortedTasks();// Метод обновляет сортированный список
    }

    private <T extends Task> void fillMaps(ArrayList<T> list) {
        if (list.isEmpty()) return;

        switch (list.get(0).getClass().getSimpleName()) {
            case "Task":
                for (Task task : list) {
                    getTasksMap().put(task.getId(), task);
                    checkId(task);
                }
                break;
            case "EpicTask":
                for (T task : list) {
                    getEpicsMap().put(task.getId(), (EpicTask) task);
                    checkId(task);
                }
                break;
            case "SubTask":
                for (T task : list) {
                    getSubTasksMap().put(task.getId(), (SubTask) task);
                    checkId(task);
                }
        }
    }

    private void checkId(Task task) {
        if (task.getId() > getId()) setID(task.getId());
    }

    private void fillHistory(String list) {
        ArrayList<Integer> historyList = gson.fromJson(list,
                new TypeToken<ArrayList<Integer>>() {
                }.getType());
        for (Integer id : historyList) {
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

}
