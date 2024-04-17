package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.exception.NotificationParseException;
import pro.sky.telegrambot.service.NotificationService;
import pro.sky.telegrambot.validator.Validator;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    public static final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private Validator validator;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    /**
     * Метод для обработки списка обновлений от клиентов telegram
     * @param updates - список обновлений
     * @return int - код результата обработки обновлений
     */
    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if(update.message() != null && update.message().text() != null){
                String message = update.message().text();
                switch (message) {
                    case "/start" -> onStartAction(update);
                    case "/my_notes_list" -> {
                        List<NotificationTask> list = notificationService.getAllTasksByChatId(update.message().chat().id());
                        if (list.isEmpty()) {
                            emptyListMessage(update.message().chat().id());
                        } else {
                            noteMessage(update.message().chat().id(), list);
                        }
                    }
                    case "/future_tasks" -> {
                        List<NotificationTask> list = notificationService.getFutureTasks(update.message().chat().id());
                        if (list.isEmpty()) {
                            emptyListMessage(update.message().chat().id());
                        } else {
                            noteMessage(update.message().chat().id(), list);
                        }
                    }
                    case "/delete_past_tasks" -> {
                        List<NotificationTask> list = notificationService.deletePastTasks(update.message().chat().id());
                        if (list.isEmpty()) {
                            emptyListMessage(update.message().chat().id());
                        } else {
                            noteMessage(update.message().chat().id(), list);
                        }
                    }
                    default -> {
                        boolean positiveValidation = true;
                        try {
                            validator.checkMessage(message);
                        } catch (NotificationParseException e) {
                            logger.info(e.getMessage());
                            notificationMessage(update.message().chat().id(), e.getMessage());
                            positiveValidation = false;
                        }
                        if (positiveValidation) {
                            onSaveNotificationAction(update);
                        }
                    }
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    /**
     * Метод для возвращения пользователю с идентификатором чата chatId
     * списка задач, которые он запросил
     * @param chatId - идентификатор чата
     * @param list - список задач полученный из базы данных
     */
    private void noteMessage(long chatId, List<NotificationTask> list){
        logger.info("Start noteMessage method");
        StringBuilder messageText = new StringBuilder();
        for(NotificationTask item : list){
            messageText.append(item.getDate().toString()).append(" ").append(item.getTime()).append(" ").append(item.getMessageText()).append("\n");
        }
        SendMessage sendMessage = new SendMessage(chatId, messageText.toString());
        telegramBot.execute(sendMessage);
    }

    /**
     * Метод для возвращения пользователю приветствия с небольшой инструкцией
     * @param update - данные полученные от пользователя
     */
    private void onStartAction(Update update){
        logger.info("Start onStartAction method");
        long chatId = update.message().chat().id();
        String userName = update.message().chat().firstName();
        String text = """
                Привет! %s
                
                для формирования напоминания введите строку вида:
                01.01.2022 20:00 Сделать домашнюю работу
                
                для получения списка всех задач введите: /my_notes_list
                
                для получения не прошедших задач введите: /future_tasks
                
                для удаления прошедших задач введите: /delete_past_tasks
                """;
        String botAnswer = String.format(text, userName);
        SendMessage sendMessage = new SendMessage(chatId, botAnswer);
        telegramBot.execute(sendMessage);
    }

    /**
     * Метод для возвращения сообщения пользователю
     * @param chatId - идентификатор чата пользователя
     * @param message - текст сообщения
     */
    public void notificationMessage(long chatId, String message){
        logger.info("Start notificationMessage method");
        SendMessage sendMessage = new SendMessage(chatId, message);
        telegramBot.execute(sendMessage);
    }

    /**
     * Метод для сохранения сообщения в базу данных
     * через использования notificationService.
     * Метод возвращает преобразованную в строку
     * новую строку из базы данных
     * @param update - данные от пользователя
     */
    private void onSaveNotificationAction(Update update){
        logger.info("Start onSaveNotificationAction method");
        long chatId = update.message().chat().id();
        String botAnswer = notificationService.saveToDataBase(update.message().chat().id(), update.message().text());
        SendMessage sendMessage = new SendMessage(chatId, botAnswer);
        telegramBot.execute(sendMessage);
    }

    /**
     * Метод для отправки сообщений пользователям в случае совпадения времени и даты запланированного дела
     * в базе данных с текущим временем и датой. Проверяется каждую минуту.
     */
    @Scheduled(cron = "0 * * * * *") // секунды минуты часы дни месяцы дни_недели
    private void sendMessageByTime(){
        List<NotificationTask> list = notificationService.taskListByDateAndTimeNow();
        for(NotificationTask item : list){
            String messageText = item.getDate().toString() + " " + item.getTime() + " " + item.getMessageText();
            notificationMessage(item.getChatId(), messageText);
        }
    }

    /**
     * Метод для возвращения пользователю с идентификатором чата chatId
     * сообщения, что список запрошенных задач пуст
     * @param chatId - идентификатор чата
     */
    private void emptyListMessage(long chatId){
        logger.info("Started emptyListMessage method");
        String message = "Список задач пуст";
        SendMessage sendMessage = new SendMessage(chatId, message);
        telegramBot.execute(sendMessage);
    }
}
