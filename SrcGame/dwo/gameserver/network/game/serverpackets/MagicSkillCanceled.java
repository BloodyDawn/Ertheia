package dwo.gameserver.network.game.serverpackets;

public class MagicSkillCanceled extends L2GameServerPacket
{
	private int _objectId;

	public MagicSkillCanceled(int objectId)
	{
		_objectId = objectId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
	}
}
