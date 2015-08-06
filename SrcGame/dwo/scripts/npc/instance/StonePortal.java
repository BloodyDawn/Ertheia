package dwo.scripts.npc.instance;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.scripts.instances.ShillienAltar;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class StonePortal extends Quest
{
	private static final int STONE_PORTAL = 33405;

	public StonePortal()
	{
		addAskId(STONE_PORTAL, 10347);
	}

	public static void main(String[] args)
	{
		new StonePortal();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(reply)
		{
			case 1:
				String result;
				if((result = checkEnterConditions(player)) != null)
				{
					return result;
				}
				else
				{
					ShillienAltar.getInstance().enterInstance(player);
					return "gate_seal_of_silen002.htm";
				}
		}
		return null;
	}

	/**
	 * Проверка условий для входа в инстанс
	 * @param player Игрок.
	 * @return удоволетворяет-ли группа персонажей дял входа в инстанс
	 */
	private String checkEnterConditions(L2PcInstance player)
	{
		if(player.isGM())
		{
			return null;
		}

		if(player.getParty() == null)
		{
			return "gate_seal_of_silen003.htm";
		}
		if(player.getParty().getMemberCount() < 7)
		{
			player.getParty().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(7));
			return "gate_seal_of_silen003.htm";
		}

		for(L2PcInstance member : player.getParty().getMembers())
		{
			if(member.getLevel() < 95)
			{
				player.getParty().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
				return "gate_seal_of_silen003.htm";
			}
		}
		return null;
	}
}