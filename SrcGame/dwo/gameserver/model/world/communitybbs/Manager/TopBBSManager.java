package dwo.gameserver.model.world.communitybbs.Manager;

import dwo.config.Config;
import dwo.config.mods.ConfigCommunityBoardPVP;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.network.game.serverpackets.packet.show.ShowBoard;

public class TopBBSManager extends BaseBBSManager
{
	public String функция_отключена = "<br><br><center>Функция выключена Администратором!</center>";

	private TopBBSManager()
	{
	}

	public static TopBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if(command.startsWith("_bbstop") || command.startsWith("_bbshome"))
		{
			String idp = "news";
			if(command.contains(";"))
			{
				idp = command.split(";")[1];
			}
			generateHtmlPage(idp, activeChar);
		}
		else if(command.startsWith("_bbsAugment;add") || command.startsWith("_bbsAugment;remove"))
		{
			separateAndSend(HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/community_board/" + Config.CUSTOM_DATA_DIRECTORY + "/7.htm"), activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>Команда: " + command + " не реализована или не существует.</center></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}

	public void generateHtmlPage(String path, L2PcInstance activeChar)
	{
		String var = null;
		if(path.contains(":"))
		{
			var = path.split(":")[1];
		}
		String index = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/community_board/" + Config.CUSTOM_DATA_DIRECTORY + "/pvp/index.htm");
		String menu = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/community_board/" + Config.CUSTOM_DATA_DIRECTORY + "/pvp/menu.htm");
		if(index != null && menu != null)
		{
			index = index.replace("%menu%", menu);
			String content = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/community_board/" + Config.CUSTOM_DATA_DIRECTORY + "/pvp/" + path.split(":")[0] + ".htm");
			if(content == null)
			{
				content = "<br><br><center>Страница " + path.split(":")[0] + " не найдена. </center>";
			}
			else
			{
				if(path.equals("info"))
				{
					content = content.replace("%XP%", String.valueOf(Config.RATE_XP));
					content = content.replace("%SP%", String.valueOf(Config.RATE_SP));
					content = content.replace("%PATYXP%", String.valueOf(Config.RATE_XP * Config.RATE_PARTY_XP));
					content = content.replace("%PATYSP%", String.valueOf(Config.RATE_SP * Config.RATE_PARTY_SP));
					content = content.replace("%DROP%", String.valueOf(Config.RATE_DROP_ITEMS));
					content = content.replace("%SPOIL%", String.valueOf(Config.RATE_DROP_SPOIL));
					content = content.replace("%ADENA%", String.valueOf(Config.RATE_DROP_ITEMS_ID.get(PcInventory.ADENA_ID)));
					content = content.replace("%RAIDDROP%", String.valueOf(Config.RATE_DROP_ITEMS_BY_RAID));
					content = content.replace("%RATEQUESTDROP%", String.valueOf(Config.RATE_QUEST_DROP));
					content = content.replace("%QUESTREWARDXP%", String.valueOf(Config.RATE_QUEST_REWARD_XP));
					content = content.replace("%RATEQUESTREWARDSP%", String.valueOf(Config.RATE_QUEST_REWARD_SP));
					content = content.replace("%RATEQUESTREWARDADENA%", String.valueOf(Config.RATE_QUEST_REWARD_ADENA));
					content = content.replace("%RATE_CAMPAINS%", String.valueOf(Config.RATE_CAMPAINS));
					content = content.replace("%WEAPONBLESSEDENCHANTBONUS%", String.valueOf(Config.WEAPON_BLESSED_ENCHANT_BONUS));
					content = content.replace("%ARMORBLESSEDENCHANTBONUS%", String.valueOf(Config.ARMOR_BLESSED_ENCHANT_BONUS));
					content = content.replace("%ENCHANT_CHANCE%", String.valueOf(Config.ENCHANT_CHANCE));
					content = content.replace("%ENCHANT_SAFE_MAX%", String.valueOf(Config.ENCHANT_SAFE_MAX));
				}
				else if(path.startsWith("bashorg"))
				{
					if(ConfigCommunityBoardPVP.COMMUNITY_BOARD_RSS_SYSTEM_ENABLE || activeChar.isGM())
					{
						content = RssBBSManager.getInstance().showQuote(var, content);
						if(content == null)
						{
							return;
						}
					}
					else
					{
						content = функция_отключена;
					}
				}
				else if(path.startsWith("classmaster"))
				{
					if(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS || activeChar.isGM())
					{
						content = ClassBBSManager.getInstance().parsecmd(var, activeChar, content);
						if(content == null)
						{
							return;
						}
					}
					else
					{
						content = функция_отключена;
					}
				}
				else if(path.startsWith("buff"))
				{
					if(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_BUFF || activeChar.isGM())
					{
						content = BuffBBSManager.getInstance().parsecmd(var, activeChar, content);
						if(content == null)
						{
							return;
						}
					}
					else
					{
						content = функция_отключена;
					}
				}
				else if(path.startsWith("teleport"))
				{
					if(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_TELEPORT || activeChar.isGM())
					{
						content = TeleportBBSManager.getInstance().parsecmd(var, activeChar, content);
						if(content == null)
						{
							return;
						}
					}
					else
					{
						content = функция_отключена;
					}
				}
				else if(path.startsWith("event"))
				{
					content = EventBBSManager.getInstance().parsecmd(var, activeChar, content);
					if(content == null)
					{
						return;
					}
				}
				else if(path.startsWith("services"))
				{
					content = ServiceBBSManager.getInstance().parsecmd(var, activeChar, content);
					if(content == null)
					{
						return;
					}
				}
			}
			index = index.replace("%page%", content);
		}
		else
		{
			index = "<html><body><br><br><center>404 :Файл не найден: 'index / menu  </center></body></html>";
		}
		separateAndSend(index, activeChar);
	}

	public String getHtml(L2PcInstance activeChar, String html)
	{
		return HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/community_board/" + Config.CUSTOM_DATA_DIRECTORY + "/pvp/" + html);
	}

	private static class SingletonHolder
	{
		protected static final TopBBSManager _instance = new TopBBSManager();
	}
}