package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.exception.NotificationParseException;
import pro.sky.telegrambot.service.NotificationSender;
import pro.sky.telegrambot.service.NotificationService;
import pro.sky.telegrambot.validator.Validator;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;
    private final NotificationService notificationService;
    private final Validator validator;
    private final NotificationSender notificationSender;
    public TelegramBotUpdatesListener(NotificationService notificationService,
                                      Validator validator,
                                      NotificationSender notificationSender) {
        this.notificationService = notificationService;
        this.validator = validator;
        this.notificationSender = notificationSender;
    }

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
                    case "/start" -> notificationSender.onStartAction(update);
                    case "/my_notes_list" -> {
                        List<NotificationTask> list = notificationService.getAllTasksByChatId(update.message().chat().id());
                        if (list.isEmpty()) {
                            notificationSender.emptyListMessage(update.message().chat().id());
                        } else {
                            notificationSender.noteMessage(update.message().chat().id(), list);
                        }
                    }
                    case "/future_tasks" -> {
                        List<NotificationTask> list = notificationService.getFutureTasks(update.message().chat().id());
                        if (list.isEmpty()) {
                            notificationSender.emptyListMessage(update.message().chat().id());
                        } else {
                            notificationSender.noteMessage(update.message().chat().id(), list);
                        }
                    }
                    case "/delete_past_tasks" -> {
                        List<NotificationTask> list = notificationService.deletePastTasks(update.message().chat().id());
                        if (list.isEmpty()) {
                            notificationSender.emptyListMessage(update.message().chat().id());
                        } else {
                            notificationSender.noteMessage(update.message().chat().id(), list);
                        }
                    }
                    default -> {
                        boolean positiveValidation = true;
                        try {
                            validator.checkMessage(message);
                        } catch (NotificationParseException e) {
                            logger.info(e.getMessage());
                            notificationSender.notificationMessage(update.message().chat().id(), e.getMessage());
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


}
