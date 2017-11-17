function getInstances() {
    var response = JSON.parse($.ajax({url: "/api/v1/aliyun/Instances", async: false}).responseText);
    var table = $("#instanceTable");
    table.empty();
    instanceList = response.data.list;
    if (instanceList.length > 0) {
        for (var i in instanceList) {
            instance = instanceList[i];
            table.append("<tr>");
            table.append("<td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td><td>{4}</td>".format(instance.id, instance.status, instance.ip, instance.regionId, instance.keyPairName));
            table.append("<td><button class='btn btn-default'><a href=\"instance?id={0}\">操作</a></button></td>".format(instance.id));
            table.append("<tr>");
        }
    }
}

function createInstance() {
    var options = {
        type: 'POST',
        url: "/api/v1/aliyun/Instance"
    };
    $.ajax(options);
    getInstances()
}

function removeInstance() {
    var options = {
        type: 'DELETE',
        url: "/api/v1/aliyun/Instance"
    };
    $.ajax(options);
    getInstances()
}






$(function () {
    $("form").on('submit', function (e) {
        console.log(e);
        e.preventDefault();
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
