import net.yandex.taskmanager.services.InMemoryTaskManager;

import org.junit.jupiter.api.BeforeEach;

public class InMemoryTaskManagerTest extends TaskManagerTest <InMemoryTaskManager>{

    @BeforeEach
    public void createTaskManager(){
        setTaskManager(new InMemoryTaskManager());
    }

}
