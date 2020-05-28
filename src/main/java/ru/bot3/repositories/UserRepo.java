package ru.bot3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bot3.models.Room;
import ru.bot3.models.User;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> getById(Long id);
    Optional<User> getByChatId(String id);
    void deleteByRoom(Room room);
}
