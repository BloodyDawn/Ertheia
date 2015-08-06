package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExAlterSkillRequest extends L2GameServerPacket
{
	private int _id;
	private int _time;

	public ExAlterSkillRequest(int skillid, int time)
	{
		_id = skillid;
		_time = skillid;
	}

	@Override
	protected void writeImpl()
	{
		//ddd
		writeD(_id);//id скила (вроде как связано с ид сикла)
		writeD(0); // Level
		writeD(_time);//Время показа кнопки в сек
	}
}
