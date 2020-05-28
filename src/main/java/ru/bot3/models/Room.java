package ru.bot3.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy =  GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @OneToMany(mappedBy = "room", fetch = FetchType.EAGER)
    private List<User> users;

    @OneToMany(mappedBy = "room", fetch = FetchType.EAGER)
    private List<Word> words;

    @OneToMany(mappedBy = "room", fetch = FetchType.EAGER)
    private List<Definition> definitions;

    @Column(unique = true)
    private String inviteKey;

    @Column
    private Long commonChannelId;

    @Column
    private int vote;

    public boolean contain(Long id){
        for (User user: users){
            if (user.getId().equals(id)){
                return true;
            }
        }
        return false;
    }

    public enum State{
        created, started, words, definitions, vote, result, end
    }

    @Enumerated(value = EnumType.STRING)
    private State state;

    public void addUser(User user){
        this.users.add(user);
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                '}';
    }

    public void voteInc(){
        vote++;
    }
}
