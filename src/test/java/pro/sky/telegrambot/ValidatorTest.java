package pro.sky.telegrambot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.sky.telegrambot.exception.NotificationParseException;
import pro.sky.telegrambot.validator.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ValidatorTest {
    private Validator validator;

    public ValidatorTest() {
        this.validator = new Validator();
    }

    /**
     * Метод для формирования списка с датами и временем начиная с текущей
     * @return List<String> - список сообщений
     */
    public List<String> prepareAppropriageMessages(){
        List<String> messages = new ArrayList<>();
        LocalDateTime localDateTime = LocalDateTime.now().plusHours(1);
        messages.add(messageToAppropriateString(localDateTime));   // сообщение с текущей датой плюс 1 час
        localDateTime = localDateTime.plusHours(5);
        messages.add(messageToAppropriateString(localDateTime));   // сообщение с датой на 5 часов позже
        localDateTime = localDateTime.plusDays(3);
        messages.add(messageToAppropriateString(localDateTime));   // сообщение с датой на 3 дней позже
        localDateTime = localDateTime.plusDays(10);
        messages.add(messageToAppropriateString(localDateTime));   // сообщение с датой на 10 дней позже
        localDateTime = localDateTime.plusMonths(1);
        messages.add(messageToAppropriateString(localDateTime));   // сообщение с датой на 1 месяц позже
        return messages;
    }

    /**
     * Метод для формирования сообщения с несуществующей датой и временем
     * @return List<String> - список сообщений
     */
    public List<String> prepareMessagesWithWrongDateTime(){
        List<String> messages = new ArrayList<>();
        String year = LocalDateTime.now().plusYears(1).toString().substring(0, 4);
        messages.add("32.04." + year + " 12:00 сообщение");                 // сообщение с несуществующей датой
        messages.add("30.00." + year + " 12:00 сообщение");                 // сообщение с несуществующей датой
        messages.add("30.13." + year + " 12:00 сообщение");                 // сообщение с несуществующей датой
        return messages;
    }
    /**
     * Метод для формирования сообщения с несуществующей датой и временем
     * @return List<String> - список сообщений
     */
    public List<String> prepareMessagesWithWrongFormat(){
        List<String> messages = new ArrayList<>();
        String year = LocalDateTime.now().plusYears(1).toString().substring(0, 4);
        messages.add("28/02/" + year + " 12:00 сообщение");                 // сообщение с несоответствующим форматом
        messages.add("28-02-" + year + " 12.00 сообщение");                 // сообщение с несоответствующим форматом
        messages.add("12.00 сообщение");                                    // сообщение с несоответствующим форматом
        messages.add("28-02-" + year + " сообщение");                        // сообщение с несоответствующим форматом
        messages.add("");                        // сообщение с несоответствующим форматом
        return messages;
    }
    /**
     * Метод для формирования сообщения с прошедшей датой и временем
     * @return List<String> - список сообщений
     */
    public List<String> prepareMessagesWithPastTime(){
        List<String> messages = new ArrayList<>();
        LocalDateTime localDateTime = LocalDateTime.now();
        messages.add(messageToAppropriateString(localDateTime));   // сообщение с текущей датой минус 1 час
        localDateTime = localDateTime.minusHours(5);
        messages.add(messageToAppropriateString(localDateTime));   // сообщение с датой на 5 часов раньше
        localDateTime = localDateTime.minusDays(3);
        messages.add(messageToAppropriateString(localDateTime));   // сообщение с датой на 3 дня раньше
        localDateTime = localDateTime.minusDays(10);
        messages.add(messageToAppropriateString(localDateTime));   // сообщение с датой на 10 дней раньше
        localDateTime = localDateTime.minusMonths(1);
        messages.add(messageToAppropriateString(localDateTime));   // сообщение с датой на 1 месяц раньше
        return messages;
    }

    /**
     * Метод для преобразования даты и времени LocalDateTime в строку с текстом сообщения
     * нужного вида - "dd.MM.yyyy HH:mm message"
     * @param localDateTime - дата и время LocalDateTime
     * @return String - сообщение вида - "dd.MM.yyyy HH:mm message"
     */
    private String messageToAppropriateString(LocalDateTime localDateTime){
        String dateTime = localDateTime.toString();

        StringBuilder appropriateFormatMessage = new StringBuilder();
        appropriateFormatMessage
                .append(dateTime.substring(8, 10))
                .append(".")
                .append(dateTime.substring(5, 7))
                .append(".")
                .append(dateTime.substring(0, 4))
                .append(" ")
                .append(dateTime.substring(11, 13))
                .append(":")
                .append(dateTime.substring(14, 16))
                .append(" ")
                .append("сообщение");
        return appropriateFormatMessage.toString();
    }

    /**
     * Метод для тестирования положительной проверки даты и времени
     */
    @Test
    public void checkDateTimePositiveTest() {
        List<String> list = prepareAppropriageMessages();
        for (int i = 0; i < list.size(); i++) {
            validator.checkMessage(list.get(i));
        }
    }

    /**
     * Метод для проверки правильности работы метода checkDateTime() класса Validate при
     * получении сообщений с прошедшей датой, несуществующей датой и несоответствующего формата
     */
    @Test
    public void checkDateTimeNegativeTest(){
        List<String> messagesWithPastTime = prepareMessagesWithPastTime();
        for (String message : messagesWithPastTime) {
            Assertions.assertThrows(RuntimeException.class, () -> validator.checkMessage(message));
        }

        List<String> messagesWithWrongDateTime = prepareMessagesWithWrongDateTime();
        for(String message : messagesWithWrongDateTime){
            Assertions.assertThrows(NotificationParseException.class, () -> validator.checkMessage(message));
        }

        List<String> messageWithWrongFormat = prepareMessagesWithWrongFormat();
        for(String message : messageWithWrongFormat){
            Assertions.assertThrows(NotificationParseException.class, () -> validator.checkMessage(message));
        }
    }


}
