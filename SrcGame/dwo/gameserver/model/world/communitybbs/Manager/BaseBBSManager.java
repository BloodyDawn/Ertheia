package dwo.gameserver.model.world.communitybbs.Manager;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.show.ShowBoard;
import javolution.util.FastList;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

public abstract class BaseBBSManager
{
	public static Logger _log = LogManager.getLogger(BuffBBSManager.class);

	public abstract void parsecmd(String command, L2PcInstance activeChar);

	public abstract void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar);

	protected void separateAndSend(String html, L2PcInstance acha)
	{
		if(html == null)
		{
			return;
		}
		if(html.length() < 4096)
		{
			acha.sendPacket(new ShowBoard(html, "101"));
			acha.sendPacket(new ShowBoard(null, "102"));
			acha.sendPacket(new ShowBoard(null, "103"));

		}
		else if(html.length() < 8192)
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 4096), "101"));
			acha.sendPacket(new ShowBoard(html.substring(4096), "102"));
			acha.sendPacket(new ShowBoard(null, "103"));

		}
		else if(html.length() < 16384)
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 4096), "101"));
			acha.sendPacket(new ShowBoard(html.substring(4096, 8192), "102"));
			acha.sendPacket(new ShowBoard(html.substring(8192), "103"));
		}
	}

	/**
	 * @param html
	 */
	protected void send1001(String html, L2PcInstance acha)
	{
		if(html.length() < 8180)
		{
			acha.sendPacket(new ShowBoard(html, "1001"));
		}
	}

	protected void send1002(L2PcInstance acha)
	{
		send1002(acha, " ", " ", "0");
	}

	/**
	 * @param activeChar
	 * @param string
	 * @param string2
	 */
	protected void send1002(L2PcInstance activeChar, String string, String string2, String string3)
	{
		List<String> _arg = new FastList<>();
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add(activeChar.getName());
		_arg.add(Integer.toString(activeChar.getObjectId()));
		_arg.add(activeChar.getAccountName());
		_arg.add("9");
		_arg.add(string2);
		_arg.add(string2);
		_arg.add(string);
		_arg.add(string3);
		_arg.add(string3);
		_arg.add("0");
		_arg.add("0");
		activeChar.sendPacket(new ShowBoard(_arg));
	}
}
