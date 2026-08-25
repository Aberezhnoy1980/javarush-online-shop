package ru.berezhnoy.shop.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.berezhnoy.shop.domain.User;

public interface UserRepository extends JpaRepository<User, Integer> {
}
