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
@Table(name = "definitions")
public class Definition {
    @Id
    @GeneratedValue(strategy =  GenerationType.AUTO)
    @Column(name = "definition_id", unique = true, nullable = false)
    private Long id;

    @Column
    private String text;

    @Column
    private Integer score;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "word_id")
    private Word word;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user")
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="room_id", nullable=false)
    private Room room;
}