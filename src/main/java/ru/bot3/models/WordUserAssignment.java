package ru.bot3.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "word_user")
public class WordUserAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "word_user1", referencedColumnName = "word_id")
    private Word word1;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "word_user2", referencedColumnName = "word_id")
    private Word word2;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "word_user3", referencedColumnName = "user_id")
    private User user;
}
