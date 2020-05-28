package ru.bot3.bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ru.bot3.bot.listeners.GameListner;

@Component
public class HelpGuildCommand extends GuildCommand {
    private final ApplicationContext context;

    public HelpGuildCommand(ApplicationContext context) {
        this.context = context;
        prefix = "!help";
        description = "Эта команда выводит список всех команд";
    }

    @Override
    public void execute(GuildMessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder().setTitle("Список моих команд");
//        builder.addField("!help", "Эта команда выводит список всех команд", false);
//        builder.addField("!room_create", "Эта команда создает комнату для игры. Только одна комната на канал!", false);
//        builder.addField("!room_delete", "Эта команда удаляет комнату для игры.", false);
//        builder.addField("!room_start", "Эта команда запускает игру", false);
//        builder.addField("!room_reg", "Эта команда регистрирует нового участника(тебя) для игры", false);
//        builder.addField("!retry", "Эта команда позволяет начать игру заново", false);
//        builder.addField("!stop", "Эта команда остонавливает игру", false);
        builder.setDescription("Commands");
        for (GuildCommand command: context.getBean(GameListner.class).getGuildCommands()){
            builder.addField(command.getName(), command.getDescription(), false);
        }
        event.getChannel().sendMessage(builder.build()).queue();
        }
}
