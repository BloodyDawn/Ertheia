package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.08.12
 * Time: 23:48
 */

public class _10383_FergasonsOffer extends Quest
{
	// Квестовые персонажи
	private static final int Сизрак = 33669;
	private static final int Аку = 33671;
	private static final int Фергюсон = 33681;

	// Квестовые предметы
	private static final int ПроклятаяПетра = 34958;

	// Квестовые монстры
	private static final int[] Монстры = {23213, 23214, 23215, 23216, 23217, 23218, 23219};

	public _10383_FergasonsOffer()
	{
		addStartNpc(Сизрак);
		addTalkId(Сизрак, Аку, Фергюсон);
		addKillId(Монстры);
		questItemIds = new int[]{ПроклятаяПетра};
	}

	public static void main(String[] args)
	{
		new _10383_FergasonsOffer();
	}

	@Override
	public int getQuestId()
	{
		return 10383;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "sofa_sizraku_q10383_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Сизрак)
		{
			if(reply == 1)
			{
				return "sofa_sizraku_q10383_02.htm";
			}
		}
		else if(npcId == Фергюсон)
		{
			if(reply == 1 && cond == 1)
			{
				return "maestro_ferguson_q10383_02.htm";
			}
			else if(reply == 2 && cond == 1)
			{
				return "maestro_ferguson_q10383_03.htm";
			}
			else if(reply == 3 && cond == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "maestro_ferguson_q10383_04.htm";
			}
		}
		else if(npcId == Аку)
		{
			if(reply == 1 && cond == 3)
			{
				st.addExpAndSp(951127800, 435041400);
				st.giveAdena(3256740, true);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "sofa_aku_q10383_03.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2Party party = st.getPlayer().getParty();
		if(party != null)
		{
			for(L2PcInstance partyMember : party.getMembersInRadius(st.getPlayer(), 900))
			{
				QuestState pst = partyMember.getQuestState(getClass());
				if(pst != null && pst.isStarted() && pst.getCond() == 2)
				{
					pst.giveItem(ПроклятаяПетра);
					if(pst.getQuestItemsCount(ПроклятаяПетра) >= 20)
					{
						pst.setCond(3);
						pst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					else
					{
						pst.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
			}
		}
		else
		{
			if(st.getCond() == 2)
			{
				st.giveItem(ПроклятаяПетра);
				if(st.getQuestItemsCount(ПроклятаяПетра) >= 20)
				{
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Сизрак)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "sofa_sizraku_q10383_05.htm";
				case CREATED:
					QuestState pst = st.getPlayer().getQuestState(_10381_TotheSeedofHellfire.class);
					if(pst == null || !pst.isCompleted())
					{
						return "sofa_sizraku_q10383_07.htm";
					}
					else
					{
						return st.getPlayer().getLevel() < 97 ? "sofa_sizraku_q10383_04.htm" : "sofa_sizraku_q10383_01.htm";
					}
				case STARTED:
					return "sofa_sizraku_q10383_06.htm";
			}
		}
		else if(npcId == Фергюсон)
		{
			if(st.isStarted())
			{
				if(cond == 1)
				{
					return "maestro_ferguson_q10383_01.htm";
				}
				else if(cond == 2)
				{
					return "maestro_ferguson_q10383_05.htm";
				}
			}
		}
		else if(npcId == Аку)
		{
			if(st.isStarted())
			{
				if(cond == 2)
				{
					return "sofa_aku_q10383_01.htm";
				}
				else if(cond == 3)
				{
					return "sofa_aku_q10383_02.htm";
				}
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pst = player.getQuestState(_10381_TotheSeedofHellfire.class);
		return player.getLevel() >= 97 && pst != null && pst.isCompleted();
	}
}