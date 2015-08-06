package dwo.gameserver.handler.bypasses;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

/**
 * Territory status handler.
 *
 * @author ANZO
 * @author Yorie
 */
public class TerritoryStatus extends CommandHandler<String>
{
	@TextCommand
	public boolean territoryStatus(BypassHandlerParams params)
	{
		L2Character target = params.getTarget();
		L2PcInstance activeChar = params.getPlayer();
		if(!(target instanceof L2Npc))
		{
			return false;
		}

		L2Npc npc = (L2Npc) target;
		String content;
		L2Clan clan;

		if(npc.getCastle().getOwnerId() > 0)
		{
			content = HtmCache.getInstance().getHtm(activeChar.getLang(), "default/defaultfeudinfo.htm");
			clan = ClanTable.getInstance().getClan(npc.getCastle().getOwnerId());
			content = content.replace("<?my_owner_name?>", clan.getLeaderName());
			content = content.replace("<?my_pledge_name?>", clan.getName());
			content = content.replace("<?current_tax_rate?>", String.valueOf(npc.getCastle().getTaxPercent()));
		}
		else
		{
			content = HtmCache.getInstance().getHtm(activeChar.getLang(), "default/nofeudinfo.htm");
		}
		content = npc.getCastle().getCastleId() < 7 ? content.replace("<?kingdom_name?>", "<fstring>" + 1001000 + "</fstring>") : content.replace("<?kingdom_name?>", "<fstring>" + 1001100 + "</fstring>");
		content = content.replace("<?feud_name?>", "<fstring>" + 100100 + npc.getCastle().getCastleId() + "</fstring>");
		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setHtml(content);
		activeChar.sendPacket(html);
		return true;
	}
}