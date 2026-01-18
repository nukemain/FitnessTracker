package pl.fitnesstracker.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import pl.fitnesstracker.dao.DatabaseConnector;
import pl.fitnesstracker.dao.ExerciseDao;
import pl.fitnesstracker.dao.StatisticsDao;
import pl.fitnesstracker.dao.TrainingSessionDao;
import pl.fitnesstracker.dao.UserDao;
import pl.fitnesstracker.dao.WorkoutPlanDao;
import pl.fitnesstracker.model.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PU_wyswietlanie {

    @Mock
    private FitnessModel model;
    @Mock
    private WorkoutPlanDao workoutPlanDao;
    @Mock
    private UserDao userDao;
    @Mock
    private ExerciseDao exerciseDao;
    @Mock
    private StatisticsDao statisticsDao;
    @Mock
    private TrainingSessionDao trainingSessionDao;
    @Mock
    private NotificationService notificationService;
    @Mock
    private SessionRecordingProcess sessionProcess;

    private FitnessSystemController controller;

    @Before
    public void setUp() throws Exception {
        controller = FitnessSystemController.getInstance();
        setField(controller, "model", model);
        setField(controller, "notificationService", notificationService);
        setField(controller, "sessionProcess", sessionProcess);

        // Konfiguracja mocków DAO
        when(model.getWorkoutPlanDao()).thenReturn(workoutPlanDao);
        when(model.getTrainingSessionDao()).thenReturn(trainingSessionDao);
    }

    @After
    public void tearDown() throws Exception {
        setField(FitnessSystemController.class, "instance", null);
    }

    @Test
    public void test_endSession() throws Exception {
        // Przygotowanie
        User user = new User();
        user.setId(123);
        setField(controller, "currentUser", user);
        setField(controller, "currentSessionId", 999);

        // Wywołanie
        controller.endSession("01:00:00");

        // Asercje
        verify(sessionProcess).finishSession(123, 999, "01:00:00");
        verify(notificationService).updateDailyWorkoutNotification(123);
        Field sessionIdField = controller.getClass().getDeclaredField("currentSessionId");
        sessionIdField.setAccessible(true);
        assertNull(sessionIdField.get(controller));
    }

    @Test
    public void test_endSession_gdyniemasesji() throws NoSuchFieldException, IllegalAccessException {
        // Przygotowanie
        setField(controller, "currentSessionId", null);

        // Wywołanie
        controller.endSession("01:00:00");

        // Asercje
        verify(sessionProcess, never()).finishSession(anyInt(), anyInt(), anyString());
        verify(notificationService, never()).updateDailyWorkoutNotification(anyInt());
    }

    @Test
    public void test_startSession() throws Exception {
        // Przygotowanie
        User loggedInUser = new User();
        loggedInUser.setId(123);
        setField(controller, "currentUser", loggedInUser);
        setField(controller, "currentSessionId", null);
        when(trainingSessionDao.startSession(123, 1)).thenReturn(999);

        // Wywołanie
        Integer sessionId = controller.startSession(1);

        // Asercje
        assertEquals(Integer.valueOf(999), sessionId);
        
        Field sessionIdField = controller.getClass().getDeclaredField("currentSessionId");
        sessionIdField.setAccessible(true);
        assertEquals(999, sessionIdField.get(controller));
    }

    @Test
    public void test_startSession_nZalog() throws Exception {
        setField(controller, "currentUser", null);
        Integer sessionId = controller.startSession(1);
        assertNull(sessionId);
    }


    //uzywane bezposrednio
    @Test
    public void test_getSessionDetails() {
        // Przygotowanie: Definiujemy ID sesji i listę rekordów, którą ma zwrócić mock
        int sessionId = 555;
        List<SessionRecord> expectedRecords = Arrays.asList(new SessionRecord(), new SessionRecord());
        when(trainingSessionDao.getSessionRecords(sessionId)).thenReturn(expectedRecords);
        List<SessionRecord> actualRecords = controller.getSessionDetails(sessionId);
        assertEquals(expectedRecords, actualRecords);
    }

    @Test
    public void test_getPlanDetails() {
        Integer planId = 1;
        List<PlanItem> expectedItems = Arrays.asList(new PlanItem(), new PlanItem());
        when(workoutPlanDao.getPlanItems(planId)).thenReturn(expectedItems);
        List<PlanItem> actualItems = controller.getPlanDetails(planId);
        assertEquals(2, actualItems.size());
        assertEquals(expectedItems, actualItems);
    }

    // --- BEZPOŚREDNI TEST DLA DAO (Skomplikowane - Niepolecane) ---
    // Poniższy test pokazuje, jak można przetestować metodę z klasy DAO w pełnej izolacji.
    // Zwróć uwagę, jak wiele kodu jest potrzebne do "udawania" działania bazy danych.
    // Musimy zamockować połączenie, zapytanie, wyniki i każdą kolumnę z osobna.
    // To jest kruche i trudne w utrzymaniu. Dlatego zazwyczaj preferuje się testowanie
    // klas DAO pośrednio, przez kontroler, tak jak w poprzednich testach.
    @Test
    public void getPlansByUser_daoDirectTest() throws Exception {
        // ---- Krok 1: Przygotowanie skomplikowanych mocków ----

        // Mockowanie singletonu DatabaseConnector
        DatabaseConnector mockConnector = mock(DatabaseConnector.class);
        setField(DatabaseConnector.class, "instance", mockConnector);

        // Mockowanie obiektów JDBC
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        // ---- Krok 2: Definiowanie zachowania mocków (programowanie udawanej bazy) ----

        // DatabaseConnector ma zwracać nasze udawane połączenie
        when(mockConnector.getConnection()).thenReturn(mockConnection);
        // Połączenie ma zwracać nasze udawane zapytanie
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        // Zapytanie ma zwracać nasz udawany zestaw wyników
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        // Konfiguracja udawanego zestawu wyników (symulujemy 2 wiersze w tabeli)
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false); // Dwa razy next() zwróci true, potem false

        // Definicja danych dla PIERWSZEGO i DRUGIEGO wiersza
        when(mockResultSet.getInt("id_planu")).thenReturn(101).thenReturn(102);
        when(mockResultSet.getString("nazwa_planu")).thenReturn("Plan Poniedziałkowy").thenReturn("Plan Środowy");
        when(mockResultSet.getString("opis")).thenReturn("Trening klatki piersiowej").thenReturn("Trening nóg");
        when(mockResultSet.getTimestamp(anyString())).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getBoolean("aktywny")).thenReturn(true);

        // ---- Krok 3: Wywołanie testowanej metody ----
        WorkoutPlanDao dao = new WorkoutPlanDao();
        List<WorkoutPlan> result = dao.getPlansByUser(123);

        // ---- Krok 4: Asercje (sprawdzenie wyników) ----
        assertNotNull(result);
        assertEquals(2, result.size());

        // Sprawdzenie pierwszego planu
        assertEquals(Integer.valueOf(101), result.get(0).getId());
        assertEquals("Plan Poniedziałkowy", result.get(0).getPlanName());
        assertEquals("Trening klatki piersiowej", result.get(0).getDescription());

        // Sprawdzenie drugiego planu
        assertEquals(Integer.valueOf(102), result.get(1).getId());
        assertEquals("Plan Środowy", result.get(1).getPlanName());
        assertEquals("Trening nóg", result.get(1).getDescription());

        // ---- Krok 5: Posprzątanie po teście ----
        // Resetujemy singleton, aby nie wpływał na inne testy
        setField(DatabaseConnector.class, "instance", null);
    }

    //--- FitnessSystemController ---
    /*

    @Test
    public void testLoginSuccess() throws Exception {
        User user = new User("test@test.pl", "password", new BigDecimal(80), 180, "goal");
        setField(controller, "currentUser", null);
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

    @Test
    public void logout_shouldClearCurrentUserAndSession() throws Exception {
        User user = new User();
        setField(controller, "currentUser", user);
        setField(controller, "currentSessionId", 999);

        controller.logout();

        assertNull(controller.getCurrentUser());
        Field sessionIdField = controller.getClass().getDeclaredField("currentSessionId");
        sessionIdField.setAccessible(true);
        assertNull(sessionIdField.get(controller));
    }

    @Test
    public void getCurrentUser_shouldReturnTheCurrentUser() throws Exception {
        User expectedUser = new User();
        expectedUser.setEmail("test@example.com");
        setField(controller, "currentUser", expectedUser);

        User actualUser = controller.getCurrentUser();

        assertEquals(expectedUser, actualUser);
    }

    @Test
    public void getUserWorkoutPlans_whenUserLoggedIn_shouldReturnPlans() throws Exception {
        User loggedInUser = new User();
        loggedInUser.setId(123);
        setField(controller, "currentUser", loggedInUser);

        List<WorkoutPlan> expectedPlans = Arrays.asList(new WorkoutPlan(), new WorkoutPlan());
        when(workoutPlanDao.getPlansByUser(123)).thenReturn(expectedPlans);

        List<WorkoutPlan> actualPlans = controller.getUserWorkoutPlans();

        assertEquals(2, actualPlans.size());
        assertEquals(expectedPlans, actualPlans);
    }

    @Test
    public void getUserWorkoutPlans_whenNotLoggedIn_shouldReturnEmptyList() throws Exception {
        setField(controller, "currentUser", null);
        List<WorkoutPlan> actualPlans = controller.getUserWorkoutPlans();
        assertTrue(actualPlans.isEmpty());
    }

    @Test
    public void getPlanDetails_shouldReturnPlanItems() {
        Integer planId = 1;
        List<PlanItem> expectedItems = Arrays.asList(new PlanItem(), new PlanItem());
        when(workoutPlanDao.getPlanItems(planId)).thenReturn(expectedItems);
        List<PlanItem> actualItems = controller.getPlanDetails(planId);
        assertEquals(2, actualItems.size());
        assertEquals(expectedItems, actualItems);
    }

    @Test
    public void getAvailableExercises_whenUserLoggedIn_shouldReturnExercises() throws Exception {
        User loggedInUser = new User();
        loggedInUser.setId(123);
        setField(controller, "currentUser", loggedInUser);
        List<Exercise> expectedExercises = Arrays.asList(new Exercise(), new Exercise());
        when(exerciseDao.getAllExercises(123)).thenReturn(expectedExercises);

        List<Exercise> actualExercises = controller.getAvailableExercises();

        assertEquals(expectedExercises, actualExercises);
    }

    @Test
    public void getAvailableExercises_whenNotLoggedIn_shouldReturnEmptyList() throws Exception {
        setField(controller, "currentUser", null);
        List<Exercise> actualExercises = controller.getAvailableExercises();
        assertTrue(actualExercises.isEmpty());
    }

    @Test
    public void getUserStatistics_whenUserLoggedIn_shouldReturnStats() throws Exception {
        User loggedInUser = new User();
        loggedInUser.setId(123);
        setField(controller, "currentUser", loggedInUser);
        Statistics expectedStats = new Statistics();
        when(statisticsDao.getUserStatistics(123)).thenReturn(expectedStats);

        Statistics actualStats = controller.getUserStatistics();

        assertEquals(expectedStats, actualStats);
    }

    @Test
    public void getUserStatistics_whenNotLoggedIn_shouldReturnNewStats() throws Exception {
        setField(controller, "currentUser", null);
        Statistics actualStats = controller.getUserStatistics();
        assertNotNull(actualStats);
    }

    @Test
    public void getCompletedSessions_shouldReturnSessionsForUser() {
        int userId = 456;
        List<TrainingSession> expectedSessions = Arrays.asList(new TrainingSession(), new TrainingSession());
        when(trainingSessionDao.getCompletedSessions(userId)).thenReturn(expectedSessions);

        List<TrainingSession> actualSessions = controller.getCompletedSessions(userId);

        assertEquals(expectedSessions, actualSessions);
    }


    @Test
    public void getAllExerciseStats_whenNotLoggedIn_shouldReturnEmptyList() throws Exception {
        setField(controller, "currentUser", null);
        List<ExerciseStatsDTO> stats = controller.getAllExerciseStats();
        assertTrue(stats.isEmpty());
    }

    @Test
    public void deleteWorkoutPlan_shouldCallDaoDelete() {
        int planIdToDelete = 789;
        controller.deleteWorkoutPlan(planIdToDelete);
        verify(workoutPlanDao).deletePlan(planIdToDelete);
    } */



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
