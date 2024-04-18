package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationTaskRepository notificationTaskRepository;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public NotificationService(NotificationTaskRepository notificationTaskRepository){
        this.notificationTaskRepository = notificationTaskRepository;
    }

    /**
     * Метод для обработки и сохранения запланированного дела в базу данных
     * через обращение к репозиторию notificationTaskRepository
     * @param chatId - идентификатор чата
     * @param inputString - сообщение пользователя
     * @return String - объект NotificationTask, преобразованный в строку
     */
    public String saveToDataBase(long chatId, String inputString){
        int index = inputString.indexOf(':') + 3;
        String dateTime = inputString.substring(0, index);
        inputString = inputString.substring(index + 1);
        LocalDateTime localDateTime = parseOfUpdateStringToDateAndTime(dateTime);
        return saveOfNotification(localDateTime, inputString, chatId).toString();
    }

    /**
     * Метод для преобразования строки с датой и временем в объект LocalDateTime
     * @param inputString - строка с датой и временем
     * @return LocalDateTime - объект класса
     */
    private LocalDateTime parseOfUpdateStringToDateAndTime(String inputString){
        return LocalDateTime.parse(inputString, dateTimeFormatter);
    }

    /**
     * Метод выполняющий создание объекта NotificationTask и вызывающий метод
     * save репозитория для сохранения его в базе данных
     * @param dateTime - объект класса LocalDateTime
     * @param note - заметка для записи в базу данных
     * @param chatId - идентификатор чата для последующей отправки напоминания именно этому пользователю
     * @return - возвращается объект NotificationTask, который уже записан в базу данных
     */
    private NotificationTask saveOfNotification(LocalDateTime dateTime, String note, long chatId){
        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setChatId(chatId);
        notificationTask.setDate(dateTime.toLocalDate());
        notificationTask.setTime(dateTime.toLocalTime());
        notificationTask.setMessageText(note);
        return notificationTaskRepository.save(notificationTask);
    }

    /**
     * Метод для получения списка записей которые соответствуют текущей дате и времени
     * @return List<NotificationTask> - список задач, которые совпадают по времени и дате
     * с текущим временем и датой
     */
    public List<NotificationTask> taskListByDateAndTimeNow(){
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        return notificationTaskRepository.findByDateAndTime(nowDate, nowTime);
    }

    /**
     * Метод для получения списка записей по идентификатору чата
     * @param chatId - идентификатор чата
     * @return List<NotificationTask> - список записей пользователя, которому соответствует chatId
     */
    public List<NotificationTask> getAllTasksByChatId(long chatId) {
        return notificationTaskRepository.findByChatId(chatId);
    }

    /**
     * Удаление из базы данных записей с прошедшим временем и датой
     * @return List<NotificationTask> - список удалённых задач
     */
    public List<NotificationTask> deletePastTasks(long chatId){
        List<NotificationTask> list = notificationTaskRepository.findByChatId(chatId)
                .stream()
                .filter(task -> task.getDate().isBefore(LocalDate.now()) ||
                        (task.getDate().isEqual(LocalDate.now()) &&
                                task.getTime().isBefore(LocalTime.now()))).toList();
        list.forEach(notificationTaskRepository::delete);
        return list;
    }

    /**
     * Метод для получения ещё не прошедших по времени задач
     * @param chatId - идентификатор чата, чтобы достать из базы данных задачи,
     *               которые соответствую этому идентификатору
     * @return List<NotificationTask> - список запланированных задач
     */
    public List<NotificationTask> getFutureTasks(Long chatId) {
        return getAllTasksByChatId(chatId).stream()
                .filter(task -> task.getDate().isAfter(LocalDate.now()) ||
                        (task.getDate().isEqual(LocalDate.now()) &&
                        task.getTime().isAfter(LocalTime.now())))
                .toList();
    }
}
