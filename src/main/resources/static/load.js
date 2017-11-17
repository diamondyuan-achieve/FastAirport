window.onload = function () {
    checkConfig();
    getInstances();
};


function checkConfig() {
    var configJson = $.ajax({url: "/api/v1/aliyun/config", async: false}).responseText;
    resultJson = JSON.parse(configJson);
    if (resultJson.data.configStatus !== "ACTIVE") {
        alert("没有配置好");
        self.location = "/config";
    }
}