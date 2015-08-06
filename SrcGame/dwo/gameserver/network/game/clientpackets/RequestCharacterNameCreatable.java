package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExIsCharNameCreatable;
import dwo.gameserver.util.Util;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 09.09.2011
 * Time: 2:32:49
 */

public class RequestCharacterNameCreatable extends L2GameClientPacket
{
	private String _nickname;

	@Override
	protected void readImpl()
	{
		_nickname = readS();
	}

	@Override
	protected void runImpl()
	{
		// TODO: Добавить остальные запрещалки (5,6,7)
		if(CharNameTable.getInstance().accountCharNumber(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT && Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0)
		{
			sendPacket(new ExIsCharNameCreatable(0x01));
		}
		else if(CharNameTable.getInstance().doesCharNameExist(_nickname))
		{
			sendPacket(new ExIsCharNameCreatable(0x02));
		}
		else if(_nickname.length() < 1 || _nickname.length() > 16)
		{
			sendPacket(new ExIsCharNameCreatable(0x03));
		}
		else if(!Util.isAlphaNumeric(_nickname) || !Util.isValidName(_nickname))
		{
			sendPacket(new ExIsCharNameCreatable(0x04));
		}
		else
		{
			sendPacket(new ExIsCharNameCreatable(0xFFFFFFFF));
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:B0 RequestCharacterNameCreatable";
	}
}