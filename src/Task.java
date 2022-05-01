public class Task {
    private int id;
    private String name;
    private String description;
    private TaskStatus status;

    public Task() { }
    // Конструктор для создания новой таски (без статуса)
    public Task(String name, String description){
        status = TaskStatus.NEW;
        this.name = name;
        this.description = description;
    }
    // Конструктор для создания новой таски (со статусом)
    public Task(String name, String description, TaskStatus status){
        this.name = name;
        this.description = description;
        this.status = status;
    }
    // Конструкторы для изменения существующей таски
    public Task(int id,String name, String description, TaskStatus status){
        this.id = id;
        this.status = status;
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString(){
        String showBody;
        if (description == null){
            showBody = "null";
        } else {
            showBody = Integer.toString(description.length());
        }
        return "Task{name= «" + name + "» | id=«" + id + "» | description(length)=«"
                + showBody + "» | status=«" + status + "»}";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
