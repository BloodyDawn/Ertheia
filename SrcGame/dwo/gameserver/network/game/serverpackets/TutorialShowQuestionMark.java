package dwo.gameserver.network.game.serverpackets;

public class TutorialShowQuestionMark extends L2GameServerPacket
{
	private int _markId;

	public TutorialShowQuestionMark(int blink)
	{
		_markId = blink;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_markId);
	}
}