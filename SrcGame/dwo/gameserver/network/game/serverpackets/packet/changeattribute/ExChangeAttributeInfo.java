package dwo.gameserver.network.game.serverpackets.packet.changeattribute;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 08.10.11
 * Time: 15:45
 */

public class ExChangeAttributeInfo extends L2GameServerPacket
{
	private int _attribute = -1;
	private int _ObjectIdStone;

	public ExChangeAttributeInfo(int att, int ObjectIdStone)
	{
		switch(att)
		{
			case 0:
				_attribute = -2;
				break;
			case 1:
				_attribute = -3;
				break;
			case 2:
				_attribute = -5;
				break;
			case 3:
				_attribute = -9;
				break;
			case 4:
				_attribute = -17;
				break;
			case 5:
				_attribute = -33;
				break;
		}
		_ObjectIdStone = ObjectIdStone;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_ObjectIdStone);
		writeD(_attribute);  //  -2 огонь -3 вода  -5 ветер -9 земля -17 святость -33 тьма
	}
}
