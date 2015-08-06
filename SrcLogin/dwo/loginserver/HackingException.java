package dwo.loginserver;

public class HackingException extends Exception
{
	/**
	 * Comment for {@code serialVersionUID}
	 */
	private static final long serialVersionUID = 4050762693478463029L;
	final String _ip;
	private final int _connects;

	public HackingException(String ip, int connects)
	{
		_ip = ip;
		_connects = connects;
	}

	public String getIP()
	{
		return _ip;
	}

	public int getConnects()
	{
		return _connects;
	}
}
