package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * User: Bacek
 * Date: 07.02.13
 * Time: 4:48
 */
public class RequestCardReward extends L2GameClientPacket
{
	private int _unk;

	@Override
	protected void readImpl()
	{
		_unk = readD();
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
