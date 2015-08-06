package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 24.04.12
 * Time: 23:02
 */

public class _10318_DecayingDarkness extends Quest
{
	// Квестовые персонажи
	private static final int Типия = 32892;

	// Квестовые предметы
	private static final int ПроклятаяСлизь = 17733;

	// Квестовые монстры
	private static final int[] ДревниеГерои = {18982, 18983};

	public _10318_DecayingDarkness()
	{

		addStartNpc(Типия);
		addTalkId(Типия);
		addKillId(ДревниеГерои);
		questItemIds = new int[]{ПроклятаяСлизь};
	}

	public static void main(String[] args)
	{
		new _10318_DecayingDarkness();
	}

	@Override
	public int getQuestId()
	{
		return 10318;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "orbis_typia_q10318_07.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == Типия)
		{
			switch(reply)
			{
				case 1:
					return "orbis_typia_q10318_04.htm";
				case 2:
					return "orbis_typia_q10318_05.htm";
				case 3:
					return "orbis_typia_q10318_06.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 1)
		{
			if(ArrayUtils.contains(ДревниеГерои, npc.getNpcId()))
			{
				if(killer.getParty() == null)
				{
					st.giveItem(ПроклятаяСлизь);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					if(st.getQuestItemsCount(ПроклятаяСлизь) >= 8)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
				else
				{
					QuestState pst = killer.getParty().getRandomPartyMember().getQuestState(getClass());
					if(pst != null && pst.getCond() == 1)
					{
						pst.giveItem(ПроклятаяСлизь);
						pst.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						if(pst.getQuestItemsCount(ПроклятаяСлизь) >= 8)
						{
							pst.setCond(2);
							pst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
					}
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Типия)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "orbis_typia_q10318_03.htm";
				case CREATED:
					QuestState previous = player.getQuestState(_10317_OrbisWitch.class);
					if(previous == null || !previous.isCompleted() || player.getLevel() < 95)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "orbis_typia_q10318_02.htm";
					}
					else
					{
						return "orbis_typia_q10318_01.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return !st.hasQuestItems(ПроклятаяСлизь) ? "orbis_typia_q10318_08.htm" : "orbis_typia_q10318_09.htm";
					}
					else if(st.getCond() == 2 && st.getQuestItemsCount(ПроклятаяСлизь) >= 8)
					{
						st.addExpAndSp(79260650, 36253450);
						st.giveAdena(5427900, true);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						return "orbis_typia_q10318_10.htm";
					}
			}
		}
		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10317_OrbisWitch.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 95;

	}
}
