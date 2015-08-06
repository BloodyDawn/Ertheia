package dwo.gameserver.network.game.clientpackets;

/**
 * L2GOD Team
 * User: keiichi
 * Date: 10.03.13
 * Time: 15:44
 * Пакет шлется при выходе из пати!
 */
public class RequestDismissMpccRoom extends L2GameClientPacket
{
	// Пакет тригер?
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
	}

	@Override
	public String getType()
	{
		return "[C] D0:5E RequestDismissMpccRoom";
	}
}
