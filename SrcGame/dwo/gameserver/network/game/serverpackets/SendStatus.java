package dwo.gameserver.network.game.serverpackets;

import dwo.config.Config;
import dwo.gameserver.instancemanager.WorldManager;

public class SendStatus extends L2GameServerPacket
{
	private int max_online = Config.MAXIMUM_ONLINE_USERS;

	@Override
	protected void writeImpl()
	{
		int online_players = WorldManager.getInstance().getAllPlayersCount();

		writeC(0x2E); // Packet ID
		writeD(0x01); // World ID
		writeD(max_online); // Max Online
		writeD(online_players); // Current Online
		writeD(online_players + 2); // Current Online
		writeD(884); // Priv.Store Chars

		// SEND TRASH
		writeH(0x30);
		writeH(0x2C);
		writeH(0x35);
		writeH(0x31);
		writeH(0x30);
		writeH(0x2C);
		writeH(0x37);
		writeH(0x37);
		writeH(0x37);
		writeH(0x35);
		writeH(0x38);
		writeH(0x2C);
		writeH(0x36);
		writeH(0x35);
		writeH(0x30);
		writeD(0x36);
		writeD(0x77);
		writeD(0xB7);
		writeQ(0x9F);
		writeD(0);
		writeH(0x41);
		writeH(0x75);
		writeH(0x67);
		writeH(0x20);
		writeH(0x32);
		writeH(0x39);
		writeH(0x20);
		writeH(0x32);
		writeH(0x30);
		writeH(0x30);
		writeD(0x39);
		writeH(0x30);
		writeH(0x32);
		writeH(0x3A);
		writeH(0x34);
		writeH(0x30);
		writeH(0x3A);
		writeH(0x34);
		writeD(0x33);
		writeD(0x57);
		writeC(0x11);
		writeC(0x5D);
		writeC(0x1F);
		writeC(0x60);
	}

	@Override
	public boolean isWriteOpCode()
	{
		return false;
	}
}