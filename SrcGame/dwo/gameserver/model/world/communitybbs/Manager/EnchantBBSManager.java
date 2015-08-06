package dwo.gameserver.model.world.communitybbs.Manager;

import dwo.config.mods.ConfigCommunityBoardPVP;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2EtcItem;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.components.SystemMessageId;
import javolution.text.TextBuilder;
import org.apache.log4j.Level;

import java.util.StringTokenizer;

public class EnchantBBSManager extends BaseBBSManager
{
	private static EnchantBBSManager _Instance;

	public static EnchantBBSManager getInstance()
	{
		if(_Instance == null)
		{
			_Instance = new EnchantBBSManager();
		}
		return _Instance;
	}

	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if(command.equals("_bbsenchant"))
		{
			String name = "Нет имени";
			name = ItemTable.getInstance().getTemplate(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ENCHANT_ITEM).getName();
			TextBuilder sb = new TextBuilder();
			sb.append("<table width=350>");
			L2ItemInstance[] arr = activeChar.getInventory().getItems();
			for(L2ItemInstance _item : arr)
			{
				if(_item == null || _item.getItem() instanceof L2EtcItem || !_item.isEquipped() || _item.isHeroItem() || _item.getItem().getCrystalType() == CrystalGrade.NONE || _item.getItemId() >= 7816 && _item.getItemId() <= 7831 || _item.isShadowItem() || _item.isCommonItem() || _item.getEnchantLevel() >= 26)
				{
					continue;
				}
				sb.append(new StringBuilder().append("<tr><td><img src=").append(ItemTable.getInstance().getTemplate(_item.getItemId()).getIcon()).append(" width=32 height=32></td><td>").toString());
				sb.append(new StringBuilder().append("<font color=\"LEVEL\">").append(_item.getItem().getName()).append(' ').append(_item.getEnchantLevel() <= 0 ? "" : new StringBuilder().append("</font><font color=3293F3>Заточено на: +").append(_item.getEnchantLevel()).toString()).append("</font><br1>").toString());

				sb.append(new StringBuilder().append("Заточка за: <font color=\"LEVEL\">").append(name).append("</font>").toString());
				sb.append("<img src=\"l2ui.squaregray\" width=\"170\" height=\"1\">");
				sb.append("</td><td>");
				sb.append(new StringBuilder().append("<button value=\"Заточить\" action=\"bypass -h _bbsenchant;enchlistpage;").append(_item.getObjectId()).append("\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">").toString());
				sb.append("</td></tr>");
				sb.append("<td>");
				sb.append(new StringBuilder().append("<button value=\"Атрибут\" action=\"bypass -h _bbsenchant;enchlistpageAtrChus;").append(_item.getObjectId()).append("\" width=75 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">").toString());
				sb.append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
			String content = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/community_board/36.htm");
			content = content.replaceAll("%enchanter%", sb.toString());
			separateAndSend(content, activeChar);
		}
		if(command.startsWith("_bbsenchant;enchlistpage;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int ItemForEchantObjID = Integer.parseInt(st.nextToken());
			int price = 0;
			String name = "None Name";
			name = ItemTable.getInstance().getTemplate(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ENCHANT_ITEM).getName();
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(ItemForEchantObjID);
			if(EhchantItem.getItem().getCrystalType() == CrystalGrade.D)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 2 : 1;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.C)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 2 : 1;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.B)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 2 : 1;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.A)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 2 : 1;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.S)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 2 : 1;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.S80)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 2 : 1;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.S84)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 2 : 1;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.R)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 2 : 1;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.R95)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 2 : 1;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.R99)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 2 : 1;
			}
			TextBuilder sb = new TextBuilder();
			sb.append("Для заточки выбрана вещь:<br1><table width=400>");
			sb.append(new StringBuilder().append("<tr><td width=32><img src=").append(ItemTable.getInstance().getTemplate(EhchantItem.getItemId()).getIcon()).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td><td width=236><center>").toString());
			sb.append(new StringBuilder().append("<font color=\"LEVEL\">").append(EhchantItem.getItem().getName()).append(' ').append(EhchantItem.getEnchantLevel() <= 0 ? "" : new StringBuilder().append("</font><font color=3293F3>Заточено на: +").append(EhchantItem.getEnchantLevel()).toString()).append("</font><br1>").toString());
			sb.append(new StringBuilder().append("Заточка производится за: <font color=\"LEVEL\">").append(name).append("</font>").toString());
			sb.append("<img src=\"l2ui.squaregray\" width=\"236\" height=\"1\"><center></td>");
			sb.append(new StringBuilder().append("<td width=32><img src=").append(ItemTable.getInstance().getTemplate(EhchantItem.getItemId()).getIcon()).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td>").toString());
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<br1>");
			sb.append("<br1>");
			sb.append("<table border=0 width=400><tr><td width=200>");
			sb.append("<button value=\"На +5 (Цена:").append(String.valueOf(price * (price + 1))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;5;").append(String.valueOf(price * (price + 1))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +6 (Цена:").append(String.valueOf(price * (price + 2))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;6;").append(String.valueOf(price * (price + 2))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +7 (Цена:").append(String.valueOf(price * (price + 3))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;7;").append(String.valueOf(price * (price + 3))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +8 (Цена:").append(String.valueOf(price * (price + 4))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;8;").append(String.valueOf(price * (price + 4))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +9 (Цена:").append(String.valueOf(price * (price + 5))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;9;").append(String.valueOf(price * (price + 5))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +10 (Цена:").append(String.valueOf(price * (price + 6))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;10;").append(String.valueOf(price * (price + 6))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +11 (Цена:").append(String.valueOf(price * (price + 7))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;11;").append(String.valueOf(price * (price + 7))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +12 (Цена:").append(String.valueOf(price * (price + 8))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;12;").append(String.valueOf(price * (price + 8))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +13 (Цена:").append(String.valueOf(price * (price + 9))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;13;").append(String.valueOf(price * (price + 9))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +14 (Цена:").append(String.valueOf(price * (price + 10))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;14;").append(String.valueOf(price * (price + 10))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +15 (Цена:").append(String.valueOf(price * (price + 11))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;15;").append(String.valueOf(price * (price + 11))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("</td><td width=200>");
			sb.append("<button value=\"На +16 (Цена:").append(String.valueOf(price * (price + 12))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;16;").append(String.valueOf(price * (price + 12))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +17 (Цена:").append(String.valueOf(price * (price + 13))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;17;").append(String.valueOf(price * (price + 13))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +18 (Цена:").append(String.valueOf(price * (price + 14))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;18;").append(String.valueOf(price * (price + 14))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +19 (Цена:").append(String.valueOf(price * (price + 15))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;19;").append(String.valueOf(price * (price + 15))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +20 (Цена:").append(String.valueOf(price * (price + 16))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;20;").append(String.valueOf(price * (price + 16))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +21 (Цена:").append(String.valueOf(price * (price + 17))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;21;").append(String.valueOf(price * (price + 17))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +22 (Цена:").append(String.valueOf(price * (price + 18))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;22;").append(String.valueOf(price * (price + 18))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +23 (Цена:").append(String.valueOf(price * (price + 19))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;23;").append(String.valueOf(price * (price + 19))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +24 (Цена:").append(String.valueOf(price * (price + 20))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;24;").append(String.valueOf(price * (price + 20))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +25 (Цена:").append(String.valueOf(price * (price + 21))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;25;").append(String.valueOf(price * (price + 21))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +26 (Цена:").append(String.valueOf(price * (price + 22))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgo;26;").append(String.valueOf(price * (price + 22))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("</td></tr></table><br1><button value=\"Назад\" action=\"bypass -h _bbsenchant\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			String content = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/community_board/36.htm");
			content = content.replace("%enchanter%", sb.toString());
			separateAndSend(content, activeChar);
		}
		if(command.equals("_bbsenchant;enchlistpageAtrChus;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int ItemForEchantObjID = Integer.parseInt(st.nextToken());
			String name = "Нет имени";
			name = ItemTable.getInstance().getTemplate(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ENCHANT_ITEM).getName();
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(ItemForEchantObjID);

			TextBuilder sb = new TextBuilder();
			sb.append("Для заточки на атрибут выбрана вещь:<br1><table width=300>");
			sb.append(new StringBuilder().append("<tr><td width=32><img src=").append(ItemTable.getInstance().getTemplate(EhchantItem.getItemId()).getIcon()).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td><td width=236><center>").toString());
			sb.append(new StringBuilder().append("<font color=\"LEVEL\">").append(EhchantItem.getItem().getName()).append(' ').append(EhchantItem.getEnchantLevel() <= 0 ? "" : new StringBuilder().append("</font><br1><font color=3293F3>Заточено на: +").append(EhchantItem.getEnchantLevel()).toString()).append("</font><br1>").toString());

			sb.append(new StringBuilder().append("Заточка производится за: <font color=\"LEVEL\">").append(name).append("</font>").toString());
			sb.append("<img src=\"l2ui.squaregray\" width=\"236\" height=\"1\"><center></td>");
			sb.append(new StringBuilder().append("<td width=32><img src=").append(ItemTable.getInstance().getTemplate(EhchantItem.getItemId()).getIcon()).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td>").toString());
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<br1>");
			sb.append("<br1>");
			sb.append("<table border=0 width=400><tr><td width=200>");
			sb.append("<center><img src=icon.etc_wind_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=\"Ветер \" action=\"bypass -h _bbsenchant;enchlistpageAtr;2;").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1><center><img src=icon.etc_earth_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=\"Земля \" action=\"bypass -h _bbsenchant;enchlistpageAtr;3;").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1><center><img src=icon.etc_fire_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=\"Огонь \" action=\"bypass -h _bbsenchant;enchlistpageAtr;0;").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("</td><td width=200>");
			sb.append("<center><img src=icon.etc_water_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=\"Вода \" action=\"bypass -h _bbsenchant;enchlistpageAtr;1;").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1><center><img src=icon.etc_holy_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=\"Свет \" action=\"bypass -h _bbsenchant;enchlistpageAtr;4;").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1><center><img src=icon.etc_unholy_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=\"Тьма \" action=\"bypass -h _bbsenchant;enchlistpageAtr;5;").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("</td></tr></table><br1><button value=\"Назад\" action=\"bypass -h _bbsenchant\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			String content = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/community_board/36.htm");
			content = content.replace("%enchanter%", sb.toString());
			separateAndSend(content, activeChar);
		}
		if(command.startsWith("_bbsenchant;enchlistpageAtr;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			int AtributType = Integer.parseInt(st.nextToken());
			int ItemForEchantObjID = Integer.parseInt(st.nextToken());
			int price = 0;
			String ElementName = "";
			if(AtributType == 0)
			{
				ElementName = "Огонь";
			}
			else if(AtributType == 1)
			{
				ElementName = "Вода";
			}
			else if(AtributType == 2)
			{
				ElementName = "Ветер";
			}
			else if(AtributType == 3)
			{
				ElementName = "Земля";
			}
			else if(AtributType == 4)
			{
				ElementName = "Свет";
			}
			else if(AtributType == 5)
			{
				ElementName = "Тьма";
			}
			String name = "Нет имени";
			name = ItemTable.getInstance().getTemplate(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ENCHANT_ITEM).getName();
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(ItemForEchantObjID);
			if(EhchantItem.getItem().getCrystalType() == CrystalGrade.S)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 10 : 8;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.S80)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 12 : 10;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.S84)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 14 : 12;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.R)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 16 : 14;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.R95)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 18 : 16;
			}
			else if(EhchantItem.getItem().getCrystalType() == CrystalGrade.R99)
			{
				price = EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON ? 20 : 18;
			}
			TextBuilder sb = new TextBuilder();
			sb.append("Выбран элемент: <font color=\"LEVEL\">").append(ElementName).append("</font><br1> Для заточки выбрана вещь:<br1><table width=300>");
			sb.append(new StringBuilder().append("<tr><td width=32><img src=").append(ItemTable.getInstance().getTemplate(EhchantItem.getItemId()).getIcon()).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td><td width=236><center>").toString());
			sb.append(new StringBuilder().append("<font color=\"LEVEL\">").append(EhchantItem.getItem().getName()).append(' ').append(EhchantItem.getEnchantLevel() <= 0 ? "" : new StringBuilder().append("</font><br1><font color=3293F3>Заточено на: +").append(EhchantItem.getEnchantLevel()).toString()).append("</font><br1>").toString());

			sb.append(new StringBuilder().append("Заточка производится за: <font color=\"LEVEL\">").append(name).append("</font>").toString());
			sb.append("<img src=\"l2ui.squaregray\" width=\"236\" height=\"1\"><center></td>");
			sb.append(new StringBuilder().append("<td width=32><img src=").append(ItemTable.getInstance().getTemplate(EhchantItem.getItemId()).getIcon()).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td>").toString());
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<br1>");
			sb.append("<br1>");
			if(EhchantItem.getItem().getCrystalType() == CrystalGrade.S || EhchantItem.getItem().getCrystalType() == CrystalGrade.S80 || EhchantItem.getItem().getCrystalType() == CrystalGrade.S84)
			{
				sb.append("<table border=0 width=400><tr><td width=200>");
				sb.append("<button value=\"На +25 (Цена:").append(String.valueOf(price * (price + 68))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgoAtr;25;").append(String.valueOf(AtributType)).append(";").append(String.valueOf(price * (price + 68))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
				sb.append("<br1>");
				sb.append("<button value=\"На +50 (Цена:").append(String.valueOf(price * (price + 88))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgoAtr;50;").append(String.valueOf(AtributType)).append(";").append(String.valueOf(price * (price + 88))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
				sb.append("<br1>");
				sb.append("<button value=\"На +75 (Цена:").append(String.valueOf(price * (price + 108))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgoAtr;75;").append(String.valueOf(AtributType)).append(";").append(String.valueOf(price * (price + 108))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
				sb.append("</td><td width=200>");
				sb.append("<button value=\"На +100 (Цена:").append(String.valueOf(price * (price + 128))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgoAtr;100;").append(String.valueOf(AtributType)).append(";").append(String.valueOf(price * (price + 128))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
				sb.append("<br1>");
				sb.append("<button value=\"На +125 (Цена:").append(String.valueOf(price * (price + 148))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgoAtr;125;").append(String.valueOf(AtributType)).append(";").append(String.valueOf(price * (price + 148))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
				sb.append("<br1>");
				sb.append("<button value=\"На +150 (Цена:").append(String.valueOf(price * (price + 168))).append(" ").append(name).append(")\" action=\"bypass -h _bbsenchant;enchantgoAtr;150;").append(String.valueOf(AtributType)).append(";").append(String.valueOf(price * (price + 168))).append(";").append(String.valueOf(ItemForEchantObjID)).append("\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
				sb.append("</td></tr></table><br1>");
			}
			else
			{
				sb.append("<table border=0 width=400><tr><td width=200>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<center><font color=\"LEVEL\">Заточка данной вещи не возможна!</font></center>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("</td></tr></table><br1>");
			}
			sb.append("<button value=\"Назад\" action=\"bypass -h _bbsenchant\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			String content = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/community_board/36.htm");
			content = content.replace("%enchanter%", sb.toString());
			separateAndSend(content, activeChar);
		}
		if(command.startsWith("_bbsenchant;enchantgo;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int EchantVal = Integer.parseInt(st.nextToken());
			int EchantPrice = Integer.parseInt(st.nextToken());
			int EchantObjID = Integer.parseInt(st.nextToken());
			L2Item item = ItemTable.getInstance().getTemplate(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ENCHANT_ITEM);
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(EchantObjID);
			_log.log(Level.WARN, "WMZSELLER: Item: " + EhchantItem + " Val: " + EchantVal + " Price: " + EchantPrice + " Player: " + activeChar.getName());

			if(activeChar.isProcessingTransaction() || activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE || activeChar.getActiveTradeList() != null)
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE);
				return;
			}

			if(pay != null && pay.getCount() >= EchantPrice)
			{
				activeChar.destroyItem(ProcessType.ENCHANT, pay, EchantPrice, activeChar, true);
				EhchantItem.setEnchantLevel(EchantVal);
				activeChar.getInventory().equipItem(EhchantItem);
				activeChar.broadcastUserInfo();
				activeChar.sendMessage("Поздравляем! Предмет " + EhchantItem.getItem().getName() + " был заточен до " + EchantVal + '.');
				parsecmd("_bbsenchant", activeChar);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
			}
		}
		if(command.startsWith("_bbsenchant;enchantgoAtr;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			int EchantVal = Integer.parseInt(st.nextToken());
			int AtrType = Integer.parseInt(st.nextToken());
			int EchantPrice = Integer.parseInt(st.nextToken());
			int EchantObjID = Integer.parseInt(st.nextToken());
			L2Item item = ItemTable.getInstance().getTemplate(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ENCHANT_ITEM);
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(EchantObjID);

			if(activeChar.isProcessingTransaction() || activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE || activeChar.getActiveTradeList() != null)
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE);
				return;
			}
			if(pay != null && pay.getCount() >= EchantPrice)
			{
				activeChar.destroyItem(ProcessType.ENCHANT, pay, EchantPrice, activeChar, true);
				EhchantItem.setElementAttr((byte) AtrType, EchantVal);
				activeChar.broadcastUserInfo();
				activeChar.sendMessage("Поздравляем! Предмет " + EhchantItem.getItem().getName() + " был заточен до " + EchantVal + '.');
				parsecmd("_bbsenchant", activeChar);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
			}
		}
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{

	}
}