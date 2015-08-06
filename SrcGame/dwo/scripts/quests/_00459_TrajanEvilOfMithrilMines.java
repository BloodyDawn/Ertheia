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
 * Date: 01.12.11
 * Time: 3:32
 * TODO: Ретейл диалоги нпц после убийства Траджана
 */

public class _00459_TrajanEvilOfMithrilMines extends Quest
{
	private static final int _Fillar = 30535;
	private static final int _Trajan = 25785;
	private static final int _proof = 19450;

	public _00459_TrajanEvilOfMithrilMines()
	{
		addStartNpc(_Fillar);
		addTalkId(_Fillar);
		addKillId(_Trajan);
	}

	public static void main(String[] args)
	{
		new _00459_TrajanEvilOfMithrilMines();
	}

	@Override
	public int getQuestId()
	{
		return 459;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equalsIgnoreCase("30535-04.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("30535-07.htm"))
		{
			st.giveItems(_proof, 20);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.ONE_TIME);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		if(npc.getNpcId() == _Trajan && st.getCond() == 1)
		{
			st.setCond(2);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == _Fillar)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				case CREATED:
					return player.getLevel() >= 85 ? "30535-00.htm" : getLowLevelMsg(85);
				case STARTED:
					if(st.getCond() == 1)
					{
						return "30535-05.htm";
					}
					if(st.getCond() == 2)
					{
						return "30535-06.htm";
					}
					break;
			}
		}
		return null;
	}
}