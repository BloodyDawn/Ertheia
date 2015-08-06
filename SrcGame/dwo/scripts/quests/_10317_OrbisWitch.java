package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.04.12
 * Time: 5:07
 */

public class _10317_OrbisWitch extends Quest
{
	// Квестовые персонажи
	private static final int Опера = 32946;
	private static final int Типия = 32892;

	public _10317_OrbisWitch()
	{
		addStartNpc(Опера);
		addTalkId(Опера, Типия);
	}

	public static void main(String[] args)
	{
		new _10317_OrbisWitch();
	}

	@Override
	public int getQuestId()
	{
		return 10317;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "mde_ug_cat_opera_q10317_08.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == Опера)
		{
			switch(reply)
			{
				case 1:
					return "mde_ug_cat_opera_q10317_04.htm";
				case 2:
					return "mde_ug_cat_opera_q10317_05.htm";
				case 3:
					return "mde_ug_cat_opera_q10317_06.htm";
				case 4:
					return "mde_ug_cat_opera_q10317_07.htm";
			}
		}
		else if(npc.getNpcId() == Типия)
		{
			if(reply == 1 && st.getCond() == 1)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(74128050, 3319695);
				st.giveAdena(506760, true);
				st.exitQuest(QuestType.ONE_TIME);
				return "orbis_typia_q10317_02.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Опера)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "mde_ug_cat_opera_q10317_02.htm";
				case CREATED:
					QuestState previous = player.getQuestState(_10316_UndecayingMemoryOfThePast.class);
					return previous == null || !previous.isCompleted() || player.getLevel() < 95 ? "mde_ug_cat_opera_q10317_03.htm" : "mde_ug_cat_opera_q10317_01.htm";
				case STARTED:
					return "mde_ug_cat_opera_q10317_09.htm";
			}
		}
		else if(npc.getNpcId() == Типия)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "orbis_typia_q10317_03.htm";
				case STARTED:
					return "orbis_typia_q10317_01.htm";
			}
		}
		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10316_UndecayingMemoryOfThePast.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 95;

	}
}
