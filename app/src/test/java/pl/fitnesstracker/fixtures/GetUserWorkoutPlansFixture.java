package pl.fitnesstracker.fixtures;

import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.dao.UserDao;
import pl.fitnesstracker.dao.WorkoutPlanDao;
import pl.fitnesstracker.model.FitnessModel;
import pl.fitnesstracker.model.User;
import pl.fitnesstracker.model.WorkoutPlan;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class GetUserWorkoutPlansFixture {

    private String userEmail;

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // Metoda zwracająca do FitNesse liczbę znalezionych planów
    public int planCount() {
        // KROK 1: Tworzymy "udawane" zależności
        FitnessModel mockModel = mock(FitnessModel.class);
        UserDao mockUserDao = mock(UserDao.class);
        WorkoutPlanDao mockPlanDao = mock(WorkoutPlanDao.class);

        // KROK 2: Konfigurujemy mocki
        when(mockModel.getUserDao()).thenReturn(mockUserDao);
        when(mockModel.getWorkoutPlanDao()).thenReturn(mockPlanDao);

        // Konfigurujemy użytkownika, który będzie zalogowany
        User loggedInUser = new User();
        loggedInUser.setId(123); // ID nie ma znaczenia, ale musi być
        when(mockUserDao.login(userEmail, "any_password")).thenReturn(Optional.of(loggedInUser));

        // Konfigurujemy, co ma zwrócić DAO
        // Domyślnie - pustą listę
        when(mockPlanDao.getPlansByUser(anyInt())).thenReturn(Collections.emptyList());
        // Dla użytkownika o ID 123 (czyli naszego zalogowanego) - listę z 3 planami
        List<WorkoutPlan> fakePlans = Arrays.asList(new WorkoutPlan(), new WorkoutPlan(), new WorkoutPlan());
        // Musimy być pewni, że ten użytkownik dostanie tę listę.
        // Symulujemy logowanie tego konkretnego użytkownika w teście FitNesse.
        when(mockPlanDao.getPlansByUser(123)).thenReturn(fakePlans);

        // KROK 3: Używamy testowalnego konstruktora
        FitnessSystemController controller = new FitnessSystemController(mockModel);

        // Symulujemy zalogowanie użytkownika, aby ustawić go w kontrolerze
        if ("user_with_plans@fit.com".equals(userEmail)) {
            controller.login(userEmail, "any_password");
        }

        // KROK 4: Wywołujemy testowaną metodę i zwracamy rozmiar listy
        return controller.getUserWorkoutPlans().size();
    }
}
