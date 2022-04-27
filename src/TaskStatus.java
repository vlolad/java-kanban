public enum TaskStatus {
    NEW ("New"),
    IN_PROGRESS ("In progress"),
    DONE ("Done");

    private final String description;

    TaskStatus(String description){
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
