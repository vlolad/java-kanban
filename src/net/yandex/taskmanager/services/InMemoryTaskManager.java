package net.yandex.taskmanager.services;

import net.yandex.taskmanager.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private Integer id = 0;

    // Мапы для хранения задач
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, EpicTask> epics = new HashMap<>();
    private final Map<Integer, SubTask> subTasks = new HashMap<>();

    protected final TreeSet<Task> sortedTasks = new TreeSet<>((o1, o2) -> {
        if (o1.getStartTime() == null) {
            if (o2.getStartTime() == null) {
                return o1.getId() - o2.getId();
            } else {
                return 1;
            }
        } else if (o2.getStartTime() == null) {
            return -1;
        } else if (o1.getStartTime().isBefore(o2.getStartTime())) {
            return -1;
        } else if (o1.getStartTime().isAfter(o2.getStartTime())) {
            return 1;
        }
        return 0;
    });

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    protected HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Методы для вывода необходимого вида тасков в виде списка
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<EpicTask> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    protected Map<Integer, Task> getTasksMap() {
        return tasks;
    }

    protected Map<Integer, EpicTask> getEpicsMap() {
        return epics;
    }

    protected Map<Integer, SubTask> getSubTasksMap() {
        return subTasks;
    }

    // Методы для удаления определенных типов таск
    @Override
    public void clearTasks() {
        if (!tasks.isEmpty()) {
            for (Task task : tasks.values()) {
                historyManager.remove(task.getId());
                removeFromSortedTasks(task.getId());
            }
            tasks.clear();
        }
    }

    @Override
    public void clearEpics() {
        for (EpicTask task : epics.values()) {
            historyManager.remove(task.getId());
        }
        epics.clear();
        for (SubTask task : subTasks.values()) {
            historyManager.remove(task.getId());
            removeFromSortedTasks(task.getId());
        }
        subTasks.clear();
    }

    @Override
    public void clearSubTasks() {
        for (SubTask task : subTasks.values()) {
            historyManager.remove(task.getId());
            removeFromSortedTasks(task.getId());
        }
        subTasks.clear();
        for (EpicTask epic : epics.values()) { // Также очищаются списки подзадач в эпиках и идет проверка их статуса
            epic.clearSubTasksIDs();
            epic.clearDateTime(); // Без Сабтасков EndTime не рассчитывается
            checkEpicForDone(epic.getId());
        }
    }

    // Методы для получения конкретного вида таски по ID
    @Override
    public Task getTaskByID(int id) {
        if (tasks.containsKey(id)) {
            historyManager.add(tasks.get(id));
            return tasks.get(id);
        } else {
            System.out.println("Task not found.");
            return null;
        }
    }

    @Override
    public Task getEpicByID(int id) {
        if (epics.containsKey(id)) {
            historyManager.add(epics.get(id));
            return epics.get(id);
        } else {
            System.out.println("Epic not found.");
            return null;
        }
    }

    @Override
    public Task getSubTaskByID(int id) {
        if (subTasks.containsKey(id)) {
            historyManager.add(subTasks.get(id));
            return subTasks.get(id);
        } else {
            System.out.println("Subtask not found.");
            return null;
        }
    }

    // Методы для создания тасков

    @Override
    public void createTask(Task task) {
        if (checkPeriodForOccupation(task)) {
            System.out.println("В данный период выполняется другая задача, задача не создана.");
        } else {
            int id = generateID();
            task.setId(id);
            tasks.put(id, task);
            addTaskToPrioritizedTasks(task);
        }
    }

    @Override
    public void createEpic(EpicTask epic) {
        if (checkPeriodForOccupation(epic)) {
            System.out.println("В данный период выполняется другая задача, Эпик не создан.");
        } else {
            int id = generateID();
            epic.setId(id);
            epics.put(id, epic);

            checkEpicForDone(id);
        }
    }

    @Override
    public void createSubTask(SubTask subTask) {
        if (epics.containsKey(subTask.getEpicID())) {
            if (checkPeriodForOccupation(subTask)) {
                System.out.println("В данный период выполняется другая задача, Сабтаск не создан.");
            } else {
                int id = generateID();
                subTask.setId(id);
                subTasks.put(id, subTask);
                epics.get(subTask.getEpicID()).addSubTaskID(id);

                addTaskToPrioritizedTasks(subTask);
                checkEpicForDone(subTask.getEpicID());
            }
        } else {
            System.out.println("There is no epic with ID " + subTask.getEpicID());
        }
    }

    // Методы для удаления тасков по ID
    @Override
    public void deleteTaskByID(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            historyManager.remove(id);
            removeFromSortedTasks(id);
        } else {
            System.out.println("Task not exist.");
        }
    }

    @Override
    public void deleteEpicByID(int id) {
        if (epics.containsKey(id)) {
            for (Integer subTaskID : epics.get(id).getSubTasksIDs()) {
                subTasks.remove(subTaskID);
                historyManager.remove(subTaskID); // Удаляет все сабтаски в эпике из истории
                removeFromSortedTasks(subTaskID);
            }
            epics.remove(id);
            historyManager.remove(id);
        } else {
            System.out.println("Epic not exist.");
        }
    }

    @Override
    public void deleteSubTaskByID(int id) {
        if (subTasks.containsKey(id)) {
            // Внес правки в removeSubTaskID(id) в классе EpicTask
            epics.get(subTasks.get(id).getEpicID()).removeSubTaskID(id); // Удаляем ID сабтаски из эпика
            checkEpicForDone(subTasks.get(id).getEpicID()); // Обновляем статус

            subTasks.remove(id); // Наконец удаляем сабтаск
            historyManager.remove(id);
            removeFromSortedTasks(id);
        } else {
            System.out.println("Subtask not exist.");
        }
    }

    // Методы для обновления существующих тасков
    @Override
    public void updateTask(Task newTask) {
        if (checkPeriodForOccupation(newTask)) {
            System.out.println("В данный период выполняется другая задача, задача не обновлена.");
        } else {
            if (tasks.containsKey(newTask.getId())) {
                tasks.put(newTask.getId(), newTask);
                updateTaskInPrioritizedTasks(newTask);
            } else {
                System.out.println("Task with this ID is not exist.");
            }
        }
    }

    @Override
    public void updateEpic(EpicTask newEpic) {
        if (epics.containsKey(newEpic.getId())) {
            epics.put(newEpic.getId(), newEpic);
            checkEpicForDone(newEpic.getId());
        } else {
            System.out.println("Epic with this ID is not exist.");
        }
    }

    @Override
    public void updateSubTask(SubTask newSubTask) {
        if (checkPeriodForOccupation(newSubTask)) {
            System.out.println("В данный период выполняется другая задача, задача не обновлена.");
        } else {
            if (subTasks.containsKey(newSubTask.getId()) && epics.containsKey(newSubTask.getEpicID())) {
                subTasks.put(newSubTask.getId(), newSubTask);
                updateTaskInPrioritizedTasks(newSubTask);
                checkEpicForDone(newSubTask.getEpicID());
            } else {
                System.out.println("Subtask or Epic with this ID is not exist.");
            }
        }
    }

    // Метод для получения списка всех сабтасков одного эпика
    @Override
    public List<SubTask> getEpicSubTasks(int epicID) {
        List<SubTask> subTaskArray = new ArrayList<>();
        if (epics.containsKey(epicID)) {
            for (Integer subID : epics.get(epicID).getSubTasksIDs()) {
                subTaskArray.add(subTasks.get(subID));
            }
            return subTaskArray;
        } else {
            System.out.println("Cannot find EpicTask by this ID.");
            return null;
        }
    }

    private void calculateEpicEndTime(int epicID) {
        LocalDateTime earliestDate = LocalDateTime.MAX;
        LocalDateTime latestDate = LocalDateTime.MIN;
        long resultDuration;

        for (SubTask sub : getEpicSubTasks(epicID)) {
            if (sub.getStartTime() != null) {
                if (sub.getStartTime().isBefore(earliestDate)) earliestDate = sub.getStartTime();
                if (sub.getEndTime().isAfter(latestDate)) latestDate = sub.getEndTime();
            }
        }
        if (earliestDate.isEqual(LocalDateTime.MAX)) {
            earliestDate = null;
            latestDate = null;
            resultDuration = 0;
        } else {
            resultDuration = Duration.between(earliestDate, latestDate).toMinutes();
        }
        epics.get(epicID).setStartTime(earliestDate);
        epics.get(epicID).setDuration(resultDuration);
        epics.get(epicID).setEndTime(latestDate);
    }

    @Override
    public TreeSet<Task> getPrioritizedTasks() {
        return sortedTasks;
    }

    private void addTaskToPrioritizedTasks(Task task) {
        sortedTasks.add(task);
    }

    private void updateTaskInPrioritizedTasks(Task newTask) {
        if (removeFromSortedTasks(newTask.getId())) {
            addTaskToPrioritizedTasks(newTask);
        } else {
            System.out.println("Задача с таким ID не найдена в sortedTasks.");
        }
    }

    private boolean checkPeriodForOccupation(Task newTask) {
        if (newTask.getStartTime() != null) {
            for (Task task : sortedTasks) {
                /*if (task.getStartTime() == null) {
                    return false;
                } else if (task.getId() == newTask.getId()) {
                    continue;
                } else if (task.getStartTime().isEqual(newTask.getStartTime())) {
                    return true;
                } else if (task.getEndTime().isEqual(newTask.getEndTime())) {
                    return true;
                } else if (task.getStartTime().isAfter(newTask.getStartTime())
                        && task.getEndTime().isBefore(newTask.getEndTime())) {
                    return true;
                } else if (task.getStartTime().isBefore(newTask.getStartTime())
                        && task.getEndTime().isAfter(newTask.getStartTime())) {
                    return true;
                } else if (task.getStartTime().isBefore(newTask.getEndTime())
                        && task.getEndTime().isAfter(newTask.getEndTime())) {
                    return true;
                }*/
                if (task.getStartTime() == null) {
                    return false;
                } else if (task.getId() == newTask.getId()) {
                    continue;
                } else if (!newTask.getEndTime().isAfter(task.getStartTime())
                        || !newTask.getStartTime().isBefore(task.getEndTime())) {
                    continue;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean removeFromSortedTasks(int id) {
        return sortedTasks.removeIf(task -> task.getId() == id);
    }

    protected void fullUpdateSortedTasks() {
        for (Task task : tasks.values()) {
            addTaskToPrioritizedTasks(task);
        }
        for (SubTask task : subTasks.values()) {
            addTaskToPrioritizedTasks(task);
            calculateEpicEndTime(task.getEpicID());
        }
    }

    // Проверка стстуса Эпика и изменение его дат начала-окончания
    protected void checkEpicForDone(int epicID) {
        boolean checkNotAllNEW = false;
        int subTasksLength = epics.get(epicID).getSubTasksIDs().size();
        int countDONE = 0;

        for (Integer id : epics.get(epicID).getSubTasksIDs()) {
            if (subTasks.get(id).getStatus() == TaskStatus.IN_PROGRESS) { // Если есть IN_PROGRESS - сразу ставим статус
                epics.get(epicID).setStatus(TaskStatus.IN_PROGRESS);
                return;
            } else if (subTasks.get(id).getStatus() == TaskStatus.DONE) { // Если есть хоть один DONE - начинаем считать
                countDONE++;
                checkNotAllNEW = true;
            }
        }
        // Выбираем новый статус и присваиваем
        if (!checkNotAllNEW || subTasksLength == 0) {
            epics.get(epicID).setStatus(TaskStatus.NEW);
        } else if (subTasksLength == countDONE) {
            epics.get(epicID).setStatus(TaskStatus.DONE);
        } else {
            epics.get(epicID).setStatus(TaskStatus.IN_PROGRESS);
        }

        calculateEpicEndTime(epicID);
    }

    // Метод генерирует ID для задач. Хотел сделать через UUID, но, кажется, пока это избыточно
    private int generateID() {
        id++;
        return id;
    }

    protected void setID(int newId) { // Метод необходим для корректной загрузки из файла
        id = newId;
    }
}
