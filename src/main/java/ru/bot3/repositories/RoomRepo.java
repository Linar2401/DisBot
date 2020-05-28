package ru.bot3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bot3.models.Room;

import java.util.Optional;

public interface RoomRepo extends JpaRepository<Room, Long> {
    Optional<Room> getByInviteKey(String string);
}
