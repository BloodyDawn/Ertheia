package dwo.gameserver;

import dwo.config.FilePath;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Broadcast;
import dwo.gameserver.util.StringUtil;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Announcements
{
	private static final List<String> _announcements = new ArrayList<>();
	private static Logger _log = LogManager.getLogger(Announcements.class);

	private Announcements()
	{
		loadAnnouncements();
	}

	public static Announcements getInstance()
	{
		return SingletonHolder._instance;
	}

	public void loadAnnouncements()
	{
		_announcements.clear();
		File file = FilePath.ANNOUNCEMENTS;
		if(file.exists())
		{
			readFromDisk(file);
		}
		else
		{
			_log.log(Level.WARN, file.getAbsolutePath() + " doesn't exist");
		}
	}

	public void showAnnouncements(L2PcInstance activeChar)
	{
		for(String _announcement : _announcements)
		{
			Say2 cs = new Say2(0, ChatType.ANNOUNCEMENT, activeChar.getName(), _announcement);
			activeChar.sendPacket(cs);
		}
	}

	public void listAnnouncements(L2PcInstance activeChar)
	{
		String content = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/admin/announce.htm");
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		StringBuilder replyMSG = StringUtil.startAppend(500, "<br>");
		for(int i = 0; i < _announcements.size(); i++)
		{
			StringUtil.append(replyMSG, "<table width=260><tr><td width=220>", _announcements.get(i), "</td><td width=40>" + "<button value=\"Delete\" action=\"bypass -h admin_del_announcement ", String.valueOf(i), "\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
		}
		adminReply.replace("%announces%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	public void addAnnouncement(String text)
	{
		_announcements.add(text);
		saveToDisk();
	}

	public void delAnnouncement(int line)
	{
		_announcements.remove(line);
		saveToDisk();
	}

	private void readFromDisk(File file)
	{
		LineNumberReader lnr = null;
		try
		{
			int i = 0;
			String line = null;
			lnr = new LineNumberReader(new FileReader(file));
			while((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if(st.hasMoreTokens())
				{
					_announcements.add(st.nextToken());
					i++;
				}
			}
		}
		catch(IOException e1)
		{
			_log.log(Level.ERROR, "Error reading announcements: ", e1);
		}
		finally
		{
			try
			{
				lnr.close();
			}
			catch(Exception e2)
			{
				_log.log(Level.ERROR, "Error while colsing announcements file: ", e2);
			}
		}
	}

	private void saveToDisk()
	{
		File file = FilePath.ANNOUNCEMENTS;
		FileWriter save = null;

		try
		{
			save = new FileWriter(file);
			for(String _announcement : _announcements)
			{
				save.write(_announcement + "\r\n");
			}
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "Saving to the announcements file has failed: ", e);
		}
		finally
		{
			try
			{
				save.close();
			}
			catch(Exception e)
			{
				// Ignored
			}
		}
	}

	public void announceToAll(String text)
	{
		Broadcast.announceToOnlinePlayers(text);
	}

	public void announceToAll(SystemMessage sm)
	{
		Broadcast.toAllOnlinePlayers(sm);
	}

	// Method for handling announcements from admin

	public void announceToInstance(SystemMessage sm, int instanceId)
	{
		Broadcast.toPlayersInInstance(sm, instanceId);
	}

	public void handleAnnounce(String command, int lengthToTrim)
	{
		try
		{
			// Announce string to everyone on server
			String text = command.substring(lengthToTrim);
			announceToAll(text);
		}
		catch(StringIndexOutOfBoundsException e)
		{
			// Ignore
		}
	}

	private static class SingletonHolder
	{
		protected static final Announcements _instance = new Announcements();
	}
}