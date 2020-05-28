package ru.bot3.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.springframework.web.socket.WebSocketSession;
import ru.bot3.models.Room;

import java.io.IOException;
import java.util.List;

public interface ChannelPool {
    Long addPrivateChannel(WebSocketSession channel);
    void addPrivateChannel(Long id, User user);
    void addChannel(TextChannel channel);
    void sendPrivateMessage(ru.bot3.models.User user, String message) throws IOException;
    void sendMessage(Room room, String message) throws IOException;
    void sendMessageToWS(Room room, String message) throws IOException;
    void doEndGame(Room room);
    boolean contains(WebSocketSession channel);
    boolean contains(User user);
    boolean contains(TextChannel channel);
}
