var exec = require("cordova/exec");

module.exports = {
 connect: function(openOrClose){
	 exec(
	 function(message){
		 alert(message);
	 },
	 function(message){
		 alert(message);
	 },
	 "MqttPlugin",
	 openOrClose,
	 []
	 );
 },
 message: function(port,host,username,password) {
	exec(
	 function(message){
		 alert(message);
	 },
	 function(message){
		 alert(message);
	 },
	 "MqttPlugin",
	 "setConfig",
	 [port,host,username,password]
	 );
 }
}