package pro.sky.telegrambot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;

import java.util.List;

@Service
public class NotificationByTime {
    private final NotificationService notificationService;
    private final NotificationSender notificationSender;
    public NotificationByTime(NotificationService notificationService,
                              NotificationSender notificationSender){
        this.notificationService = notificationService;
        this.notificationSender = notificationSender;
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
            notificationSender.notificationMessage(item.getChatId(), messageText);
        }
    }

}
