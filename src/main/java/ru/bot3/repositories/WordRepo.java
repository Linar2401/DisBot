package ru.bot3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bot3.models.Word;

public interface WordRepo extends JpaRepository<Word, Long> {
}
