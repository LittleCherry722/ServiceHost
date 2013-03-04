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

	Message.belongsTo( "fromUser", { modelName: "user" } );
	Message.belongsTo( "toUser",   { modelName: "user" } );


	Message.fetch = function( callback ) { if ( callback ) { callback() } };

	return Message;
});
