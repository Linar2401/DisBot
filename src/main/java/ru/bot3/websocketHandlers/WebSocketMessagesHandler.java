package ru.bot3.websocketHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.bot3.bot.commands.GuildCommand;
import ru.bot3.bot.listeners.GameListner;
import ru.bot3.dto.Message;
import ru.bot3.models.Room;
import ru.bot3.models.User;
import ru.bot3.services.ChannelPool;
import ru.bot3.services.GameService;
import ru.bot3.services.UserService;

import java.io.IOException;
import java.util.Optional;

@Component
@EnableWebSocket
@Slf4j
public class WebSocketMessagesHandler extends TextWebSocketHandler {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChannelPool channelPool;

    @Autowired
    private final ApplicationContext context;

    public WebSocketMessagesHandler(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String messageText = (String) message.getPayload();
        Message messageJ = objectMapper.readValue(messageText, Message.class);
        if (messageJ.getType() == 3){
            Optional<User> optional = userService.save(session, messageJ);
            if (optional.isPresent()){
                sendMessage(session, 3, "" + optional.get().getUserInChatId());
            }
        } else if (messageJ.getType() == 2){
            onPrivateMessageReceived(session, messageJ);
        } else if (messageJ.getType() == 1){
            onMessageReceived(session, messageJ);
        } else  if (messageJ.getType() == 0){
            if (messageJ.getText().equals("l")){
                listCommand(session, messageJ);
            } else if (messageJ.getText().equals("h")){
                helpCommand(session, messageJ);
            } else if (messageJ.getText().equals("s")){
                startCommand(session, messageJ);
            }
        }
    }

    private void onPrivateMessageReceived(WebSocketSession session, Message message) throws IOException {
        if (channelPool.contains(session)){
            User user = userService.getByChatId(message.getFrom());
            Room room = user.getRoom();
            if (room.getState().equals(Room.State.words)){
                gameService.addWord(message.getText(), user);
            }
            else if(room.getState().equals(Room.State.definitions)){
                if (user.isFirstDef()){
                    sendMessage(session, 2, "Вы создали первое определение, давайте следущее");
                }
                gameService.addDefinition(message.getText(), user);
            }
            else if (room.getState().equals(Room.State.vote)){
                gameService.incScore(room, user);
            }
            else {
                sendMessage(session, 2, "Вам пока не доступны какие либо команды. Внимательно следите за главным чатом и прогрессом игры");
            }
        }
        else {
            sendMessage(session, 2, "Вы не зарегистрированы в какой-либо игре.");
        }
    }

    private void onMessageReceived(WebSocketSession session, Message message) throws IOException {
        if (channelPool.contains(session)){
            User user = userService.getByChatId(message.getFrom());
            Room room = user.getRoom();
            channelPool.sendMessage(room, String.format("%s: %s", user.getName(), message.getText()));
        }
        else {
            sendMessage(session, 1, "Вы не зарегистрированы в какой-либо игре.");
        }
    }

    private void helpCommand(WebSocketSession session, Message message) throws IOException {
        if (channelPool.contains(session)){
            User user = userService.getByChatId(message.getFrom());
            Room room = user.getRoom();
            StringBuilder sb = new StringBuilder();
            channelPool.sendMessage(room, String.format("%s: %s", user.getName(), "!help"));
            sb.append("Список комманд:\n");
            for (GuildCommand command: context.getBean(GameListner.class).getGuildCommands()){
                sb.append(command.getName()).append(":").append(command.getDescription());
            }
            channelPool.sendMessage(room, sb.toString());
        }
        else {
            sendMessage(session, 3, "Вы не зарегистрированы в какой-либо игре.");
        }
    }

    private void listCommand(WebSocketSession session, Message message) throws IOException {
        if (channelPool.contains(session)){
            User user = userService.getByChatId(message.getFrom());
            Room room = user.getRoom();
            StringBuilder sb = new StringBuilder();
            channelPool.sendMessage(room, String.format("%s: %s", user.getName(), "!list"));
            sb.append("Список участников:\n");
            for (User user1: room.getUsers()){
                sb.append(String.format("%s: %d\n", user1.getName(), user1.getScore()));
            }
            channelPool.sendMessage(room, sb.toString());
        }
        else {
            sendMessage(session, 3, "Вы не зарегистрированы в какой-либо игре.");
        }
    }

    private void startCommand(WebSocketSession session, Message message) throws IOException{
        if (channelPool.contains(session)){
            User user = userService.getByChatId(message.getFrom());
            Room room = user.getRoom();
            channelPool.sendMessage(room, "!start");
        }
        else {
            sendMessage(session, 3, "Вы не зарегистрированы в какой-либо игре.");
        }
    }

    private void sendMessage(WebSocketSession session, int type, String message) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                Message.builder()
                        .text(message)
                        .type(type)
                        .build()
        )));
    }
}
