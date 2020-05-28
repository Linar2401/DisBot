package ru.bot3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bot3.models.Definition;

public interface DefinitionRepo extends JpaRepository<Definition, Long> {
    }
