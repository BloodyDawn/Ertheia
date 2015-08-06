package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.util.Rnd;
import dwo.scripts.quests._10296_SevenSignsOneWhoSeeksThePowerOfTheSeal;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;

public class EQ2_PenalColonyOfDarknessRaid extends Quest
{
	private static final String qn = "EQ2_PenalColonyOfDarknessRaid";
	// NPC's
	private static final int Odd_Globe = 32815;
	private static final int Elcadia_Support = 32787;
	private static final int ErisEvilThoughts = 32792;
	// Teleports
	private static final int ENTER = 0;
	private static final int EXIT = 1;
	private static final int BossRoom = 2;
	private static final int[][] TELEPORTS = {
		{120710, -86971, -3392}, // enter
		{115599, -81415, -3400}, // exit
		{76632, -240981, -10832}, // BossRoom
		{56085, -252978, -6769}, // GoldRoom
		{56065, -250827, -6765} // LastRoom
	};
	private static final int[] skill = {6725, 6728, 6730};
	private final TIntObjectHashMap<InstanceHolder> instanceWorlds = new TIntObjectHashMap<>();

	public EQ2_PenalColonyOfDarknessRaid()
	{
		addStartNpc(Odd_Globe);
		addTalkId(Odd_Globe);
		addStartNpc(ErisEvilThoughts);
		addTalkId(ErisEvilThoughts);
		addTalkId(Elcadia_Support);
	}

	public static void main(String[] args)
	{
		new EQ2_PenalColonyOfDarknessRaid();
	}

