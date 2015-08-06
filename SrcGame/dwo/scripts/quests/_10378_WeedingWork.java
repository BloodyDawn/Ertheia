package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 31.08.12
 * Time: 14:28
 */

public class _10378_WeedingWork extends Quest
{
	// Квестовые персонажи
	private static final int Dadfena = 33697;

	// Квестовые монстры
	private static final int[] Mandragoras = {23210, 23211};

	// Квестовые предметы
	private static final int MandragoraStalk = 34974; // TODO
	private static final int MandragoraRoot = 34975; // TODO

	// Квестовые награды
	private static final int TPScroll = 35292; // TODO: Реализовать предмет

	public _10378_WeedingWork()
	{
		addStartNpc(Dadfena);
		addTalkId(Dadfena);
		addKillId(Mandragoras);
		questItemIds = new int[]{MandragoraRoot, MandragoraStalk};
	}

	public static void main(String[] args)
	{
		new _10378_WeedingWork();
	}

	@Override
	public int getQuestId()
	{
		return 10378;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return null;
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(ArrayUtils.contains(Mandragoras, npc.getNpcId()))
		{
			QuestState st;
			L2Party party = player.getParty();
			if(party != null)
			{
				for(L2PcInstance member : party.getMembersInRadius(player, 900))
				{
					st = member.getQuestState(getClass());
					if(st != null && st.getCond() == 1)
					{
						if(npc.getNpcId() == Mandragoras[0])
						{
							if(st.getQuestItemsCount(MandragoraStalk) < 5)
							{
								st.giveItem(MandragoraStalk);
								st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
						else
						{
							if(st.getQuestItemsCount(MandragoraRoot) < 5)
							{
								st.giveItem(MandragoraRoot);
								st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
						if(st.getQuestItemsCount(MandragoraRoot) + st.getQuestItemsCount(MandragoraStalk) >= 10)
						{
							st.setCond(2);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
					}
				}
			}
			else
			{
				st = player.getQuestState(getClass());
				if(st != null && st.getCond() == 1)
				{
					if(npc.getNpcId() == Mandragoras[0])
					{
						if(st.getQuestItemsCount(MandragoraStalk) < 5)
						{
							st.giveItem(MandragoraStalk);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					else
					{
						if(st.getQuestItemsCount(MandragoraRoot) < 5)
						{
							st.giveItem(MandragoraRoot);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					if(st.getQuestItemsCount(MandragoraRoot) + st.getQuestItemsCount(MandragoraStalk) >= 10)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Dadfena)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() >= 95 ? "001.htm" : "np-level.htm";
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "go-fuck-mobs.htm";
						case 2:
							return "reward.htm";
					}
				case COMPLETED:
					return "completed.htm";
			}
		}

		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 95;
	}
}