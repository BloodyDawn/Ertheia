package dwo.scripts.npc.town;

import dwo.gameserver.instancemanager.vehicle.AirShipManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.09.12
 * Time: 00:16
 */

public class EngineerLekon extends Quest
{
	private static final int LEKON = 32557;

	private static final int LICENSE = 13559;
	private static final int STARSTONE = 13277;
	private static final int LICENSE_COST = 10;

	public EngineerLekon()
	{
		addAskId(LEKON, -1724);
	}

	public static void main(String[] args)
	{
		new EngineerLekon();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 2)
		{
			if(!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.THE_AIRSHIP_NO_PRIVILEGES);
				return null;
			}
			if(player.getClan() == null || player.getClan().getLevel() < 5)
			{
				player.sendPacket(SystemMessageId.THE_AIRSHIP_NEED_CLANLVL_5_TO_SUMMON);
				return "engineer_recon003.htm";
			}
			if(AirShipManager.getInstance().hasAirShipLicense(player.getClanId()) || player.getInventory().getItemByItemId(LICENSE) != null)
			{
				player.sendPacket(SystemMessageId.THE_AIRSHIP_SUMMON_LICENSE_ALREADY_ACQUIRED);
				return "engineer_recon005.htm";
			}
			if(!player.destroyItemByItemId(ProcessType.CONSUME, STARSTONE, LICENSE_COST, npc, true))
			{
				return "engineer_recon004.htm";
			}

			player.addItem(ProcessType.CONSUME, LICENSE, 1, npc, true);
		}
		return null;
	}
}