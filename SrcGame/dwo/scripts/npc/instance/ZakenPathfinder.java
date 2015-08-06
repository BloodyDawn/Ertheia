package dwo.scripts.npc.instance;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.scripts.instances.RB_Zaken;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 20.09.12
 * Time: 18:14
 */

public class ZakenPathfinder extends Quest
{
	private static final int PATHFINDER = 32713;

	private static final int INSTANCE_DAY_ZAKEN = InstanceZoneId.CAVERN_OF_THE_PIRATE_CAPTAIN_DAYDREAM_1.getId();
	private static final int INSTANCE_NIGHTMARE_ZAKEN = InstanceZoneId.CAVERN_OF_THE_PIRATE_CAPTAIN_NIGHTMARE.getId();
	private static final int INSTANCE_DAY_ZAKEN_ULTIMATE = InstanceZoneId.CAVERN_OF_THE_PIRATE_CAPTAIN_DAYDREAM_ULTIMATE.getId();

	public ZakenPathfinder()
	{
		addAskId(PATHFINDER, -2124001);
	}

	public static void main(String[] args)
	{
		new ZakenPathfinder();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == PATHFINDER)
		{
			String result;
			switch(reply)
			{
				// Daily Zaken
				case 1:
					result = checkConditions(player, 1);
					if(result == null)
					{
						RB_Zaken.getInstance().enterInstance(player, INSTANCE_DAY_ZAKEN);
					}
					else
					{
						return result;
					}
					// Nightmare Zaken
				case 2:
					result = checkConditions(player, 2);
					if(result == null)
					{
						RB_Zaken.getInstance().enterInstance(player, INSTANCE_NIGHTMARE_ZAKEN);
					}
					else
					{
						return result;
					}
					// Extreme Zaken
				case 3:
					result = checkConditions(player, 3);
					if(result == null)
					{
						RB_Zaken.getInstance().enterInstance(player, INSTANCE_DAY_ZAKEN_ULTIMATE);
					}
					else
					{
						return result;
					}
			}
		}

		return null;
	}

	private String checkConditions(L2PcInstance player, int zakenType)
	{
		if(player.isGM())
		{
			return null;
		}

		L2Party party = player.getParty();

		if(party == null)
		{
			return "zaken_enter001b.htm";
		}

		switch(zakenType)
		{
			// Daily
			case 1:
				if(party.getCommandChannel() == null)
				{
					if(!party.getLeader().equals(player))
					{
						party.broadcastPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
						return "zaken_enter001a.htm";
					}
					else if(party.getMemberCount() < 7)
					{
						party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(7));
						return "zaken_enter001d.htm";
					}
				}
				else
				{
					if(!party.getCommandChannel().getLeader().equals(player))
					{
						party.getCommandChannel().broadcastMessage(SystemMessageId.ONLY_FOR_ALLIANCE_CHANNEL_LEADER);
						return "zaken_enter001c.htm";
					}
					else if(party.getCommandChannel().getMemberCount() < 7)
					{
						party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(7));
						return "zaken_enter001d.htm";
					}
				}
				break;
			// Nightmare
			case 2:
				if(party.getCommandChannel() == null || !party.getCommandChannel().getLeader().equals(player))
				{
					party.broadcastPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
					return "zaken_enter001c.htm";
				}
				if(party.getCommandChannel().getMemberCount() < 56)
				{
					party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(56));
					return "zaken_enter001d.htm";
				}
				break;
			// Extreme
			case 3:
				if(party.getCommandChannel() == null)
				{
					if(!party.getLeader().equals(player))
					{
						party.broadcastPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
						return "zaken_enter001a.htm";
					}
					else if(party.getMemberCount() < 7)
					{
						party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(7));
						return "zaken_enter001d.htm";
					}
				}
				else
				{
					if(!party.getCommandChannel().getLeader().equals(player))
					{
						party.getCommandChannel().broadcastMessage(SystemMessageId.ONLY_FOR_ALLIANCE_CHANNEL_LEADER);
						return "zaken_enter001c.htm";
					}
					else if(party.getCommandChannel().getMemberCount() < 7)
					{
						party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(7));
						return "zaken_enter001d.htm";
					}
				}
				break;
		}
		return null;
	}
}
