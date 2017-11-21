window.onload = function () {
    setConfigMap();
};


function setConfigMap() {
    var configJson = $.ajax({url: "/api/v1/aliyun/config", async: false}).responseText;
    resultJson = JSON.parse(configJson);
    configDetail = resultJson.data;
    table = $("#configTable");
    table.empty();
    for (var key in configDetail) {
        val = configDetail[key];
        if (key === "configStatus") {
            continue;
        }
        table.append(getLine(key, val));
    }
}


function getLine(key, value) {
    status = "success";
    if (value === null) {
        status = "danger";
        value = "暂无";
    }
    return "<tr class=\"{0}\"><td>{1}</td><td>{2}</td></tr>".format(status, key, value);

}


function aliyunInit() {
    var configJson = $.ajax({url: "/api/v1/aliyun", async: false});
}

$(function () {
    $("#aliyunInit").click(function () {
        aliyunInit()
    })
});