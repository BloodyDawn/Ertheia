package dwo.gameserver.network.game.serverpackets;

public class TutorialShowHtml extends L2GameServerPacket
{
	public static int SERVER_SIDE = 1;
	public static int CLIENT_SIDE = 2;

	private String _html;
	private int _type;

	/**
	 * Показывает страницу туториала
	 * @param type 1 - html с сервера, 2 - html с клиента
	 * @param html html или путь до html клиента
	 */
	public TutorialShowHtml(int type, String html)
	{
		_type = type;
		_html = html;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_type);
		writeS(_html);
	}
}