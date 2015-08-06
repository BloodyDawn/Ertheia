package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.SendStatus;
import dwo.gameserver.network.game.serverpackets.VersionCheck;
import org.apache.log4j.Level;

public class ProtocolVersion extends L2GameClientPacket
{
	private int _version;
	private byte[] _data;
	private byte[] _check;

	@Override
	protected void readImpl()
	{
		if(_buf.remaining() >= 4)
		{
			_version = readD();
		}
	}

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		// this packet is never encrypted
		if(_version == -2)
		{
			// this is just a ping attempt from the new C2 client
			client.close((L2GameServerPacket) null);
		}
		else if(_version == -3)
		{
			_log.log(Level.INFO, "Emulate Official Server : L2Top Status requested...");
			client.sendPacket(new SendStatus());
			_log.log(Level.INFO, "Emulate Official Server : L2Top Status sended!");
		}
		else if(!Config.PROTOCOL_LIST.contains(_version))
		{
			getClient().setProtocolOk(false);
			getClient().close(new VersionCheck(getClient().enableCrypt(), 0));
		}
		else
		{
			getClient().sendPacket(new VersionCheck(getClient().enableCrypt(), 1));
			getClient().setProtocolOk(true);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 00 ProtocolVersion";
	}
}
