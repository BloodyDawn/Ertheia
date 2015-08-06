package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 17.03.12
 * Time: 15:48
 */

public class _00460_RareMaterialsResearch extends Quest
{
	// Квестовые персонажи
	private static final int Амер = 33092;
	private static final int Филар = 30535;

	// Квестовые предметы
	private static final int ЧастьЯйцаТраджана = 17735;

	// Квестовые монстры
	private static final int[] Яйца = {18997, 19023};

	// Квестовые предметы
	private static final int ДоказательствоВерности = 19450;

	public _00460_RareMaterialsResearch()
	{

		addStartNpc(Амер);
		addTalkId(Амер, Филар);
		addKillId(Яйца);
		questItemIds = new int[]{ЧастьЯйцаТраджана};
	}

	public static void main(String[] args)
	{
		new _00460_RareMaterialsResearch();
	}

	@Override
	public int getQuestId()
	{
		return 460;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		switch(event)
		{
			case "30535-01.htm":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.takeItems(ЧастьЯйцаТраджана, -1);
				st.giveItems(ДоказательствоВерности, 3);
				st.exitQuest(QuestType.DAILY);
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 1 && Rnd.getChance(50) && ArrayUtils.contains(Яйца, npc.getNpcId()))
		{
			st.giveItem(ЧастьЯйцаТраджана);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			if(st.getQuestItemsCount(ЧастьЯйцаТраджана) >= 20)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == Амер)
		{
			if(player.getLevel() < 85)
			{
				return getLowLevelMsg(85);
			}
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.DAILY); // TODO: Верное сообщение
				case CREATED:
					st.startQuest();
					return "33092-00.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33092-00.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == Филар)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					return "30535-00.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		return player.getLevel() >= 85 && st != null && st.isCompleted();

	}
}
