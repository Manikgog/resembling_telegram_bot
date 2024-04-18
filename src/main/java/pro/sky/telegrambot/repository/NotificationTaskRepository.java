package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.entity.NotificationTask;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Integer> {
    @Query(value = "SELECT * FROM public.notification_task WHERE notification_date = :localDate AND notification_time = :localTime", nativeQuery = true)
    List<NotificationTask> findByDateAndTime(LocalDate localDate, LocalTime localTime);

    @Query(value = "SELECT * FROM public.notification_task WHERE chat_id = :chatId", nativeQuery = true)
    List<NotificationTask> findByChatId(long chatId);

}
