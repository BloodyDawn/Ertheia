package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.Location;
import dwo.scripts.instances.EQ2_ElcadiaTent;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;
import org.apache.commons.lang3.ArrayUtils;

public class _10292_SevenSignsGirlofDoubt extends Quest
{
	// NPC
	private static final int Hardin = 30832;
	private static final int Wood = 32593;
	private static final int Franz = 32597;
	private static final int Elcadia = 32784;
	private static final int Gruff_looking_Man = 32862;
	private static final int Jeina = 32617;
	// Item
	private static final int Elcadias_Mark = 17226;
	// Mobs
	private static final int[] Mobs = {22801, 22802, 22804, 22805};

	private final TIntObjectHashMap<InstanceHolder> instanceWorlds = new TIntObjectHashMap<>();

	public _10292_SevenSignsGirlofDoubt()
	{
		addStartNpc(Wood);
		addTalkId(Wood);
		addTalkId(Franz);
		addTalkId(Hardin);
		addTalkId(Elcadia);
		addTalkId(Gruff_looking_Man);
		addTalkId(Jeina);
		addKillId(27422);
		addKillId(Mobs);

		questItemIds = new int[]{Elcadias_Mark};
	}

	public static void main(String[] args)
	{
		new _10292_SevenSignsGirlofDoubt();
	}

	@Override
	public int getQuestId()
	{
		return 10292;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		int instanceId = npc.getInstanceId();
		InstanceHolder holder = instanceWorlds.get(instanceId);
		if(holder == null && instanceId > 0)
		{
			holder = new InstanceHolder();
			instanceWorlds.put(instanceId, holder);
		}

		if(st == null)
		{
			return null;
		}
		if(event.equalsIgnoreCase("evil_despawn"))
		{
			holder.spawned = false;
			holder.mobs.stream().filter(h -> h != null).forEach(h -> h.getLocationController().delete());
			holder.mobs.clear();
			instanceWorlds.remove(instanceId);
			return null;
		}
		if(npc.getNpcId() == Wood)
		{
			if(event.equalsIgnoreCase("32593-05.htm"))
			{
				st.startQuest();
			}
		}
		else if(npc.getNpcId() == Franz)
		{
			if(event.equalsIgnoreCase("32597-08.htm"))
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		else if(npc.getNpcId() == Hardin)
		{
			if(event.equalsIgnoreCase("30832-02.html"))
			{
				st.setCond(8);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		else if(npc.getNpcId() == Elcadia)
		{
			if(event.equalsIgnoreCase("32784-03.html"))
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if(event.equalsIgnoreCase("32784-14.html"))
			{
				st.setCond(7);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if(event.equalsIgnoreCase("spawn"))
			{
				if(holder.spawned)
				{
					return "32593-02.htm";
				}
				else
				{
					st.takeItems(Elcadias_Mark, -1);
					holder.spawned = true;
					L2Npc evil = addSpawn(27422, 89440, -238016, -9632, 335, false, 0, false, player.getInstanceId());
					evil.setIsNoRndWalk(true);
					holder.mobs.add(evil);
					L2Npc evil1 = addSpawn(27424, 89524, -238131, -9632, 56, false, 0, false, player.getInstanceId());
					evil1.setIsNoRndWalk(true);
					holder.mobs.add(evil1);
					startQuestTimer("evil_despawn", 60000, evil, player);
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == Gruff_looking_Man)
		{
			if(reply == 1)
			{
				return EQ2_ElcadiaTent.getInstance().checkAndEnterToInstance(player);
			}
		}
		else if(npc.getNpcId() == Elcadia)
		{
			if(reply == 10)
			{
				player.teleToInstance(new Location(43316, -87986, -2832), 0);
				return null;
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(st != null && st.getCond() == 3 && ArrayUtils.contains(Mobs, npc.getNpcId()) && st.getQuestItemsCount(Elcadias_Mark) < 10 && st.getQuestItemsCount(Elcadias_Mark) != 9)
		{
			st.giveItems(Elcadias_Mark, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(st != null && st.getCond() == 3 && ArrayUtils.contains(Mobs, npc.getNpcId()) && st.getQuestItemsCount(Elcadias_Mark) >= 9)
		{
			st.giveItems(Elcadias_Mark, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			st.setCond(4);
		}
		else if(st != null && st.getCond() == 5 && npc.getNpcId() == 27422)
		{
			int instanceid = npc.getInstanceId();
			InstanceHolder holder = instanceWorlds.get(instanceid);
			if(holder == null)
			{
				return null;
			}
			holder.mobs.stream().filter(h -> h != null).forEach(h -> h.getLocationController().delete());
			holder.spawned = false;
			holder.mobs.clear();
			instanceWorlds.remove(instanceid);
			st.setCond(6);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == Wood)
		{
			if(st.isCompleted())
			{
				return "32593-02.htm";
			}
			else if(player.getLevel() < 81)
			{
				return "32593-03.htm";
			}
			else if(player.getQuestState(_00198_SevenSignEmbryo.class) == null || !player.getQuestState(_00198_SevenSignEmbryo.class).isCompleted())
			{
				return "32593-03.htm";
			}
			else if(st.isCreated())
			{
				return "32593-01.htm";
			}
			else if(st.getCond() >= 1)
			{
				return "32593-07.html";
			}
		}
		else if(npc.getNpcId() == Franz)
		{
			if(st.getCond() == 1)
			{
				return "32597-01.htm";
			}
			else if(st.getCond() == 2)
			{
				return "32597-03.html";
			}
		}
		else if(npc.getNpcId() == Elcadia)
		{
			if(st.getCond() == 2)
			{
				return "32784-01.html";
			}
			else if(st.getCond() == 3)
			{
				return "32784-04.html";
			}
			else if(st.getCond() == 4)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(5);
				return "32784-05.html";
			}
			else if(st.getCond() == 5)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "32784-05.html";
			}
			else if(st.getCond() == 6)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "32784-11.html";
			}
			else if(st.getCond() == 8)
			{
				if(player.isSubClassActive())
				{
					return "32784-18.html";
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.addExpAndSp(10000000, 1000000);
					st.exitQuest(QuestType.ONE_TIME);
					return "32784-16.html";
				}
			}
		}
		else if(npc.getNpcId() == Hardin)
		{
			if(st.getCond() == 7)
			{
				return "30832-01.html";
			}
			else if(st.getCond() == 8)
			{
				return "30832-04.html";
			}
		}
		return null;
	}

	private static class InstanceHolder
	{
		// List
		FastList<L2Npc> mobs = new FastList<>();
		// State
		boolean spawned;
	}
}