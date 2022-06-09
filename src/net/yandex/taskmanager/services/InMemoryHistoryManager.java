package net.yandex.taskmanager.services;

import net.yandex.taskmanager.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    //private final List<Task> history = new ArrayList<>();
    private final Map <Integer, Node> customLinkedList = new HashMap<>();
    private Node first;
    private Node last;

    @Override
    public void add(Task task){
        if (last != null && task.equals(last.body)){ // Если переданный объект уже последний в истории, метод отключается.
            return;
        } else {
            if (customLinkedList.containsKey(task.getId())){
                removeNode(customLinkedList.get(task.getId()));
            }
            linkLast(task);
            customLinkedList.put(task.getId(), last);
        }
    }

    private void linkLast(Task task){
        if (customLinkedList.isEmpty()){
            first = new Node(null, task, null);
            last = first;
        } else {
            Node secondLast = last;
            last = new Node (secondLast, task, null);
            secondLast.next = last;
        }
    }

    private void removeNode(Node node) {
        if (customLinkedList.size() == 1){ // Т.е. в мапе только одна нода
            customLinkedList.clear();
        } else if (node.prev == null){ // если нода first
            Node oldNext = node.next;
            oldNext.prev = null;
            first = oldNext;
            customLinkedList.remove(node.body.getId());
        }  else if (node.next == null){ // если нода last
            Node oldPrev = node.prev;
            oldPrev.next = null;
            last = oldPrev;
            customLinkedList.remove(node.body.getId());
        } else { // если нода где-то в середине списка
            Node oldNext = node.next;
            Node oldPrev = node.prev;
            oldPrev.next = oldNext;
            oldNext.prev = oldPrev;
            customLinkedList.remove(node.body.getId());
        }
    }

    private List<Task> getTasks(){
        List<Task> taskList = new ArrayList<>();
        for (Node a = first; a != null; a = a.next){
            taskList.add(a.body);
        }
        return taskList;
    }

    @Override
    public void remove(int id){ // переписал метод с учетом нового функционала
        if (customLinkedList.containsKey(id)){
            removeNode(customLinkedList.get(id));
        }
    }

    @Override
    public List<Task> getHistory(){
        return getTasks();
    }

    private static class Node {
        private Node prev;
        private final Task body;
        private Node next;

        public Node(Node prev, Task body, Node next){
            this.body = body;
            this.prev = prev;
            this.next = next;
        }
    }
}
