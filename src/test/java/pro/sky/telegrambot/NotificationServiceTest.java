package pro.sky.telegrambot;

import io.restassured.RestAssured;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.service.NotificationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import static io.restassured.RestAssured.given;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TelegramBotApplication.class)
public class NotificationServiceTest {
	@LocalServerPort
	private int port;
	@Value("${telegram.bot.token}")
	private String token;
	@Autowired
	private TestRestTemplate testRestTemplate;
	@Autowired
	private NotificationTaskRepository notificationTaskRepository;
	@Autowired
	private NotificationService notificationService;
	private final Faker faker = new Faker();

    @AfterEach
    public void afterEach(){
		notificationTaskRepository.deleteAll();
	}
	private final long CHAT_ID = 1;

	@BeforeEach
	public void beforeEach(){
		int numbersOfNotes = 10;
		// добавление заметок на следующий день от текущего
		for (int i = 0; i < numbersOfNotes; i++) {
			NotificationTask notificationTask = new NotificationTask();
			notificationTask.setId(i + 1);
			notificationTask.setChatId(CHAT_ID);
			notificationTask.setMessageText(faker.music().genre());
			notificationTask.setDate(LocalDate.now().plusDays(i+1));
			notificationTask.setTime(LocalTime.now());
			notificationTaskRepository.save(notificationTask);
		}
		// добавление заметок на текущий день, на более позднее время от текущего
		for (int i = numbersOfNotes; i < numbersOfNotes*2; i++) {
			NotificationTask notificationTask = new NotificationTask();
			notificationTask.setId(i + 1);
			notificationTask.setChatId(1);
			notificationTask.setMessageText(faker.music().genre());
			notificationTask.setDate(LocalDate.now());
			notificationTask.setTime(LocalTime.now().plusMinutes((i + 1) * 2));
			notificationTaskRepository.save(notificationTask);
		}
		// добавление прошедших заметок
		for (int i = numbersOfNotes*2; i < numbersOfNotes*2 + numbersOfNotes; i++) {
			NotificationTask notificationTask = new NotificationTask();
			notificationTask.setId(i + 1);
			notificationTask.setChatId(CHAT_ID);
			notificationTask.setMessageText(faker.music().genre());
			notificationTask.setDate(LocalDate.now());
			notificationTask.setTime(LocalTime.now().minusMinutes((i + 1) * 2));
			notificationTaskRepository.save(notificationTask);
		}
		// добавление задач с другим chat_id
		for (int i = numbersOfNotes*3; i < numbersOfNotes*3 + numbersOfNotes; i++) {
			NotificationTask notificationTask = new NotificationTask();
			notificationTask.setId(i + 1);
			notificationTask.setChatId(CHAT_ID + 1);
			notificationTask.setMessageText(faker.music().genre());
			notificationTask.setDate(LocalDate.now());
			notificationTask.setTime(LocalTime.now().minusMinutes((i + 1) * 2));
			notificationTaskRepository.save(notificationTask);
		}

	}

	@Test
	public void test(){
		RestAssured.baseURI = "https://api.telegram.org/bot" + token;
		given().param("text", "17.04.2024 18:00 test notification")
				.param("chat_id", "1874598997")
				.when()
				.get("/sendMessage")
				.then()
				.statusCode(200);

	}

	/**
	 * Метод для тестирования метода getFutureTasks() класса NotificationService
	 */
	@Test
	public void getFutureTasks_Test(){
		// создается список заметок напрямую из базы данных c CHAT_ID и с заметками срок, которых еще не прошел
		List<NotificationTask> notificationTaskList = notificationTaskRepository.findAll()
				.stream()
				.filter(n -> n.getChatId() == CHAT_ID)
				.filter(n -> LocalDate.now().isBefore(n.getDate()) ||
						(LocalDate.now().equals(n.getDate()) && LocalTime.now().isBefore(n.getTime())))
				.toList();
		// создается список заметок с CHAT_ID через тестируемый метод срок, которых еще не прошел
		List<NotificationTask> notificationTaskList1 = notificationService.getFutureTasks(CHAT_ID);

		// полученные списки сравниваются
		Assertions.assertEquals(notificationTaskList, notificationTaskList1);

	}

