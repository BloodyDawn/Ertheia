package dwo.scripts.npc;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.scripts.instances.Kartia;

/**
 * L2GOD Team
 * User: Yorie, ANZO
 * Date: 13.02.12
 * Time: 7:41
 */

public class KartiaHelpers extends Quest
{
	// Квестовые персонажи
	private static final int KARTIA_RESEARCHER = 33647;
	private static final int ADOLPH_S85 = 33608;
	private static final int ADOLPH_S90 = 33619;
	private static final int ADOLPH_S95 = 33630;

	public KartiaHelpers()
	{
		addAskId(KARTIA_RESEARCHER, 4990);
		addAskId(ADOLPH_S85, 499);
		addAskId(ADOLPH_S90, 499);
		addAskId(ADOLPH_S95, 499);
	}

	public static void main(String[] args)
	{
		new KartiaHelpers();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(npc.getNpcId())
		{
			case KARTIA_RESEARCHER:
				switch(reply)
				{
					// Solo 85
					case 1:
						return checkRequirments(player, Kartia.KartiaType.SOLO85);
					// Solo 90
					case 2:
						return checkRequirments(player, Kartia.KartiaType.SOLO90);
					// Solo 95
					case 3:
						return checkRequirments(player, Kartia.KartiaType.SOLO95);
					// Party 85
					case 4:
						return checkRequirments(player, Kartia.KartiaType.PARTY85);
					// Party 90
					case 5:
						return checkRequirments(player, Kartia.KartiaType.PARTY90);
					// Party 95
					case 6:
						return checkRequirments(player, Kartia.KartiaType.PARTY95);
				}
				break;
			case ADOLPH_S85:
			case ADOLPH_S90:
			case ADOLPH_S95:
				Kartia.KartiaWorld world = InstanceManager.getInstance().getInstanceWorld(player, Kartia.KartiaWorld.class);

				if(world == null)
				{
					return null;
				}

				// Убираем одного из саппортов
				switch(reply)
				{
					case 1:
						Kartia.getInstance().deselectSupport(world, "WARRIOR");
						break;
					case 2:
						Kartia.getInstance().deselectSupport(world, "ARCHER");
						break;
					case 3:
						Kartia.getInstance().deselectSupport(world, "SUMMONER");
						break;
					case 4:
						Kartia.getInstance().deselectSupport(world, "HEALER");
						break;
				}
				break;
		}
		return null;
	}

	public String checkRequirments(L2PcInstance player, Kartia.KartiaType type)
	{
		int minLevel = 0;
		int maxLevel = 0;
		int templateId = 0;
		int minPlayers = 0;

		switch(type)
		{
			case SOLO85:
				minLevel = 85;
				maxLevel = 89;
				templateId = Kartia.KARTIA_SOLO85;
				minPlayers = 1;
				break;
			case SOLO90:
				minLevel = 90;
				maxLevel = 94;
				templateId = Kartia.KARTIA_SOLO90;
				minPlayers = 1;
				break;
			case SOLO95:
				minLevel = 95;
				maxLevel = 99;
				templateId = Kartia.KARTIA_SOLO95;
				minPlayers = 1;
				break;
			case PARTY85:
				minLevel = 85;
				maxLevel = 89;
				templateId = Kartia.KARTIA_PARTY85;
				minPlayers = 2;
				break;
			case PARTY90:
				minLevel = 90;
				maxLevel = 94;
				templateId = Kartia.KARTIA_PARTY90;
				minPlayers = 2;
				break;
			case PARTY95:
				minLevel = 95;
				maxLevel = 99;
				templateId = Kartia.KARTIA_PARTY95;
				minPlayers = 2;
		}

		if(player.isGM())
		{
			minPlayers = 1;
			minLevel = 85;
			maxLevel = 99;
		}

		if(minLevel == 0 || maxLevel == 0 || templateId == 0 || minPlayers == 0)
		{
			return "agent_cartia_aden004.htm";
		}

		long reEnterTime = InstanceManager.getInstance().getInstanceTime(player.getObjectId(), templateId);
		if(System.currentTimeMillis() < reEnterTime)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(player));
			return "agent_cartia_aden002.htm";
		}
		if(minPlayers < 2)
		{
			if(player.getLevel() < minLevel)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(player));
				return "agent_cartia_aden005.htm";
			}
			else if(player.getLevel() > maxLevel)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(player));
				return "agent_cartia_aden004.htm";
			}
		}
		else if(minPlayers >= 2)
		{
			if(player.getParty() == null || !player.getParty().isLeader(player))
			{
				return "agent_cartia_aden003.htm";
			}
			else if(!checkPartymemberForEnter(player.getParty(), templateId))
			{
				return "agent_cartia_aden005.htm";
			}
		}

		Kartia.getInstance().enterInstance(player, templateId);
		return "agent_cartia_aden001b.htm";
	}

	private boolean checkPartymemberForEnter(L2Party party, int instanceId)
	{
		Long reentertime = 0L;
		for(L2PcInstance member : party.getMembers())
		{
			reentertime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), instanceId);
			if(System.currentTimeMillis() < reentertime)
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
				return false;
			}
			if(instanceId == Kartia.KARTIA_PARTY85)
			{
				if(member.getLevel() < 85 || member.getLevel() >= 90)
				{
					party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
					return false;
				}
			}
			else if(instanceId == Kartia.KARTIA_PARTY90)
			{
				if(member.getLevel() < 90 || member.getLevel() >= 95)
				{
					party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
					return false;
				}
			}
			else if(instanceId == Kartia.KARTIA_PARTY95)
			{
				if(member.getLevel() < 95)
				{
					party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
					return false;
				}
			}
		}
		return true;
	}
}
