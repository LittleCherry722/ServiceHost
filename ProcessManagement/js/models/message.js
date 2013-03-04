define([
	"model"
], function( Model ) {

	Message = Model( "Message" );

	Message.attrs({
		title: "string",
		content: "string",
		fromUserId: 'integer',
		toUserId: 'integer'
	});

	Message.belongsTo( "fromUser", { modelName: "User" } );
	Message.belongsTo( "toUser",   { modelName: "User" } );


	Message.fetch = function() {};

	return Message;
});
