package dwo.gameserver.network.game.clientpackets;

@Deprecated
public class RequestDominionInfo extends L2GameClientPacket
{
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
		return "[C] D0:58 RequestDominionInfo";
	}
}