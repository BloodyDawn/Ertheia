package dwo.gameserver.network.game.clientpackets.packet.CuriousHouse;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.scripts.instances.ChaosFestival;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 29.09.12
 * Time: 22:52
 */
public class RequestCuriousHouseHtml extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar != null && ChaosFestival.getInstance().canParticipate(activeChar) && ChaosFestival.getInstance().getStatus() != ChaosFestival.ChaosFestivalStatus.SCHEDULED)
		{
			activeChar.sendPacket(new NpcHtmlMessage(5, HtmCache.getInstance().getHtm(activeChar.getLang(), "default/chaos_festival_invitation.htm")));
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:C9 RequestCuriousHouseHtml";
	}
}
