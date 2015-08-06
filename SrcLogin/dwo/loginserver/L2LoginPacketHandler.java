package dwo.loginserver;

import dwo.config.Config;
import dwo.loginserver.L2LoginClient.LoginClientState;
import dwo.loginserver.network.loginclientcon.clientpackets.AuthGameGuard;
import dwo.loginserver.network.loginclientcon.clientpackets.RequestAuthLogin;
import dwo.loginserver.network.loginclientcon.clientpackets.RequestLogin;
import dwo.loginserver.network.loginclientcon.clientpackets.RequestServerList;
import dwo.loginserver.network.loginclientcon.clientpackets.RequestServerLogin;
import dwo.util.mmocore.IPacketHandler;
import dwo.util.mmocore.ReceivablePacket;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;

/**
 * Handler for packets received by Login Server
 *
 * @author KenM
 */
public class L2LoginPacketHandler implements IPacketHandler<L2LoginClient>
{
	static final Logger _log = LogManager.getLogger(L2LoginPacketHandler.class);

	public static String getHexString(byte[] b)
	{
		String result = "";
		for(byte aB : b)
		{
			result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	@Override
	public ReceivablePacket<L2LoginClient> handlePacket(ByteBuffer buf, L2LoginClient client)
	{
		int opcode = buf.get() & 0xFF;

		ReceivablePacket<L2LoginClient> packet = null;
		LoginClientState state = client.getState();

		switch(state)
		{
			case CONNECTED:
				if(opcode == 0x07)
				{
					packet = new AuthGameGuard();
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
			case AUTHED_GG:
				if(opcode == 0x00)
				{
					packet = Config.USE_ONLY_CMD_AUTH ? new RequestLogin() : new RequestAuthLogin();
				}
				else if(opcode == 0x0b)
				{
					packet = new RequestLogin();
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
			case AUTHED_LOGIN:
				if(opcode == 0x05)
				{
					packet = new RequestServerList();
				}
				else if(opcode == 0x02)
				{
					packet = new RequestServerLogin();
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
		}
		return packet;
	}

	private void debugOpcode(int opcode, LoginClientState state)
	{
		_log.log(Level.WARN, "Unknown Opcode: " + opcode + " for state: " + state.name());
	}
}
