package dwo.gameserver.network.game.clientpackets;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 28.05.2011
 * Time: 14:27:57
 */

public class RequestCrystallizeItemCancel extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		//??
	}

	@Override
	public String getType()
	{
		return "[C] D0:92 RequestCrystallizeItemCancel";
	}
}
