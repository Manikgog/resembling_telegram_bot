package pro.sky.telegrambot.exception;

public class NotificationParseException extends RuntimeException {
    private final String message;
    public NotificationParseException(String message){
        this.message = message;
    }

    @Override
    public String getMessage(){
        return message;
    }

}
