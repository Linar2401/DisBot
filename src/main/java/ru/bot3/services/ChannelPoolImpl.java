package ru.bot3.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.bot3.dto.Message;
import ru.bot3.models.Room;
import ru.bot3.repositories.RoomRepo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChannelPoolImpl implements ChannelPool {
    private Long nextId;
    private volatile Map<Long, WebSocketSession> webSocketClients;
    private volatile Map<Long, User> discordClients;
    private volatile Map<Long, TextChannel> channels;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    RoomRepo roomRepo;

    public ChannelPoolImpl() {
        webSocketClients = new HashMap<>();
        discordClients = new HashMap<>();
        channels = new HashMap<>();
        nextId = 0L;
    }

    @Override
    public Long addPrivateChannel(WebSocketSession channel) {
        webSocketClients.put(nextId++, channel);
        return nextId - 1;
    }

    @Override
    public void addPrivateChannel(Long id, User user) {
        discordClients.put(id, user);
    }

    @Override
    public void addChannel(TextChannel channel) {
        channels.put(channel.getIdLong(), channel);
    }

    @Override
    public void sendPrivateMessage(ru.bot3.models.User user, String message) throws IOException {
        if (user.isDiscord()) {
            discordClients.get(user.getId()).openPrivateChannel()
                    .queue(privateChannel -> privateChannel.sendMessage(message).queue());
        }
        else {
            webSocketClients.get(user.getUserInChatId())
                    .sendMessage(new TextMessage(objectMapper.writeValueAsString(Message.builder()
                            .type(2)
                            .text(message)
                            .build())));
        }
    }

    @Override
    public void sendMessage(Room room, String message) throws IOException {
        channels.get(room.getCommonChannelId()).sendMessage(message).queue();
//        sendMessageToWS(room, message);
    }

    @Override
    public void sendMessageToWS(Room room, String message) throws IOException {
        if (room.getUsers() != null){
            for (ru.bot3.models.User user: room.getUsers()){
                if (!user.isDiscord()){
                    webSocketClients.get(user.getUserInChatId()).sendMessage(new TextMessage(objectMapper.writeValueAsString(Message.builder()
                            .type(1)
                            .text(message)
                            .build())));
                }
            }
        }
    }

    @Override
    public void doEndGame(Room room) {
        channels.remove(room.getCommonChannelId());
        for (ru.bot3.models.User user: room.getUsers()){
            if (user.isDiscord()){
                discordClients.remove(user.getId());
            }
            else {
                webSocketClients.remove(user.getId());
            }
        }
    }

    @Override
    public boolean contains(WebSocketSession channel) {
        for (WebSocketSession channel1: webSocketClients.values()){
            if (channel.equals(channel)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(User user) {
        for(User user1: discordClients.values()){
            if (user1.equals(user)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(TextChannel channel) {
        for(TextChannel channel1: channels.values()){
            if (channel1.equals(channel)){
                return true;
            }
        }
        return false;
    }

}
