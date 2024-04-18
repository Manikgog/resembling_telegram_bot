package pro.sky.telegrambot.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.sky.telegrambot.exception.NotificationParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.List;

public class Validator {
    private static final Logger logger = LoggerFactory.getLogger(Validator.class);
    /**
     * Метод для проверки соответствия сообщения нужному формату -> dd.MM.yyyy HH:mm
     * даты и времени.
     * @param message - сообщение с датой и временем
     */
    public void checkMessage(String message){
        message = message.trim();

        List<String> list = Arrays.stream(message.split(" ")).toList();
        if(list.isEmpty()){
            throw new NotificationParseException("Сообщение пустое");
        }
        String date = list.get(0);
        if(list.size() == 1){
            throw new NotificationParseException("Ошибка ввода");
        }
        String time = list.get(1);
        StringBuilder note = new StringBuilder();
        list.stream().skip(2).forEach((s) -> note.append(s).append(" "));

        boolean dateErrorFlag = date.length() != 10;

        int indexOfFirstDot = date.indexOf('.');
        int indexOfSecondDot = date.indexOf('.', indexOfFirstDot+1);

        if(indexOfFirstDot != 2 || indexOfSecondDot != 5){
            dateErrorFlag = true;
        }
        boolean timeErrorFlag = false;
        int indexOfColon = time.indexOf(':');
        if(time.length() != 5){
            timeErrorFlag = true;
        }

        if(indexOfColon != 2){
            timeErrorFlag = true;
        }

        if(timeErrorFlag || dateErrorFlag){
            throw new NotificationParseException("Ошибка ввода даты и времени");
        }

        if(note.isEmpty()){
            throw new NotificationParseException("Ошибка ввода запланированного дела");
        }

        try {
            checkDateTime(date + " " + time);
        }catch (RuntimeException e){
            logger.info(e.getMessage());
            throw new NotificationParseException(e.getMessage());
        }
        try {
            checkingForThePastDateAndTime(date + " " + time);
        }catch (RuntimeException e){
            throw new NotificationParseException(e.getMessage());
        }
    }

    /**
     * Метод для проверки даты и времени на прошлое время
     * @param dateAndTime - строка с датой и временем
     */
    private void checkingForThePastDateAndTime(String dateAndTime){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(dateAndTime, dateTimeFormatter);
        if(LocalDateTime.now().isAfter(localDateTime)){
            throw new RuntimeException("Введенные дата и время - " + dateAndTime + " уже прошли");
        }
    }

    /**
     * Метод для проверки соответствия даты и времени на соответствие:
     * - количеству часов в сутках
     * - минут в часе
     * - дней в месяце
     * @param dateTime - строка с датой и временем
     */
    private void checkDateTime(String dateTime){
        try{
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withResolverStyle(ResolverStyle.SMART);
            dateTimeFormatter.parse(dateTime);
        }catch (DateTimeParseException e){
            logger.error("Error occurred: " + e.getMessage());
            throw new RuntimeException("Такой даты не существует " + dateTime);
        }
    }

}
