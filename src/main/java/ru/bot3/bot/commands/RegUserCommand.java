package ru.bot3.bot.commands;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bot3.models.Room;
import ru.bot3.services.UserService;

import java.io.IOException;

@Component
@Slf4j
public class RegUserCommand extends GuildCommand {
    @Autowired
    private UserService userService;

    public RegUserCommand() {
        prefix = "!reg";
        description = "Эта команда регистрирует участника игры";
    }

    @SneakyThrows
    @Override
    public void execute(GuildMessageReceivedEvent event) {
        User user = event.getAuthor();
        TextChannel channel = event.getChannel();
        Room room = gameService.getRoom(event.getChannel().getIdLong());
        if (!channelPool.contains(channel)){
            channel.sendMessage("Эта команда пока недоступна в вашем чате. Для ее использования создайте комнату для игры.").queue();
        }
        else {
            if (channelPool.contains(user)){
                try {
                    channelPool.sendMessage(gameService.getRoom(channel.getIdLong()), user.getAsMention() + ", вы уже зарегистрированы как участник.");
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            else {
                ru.bot3.models.User user1 = userService.save(event);
                channelPool.addPrivateChannel(user1.getId(), user);
                channelPool.sendPrivateMessage(user1, "Это приватный канал для общения с ботом");
                channel.sendMessage(user.getAsMention() + ", вы успешно зарегистрированы как участник.").queue();
            }
        }
    }
}
