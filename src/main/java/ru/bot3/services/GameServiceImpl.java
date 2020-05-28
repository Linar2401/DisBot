package ru.bot3.services;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import ru.bot3.models.*;
import ru.bot3.repositories.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class GameServiceImpl implements GameService {
    private final String[] DEFAULT_WORDS = new String[]{"ПНХ", "ПМЖ", "ЧВК", "СТГ", "МДК"};

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoomRepo roomRepo;

    @Autowired
    private WordRepo wordRepo;

    @Autowired
    private WordAssignmentRepo wordAssignmentRepo;

    @Autowired
    private DefinitionRepo definitionRepo;

    @Autowired
    private ChannelPool channelPool;

    @Override
    public Room createRoom(TextChannel channel) {
        return createRoom(channel.getIdLong());
    }

    @Override
    public Room createRoom(WebSocketSession session) {
        return createRoom(channelPool.addPrivateChannel(session));
    }

    private Room createRoom(Long channelId){
        Room room = Room.builder().inviteKey(generateInviteKey()).state(Room.State.started).commonChannelId(channelId)
                .build();
        roomRepo.save(room);
        return room;
    }


    @Override
    public void startGame(Long userIdInChat) {

    }

    @Override
    public void doEndGame(Room room) {
        roomRepo.delete(room);
    }

    @Override
    public void addWord(String word, User user) {
        Word word1 = Word.builder().text(word).user(user).room(user.getRoom()).build();
        wordRepo.save(word1);
        user.setWord(word1);
        userRepo.save(user);
        Room room = user.getRoom();
        room.getWords().add(word1);
        roomRepo.save(room);
    }

    @Override
    public void addDefinition(String def, User user) {
        Definition definition;
        Word word = user.isFirstDef()?user.getAppointed().getWord1():user.getAppointed().getWord2();
        Room room = user.getRoom();
        definition = Definition.builder().room(user.getRoom()).score(0).text(def).user(user).word(word).build();
        definitionRepo.save(definition);
        word.getDefinitions().add(definition);
        wordRepo.save(word);
        room.getDefinitions().add(definition);
        roomRepo.save(room);
    }

    @Override
    public synchronized void incScore(Room room, User user) {
        Word word = room.getWords().get(room.getVote());
        Definition d;
        if (user.isFirstDef()){
            d = word.getDefinitions().get(0);
            user.setFirstDef(false);
            userRepo.save(user);
        }
        else {
            d = word.getDefinitions().get(1);
        }
        d.setScore(d.getScore()+1);
        definitionRepo.save(d);
    }

    @Override
    public Room getRoom(Long chatId) {
        for (Room room: roomRepo.findAll()){
            if (room.getCommonChannelId().equals(chatId)){
                return room;
            }
        }
        return null;
    }

    @Override
    public boolean contain(Room room, Long inChatId) {
        for (User user: room.getUsers()){
            if (user.getUserInChatId().equals(inChatId)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void changeState(Room.State state, Room room) {
        room = roomRepo.getOne(room.getId());
        room.setState(state);
        roomRepo.save(room);
    }

    @Override
    public void assignWords(Room room) {
        List<User> users = room.getUsers();
        List<Word> words = room.getWords();
        List<Integer> a = sequence(users.size());
        List<Integer> b = sequence(users.size());
        User user;
        for (int i = 0; i < users.size(); i++){
            user = users.get(i);
            WordUserAssignment wordUserAssignment = WordUserAssignment.builder().user(user).word1(words.get(a.get(i))).word2((words.get(b.get(i)))).build();
            wordAssignmentRepo.save(wordUserAssignment);
            user.setAppointed(wordUserAssignment);
            userRepo.save(user);
            try {
                channelPool.sendPrivateMessage(user, String.format("Тебе 2 слова: \"%s\" и \"%s\"", words.get(a.get(i)).getText(), words.get(b.get(i)).getText()));
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public void fillEmptyWords(Room room) {
        Random r = new Random();
        List<User> users = room.getUsers();
        for(User user: users){
            if (user.getWord() == null){
                Word word = Word.builder().room(room).text(DEFAULT_WORDS[r.nextInt(DEFAULT_WORDS.length)]).user(user).build();
                wordRepo.save(word);
                user.setWord(word);
                wordRepo.save(word);
            }
        }
    }

    @Override
    public void updateScore(Room room) {
        for (User user: room.getUsers()){
            int score = 0;
            for (Definition definition: user.getDefenitions()){
                score+= definition.getScore();
            }
            user.setScore(score);
            userRepo.save(user);
        }
    }


    private String generateInviteKey(){
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        boolean unique = false;
        while (!unique){
            int upper = 0;
            for (int i = 0; i < 5; i++){
                upper = r.nextBoolean()?32:0;
                sb.append((char)(r.nextInt(26)+65+upper));
            }
            unique = true;
            for (Room room: roomRepo.findAll()){
                if (room.getInviteKey().equals(sb.toString())){
                    unique = false;
                    break;
                }
            }
        }
        return sb.toString();
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
