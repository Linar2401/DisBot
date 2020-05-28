package ru.bot3.models;

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
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy =  GenerationType.AUTO)
    @Column(name = "user_id", unique = true, nullable = false)
    private Long id;

    @Column
    private Long userInChatId;

    @Column
    private Long chatId;

    @Column
    private boolean discord;

    @Column
    private String name;

    @Column
    private Integer score = 0;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "word_id")
    private Word word;

    @OneToOne(cascade = CascadeType.ALL)
    private WordUserAssignment appointed;

    @ManyToOne
    @JoinColumn(name="room_id", nullable=false)
    private Room room;

    @OneToMany(mappedBy = "user")
    private List<Definition> defenitions;

    @Column
    private boolean firstDef;
}
