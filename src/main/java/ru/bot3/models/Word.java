package ru.bot3.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "word")
public class Word {
    @Id
    @GeneratedValue(strategy =  GenerationType.AUTO)
    @Column(name = "word_id", unique = true, nullable = false)
    private Long id;

    @Column
    private String text;

    @OneToOne(cascade = CascadeType.ALL)
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    private User appointed;

    @OneToMany(mappedBy = "word")
    private List<Definition> definitions;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="room_id", nullable=false)
    private Room room;
}
