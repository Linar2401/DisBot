<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Чат</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <script>
        var webSocket;
        var id = 0;
        function connect() {
            webSocket = new WebSocket('ws://localhost:/bot');
            webSocket.onmessage = function receiveMessage(response) {
                let data = response['data'];
                let json = JSON.parse(data);
                if (json['type'] == 3){
                    id = json['text']
                    $('#messagesList4').first().after("<li>Вы успешно зарегистрированы</li>")
                } else if(json['type'] == 2){
                    $('#messagesList3').first().after("<li>" + json['text'] + "</li>")
                } else {
                    $('#messagesList1').first().after("<li>" + json['text'] + "</li>")
                }
            }
        }
        function sendMessage(text, type) {
            let message = {
                "text": text,
                "from": id,
                "type": type
            };
            webSocket.send(JSON.stringify(message));
        }
    </script>
</head>
<body onload="connect()">
<div>
    <div class="container">
        <div class="row">
            <div class="col-3">
                <h5>Общий чат с ботом</h5>
                <label for="message1">Текст сообщения</label>
                <input name="message1" id="message1" placeholder="Сообщение">
                <button onclick="sendMessage($('#message1').val(), 1)">Отправить</button>
                <h3>Сообщения</h3>
                <ul id="messagesList1">
                </ul>
            </div>
            <div class="col-3">
                <h5>Команды</h5>
                <button onclick="sendMessage('l', 0)">!list</button>
                <button onclick="sendMessage('h', 0)">!help</button>
                <button onclick="sendMessage('s', 0)">!start</button>
                <h3>Сообщения</h3>
                <ul id="messagesList2">
                </ul>
            </div>
            <div class="col-3">
                <h5>Приватный чат с ботом</h5>
                <label for="message2">Текст сообщения</label>
                <input name="message2" id="message2" placeholder="Сообщение">
                <button onclick="sendMessage($('#message2').val(), 2)">Отправить</button>
                <h3>Сообщения</h3>
                <ul id="messagesList3">
                </ul>
            </div>
            <div class="col-3">
                <h5>Присоединение к игре</h5>
                <label for="message3">Код игры</label>
                <input name="message3" id="message3" placeholder="код">
                <br>
                <label for="message3">Имя</label>
                <input name="message4" id="message4" placeholder="имя">
                <button onclick="sendMessage($('#message3').val() + '|' + $('#message4').val(), 3)">Зарегистрироваться</button>
                <h3>Сообщения</h3>
                <ul id="messagesList4">
                </ul>
            </div>
        </div>
    </div>
    <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>
</div>
</body>
</html>