package ru.bot3.bot.commands;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import ru.bot3.bot.Game;
import ru.bot3.exceptions.GameStopException;
import ru.bot3.models.Definition;
import ru.bot3.models.Room;
import ru.bot3.models.User;
import ru.bot3.models.Word;
import ru.bot3.repositories.DefinitionRepo;
import ru.bot3.repositories.RoomRepo;
import ru.bot3.repositories.WordRepo;
import ru.bot3.services.ChannelPool;
import ru.bot3.services.GameService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class StartGameCommand extends GuildCommand {
    private final String PRIVATE_MSG = "Привет, пиши мне для ввода слов и сокращений, а также голосования.";
    private final String ERROR_MSG = "Извините, что-то пошло не так и я вынужден с вами попращаться. Пока!";
    private final String HELLO_MSG = "Итак, вы решили начать игру дешифровка.";
    private final String HELLO_MSG2 = "Напомню правила. Сначала вы загадываете сокращение из нескольких букв.";
    private final String HELLO_MSG3 = "Затем, вы даете 2 расшифровки для предложенных вам сокращений.";
    private final String HELLO_MSG4 = "Затем я покажу вам сокращение и 2 варианта расшифровки и вы выбрать понравившуюся больше всего расшифровку";
    private final String HELLO_MSG5 = "А в конце я покажу вам самую лучшую пару. Ну что Начинаем!";
    private final String GAME_WORD = "Каждому я написал в личные сообщения и жду сокращений. Вам дается 60 секунд.";
    private final String GAME_DEF = "Теперь придумайте расшифровку. Вам дается 60 секунд.";
    private final String GAME_CHOOSE = "Теперь пришло время выбрать лучших.";
    private final String GAME_CHOOSE_BETWEEN = "У нас сокращение \"%s\" и 2 расшифровки:";
    private final String GAME_CHOOSE_BETWEEN2 = "|1|: \"%s\" |2|: \"%s\"";
    private final String GAME_CHOOSE_BETWEEN3 = "Пишите 1 или 2 в личный чат со мной для голосования";
    private final String GAME_CHOOSE_BETWEEN4 = "И для сокращения \"%s\" побеждает расшифровка \"%s\".";
    private final String GAME_CHOOSE_FAIL = "И у нас только 1 расшифровка для слова \"%s\". И \"%s\" получает все ваши голоса.";
    private final String GAME_CHOOSE_FAIL2 = "И у нас нет расшифровок для слова \"%s\". Не надо так.:(";
    private final String GAME_CHOOSE_FAIL3 = "И у нас нет слов. Совсем(. Ну... Тогда игра закончилась и у нас нет победителя.";
    private final String GAME_CHOOSE_RESULT = "Слово придумал \"%s\", а расшифровку \"%s\"";
    private final String GAME_CHOOSE_PAUSE = "Ну что, погнали дальше!";
    private final String GAME_FINAL = "И так, слова закончились и осталось лишь назвать победителя";
    private final String GAME_FINAL2 = "И им становится \"%s\" с расшифровкой \"%s\"! Поздравляем!";
    private final String GAME_FINAL3 = "Желаете поиграть еще?";
    private final String GAME_TIMER = "Осталось \"%d\" секунд";
    private final String GAME_TIMER_END = "Время вышло";

    private final Integer TIMER = 60;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GameService gameService;

    @Autowired
    private ChannelPool channelPool;

    @Autowired
    private RoomRepo roomRepo;

    @Autowired
    private DefinitionRepo definitionRepo;

    @Autowired
    private WordRepo wordRepo;

    private Long bestDefId = null;

    private Room room;

    public StartGameCommand() {
        prefix = "!start";
        description = "Эта команда начинает игру";
    }

    @Override
    public void execute(GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        room = gameService.getRoom(event.getChannel().getIdLong());
        if (!channelPool.contains(channel)){
            channel.sendMessage("Эта команда пока недоступна в вашем чате. Для ее использования создайте комнату для игры.").queue();
        }
        else {
            if (gameService.contain(room, event.getAuthor().getIdLong()) || event.getAuthor().equals(event.getJDA().getSelfUser())){
                if (room.getUsers().size() == 0){
                    event.getChannel().sendMessage("К сожалению, еще мало людей для игры. Присоеденесь!").queue();
                }
                else {
//                    Game game = applicationContext.getBean(Game.class);
//                    game.setRoomId(room.getCommonChannelId());
//                    taskExecutor.execute(game);
//                    applicationContext.getAutowireCapableBeanFactory().autowireBean(game);
//                    game.start();
                    try {
                        startGame();
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }
            }
            else {
                event.getChannel().sendMessage(String.format("К сожалению, %s, только зарегистрированные участники имеют право наинать игру.", event.getAuthor().getAsMention())).queue();
            }
        }
    }

    private void startGame() throws IOException {
        try {
            room.setState(Room.State.created);
            room = roomRepo.save(room);

            channelPool.sendMessage(room, HELLO_MSG);
            channelPool.sendMessage(room, HELLO_MSG2);
            channelPool.sendMessage(room, HELLO_MSG3);
            channelPool.sendMessage(room, HELLO_MSG4);
            channelPool.sendMessage(room, HELLO_MSG5);
            sendPrivateMessage(room, PRIVATE_MSG);

            room.setState(Room.State.words);
            room = roomRepo.save(room);

            channelPool.sendMessage(room, GAME_WORD);
            doTimer(TIMER, room);

            room.setState(Room.State.definitions);
            gameService.fillEmptyWords(room);
            gameService.assignWords(room);
            room = roomRepo.save(room);

            channelPool.sendMessage(room, GAME_DEF);
            doTimer(TIMER, room);

            room.setState(Room.State.vote);
            room = roomRepo.save(room);

            channelPool.sendMessage(room, GAME_CHOOSE);
            if (room.getWords().size() == 0) {
                channelPool.sendMessage(room, GAME_CHOOSE_FAIL3);
                room.setState(Room.State.created);
                room = roomRepo.save(room);
            } else {
                for (Word word : room.getWords()) {
                    if (word.getDefinitions().size() == 0) {
                        channelPool.sendMessage(room, GAME_CHOOSE_FAIL2);
                    } else if (word.getDefinitions().size() == 2) {
                        channelPool.sendMessage(room, String.format(GAME_CHOOSE_BETWEEN, word.getText()));
                        channelPool.sendMessage(room, String.format(GAME_CHOOSE_BETWEEN2, word.getDefinitions().get(0).getText(), word.getDefinitions().get(1).getText()));
                        channelPool.sendMessage(room, GAME_CHOOSE_BETWEEN3);
                        doTimer(TIMER/2, room);
                        word = wordRepo.getOne(word.getId());
                        Definition d = word.getDefinitions().get(0).getScore() > word.getDefinitions().get(1).getScore() ? word.getDefinitions().get(0) : word.getDefinitions().get(1);
                        channelPool.sendMessage(room, String.format(GAME_CHOOSE_BETWEEN4, word.getText(), d.getText()));
                        channelPool.sendMessage(room, String.format(GAME_CHOOSE_RESULT, word.getUser().getName(), d.getUser().getName()));

                        if (bestDefId == null) {
                            bestDefId = d.getId();
                        } else {
                            bestDefId = definitionRepo.getOne(bestDefId).getScore() > d.getScore() ? bestDefId: d.getId();
                        }
                    } else {
                        Definition d = word.getDefinitions().get(0) == null ? word.getDefinitions().get(1) : word.getDefinitions().get(0);
                        channelPool.sendMessage(room, String.format(GAME_CHOOSE_FAIL, word.getText(), d.getText()));
                        channelPool.sendMessage(room, String.format(GAME_CHOOSE_RESULT, word.getUser().getName(), d.getUser().getName()));
                        d.setScore(room.getUsers().size());
                        if (bestDefId == null) {
                            bestDefId = d.getId();
                        } else {
                            bestDefId = definitionRepo.getOne(bestDefId).getScore() > d.getScore() ? bestDefId: d.getId();
                        }
                        definitionRepo.save(d);
                    }
                    channelPool.sendMessage(room, GAME_CHOOSE_PAUSE);
                    room.voteInc();
                    roomRepo.save(room);
                    room = roomRepo.getOne(room.getId());
                }
                channelPool.sendMessage(room, GAME_FINAL);

                room.setState(Room.State.result);
                room = roomRepo.save(room);

                Definition definition = definitionRepo.getOne(bestDefId);
                channelPool.sendMessage(room, String.format(GAME_FINAL2, definition.getWord().getText(), definition.getText()));
                channelPool.sendMessage(room, String.format(GAME_CHOOSE_RESULT, definition.getWord().getUser().getName(), definition.getUser().getName()));

                room.setState(Room.State.end);
                room = roomRepo.save(room);
                channelPool.sendMessage(room, GAME_FINAL3);
                gameService.updateScore(room);
            }
        } catch (GameStopException | IOException | InterruptedException e) {
            channelPool.sendMessage(roomRepo.getOne(room.getId()), ERROR_MSG);
        }
    }

    private void doTimer(int sec, Room room) throws InterruptedException, GameStopException, IOException {
        if (sec > 30) {
            channelPool.sendMessage(room, String.format(GAME_TIMER, sec));
            if (Thread.currentThread().isInterrupted()) {
                throw new GameStopException("Game stop");
            }
            Thread.sleep(sec / 2 * 1000);
            channelPool.sendMessage(room, String.format(GAME_TIMER, sec / 2));
            Thread.sleep(sec / 4 * 1000);
            channelPool.sendMessage(room, String.format(GAME_TIMER, sec / 4));
            Thread.sleep(sec / 4 * 1000);
            if (Thread.currentThread().isInterrupted()) {
                throw new GameStopException("Game stop");
            }
            channelPool.sendMessage(room, GAME_TIMER_END);
        } else {
            channelPool.sendMessage(room, String.format(GAME_TIMER, sec));
            Thread.sleep(sec / 2 * 1000);
            if (Thread.currentThread().isInterrupted()) {
                throw new GameStopException("Game stop");
            }
            channelPool.sendMessage(room, String.format(GAME_TIMER, sec / 2));
            Thread.sleep(sec / 2 * 1000);
            if (Thread.currentThread().isInterrupted()) {
                throw new GameStopException("Game stop");
            }
            channelPool.sendMessage(room, GAME_TIMER_END);
        }
    }

    private void sendPrivateMessage(Room room, String message) {
        for (User user : room.getUsers()) {
            try {
                channelPool.sendPrivateMessage(user, message);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private List<Integer> sequence(int size){
        Random r = new Random();
        List<Integer> list = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();
        for (int i = 0; i < size; i++){
            list.add(i);
        }
        for (int i = 0; i < size; i++){
            list2.add(list.get(r.nextInt(list.size())));
        }
        return list2;
    }

}
