package ru.bot3.bot.commands;

import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bot3.bot.listeners.GameListner;
import ru.bot3.models.Room;
import ru.bot3.models.User;
import ru.bot3.services.UserService;

import java.io.IOException;

@Component
public class ListCommand extends GuildCommand {
    @Autowired
    private UserService userService;

    public ListCommand() {
        prefix = "!list";
        description = "Эта команда показывает список участников игры и счет";
    }

    @SneakyThrows
    @Override
    public void execute(GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        Room room = gameService.getRoom(event.getChannel().getIdLong());
        if (!channelPool.contains(channel)){
            channel.sendMessage("Эта команда пока недоступна в вашем чате. Для ее использования создайте комнату для игры.").queue();
        }
        else {
            if (room.getUsers().size() == 0){
                event.getChannel().sendMessage("К сожалению, еще никто не присоеденился к игре. Ты можешь стать первым!").queue();
            }
            else {
                EmbedBuilder builder = new EmbedBuilder().setTitle("Список зарегистрированных участников");
                for (User user: room.getUsers()){
                    builder.addField(user.getName(), "Очки: " + user.getScore(), false);
                }
                event.getChannel().sendMessage(builder.build()).queue();
            }
        }
    }
}