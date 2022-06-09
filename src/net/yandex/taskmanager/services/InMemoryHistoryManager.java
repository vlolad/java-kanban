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
        if (customLinkedList.containsKey(task.getId())){
            removeNode(customLinkedList.get(task.getId()));
        }
        linkLast(task);
        customLinkedList.put(task.getId(), last);
    }

    private void linkLast(Task task){
        if (customLinkedList.isEmpty()){
            first = new Node(null, task, null);
            last = first;
        } else {
            Node secondLast = last;
            last = new Node (secondLast, task, null);
            secondLast.head = last;
        }
    }

    private void removeNode(Node node) {
        if (customLinkedList.size() == 1){ // Т.е. в мапе только одна нода
            customLinkedList.clear();
        } else if (node.tail == null){ // если нода first
            Node oldHead = node.head;
            oldHead.tail = null;
            first = oldHead;
            customLinkedList.remove(node.body.getId());
        } else if (node.head == null){ // если нода last
            Node oldTail = node.tail;
            oldTail.head = null;
            last = oldTail;
            customLinkedList.remove(node.body.getId());
        } else { // если нода где-то в середине списка
            Node oldHead = node.head;
            Node oldTail = node.tail;
            oldTail.head = oldHead;
            oldHead.tail = oldTail;
            customLinkedList.remove(node.body.getId());
        }
    }

    private List<Task> getTasks(){
        List<Task> taskList = new ArrayList<>();
        for (Node a = first; a != null; a = a.head){
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
        private Node tail;
        private final Task body;
        private Node head;

        public Node(Node tail, Task body, Node head){
            this.body = body;
            this.tail = tail;
            this.head = head;
        }
    }
}
