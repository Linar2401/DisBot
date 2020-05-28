package ru.bot3.bot.listeners;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ru.bot3.bot.commands.GuildCommand;
import ru.bot3.models.Room;
import ru.bot3.models.User;
import ru.bot3.services.ChannelPool;
import ru.bot3.services.GameService;
import ru.bot3.services.UserService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GameListner extends ListenerAdapter {
    @Getter
    private List<GuildCommand> guildCommands = new LinkedList<>();

    private final ApplicationContext context;

    @Autowired
    private ChannelPool channelPool;

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    public GameListner(ApplicationContext context) {
        this.context = context;
        Map<String, GuildCommand> s2 = context.getBeansOfType(GuildCommand.class);
        context.getBeansOfType(GuildCommand.class).forEach((s, guildCommand) -> guildCommands.add(guildCommand));
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        for (GuildCommand guildCommand : guildCommands){
            if (guildCommand.use(event)){
                guildCommand.execute(event);
                break;
            }
        }
        redirectMessage(event);
    }

    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()){
            if (channelPool.contains(event.getAuthor())){
                User user = userService.getByChatId(event.getAuthor().getIdLong());
                Room room = user.getRoom();
                if (room.getState().equals(Room.State.words)){
                    gameService.addWord(event.getMessage().getContentDisplay(), user);
                }
                else if(room.getState().equals(Room.State.definitions)){
                    if (user.isFirstDef()){
                        event.getChannel().sendMessage("Вы создали первое определение, давайте следущее").queue();
                    }
                    gameService.addDefinition(event.getMessage().getContentDisplay(), user);
                }
                else if (room.getState().equals(Room.State.vote)){
                    gameService.incScore(room, user);
                }
                else {
                    event.getChannel().sendMessage("Вы вам пока не доступны никакие команды").queue();
                }
            }
            else {
                event.getChannel().sendMessage("Вы не зарегистрированы в какой-либо игре.").queue();
            }
        }
    }

    private void redirectMessage(GuildMessageReceivedEvent event){
        Room room = gameService.getRoom(event.getChannel().getIdLong());
        try {
            if (event.getAuthor().equals(event.getJDA().getSelfUser())){
                channelPool.sendMessageToWS(room, event.getMessage().getContentDisplay());
            }
            else {
                channelPool.sendMessageToWS(room, event.getAuthor().getName() + ":" + event.getMessage().getContentDisplay());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
