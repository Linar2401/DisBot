package ru.bot3.bot.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ru.bot3.bot.listeners.GameListner;
import ru.bot3.models.Room;

import java.io.IOException;

@Component
@Slf4j
public class CreateRoomCommand extends GuildCommand {
    public CreateRoomCommand() {
        prefix = "!create";
        description = "Эта команда создает комнату для игры";
    }

    @Override
    public void execute(GuildMessageReceivedEvent event) {
        if (gameService.getRoom(event.getChannel().getIdLong()) == null){
            Room room = gameService.createRoom(event.getChannel());
            channelPool.addChannel(event.getChannel());
            try {
                channelPool.sendMessage(room, String.format("Игра успешно создана. Код для приглашения: %s", room.getInviteKey()));
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        else {
            event.getChannel().sendMessage("Комната уже создана").queue();
        }
    }
}
