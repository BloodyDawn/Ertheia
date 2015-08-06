package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.scripts.quests._10284_AcquisitionOfDivineSword;

import java.util.ArrayList;
import java.util.List;

public class FQ_MithrilMine extends Quest
{
	// Персонажи
	private static final int KEGOR = 18846;
	// Предметы
	private static final int ANTIDOTE = 15514;
	// Монстры
	private static final int GRIMA = 22766;
	// Точка входа
	private static final Location ENTRY_POINT = new Location(186852, -173492, -3800);
	private static final int[][] SPAWNLIST = {
		{22766, 185173, -184042, -3313, 48588}, {22766, 185374, -184130, -3313, 46222},
		{22766, 185571, -184304, -3313, 42528}, {22766, 185728, -184401, -3313, 39138},
		{22766, 185822, -184557, -3313, 36467}, {22766, 185910, -184716, -3313, 33568}
	};
	private static FQ_MithrilMine _mithrilInstance;

	public FQ_MithrilMine()
	{
		addFirstTalkId(KEGOR);
		addKillId(GRIMA);
		addAskId(KEGOR, -2315);
	}

	private static boolean checkConditions(L2PcInstance player)
	{
		if(player.getLevel() < 82)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		QuestState st = player.getQuestState(_10284_AcquisitionOfDivineSword.class);
		if(st == null || !st.isStarted())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}

		if(!st.hasQuestItems(ANTIDOTE))
		{
			st.giveItems(ANTIDOTE, 1);
		}

		st.setCond(4);
		st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);

		return true;
	}

	public static void main(String[] args)
	{
		_mithrilInstance = new FQ_MithrilMine();
	}

	public static FQ_MithrilMine getInstance()
	{
		return _mithrilInstance;
	}

	public void enterInstance(L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world != null)
		{
			if(!(world instanceof MMWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				player.teleToInstance(ENTRY_POINT, world.instanceId);
				if(player.getItemsCount(ANTIDOTE) < 0)
				{
					player.addItem(ProcessType.NPC, ANTIDOTE, 1, null, true);
				}
			}
		}
		else
		{
			if(!checkConditions(player))
			{
				return;
			}

			int instanceId = InstanceManager.getInstance().createDynamicInstance("FQ_MithrilMine.xml");

			world = new MMWorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.MITHRIL_MINE.getId();
			world.status = 0;

			Instance ins = InstanceManager.getInstance().getInstance(instanceId);
			ins.setSpawnLoc(player.getLoc());

			InstanceManager.getInstance().addWorld(world);

			((MMWorld) world).kegor = addSpawn(18846, 185198, -184818, -3288, 6800, false, 0, false, world.instanceId);

			world.allowed.add(player.getObjectId());
			player.teleToInstance(ENTRY_POINT, instanceId);

			if(player.getItemsCount(ANTIDOTE) == 0)
			{
				player.addItem(ProcessType.NPC, ANTIDOTE, 1, null, true);
			}
		}
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -2315)
		{
			if(reply == 1)
			{
				InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());

				if(tmpworld instanceof MMWorld)
				{
					MMWorld world = (MMWorld) tmpworld;

					if(world.status == 0)
					{
						world.status = 1;
						QuestState state = player.getQuestState(_10284_AcquisitionOfDivineSword.class);
						if(state != null && state.getMemoState() == 2)
						{
							state.takeItems(ANTIDOTE, -1);
							state.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							state.setCond(5);
						}
						for(int[] SP : SPAWNLIST)
						{
							L2Attackable attacker = (L2Attackable) addSpawn(SP[0], SP[1], SP[2], SP[3], SP[4], false, 0, false, world.instanceId);
							if(attacker != null)
							{
								world.attackers.add(attacker);
								attacker.attackCharacter(player);
							}
						}
						return "kegor_savedun_q10284_01.htm";
					}
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof MMWorld)
		{
			MMWorld world = (MMWorld) tmpworld;

			if(world.status == 1)
			{
				if(world.attackers.contains(npc))
				{
					world.attackers.remove(npc);
				}

				if(world.attackers.isEmpty() || world.attackers.isEmpty())
				{
					world.status = 2;
					world.kegor.broadcastPacket(new NS(world.kegor.getObjectId(), ChatType.ALL, npc.getNpcId(), NpcStringId.I_CAN_FINALLY_TAKE_A_BREATHER_BY_THE_WAY_WHO_ARE_YOU_HMM_I_THINK_I_KNOW_WHO_SENT_YOU));

					QuestState st = player.getQuestState(_10284_AcquisitionOfDivineSword.class);
					if(st != null)
					{
						st.setCond(6);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof MMWorld)
		{
			MMWorld world = (MMWorld) tmpworld;

			if(world.status == 0)
			{
				return "kegor_savedun001.htm";
			}
			else if(world.status == 1)
			{
				return "kegor_savedun_q10284_02.htm";
			}
			else if(world.status == 2)
			{
				QuestState st = player.getQuestState(_10284_AcquisitionOfDivineSword.class);
				if(st != null && st.getMemoState() == 3)
				{
					st.giveAdena(296425, true);
					st.addExpAndSp(921805, 82230);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					InstanceManager.getInstance().getInstance(player.getInstanceId()).setDuration(120);
					return "kegor_savedun_q10284_03.htm";
				}
				else
				{
					return "kegor_savedun_q10284_04.htm";
				}
			}
		}
		return null;
	}

	private class MMWorld extends InstanceWorld
	{
		L2Npc kegor;

		private List<L2Npc> attackers;

		public MMWorld()
		{
			attackers = new ArrayList<>(6);
		}
	}
}