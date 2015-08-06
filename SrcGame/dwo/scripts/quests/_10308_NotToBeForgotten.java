package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _10308_NotToBeForgotten extends Quest
{
	private static final int ADVENTURER_HELPER = 33463;
	private static final int KURTIZ = 30870;

	private static final int[] MOBS = {20679, 20680, 21017, 21019, 21020, 21022, 21258, 21259, 21018, 21021};

	private static final int LEGACY_CORE = 19487;

	private static final int DROP_CHANCE = 60;

	public _10308_NotToBeForgotten()
	{
		setMinMaxLevel(55, 58);
		addStartNpc(ADVENTURER_HELPER);
		addTalkId(ADVENTURER_HELPER, KURTIZ);
		addKillId(MOBS);
		questItemIds = new int[]{LEGACY_CORE};
	}

	public static void main(String[] args)
	{
		new _10308_NotToBeForgotten();
	}

	@Override
	public int getQuestId()
	{
		return 10308;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(npc.getNpcId() == ADVENTURER_HELPER && event.equalsIgnoreCase("33463-04.htm"))
		{
			st.startQuest();
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());
		if(npc == null || st == null)
		{
			return super.onKill(npc, killer, isPet);
		}

		if(st.getCond() == 1 && Rnd.getChance(DROP_CHANCE))
		{
			st.giveItems(LEGACY_CORE, 1);

			if(st.getQuestItemsCount(LEGACY_CORE) >= 40)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == ADVENTURER_HELPER)
		{
			if(st.isCreated())
			{
				return "33463-01.htm";
			}
			else if(st.isStarted() && st.getCond() == 1)
			{
				return "33463-05.htm";
			}
			else if(st.isCompleted())
			{
				return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
			}
		}
		else if(npc.getNpcId() == KURTIZ)
		{
			if(st.isCompleted())
			{
				return "30870-03.htm";
			}
			else if(st.isStarted() && st.getCond() == 1)
			{
				return "30870-01.htm";
			}
			else if(st.isStarted() && st.getCond() == 2)
			{
				st.takeItems(LEGACY_CORE, -1);
				st.addExpAndSp(2322445, 1968325);
				st.giveAdena(376704, true);
				st.unset("cond");
				st.exitQuest(QuestType.ONE_TIME);
				return "30870-02.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 55 && player.getLevel() < 59;

	}
}