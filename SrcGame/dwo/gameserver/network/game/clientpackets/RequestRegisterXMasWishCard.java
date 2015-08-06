package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * User: Bacek
 * Date: 07.02.13
 * Time: 4:45
 */
public class RequestRegisterXMasWishCard extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar != null)
		{

		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:F0 RequestRegisterXMasWishCard";
	}
}
