package dwo.gameserver.network.game.serverpackets;

public class SocialAction extends L2GameServerPacket
{
	public static final int LEVEL_UP = 2122;

	private int _charObjId;
	private int _actionId;
	private int _unk;

	public SocialAction(int objectId, int actionId)
	{
		_charObjId = objectId;
		_actionId = actionId;
	}

	public SocialAction(int objectId, int actionType, int unk)
	{
		_charObjId = objectId;
		_actionId = actionType;
		_unk = unk;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_actionId);
		writeD(_unk); //TODO: God Нужно разобрать!
	}
}
