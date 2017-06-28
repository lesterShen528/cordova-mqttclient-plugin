var exec = require("cordova/exec")
exports.connect = function(successCallback,errorCallback, obj) {
    var host = obj.host;
    var port = obj.port ? obj.port : "8883";
    var ka = obj.ka ? obj.ka : 20;
    var cleanSess = obj.cleanSess ? obj.cleanSess : true;
    var connTimeOut = obj.connTimeOut ? obj.connTimeOut : 100;
    var qos = obj.qos ? obj.qos : 1;
    exec(
        successCallback,
        errorCallback,
        'Client',
        'connect',
        [{
            "type":obj.type,
            "host":host,
            "port":port,
            "cid":obj.cid,
            "ka":ka,
            "cleanSess":cleanSess,
            "connTimeOut":connTimeOut,
            "username":obj.username,
            "password":obj.password,
            "topic":obj.topic,
            "qos":qos,
            "key":obj.key,
            "cert":obj.cert,
            "ca":obj.ca
        }]
    )
};
exports.publish = function(successCallback,errorCallback, obj) {
    var qos = obj.qos ? obj.qos : 1;
    var retained = obj.retained ? obj.retained : false;
    exec(
        successCallback,
        errorCallback,
        "Client",
        'publish',
        [{
            "type":obj.type,
            "topic":obj.topic,
            "payLoad":obj.payLoad,
            "qos":qos,
            "retained":retained
        }]
    )
};
exports.receive = function(successCallback, errorCallback, type){
    exec(
        successCallback,
        errorCallback,
        'Client',
        'receive',
        [{
            "type":type
        }]
    )
};
exports.disconnect = function(successCallback, errorCallback, type){
    exec(
        successCallback,
        errorCallback,
        'Client',
        'disconnect',
        [{
            "type":type
        }]
    )
};