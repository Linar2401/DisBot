package ru.bot3.bot.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bot3.services.ChannelPool;
import ru.bot3.services.GameService;

@Component
public abstract class GuildCommand {
    @Autowired
    protected GameService gameService;

    @Autowired
    protected ChannelPool channelPool;

    protected String prefix;
    protected String description;
    public void execute(GuildMessageReceivedEvent event){

    };
    public boolean use(GuildMessageReceivedEvent event){
        return event.getMessage().getContentDisplay().startsWith(prefix);
    };
    public String getName(){
        return prefix;
    };
    public String getDescription(){
        return description;
    };
}
