package ru.bot3.services;

import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.web.socket.WebSocketSession;
import ru.bot3.models.Room;
import ru.bot3.models.User;

public interface GameService {
    Room createRoom(TextChannel channel);
    Room createRoom(WebSocketSession session);
    void startGame(Long userIdInChat);
    void doEndGame(Room room);
    void addWord(String word, User user);
    void addDefinition(String word, User user);
    void incScore(Room room, User user);
    Room getRoom(Long chatId);
    boolean contain(Room room, Long inChatId);
    void changeState(Room.State state, Room room);
    void assignWords(Room room);
    void fillEmptyWords(Room room);

    void updateScore(Room room);
}
