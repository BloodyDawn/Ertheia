package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.player.ChatType;

public class Snoop extends L2GameServerPacket
{
	private int _convoId;
	private String _name;
	private ChatType _type;
	private String _speaker;
	private String _msg;

	public Snoop(int id, String name, ChatType type, String speaker, String msg)
	{
		_convoId = id;
		_name = name;
		_type = type;
		_speaker = speaker;
		_msg = msg;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_convoId);
		writeS(_name);
		writeD(0x00); //??
		writeD(_type.ordinal());
		writeS(_speaker);
		writeS(_msg);
	}
}