	private void teleportPlayer(L2Npc npc, L2PcInstance player, int[] coords, int instanceId)
	{
		InstanceHolder holder = instanceWorlds.get(instanceId);
		if(holder == null && instanceId > 0)
		{
			holder = new InstanceHolder();
			instanceWorlds.put(instanceId, holder);
		}
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.getInstanceController().setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], false);
		cancelQuestTimer("check_follow", npc, player);
		if(holder != null)
		{
			for(L2Npc h : holder.mobs)
			{
				h.getLocationController().delete();
			}
			holder.mobs.clear();
		}
		if(instanceId > 0)
		{
			L2Npc support = addSpawn(Elcadia_Support, player.getX(), player.getY(), player.getZ(), 0, false, 0, false, player.getInstanceId());
			holder.mobs.add(support);
			startQuestTimer("check_follow", 3000, support, player);
		}
	}

	protected void enterInstance(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if(world != null)
		{
			if(!(world instanceof PenalColonyOfDarknessRaidWorld))
			{
				int instanceId = InstanceManager.getInstance().createDynamicInstance("PenalColonyOfDarknessRaid.xml");

				world = new PenalColonyOfDarknessRaidWorld();
				world.instanceId = instanceId;
				world.templateId = InstanceZoneId.PRISON_OF_DARKNESS.getId();
				world.status = 0;
				InstanceManager.getInstance().addWorld(world);

				world.allowed.add(player.getObjectId());

				teleportPlayer(npc, player, TELEPORTS[ENTER], instanceId);

				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				teleportPlayer(npc, player, TELEPORTS[ENTER], world.instanceId);
			}
		}
		else
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance("EQ2_PenalColonyOfDarknessRaid.xml");

			world = new PenalColonyOfDarknessRaidWorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.PRISON_OF_DARKNESS.getId();
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());

			teleportPlayer(npc, player, TELEPORTS[ENTER], instanceId);
		}
	}

	protected void enterInstance2(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if(world != null)
		{
			if(!(world instanceof PenalColonyOfDarknessRaidWorld))
			{
				int instanceId = InstanceManager.getInstance().createDynamicInstance("EQ2_PenalColonyOfDarknessRaid.xml");

				world = new PenalColonyOfDarknessRaidWorld();
				world.instanceId = instanceId;
				world.templateId = InstanceZoneId.PRISON_OF_DARKNESS.getId();
				world.status = 0;
				InstanceManager.getInstance().addWorld(world);

				world.allowed.add(player.getObjectId());

				teleportPlayer(npc, player, TELEPORTS[BossRoom], instanceId);

				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				teleportPlayer(npc, player, TELEPORTS[BossRoom], world.instanceId);
			}
		}
		else
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance("PenalColonyOfDarknessRaid.xml");

			world = new PenalColonyOfDarknessRaidWorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.PRISON_OF_DARKNESS.getId();
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());

			teleportPlayer(npc, player, TELEPORTS[ENTER], instanceId);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if(st == null)
		{
			st = newQuestState(player);
		}

		if(event.equalsIgnoreCase("check_follow"))
		{
			npc.getAI().stopFollow();
			npc.setIsRunning(true);
			npc.getAI().startFollow(player);
			npc.setTarget(player);
			npc.doCast(SkillTable.getInstance().getInfo(skill[Rnd.get(0, skill.length - 1)], 1));
			startQuestTimer("check_follow", 20000, npc, player);
			return "";
		}
		if(event.equalsIgnoreCase("start_movie"))
		{
			InstanceManager.getInstance().destroyInstance(player.getInstanceId());
			enterInstance2(npc, player);
			return null;
		}
		if(npc.getNpcId() == Odd_Globe)
		{
			if(event.equalsIgnoreCase("enter"))
			{
				enterInstance(npc, player);
				return null;
			}
		}
		else if(npc.getNpcId() == Elcadia_Support)
		{
			Instance inst = InstanceManager.getInstance().getInstance(player.getInstanceId());
			if(event.equalsIgnoreCase("exit"))
			{
				InstanceHolder holder = instanceWorlds.get(npc.getInstanceId());
				if(holder != null)
				{
					for(L2Npc h : holder.mobs)
					{
						h.getLocationController().delete();
					}
					holder.mobs.clear();
				}
				teleportPlayer(npc, player, TELEPORTS[EXIT], 0);
				inst.setDuration(60000);
				return null;
			}
		}
		else if(npc.getNpcId() == ErisEvilThoughts)
		{
			if(event.equalsIgnoreCase("video"))
			{
				if(player.getQuestState(_10296_SevenSignsOneWhoSeeksThePowerOfTheSeal.class).getInt("guard1") != 1)
				{
					player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_BOSS_OPENING);
					startQuestTimer("start_movie", 60000, npc, player);
					return null;
				}
			}
			else if(event.equalsIgnoreCase("presentation"))
			{
				player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_ELYSS_NARRATION);
				return null;
			}
			else if(event.equalsIgnoreCase("exit"))
			{
				teleportPlayer(npc, player, TELEPORTS[EXIT], 0);
				InstanceHolder holder = instanceWorlds.get(npc.getInstanceId());
				if(holder != null)
				{
					for(L2Npc h : holder.mobs)
					{
						h.getLocationController().delete();
					}
					holder.mobs.clear();
				}
				return null;
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if(st == null)
		{
			st = newQuestState(player);
		}
		else if(npc.getNpcId() == Odd_Globe)
		{
			if(player.getQuestState("10295_SevenSignsSolinasTomb").isCompleted() && !player.getQuestState("10296_SevenSignsOneWhoSeeksThePowerOfTheSeal").isCompleted() && player.getQuestState("10296_SevenSignsOneWhoSeeksThePowerOfTheSeal").getInt("guard1") != 1)
			{
				enterInstance(npc, player);
				return null;
			}
			else
			{
				htmltext = "32815-01.html";
			}
		}
		return htmltext;
	}

	private static class InstanceHolder
	{
		FastList<L2Npc> mobs = new FastList<>();
	}

	private class PenalColonyOfDarknessRaidWorld extends InstanceWorld
	{
		public PenalColonyOfDarknessRaidWorld()
		{
		}
	}
}
