package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.TutorialShowHtml;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.11.11
 * Time: 23:34
 */

public class _10324_MeetingWithGallint extends Quest
{
	private static final int SHENON = 32974;
	private static final int GALLINT = 32980;

	public _10324_MeetingWithGallint()
	{
		addStartNpc(SHENON);
		addTalkId(SHENON, GALLINT);
	}

	public static void main(String[] args)
	{
		new _10324_MeetingWithGallint();
	}

	@Override
	public int getQuestId()
	{
		return 10324;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "si_illusion_shannon_q10324_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == SHENON)
		{
			if(reply == 1)
			{
				return "si_illusion_shannon_q10324_04.htm";
			}
		}
		else if(npc.getNpcId() == GALLINT)
		{
			if(reply == 1)
			{
				player.sendPacket(new TutorialShowHtml(TutorialShowHtml.CLIENT_SIDE, "..\\L2text\\QT_004_skill_01.htm"));
				st.giveAdena(11000, true);
				st.addExpAndSp(1700, 5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "si_galint_new_q10324_04.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		L2PcInstance player = st.getPlayer();
		QuestState prevSt = player.getQuestState(_10323_GoingIntoARealWar.class);

		if(npcId == SHENON)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "si_illusion_shannon_q10324_03.htm";
				case CREATED:
					if(player.getLevel() < 20 && prevSt != null && prevSt.isCompleted())
					{
						return "si_illusion_shannon_q10324_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_shannon_q10324_02.htm";
					}
				case STARTED:
					return "si_illusion_shannon_q10324_06.htm";
			}
		}
		else if(npcId == GALLINT)
		{
			if(st.isCompleted())
			{
				return "si_galint_new_q10324_02.htm";
			}
			if(st.getCond() == 1)
			{
				return "si_galint_new_q10324_03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10323_GoingIntoARealWar.class);
		return previous != null && previous.isCompleted() && player.getLevel() < 20;

	}
}