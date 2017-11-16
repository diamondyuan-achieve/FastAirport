var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/greetings', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });
    });
}

function aliyunInit() {
    var configJson = $.ajax({url: "/api/v1/aliyun", async: false});
}

function getInstances() {
    var response = JSON.parse($.ajax({url: "/api/v1/aliyun/Instances", async: false}).responseText);

    // if(response.code ==="1000000"){

    // }
    var table = $("#instanceTable");
    table.empty();
    instanceList = response.data.list;
    if (instanceList.length > 0) {

        for (var i in instanceList) {
            instance = instanceList[i];
            table.append("<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td></tr>".format(instance.id, instance.status, instance.ip, instance.regionId));
            console.log(instance);
        }

    }

    //
    // $( "#instanceTable" ),
}

function createInstance() {
    var options = {
        type: 'POST',
        url: "/api/v1/aliyun/Instance"
    };
    $.ajax(options);
}

function removeInstance() {
    var options = {
        type: 'DELETE',
        url: "/api/v1/aliyun/Instance"
    };
    $.ajax(options);
}


function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/app/hello", {}, JSON.stringify({'name': $("#name").val()}));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        console.log(e);
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });
    $("#disconnect").click(function () {
        disconnect();
    });
    $("#send").click(function () {
        sendName();
    });
    $("#aliyunInit").click(function () {
        aliyunInit()
    });
    $("#getInstances").click(function () {
        getInstances()
    });
    $("#createInstance").click(function () {
        createInstance()
    });
    $("#removeInstance").click(function () {
        removeInstance()
    });
});

String.prototype.format = function (args) {
    var result = this;
    if (arguments.length > 0) {
        if (arguments.length == 1 && typeof (args) == "object") {
            for (var key in args) {
                if (args[key] != undefined) {
                    var reg = new RegExp("({" + key + "})", "g");
                    result = result.replace(reg, args[key]);
                }
            }
        }
        else {
            for (var i = 0; i < arguments.length; i++) {
                if (arguments[i] != undefined) {
                    var reg = new RegExp("({[" + i + "]})", "g");
                    result = result.replace(reg, arguments[i]);
                }
            }
        }
    }
    return result;
}