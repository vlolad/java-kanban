package net.yandex.taskmanager.services;

import net.yandex.taskmanager.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    //private final List<Task> history = new ArrayList<>();
    private final Map<Integer, Node> customLinkedList = new HashMap<>();
    private Node first;
    private Node last;

    @Override
    public void add(Task task) {
        /*if (last != null && task.equals(last.body)){ // Если переданный объект уже последний в истории, метод отключается.
            return;
        } else {*/
        if (task == null) {
            return;
        }

        if (customLinkedList.containsKey(task.getId())) {
            removeNode(customLinkedList.get(task.getId()));
        }
        linkLast(task);
        customLinkedList.put(task.getId(), last);
        //}
    }

    private void linkLast(Task task) {
        if (customLinkedList.isEmpty()) {
            first = new Node(null, task, null);
            last = first;
        } else {
            Node secondLast = last;
            last = new Node(secondLast, task, null);
            secondLast.next = last;
        }
    }

    private void removeNode(Node node) {
        final Node prev = node.prev;
        final Node next = node.next;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            node.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }
        customLinkedList.remove(node.body.getId());
        node.body = null;
    }

    private List<Task> getTasks() {
        List<Task> taskList = new ArrayList<>();
        for (Node a = first; a != null; a = a.next) {
            taskList.add(a.body);
        }
        return taskList;
    }

    @Override
    public void remove(int id) { // переписал метод с учетом нового функционала
        if (customLinkedList.containsKey(id)) {
            removeNode(customLinkedList.get(id));
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private static class Node {
        private Node prev;
        private Task body;
        private Node next;

        public Node(Node prev, Task body, Node next) {
            this.body = body;
            this.prev = prev;
            this.next = next;
        }
    }
}
