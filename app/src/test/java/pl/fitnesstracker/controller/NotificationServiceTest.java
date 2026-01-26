package pl.fitnesstracker.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.NotificationManager;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import pl.fitnesstracker.dao.DayOfWeekDao;
import pl.fitnesstracker.model.FitnessModel;
import pl.fitnesstracker.model.Notification;
import pl.fitnesstracker.dao.NotificationDao;
import pl.fitnesstracker.model.User;
import pl.fitnesstracker.dao.WorkoutPlanDao;
import pl.fitnesstracker.model.WorkoutPlan;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest {

    @Mock
    private FitnessModel model;
    @Mock
    private Context context;
    @Mock
    private NotificationDao notificationDao;
    @Mock
    private WorkoutPlanDao workoutPlanDao;
    @Mock
    private DayOfWeekDao dayOfWeekDao;
    @Mock
    private NotificationManager notificationManager;

    private NotificationService service;

    @Before
    public void setUp() {
        when(model.getNotificationDao()).thenReturn(notificationDao);
        when(model.getWorkoutPlanDao()).thenReturn(workoutPlanDao);
        when(model.getDayOfWeekDao()).thenReturn(dayOfWeekDao);
        
        // Mocking Context behavior for showNotification
        when(context.getApplicationContext()).thenReturn(context);
        when(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManager);

        service = new NotificationService(model);
        service.setContext(context);
    }

    @Test
    public void createAchievementNotification_ShouldCreateNotificationInDao() {
        int userId = 1;
        String title = "New Record";
        String message = "You lifted 100kg!";
        
        try {
            service.createAchievementNotification(userId, title, message);
        } catch (RuntimeException e) {
            // Expected if Android classes are touched
            if (!e.getMessage().contains("Method") && !e.getMessage().contains("stub")) {
                throw e;
            }
        }

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDao).createNotification(notificationCaptor.capture());

        Notification captured = notificationCaptor.getValue();
        assert captured.getUserId() == userId;
        assert captured.getType().equals("Osiągnięcie");
        assert captured.getMessage().equals(title + ": " + message);
    }

    @Test
    public void checkAndNotifyScheduledWorkout_ShouldNotify_WhenScheduledAndNotNotified() {
        User user = new User();
        user.setId(1);
        
        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(100);
        plan.setPlanName("Leg Day");
        
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            LocalDate monday = LocalDate.of(2023, 1, 2); // A Monday
            mockedLocalDate.when(LocalDate::now).thenReturn(monday);
            
            when(workoutPlanDao.getPlansByUser(user.getId())).thenReturn(Collections.singletonList(plan));
            when(dayOfWeekDao.isPlanScheduledForDay(plan.getId(), "Poniedziałek")).thenReturn(true);
            when(notificationDao.hasNotificationForToday(user.getId(), "Przypomnienie")).thenReturn(false);

            service.checkAndNotifyScheduledWorkout(user);

            verify(notificationDao).createNotification(any(Notification.class));
        }
    }

    @Test
    public void checkAndNotifyScheduledWorkout_ShouldNotNotify_WhenNotScheduled() {
        User user = new User();
        user.setId(1);
        
        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(100);
        plan.setPlanName("Leg Day");
        
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            LocalDate monday = LocalDate.of(2023, 1, 2); // A Monday
            mockedLocalDate.when(LocalDate::now).thenReturn(monday);
            
            when(workoutPlanDao.getPlansByUser(user.getId())).thenReturn(Collections.singletonList(plan));
            when(dayOfWeekDao.isPlanScheduledForDay(plan.getId(), "Poniedziałek")).thenReturn(false);

            service.checkAndNotifyScheduledWorkout(user);

            verify(notificationDao, never()).createNotification(any(Notification.class));
        }
    }

    @Test
    public void checkAndNotifyScheduledWorkout_ShouldNotNotify_WhenAlreadyNotified() {
        User user = new User();
        user.setId(1);
        
        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(100);
        plan.setPlanName("Leg Day");
        
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            LocalDate monday = LocalDate.of(2023, 1, 2); // A Monday
            mockedLocalDate.when(LocalDate::now).thenReturn(monday);
            
            when(workoutPlanDao.getPlansByUser(user.getId())).thenReturn(Collections.singletonList(plan));
            when(dayOfWeekDao.isPlanScheduledForDay(plan.getId(), "Poniedziałek")).thenReturn(true);
            when(notificationDao.hasNotificationForToday(user.getId(), "Przypomnienie")).thenReturn(true);

            service.checkAndNotifyScheduledWorkout(user);

            verify(notificationDao, never()).createNotification(any(Notification.class));
        }
    }

    @Test
    public void updateDailyWorkoutNotification_ShouldDeleteOldAndCreateNew() {
        int userId = 1;

        service.updateDailyWorkoutNotification(userId);

        verify(notificationDao).deleteNotificationsByType(userId, "Przypomnienie");
        
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDao).createNotification(notificationCaptor.capture());
        
        Notification captured = notificationCaptor.getValue();
        assert captured.getUserId() == userId;
        assert captured.getType().equals("Info");
        assert captured.getMessage().contains("Dobra robota");
    }
}
