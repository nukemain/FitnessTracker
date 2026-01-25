package pl.fitnesstracker.fixtures;

import org.mockito.ArgumentMatcher;
import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.dao.UserDao;
import pl.fitnesstracker.model.FitnessModel;
import pl.fitnesstracker.model.User;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class RegisterFixture {

    private String email;
    private String password;
    private double weight;
    private int height;
    private String goal;

    // Metody do ustawiania danych z tabeli FitNesse
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    // Metoda zwracająca wynik do FitNesse
    public boolean registrationSuccessful() {
        // KROK 1: Tworzymy "udawane" zależności
        FitnessModel mockModel = mock(FitnessModel.class);
        UserDao mockUserDao = mock(UserDao.class);
        when(mockModel.getUserDao()).thenReturn(mockUserDao);

        // KROK 2: Konfigurujemy mocka
        // Domyślnie każda rejestracja się udaje...
        when(mockUserDao.registerUser(any(User.class))).thenReturn(true);
        // ...z wyjątkiem tego jednego, konkretnego emaila.
        when(mockUserDao.registerUser(argThat(user -> "zajety@email.com".equals(user.getEmail())))).thenReturn(false);

        // KROK 3: Używamy testowalnego konstruktora
        FitnessSystemController controller = new FitnessSystemController(mockModel);

        // KROK 4: Wywołujemy testowaną metodę
        return controller.register(email, password, BigDecimal.valueOf(weight), height, goal);
    }
}
