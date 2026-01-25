package pl.fitnesstracker.fixtures;

import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.dao.UserDao;
import pl.fitnesstracker.model.FitnessModel;
import pl.fitnesstracker.model.User;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class LoginFixture {

    private String email;
    private String password;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean loginSuccessful() {
        // --- OSTATECZNE, POPRAWNE I PROSTE PODEJŚCIE ---
        
        // KROK 1: Tworzymy "udawane" zależności, których potrzebujemy.
        FitnessModel mockModel = mock(FitnessModel.class);
        UserDao mockUserDao = mock(UserDao.class);

        // KROK 2: Konfigurujemy mocki.
        when(mockModel.getUserDao()).thenReturn(mockUserDao);
        when(mockUserDao.login(anyString(), anyString())).thenReturn(Optional.empty()); // Domyślnie porażka
        
        User fakeUser = new User("test@fit.com", "password123", new BigDecimal(80), 180, "goal");
        when(mockUserDao.login("test@fit.com", "password123")).thenReturn(Optional.of(fakeUser)); // Jeden przypadek sukcesu

        // KROK 3: Używamy nowego, testowalnego konstruktora, aby stworzyć kontroler z naszymi "atrapami".
        // Żadna prawdziwa baza danych nie jest już dotykana.
        FitnessSystemController controller = new FitnessSystemController(mockModel);

        // KROK 4: Wywołujemy testowaną metodę.
        return controller.login(this.email, this.password);
    }
}
