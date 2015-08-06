package dwo.gameserver.model.world.communitybbs.Manager;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.show.ShowBoard;

public class AdminBBSManager extends BaseBBSManager
{
	private AdminBBSManager()
	{
	}

	public static AdminBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if(!activeChar.isGM())
		{
			return;
		}
		if(command.startsWith("admin_bbs"))
		{
			separateAndSend("<html><body><br><br><center>This Page is only an exemple :)<br><br>command=" + command + "</center></body></html>", activeChar);
		}
		else
		{

			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}

	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}

	private static class SingletonHolder
	{
		protected static final AdminBBSManager _instance = new AdminBBSManager();
	}
}