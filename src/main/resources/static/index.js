var ifJoin=true
var websocket = null;
function connection() {
    let uid='';
    for (let index = 0; index < 5; index++) {
      uid +=Math.floor(Math.random()*10).toString()
    }
    var num=true
    if ('WebSocket' in window) {
        websocket = new WebSocket("ws://17.19.2.112:8083/WebSocketServer/1111/"+uid);
    } else if ('MozWebSocket' in window) {
        websocket = new MozWebSocket("ws://17.19.2.112:8083/WebSocketServer/1111/"+uid);
    } else {
        _alert("当前浏览器不支持websocket协议,建议使用现代浏览器")
    }
    
    // 报错时的回调函数
    websocket.onerror = function () {
        setMessageInnerHTML("连接失败，刷新重试");
    };
    
    
    //连接成功后的回调函数
    websocket.onopen = function (event) {
        setMessageInnerHTML(getDateString(new Date())+"加入聊天室");
    }
    
    
    //收到服务器数据后的回调函数
    websocket.onmessage = function (event) {
        console.log(event.data);
        if (num) {
            setMessageInnerHTML(event.data);
            num=false
        }else{
            appendText(event.data)
        }
        
    }
    //连接关闭后的回调函数
    websocket.onclose = function () {
    setMessageInnerHTML(getDateString(new Date())+"离开聊天室");
    }
    
    
    //浏览器关闭时关闭通讯
    window.onbeforeunload = function () {
        websocket.close();
    }  
}


//显示添加信息
function setMessageInnerHTML(innerHTML) {
    let txt=$('<p style="text-align: center; color: #777;margin:10px 0;font-size:12px"></p>').text(innerHTML); 
    $("#message").append(txt)
}


//关闭
function closeWebSocket() {
    websocket.close();
    this.ifJoin=false
    $("#message").scrollTop($("#message")[0].scrollHeight)
}

$('#akai').text('阿凯聊天室')
//发送
function send() {

    
    if ($('#text').val().replace(/[\r\n]/g,"")) {
        appendText( $('#text').val(),1)
        websocket.send($('#text').val())
        $('#text').val('')
    
    }
    
   
}

function appendText(text,who=0){
    if (who==1) {
        var div=$("<div class='me'></div>")
        let img=$("<img style='margin-right:10px' src='./img/20160702122710_iRUXT.thumb.700_0.jpg'></img>")
        let txt=$("<p></p>").text(text); 
        div.append(img,txt)
    }else{
        var div=$("<div class='they'></div>")
        let img=$("<img style='margin-left:10px' src='./img/ak"+text.substr(0, 1)+".jpg'></img>")
        let txt=$("<p></p>").text(text); 
	    div.append(txt,img)
    }
	
    $("#message").append(div); 
    $("#message").scrollTop($("#message")[0].scrollHeight)
}

function getDateString(date) {
    var year = date.getFullYear().toString().padStart(4, "0");
    var month = (date.getMonth() + 1).toString().padStart(2, "0");
    var day = date.getDate().toString().padStart(2, "0");
    var hour = date.getHours().toString().padStart(2, "0");
    var minute = date.getMinutes().toString().padStart(2, "0");
    var second = date.getSeconds().toString().padStart(2, "0");

    return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
}
function join() {
    if (!ifJoin) {
    connection()
    this.ifJoin=true
    }else{
        alert('您当前在聊天室内')
    }
}

connection()

$(document).keyup(e=>{
    if (e.keyCode===13) {
        send()
    }
});