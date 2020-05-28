# DisBot
Discord bot with WebSocketClient.
A bot made while studying at the 2nd year of KFU of the Higher School of ITIS.
# Imperfections
* Due to the use of sqlite database, hibernate loses session in a separate thread
* Due to the reason above, in single-threaded mode, the bot blocks the reception of messages during the game.
