var stompClient = null;
var instanceId = null;


function pageInit() {
    id = getParameterByName('id');
    if (id === null || id === "") {
        alertAndBackToHome("请输入实例ID")
    }
    instanceId = id;
    $("#instanceId").html("实例id:{0}".format(id))
}

function alertAndBackToHome(message) {
    alert(message);
    self.location = "/";
}

window.onload = function () {
    pageInit();
};

function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
}

function sendName() {
    stompClient.send("/instance/{0}/command".format(instanceId), {}, JSON.stringify({'content': $("#name").val()}));
}

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
    var socket = new SockJS('/aliyun-instance-ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        // stompClient.subscribe('/instance/{0}/message'.format(instanceId), function (greeting) {
        //     showGreeting(JSON.parse(greeting.body).content);
        // });
    });
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
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
});