package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;

import java.util.List;

@Service
public class NotificationSender {
    private static final Logger logger = LoggerFactory.getLogger(NotificationSender.class);
    @Autowired
    private TelegramBot telegramBot;

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
     * Метод для возвращения пользователю с идентификатором чата chatId
     * сообщения, что список запрошенных задач пуст
     * @param chatId - идентификатор чата
     */
    public void emptyListMessage(long chatId){
        logger.info("Started emptyListMessage method");
        String message = "Список задач пуст";
        SendMessage sendMessage = new SendMessage(chatId, message);
        telegramBot.execute(sendMessage);
    }
    /**
     * Метод для возвращения пользователю с идентификатором чата chatId
     * списка задач, которые он запросил
     * @param chatId - идентификатор чата
     * @param list - список задач полученный из базы данных
     */
    public void noteMessage(long chatId, List<NotificationTask> list){
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
    public void onStartAction(Update update){
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

}
