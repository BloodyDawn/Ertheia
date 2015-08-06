package dwo.xmlrpcserver.model;

/**
 * Message model for seding via XML-RPC.
 *
 * @author Yorie
 * Date: 16.03.12
 */
public class Message
{
	private final MessageType type;
	private final String message;
	private final String data;

	public Message(MessageType type)
	{
		this.type = type;
		message = "";
		data = "";
	}

	public Message(MessageType type, String message)
	{
		this.type = type;
		this.message = message;
		data = "";
	}

	public Message(MessageType type, String message, String data)
	{
		this.type = type;
		this.message = message;
		this.data = data;
	}

	public enum MessageType
	{
		ERROR,
		WARNING,
		NOTICE,
		OK,
		FAILED,
	}
}
