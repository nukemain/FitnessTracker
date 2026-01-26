package pl.fitnesstracker.fitnesse;

import pl.fitnesstracker.dao.UserDao;
import pl.fitnesstracker.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryUserDao extends UserDao {
    private final List<User> users = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(100);

    @Override
    public boolean registerUser(User user) {
        user.setId(idGenerator.incrementAndGet());
        users.add(user);
        return true;
    }

    @Override
    public Optional<User> login(String email, String rawPassword) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }
    
    @Override
    public boolean updateUser(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
}
