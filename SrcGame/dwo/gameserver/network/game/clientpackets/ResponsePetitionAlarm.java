package dwo.gameserver.network.game.clientpackets;

/**
 * User: Bacek
 * Date: 01.02.13
 * Time: 19:28
 */
public class ResponsePetitionAlarm extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// Пусто
	}

	@Override
	protected void runImpl()
	{

	}

	@Override
	public String getType()
	{
		return "[C] D0:ED ResponsePetitionAlarm";
	}
}
