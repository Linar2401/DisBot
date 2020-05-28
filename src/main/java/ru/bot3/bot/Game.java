package ru.bot3.bot;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
import org.springframework.orm.hibernate5.SpringSessionContext;

import java.io.IOException;

@Slf4j

@Component
@Scope("prototype")
public class Game extends Thread {
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

    @Setter
    private Long roomId;

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

    private Long bestDefId = null;


    @SneakyThrows
    @Override
    @Transactional
    public void run() {
        try {
            Room room = gameService.getRoom(roomId);
            gameService.changeState(Room.State.started, room);
            room = gameService.getRoom(roomId);


            channelPool.sendMessage(room, HELLO_MSG);
            channelPool.sendMessage(room, HELLO_MSG2);
            channelPool.sendMessage(room, HELLO_MSG3);
            channelPool.sendMessage(room, HELLO_MSG4);
            channelPool.sendMessage(room, HELLO_MSG5);
            sendPrivateMessage(room, PRIVATE_MSG);

            gameService.changeState(Room.State.words, room);
            room = gameService.getRoom(roomId);

            channelPool.sendMessage(room, GAME_WORD);
            doTimer(10, room);

            gameService.changeState(Room.State.definitions, room);
            room = gameService.getRoom(roomId);

            channelPool.sendMessage(room, GAME_DEF);
            doTimer(10, room);

            gameService.changeState(Room.State.vote, room);
            room = gameService.getRoom(roomId);

            channelPool.sendMessage(room, GAME_CHOOSE);
            if (room.getWords().size() == 0) {
                channelPool.sendMessage(room, GAME_CHOOSE_FAIL3);
                room.setState(Room.State.created);
                roomRepo.save(room);
                room = gameService.getRoom(roomId);
            } else {
                for (Word word : room.getWords()) {
                    if (word.getDefinitions().size() == 0) {
                        channelPool.sendMessage(room, GAME_CHOOSE_FAIL2);
                    } else if (word.getDefinitions().size() == 2) {
                        channelPool.sendMessage(room, String.format(GAME_CHOOSE_BETWEEN, word.getText()));
                        channelPool.sendMessage(room, String.format(GAME_CHOOSE_BETWEEN2, word.getDefinitions().get(0).getText(), word.getDefinitions().get(1).getText()));
                        channelPool.sendMessage(room, GAME_CHOOSE_BETWEEN3);
                        doTimer(10, room);
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
                    room = gameService.getRoom(roomId);
                }
                channelPool.sendMessage(room, GAME_FINAL);

                room.setState(Room.State.result);
                roomRepo.save(room);
                room = gameService.getRoom(roomId);

                Definition definition = definitionRepo.getOne(bestDefId);
                channelPool.sendMessage(room, String.format(GAME_FINAL2, definition.getWord().getText(), definition.getText()));
                channelPool.sendMessage(room, String.format(GAME_CHOOSE_RESULT, definition.getWord().getUser().getName(), definition.getUser().getName()));

                room.setState(Room.State.end);
                roomRepo.save(room);
                room = gameService.getRoom(roomId);

                channelPool.sendMessage(room, GAME_FINAL3);

            }
        } catch (GameStopException e) {
            channelPool.sendMessage(roomRepo.getOne(roomId), ERROR_MSG);
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

}