	/**
	 * Метод для тестирования метода getAllTasksByChatId класса  NotificationService,
	 * который возвращает список всех заметок соответствующих chat_id
	 *
	 */
	@Test
	public void getAllTasksByChatId_Test(){

		List<NotificationTask> notificationTaskListFromDB = notificationTaskRepository.findAll()
				.stream()
				.filter(n -> n.getChatId() == CHAT_ID)
				.toList();


		List<NotificationTask> notificationTaskList = notificationService.getAllTasksByChatId(CHAT_ID);

		Assertions.assertEquals(notificationTaskList, notificationTaskListFromDB);
	}

	/**
	 * Метод для тестирования метода deletePastTasks() класса NotificationService,
	 * который удаляет по соответствующему chat_id все заметки, которые просрочены
	 * и возвращает список удаленных заметок
	 */
	@Test
	public void deletePastTasks_Test(){
		// создается список заметок напрямую из базы данных c CHAT_ID и с заметками срок, которых прошел
		List<NotificationTask> notificationTaskListFromBD = notificationTaskRepository.findAll()
				.stream()
				.filter(n -> n.getChatId() == CHAT_ID)
				.filter(n -> LocalDate.now().isAfter(n.getDate()) ||
						(LocalDate.now().equals(n.getDate()) && LocalTime.now().isAfter(n.getTime())))
				.toList();
		// удаляются просроченные заметки (задачи) и возвращается список удаленных задач
		List<NotificationTask> notificationTaskList = notificationService.deletePastTasks(CHAT_ID);

		Assertions.assertEquals(notificationTaskList, notificationTaskListFromBD);
	}

	/**
	 * Метод для тестирования метода taskListByDateAndTimeNow класса NotificationService,
	 * который возвращает список заметок из базы данных, у которых время и дата соответствуют
	 * текущему времени и дате
	 */
	@Test
	public void taskListByDateAndTimeNow_Test(){
		long maxId = notificationTaskRepository.findAll()
				.stream()
				.mapToLong(n -> n.getId())
				.max().getAsLong();
		// создается заметка, которой задается время на секунду позже текущего времени
		NotificationTask notificationTask = new NotificationTask();
		notificationTask.setId(maxId++);
		notificationTask.setChatId(CHAT_ID);
		notificationTask.setMessageText("now test message");
		notificationTask.setDate(LocalDate.now());
		notificationTask.setTime(LocalTime.now().plusSeconds(1));

		notificationTaskRepository.save(notificationTask);
		List<NotificationTask> notificationTaskFromMethod = null;
		// в цикле ожидаем пока не наступит время записанное в заметку
		while(LocalTime.now().isBefore(notificationTask.getTime())){
			notificationTaskFromMethod = notificationService.taskListByDateAndTimeNow();
		}

		Assertions.assertEquals(List.of(notificationTask), notificationTaskFromMethod);
	}

	/**
	 * Метод для тестирования сохранения сообщения от пользователя в базу данных
	 *
	 */
	@Test
	public void saveToDataBase_Test(){
		// формируется строка с актуальной датой на день вперед от текущей
		LocalDate ld = LocalDate.now().plusDays(1);
		String date = ld.toString();
		StringBuilder appropriateFormatMessage = new StringBuilder();
		appropriateFormatMessage
				.append(date, 8, 10)
				.append(".")
				.append(date, 5, 7)
				.append(".")
				.append(date, 0, 4);
		date = appropriateFormatMessage.toString();
		// заметка сохраняется в базу данных
		long chatId = new Random().nextInt();
		String notificationTask = notificationService.saveToDataBase(chatId, date + " " + "18:00 testing note");
		// осуществляем поиск базе данных по дате и времени сохраненного сообщения
		List<NotificationTask> listNotes = notificationTaskRepository.findByDateAndTime(ld, LocalTime.of(18, 0, 0, 0));
		NotificationTask notificationTaskFromDB = listNotes.stream().filter(n -> n.getChatId() == chatId).findFirst().get();
		Assertions.assertEquals(notificationTask, notificationTaskFromDB.toString());
	}


}
