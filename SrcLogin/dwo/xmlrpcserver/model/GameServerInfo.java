package dwo.xmlrpcserver.model;

/**
 * @author Yorie
 *         Date: 29.04.12
 */
public class GameServerInfo
{
	private int id;
	private boolean isAuthed;
	private String name;
	private String ip;
	private int port;

	/**
	 *
	 * @param id Server ID.
	 * @param isAuthed Is authorized by login server.
	 * @param name Server name.
	 * @param ip Server IP.
	 */
	public GameServerInfo(int id, boolean isAuthed, String name, String ip, int port)
	{
		this.id = id;
		this.isAuthed = isAuthed;
		this.name = name;
		this.ip = ip;
		this.port = port;
	}

	public int getId()
	{
		return id;
	}

	public boolean isAuthed()
	{
		return isAuthed;
	}

	public String getName()
	{
		return name;
	}

	public String getIp()
	{
		return ip;
	}
}
