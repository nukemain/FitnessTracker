package pl.fitnesstracker.fixtures;

import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.dao.TrainingSessionDao;
import pl.fitnesstracker.dao.UserDao;
import pl.fitnesstracker.model.FitnessModel;
import pl.fitnesstracker.model.User;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class StartSessionFixture {

    private int planId;
    private String userEmail;

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    // Użyjemy emaila, aby symulować różnych użytkowników
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // Zwraca ID sesji lub 0, jeśli się nie udało
    public int sessionId() throws Exception {
        // KROK 1: Tworzymy "udawane" zależności
        FitnessModel mockModel = mock(FitnessModel.class);
        UserDao mockUserDao = mock(UserDao.class);
        TrainingSessionDao mockSessionDao = mock(TrainingSessionDao.class);

        // KROK 2: Konfigurujemy mocki
        when(mockModel.getUserDao()).thenReturn(mockUserDao);
        when(mockModel.getTrainingSessionDao()).thenReturn(mockSessionDao);

        // Symulujemy, że użytkownik jest już zalogowany
        User loggedInUser = new User();
        loggedInUser.setId(123); // Dajemy mu stałe ID dla testu
        when(mockUserDao.login(userEmail, "any_password")).thenReturn(Optional.of(loggedInUser));

        // Programujemy główną logikę: jeśli użytkownik o ID 123 rozpoczyna plan 99, zwróć ID sesji 1001
        when(mockSessionDao.startSession(123, 99)).thenReturn(1001);

        // KROK 3: Używamy testowalnego konstruktora
        FitnessSystemController controller = new FitnessSystemController(mockModel);

        // Logujemy "udawanego" użytkownika, aby ustawić go w kontrolerze
        controller.login(userEmail, "any_password");

        // KROK 4: Wywołujemy testowaną metodę
        Integer result = controller.startSession(this.planId);

        // FitNesse nie lubi typów null, więc zwracamy 0 w przypadku porażki
        return result != null ? result : 0;
    }
}
