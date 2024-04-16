package pro.sky.telegrambot;

import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDate;
import java.time.LocalTime;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TelegramBotApplicationTestRestTemplateTest {
	@LocalServerPort
	private int port;
	@Autowired
	private TestRestTemplate testRestTemplate;
	@Autowired
	private NotificationTaskRepository notificationTaskRepository;
	private final Faker faker = new Faker();

    @AfterEach
    public void afterEach(){
		notificationTaskRepository.deleteAll();
	}

	@BeforeEach
	public void beforeEach(){
		int numbersOfNotes = 10;
		for (int i = 0; i < numbersOfNotes; i++) {
			NotificationTask notificationTask = new NotificationTask();
			notificationTask.setId(i + 1);
			notificationTask.setChatId((i+1)*10);
			notificationTask.setMessageText(faker.music().genre());
			notificationTask.setDate(LocalDate.now().plusDays(i+1));
			notificationTask.setTime(LocalTime.now());
			notificationTaskRepository.save(notificationTask);
		}
	}

	@Test
	public void test(){
		int numbersOfNotes = 10;
		for (int i = 0; i < numbersOfNotes; i++) {
			NotificationTask notificationTask = new NotificationTask();
			notificationTask.setId(i + 1);
			notificationTask.setChatId((i+1)*10);
			notificationTask.setMessageText(faker.music().genre());
			notificationTask.setDate(LocalDate.now().plusDays(i+1));
			notificationTask.setTime(LocalTime.now());
			notificationTaskRepository.save(notificationTask);
		}
		System.out.println(notificationTaskRepository.findAll());
	}

}
