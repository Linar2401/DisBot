package ru.bot3.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.bot3.dto.Message;
import ru.bot3.models.Room;
import ru.bot3.models.User;
import ru.bot3.repositories.RoomRepo;
import ru.bot3.repositories.UserRepo;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepo repo;

    @Autowired
    private RoomRepo roomRepo;

    @Autowired
    private GameService gameService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChannelPool channelPool;

    @Override
    public User save(GuildMessageReceivedEvent event) {
        Room room = gameService.getRoom(event.getChannel().getIdLong());
        User user = User.builder()
                .userInChatId(event.getAuthor().getIdLong())
                .chatId(event.getChannel().getIdLong())
                .discord(true)
                .room(room)
                .name(event.getAuthor().getName())
                .score(0)
                .firstDef(true)
                .build();
        repo.save(user);
        return user;
    }

    @Override
    public Optional<User> save(WebSocketSession session, Message message) throws IOException {
        String roomKey = message.getText().substring(0, message.getText().indexOf("|"));
        Optional<Room> roomOptional = roomRepo.getByInviteKey(roomKey);
        if (!roomOptional.isPresent()){
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                    Message.builder()
                            .text("Для общения необходимо зарегистрироваться.")
                            .type(2)
                            .build()
            )));
        }
        else {
            Room room = roomOptional.get();
            Long id = channelPool.addPrivateChannel(session);
            User user = User.builder()
                    .name(message.getText().substring(message.getText().indexOf("|")+1))
                    .firstDef(true)
                    .room(room)
                    .discord(false)
                    .chatId(room.getCommonChannelId())
                    .userInChatId(id)
                    .score(0)
                    .build();
            repo.save(user);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    public void remove(Long id) {
        repo.deleteById(id);
    }

    @Override
    public void remove(List<Long> id) {
        for (Long d:id){
            this.remove(d);
        }
    }

    @Override
    public User get(Long id) {
        return null;
    }

    @Override
    public User getByChatId(Long id) {
        for (User user: repo.findAll()){
            if (user.getUserInChatId().equals(id)){
                return user;
            }
        }
        return null;
    }
}
