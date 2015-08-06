package dwo.gameserver.network.game.serverpackets;

public class ShortBuffStatusUpdate extends L2GameServerPacket
{
	private int _skillId;
	private int _skillLvl;
	private int _duration;

	public ShortBuffStatusUpdate(int skillId, int skillLvl, int duration)
	{
		_skillId = skillId;
		_skillLvl = skillLvl;
		_duration = duration;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_skillId);
		writeD(_skillLvl);
		writeD(_duration);
	}
}
