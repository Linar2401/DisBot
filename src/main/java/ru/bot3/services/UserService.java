package ru.bot3.services;


import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.web.socket.WebSocketSession;
import ru.bot3.dto.Message;
import ru.bot3.models.User;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User save(GuildMessageReceivedEvent event);
    Optional<User> save(WebSocketSession session, Message message) throws IOException;
    void remove(Long id);
    void remove(List<Long> id);
    User get(Long id);
    User getByChatId(Long id);
}
