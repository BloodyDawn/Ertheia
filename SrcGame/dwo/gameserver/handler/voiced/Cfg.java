package dwo.gameserver.handler.voiced;

import dwo.config.Config;
import dwo.config.main.ConfigLocalization;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

import java.util.Arrays;
import java.util.List;

/**
 * Player configuration commands handler.
 * Configuration is basically HTML window when user can set up its own options, such as loot/xp/sp options.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class Cfg extends CommandHandler<String>
{
	private static final List<String> allowedVariables = Arrays.asList("useAutoLoot@", "useAutoLootHerbs@", "ableToGainExp@", "lang@", "useTitlePvpMode@");
	private static final String[] onOffText = {"Вкл", "Выкл"};

	@TextCommand
	public boolean cfg(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();
		List<String> adParams = params.getArgs();

		if(adParams.isEmpty())
		{
			showConfigMenu(params.getPlayer());
		}
		else if(adParams.size() == 3 && adParams.get(0).equalsIgnoreCase("set_var") && allowedVariables.contains(adParams.get(1)))
		{
			activeChar.getVariablesController().set(adParams.get(1), adParams.get(2));
			showConfigMenu(activeChar);
		}

		return true;
	}

	private void showConfigMenu(L2PcInstance activeChar)
	{
		StringBuilder html = new StringBuilder();
		html.append("<html><title>Настройки</title><body><br><table width=270>");
		if(ConfigLocalization.MULTILANG_ENABLE)
		{
			html.append("<tr><td width=130>Язык</td><td width=40 align=center><font color=\"LEVEL\">").append(activeChar.getLang().toUpperCase()).append("</font></td>");
			int count1 = ConfigLocalization.MULTILANG_ALLOWED.length / 2;
			int i = 0;
			html.append("<td width=50 align=center>");
			for(String lng : ConfigLocalization.MULTILANG_ALLOWED)
			{
				i++;
				html.append("<button width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_cfg set_var lang@ ").append(lng).append("\" value=\"").append(lng.toUpperCase()).append("\">");
				if(i == count1)
				{
					html.append("</td><td width=50 align=center>");
				}
			}
			html.append("</td></tr>");
		}
		html.append(getBooleanFrame("Автолут", "useAutoLoot@", activeChar.getVariablesController().get("useAutoLoot@", Boolean.class, false), Config.ALLOW_AUTOLOOT_COMMAND, onOffText));
		html.append(getBooleanFrame("Автолут Настоек", "useAutoLootHerbs@", activeChar.getVariablesController().get("useAutoLootHerbs@", Boolean.class, false), Config.ALLOW_AUTOLOOT_COMMAND, onOffText));
		html.append(getBooleanFrame("Блокировка опыта", "ableToGainExp@", activeChar.getVariablesController().get("ableToGainExp@", Boolean.class, false), true, onOffText));
        html.append(getBooleanFrame("ПВП-Титул", "useTitlePvpMode@", activeChar.getVariablesController().get("useTitlePvpMode@", Boolean.class, false), Config.TITLE_PVP_MODE, onOffText));

        html.append("</table></body></html>");
        activeChar.broadcastUserInfo();
        activeChar.broadcastTitleInfo();
		activeChar.sendPacket(new NpcHtmlMessage(6, html.toString()));
	}

	private String getBooleanFrame(String configTitle, String configName, boolean isON, boolean allowtomod, String[] str)
	{
		StringBuilder html = new StringBuilder();
		html.append("<tr>");
		html.append("<td width=130>").append(configTitle).append(":</td>");
		html.append("<td width=40 align=center><font color=\"").append(isON ? "00FF00" : "FF0000").append("\">").append(isON ? str[0] : str[1]).append("</font></td>");
		if(allowtomod)
		{
			html.append("<td width=50 align=center><button width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_cfg set_var ").append(configName).append(" true\" value=\"").append(str[0]).append("\"></td>");
			html.append("<td width=50 align=center><button width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_cfg set_var ").append(configName).append(" false\" value=\"").append(str[1]).append("\"></td>");
		}
		else
		{
			html.append("<td width=50  align=center>" + "Н/Д" + "</td>");
			html.append("<td width=50></td>");
		}
		html.append("</tr>");
		return html.toString();
	}
}