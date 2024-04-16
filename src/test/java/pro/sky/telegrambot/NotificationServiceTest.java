package pro.sky.telegrambot;

import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.service.NotificationService;

import java.time.LocalDate;
import java.time.LocalTime;

@SpringBootTest
public class NotificationServiceTest {
    @Autowired
    private final NotificationTaskRepository notificationTaskRepository;
    private final NotificationService notificationService;
    private final Faker faker = new Faker();

    public NotificationServiceTest(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.notificationService = new NotificationService(this.notificationTaskRepository);
    }


    public void afterEach(){
        notificationTaskRepository.deleteAll();
    }


    public void beforeEach(){
        int numbersOfNotes = 10;
        for (int i = 0; i < numbersOfNotes; i++) {
            NotificationTask notificationTask = new NotificationTask();
            notificationTask.setId(i + 1);
            notificationTask.setChatId((i+1)*10);
            notificationTask.setMessageText(faker.music().genre());
            notificationTask.setDate(LocalDate.now().plusDays(i+1));
            notificationTask.setTime(LocalTime.now());
        }
    }

    @Test
    public void test(){
        beforeEach();
        System.out.println(notificationTaskRepository.findAll());
        afterEach();
    }
}
