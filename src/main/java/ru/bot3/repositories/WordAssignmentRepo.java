package ru.bot3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bot3.models.WordUserAssignment;

public interface WordAssignmentRepo extends JpaRepository<WordUserAssignment, Long> {
}
