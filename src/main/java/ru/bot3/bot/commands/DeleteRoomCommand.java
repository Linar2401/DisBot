package ru.bot3.bot.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.stereotype.Component;
import ru.bot3.models.Room;

import java.io.IOException;

@Component
@Slf4j
public class DeleteRoomCommand extends GuildCommand {
    public DeleteRoomCommand() {
        prefix = "!delete_room";
        description = "Эта команда удаляет комнату для игры";
    }

    @Override
    public void execute(GuildMessageReceivedEvent event) {
        Room room = gameService.getRoom(event.getChannel().getIdLong());
        if (room != null){
            channelPool.doEndGame(room);
            gameService.doEndGame(room);
            event.getChannel().sendMessage("Комната в этом чате успешно удалена").queue();
        }
        else {
            event.getChannel().sendMessage("Комната в этом чате не существует").queue();
        }
    }
}
