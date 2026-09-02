package ru.berezhnoy.shop.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.berezhnoy.shop.domain.User;

@Service
public class DemoUserService {

    private final int userId;
    private final UserRepository userRepository;

    public DemoUserService(
            @Value("${shop.demo-user-id}") int userId,
            UserRepository userRepository
    ) {
        this.userId = userId;
        this.userRepository = userRepository;
    }

    public int userId() {
        return userId;
    }

    public User requireUser() {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Demo user " + userId + " is missing"));
    }
}
