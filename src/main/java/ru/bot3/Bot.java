package ru.bot3;


import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.bot3.bot.listeners.GameListner;

import javax.security.auth.login.LoginException;

@Service
public class Bot {

    private String SECRET_TOKEN = "NzA3OTE5Njc0ODU5NDU0NDc0.XrQD7A.QpcH_H5wP50a9oHDwBLu4efDLeA";
    private JDA jda;


    public Bot(ApplicationContext context) {
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(SECRET_TOKEN)
                    .setActivity(Activity.watching("Game of Thrones"))
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .addEventListeners(context.getBean(GameListner.class))
                    .build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }



    public JDA getJda() {
        return jda;
    }

    public void setJda(JDA jda) {
        this.jda = jda;
    }
}
