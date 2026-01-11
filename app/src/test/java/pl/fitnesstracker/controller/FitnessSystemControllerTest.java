
package pl.fitnesstracker.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pl.fitnesstracker.dao.UserDao;
import pl.fitnesstracker.model.FitnessModel;
import pl.fitnesstracker.model.User;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FitnessSystemControllerTest {

    @Mock
    private FitnessModel model;
    @Mock
    private UserDao userDao;
    @Mock
    private NotificationService notificationService;

    private FitnessSystemController controller;

    @Before
    public void setUp() throws Exception {
        controller = FitnessSystemController.getInstance();

        setField(controller, "model", model);
        setField(controller, "notificationService", notificationService);

        when(model.getUserDao()).thenReturn(userDao);
    }

    @After
    public void tearDown() throws Exception {
        setField(FitnessSystemController.class, "instance", null);
    }

    @Test
    public void testLoginSuccess() {
        User user = new User("test@test.pl", "password", new BigDecimal(80), 180, "goal");
        when(userDao.login("test@test.pl", "password")).thenReturn(Optional.of(user));

        boolean result = controller.login("test@test.pl", "password");

        assertTrue(result);
        assertEquals(user, controller.getCurrentUser());
        verify(notificationService).checkAndNotifyScheduledWorkout(user);
    }

    @Test
    public void testLoginFailure() {
        when(userDao.login("wrong@test.pl", "wrong")).thenReturn(Optional.empty());

        boolean result = controller.login("wrong@test.pl", "wrong");

        assertFalse(result);
        assertNull(controller.getCurrentUser());
        verify(notificationService, never()).checkAndNotifyScheduledWorkout(any(User.class));
    }

    @Test
    public void testRegisterSuccess() {
        when(userDao.registerUser(any(User.class))).thenReturn(true);

        boolean result = controller.register("new@user.com", "pass123", new BigDecimal(70), 175, "new goal");

        assertTrue(result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).registerUser(userCaptor.capture());
        assertEquals("new@user.com", userCaptor.getValue().getEmail());
    }

    @Test
    public void testRegisterFailure() {
        when(userDao.registerUser(any(User.class))).thenReturn(false);

        boolean result = controller.register("new@user.com", "pass123", new BigDecimal(70), 175, "new goal");

        assertFalse(result);
    }


    private void setField(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void setField(Class<?> targetClass, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }
}
