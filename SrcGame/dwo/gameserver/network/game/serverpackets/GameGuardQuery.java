package dwo.gameserver.network.game.serverpackets;

public class GameGuardQuery extends L2GameServerPacket
{

	@Override
	public void runImpl()
	{
		getClient().setGameGuardOk(false);
	}

	@Override
	public void writeImpl()
	{

	}
}
