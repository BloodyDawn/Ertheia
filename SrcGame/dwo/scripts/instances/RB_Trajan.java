package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.Calendar;
import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 25.10.11
 * Time: 15:27
 */
/*
    http://www.youtube.com/watch?v=8haPExrp5O8
    http://www.youtube.com/watch?v=h2Te1yy7a88
    http://www.youtube.com/watch?v=s2xhKAuDKyc
    http://www.youtube.com/watch?v=OQ1DsqPAieU
    http://www.youtube.com/watch?v=xHwnAgTMCq8
    http://www.youtube.com/watch?v=PWNP4z7k6NY
    http://www.youtube.com/watch?v=UVRKKL5QwfU
    http://www.youtube.com/watch?v=kEXw-hyuHtg
    http://www.youtube.com/watch?v=1cXnh7_S_r8
    http://www.youtube.com/watch?v=uFDgBZH4gHs

    25785 14110 spatk01 a,s_npc_monster_trajan_strike1\0 a,trajan\0 a,trajan\0
25785 14111 atk01 a,s_npc_monster_trajan_fan_attack1\0 a,trajan\0 a,trajan\0
25785 14112 spatk02 a,s_npc_monster_trajan_poison_attack1\0 a,trajan\0 a,trajan\0
25785 14114 atk01 a,s_npc_monster_trajan_physical_immune1\0 a,trajan\0 a,trajan\0
25785 14115 atk01 a,s_npc_monster_trajan_magic_immune1\0 a,trajan\0 a,trajan\0
25785 14116 atk01 a,s_npc_monster_trajan_fury1\0 a,trajan\0 a,trajan\0
 */
public class RB_Trajan extends Quest
{
	private static final String qn = "RB_Trajan";

	// Квестовые НПЦ
	private static final int FILLAR = 30535;
	private static final int ADVENTURE_GUILDSMAN = 33385;
	private static final int TRAJAN = 25785;

	// Квестовые предметы
	private static final int SEAL_OF_ALLIGIANCE = 17739;
	private static final int INSTANCEID = InstanceZoneId.TEREDOR_WARZONE.getId();
	private static final Location ENTRY_POINT = new Location(186805, -173776, -3872);
	private static final Location EXIT_POINT = new Location(85729, -142517, -1336);
	private static final Location TRAJAN_SPAWN = new Location(176160, -185200, -3800);

	public RB_Trajan()
	{
		addStartNpc(FILLAR);
		addKillId(TRAJAN);
		addTalkId(FILLAR);
	}

	public static void main(String[] args)
	{
		new RB_Trajan();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("enter"))
		{
			enterInstance(player, "RB_Trajan.xml");
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc.getNpcId() == TRAJAN)
		{
			if(killer.getParty() != null)
			{
				for(L2PcInstance member : killer.getParty().getMembers())
				{
					member.addItem(ProcessType.QUEST, SEAL_OF_ALLIGIANCE, 1, null, true);
				}
			}

			Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
			if(inst != null)
			{
				InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());

				// Закрываем инстанс через 10 минут
				if(inst.getInstanceEndTime() - System.currentTimeMillis() > 300000)
				{
					inst.setDuration(300000);
				}

				inst.setEmptyDestroyTime(0);

				if(world instanceof TrajanWorld)
				{
					setReenterTime(world);
				}

				addSpawn(ADVENTURE_GUILDSMAN, npc.getX() + 100, npc.getY() + 100, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			st = newQuestState(player);
		}
		if(npc.getNpcId() == FILLAR)
		{
			if(player.getParty() != null && player.getParty().getMemberCount() >= 5)
			{
				htmltext = player.getParty().isLeader(player) ? "30535.htm" : "30535-nopl.htm";
			}
			else
			{
				htmltext = "30535-noparty.htm";
			}
		}
		return htmltext;
	}

	private int enterInstance(L2PcInstance player, String template)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if(world != null)
		{
			if(!(world instanceof TrajanWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return 0;
			}
			player.teleToInstance(ENTRY_POINT, world.instanceId);
			return world.instanceId;
		}
		else
		{
			if(!checkTeleport(player))
			{
				return 0;
			}

			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new TrajanWorld();
			world.instanceId = instanceId;
			world.templateId = INSTANCEID;
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);
			_log.log(Level.INFO, "Battle with Boss Trajan " + template + " Instance: " + instanceId + " created by player: " + player.getName());

			if(player.isGM())
			{
				player.teleToInstance(ENTRY_POINT, instanceId);
				world.allowed.add(player.getObjectId());
				init((TrajanWorld) world);
				return instanceId;
			}

			for(L2PcInstance partyMember : player.getParty().getMembers())
			{
				partyMember.teleToInstance(ENTRY_POINT, instanceId);
				world.allowed.add(partyMember.getObjectId());
			}

			init((TrajanWorld) world);
			return instanceId;
		}
	}

	private void init(TrajanWorld world)
	{
		addSpawn(TRAJAN, TRAJAN_SPAWN.getX(), TRAJAN_SPAWN.getY(), TRAJAN_SPAWN.getZ(), 0, false, 0, false, world.instanceId);
	}

	/**
	 * Проверка игрока и сопартийцев на
	 * возможность входа
	 * @param player игрок для проверки
	 * @return может ли игрок войти
	 */
	private boolean checkTeleport(L2PcInstance player)
	{
		L2Party party = player.getParty();

		if(party == null)
		{
			return false;
		}

		if(player.getObjectId() != party.getLeaderObjectId())
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}

		for(L2PcInstance partyMember : party.getMembers())
		{
			if(partyMember.getLevel() < 85)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}

			if(!Util.checkIfInRange(500, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}

			if(InstanceManager.getInstance().getPlayerWorld(player) != null)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}

			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID);
			if(System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}

	private String checkConditions(L2PcInstance player)
	{
		if(player.getParty() == null)
		{
			return "gk-noparty.htm";
		}
		if(player.getParty().getLeaderObjectId() != player.getObjectId())
		{
			return "gk-noleader.htm";
		}
		return null;
	}

	/**
	 * Вычисление времени возможности следующего захода
	 * @param world текущий мир игрока
	 */
	public void setReenterTime(InstanceWorld world)
	{
		if(world instanceof TrajanWorld)
		{
			// Вычисляем время следующиего захода
			Calendar reenter;
			Calendar now = Calendar.getInstance();
			Calendar reenterNextDay = (Calendar) now.clone();
			reenterNextDay.add(Calendar.DAY_OF_MONTH, 1);
			reenter = (Calendar) reenterNextDay.clone();

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
			sm.addString(InstanceManager.getInstance().getInstanceIdName(world.templateId));

			// Выставляем всем участникам рейда время перезахода
			for(int objectId : world.allowed)
			{
				L2PcInstance player = WorldManager.getInstance().getPlayer(objectId);
				if(player != null && player.isOnline())
				{
					InstanceManager.getInstance().setInstanceTime(objectId, world.templateId, reenter.getTimeInMillis());
					player.sendPacket(sm);
				}
			}
		}
	}

	public class TrajanWorld extends InstanceWorld
	{
		public List<L2PcInstance> playersInLairZone = new FastList<>();
	}
}