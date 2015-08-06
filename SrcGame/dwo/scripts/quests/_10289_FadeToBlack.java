package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Util;

public class _10289_FadeToBlack extends Quest
{
	// NPCs
	private static final int GREYMORE = 32757;

	// Items
	private static final int MARK_OF_DARKNESS = 15528;
	private static final int MARK_OF_SPLENDOR = 15527;

	//MOBs
	private static final int ANAYS = 25701;

	public _10289_FadeToBlack()
	{
		addStartNpc(GREYMORE);
		addTalkId(GREYMORE);
		addKillId(ANAYS);
	}

	public static void main(String[] args)
	{
		new _10289_FadeToBlack();
	}

	@Override
	public int getQuestId()
	{
		return 10289;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(npc.getNpcId() == GREYMORE)
		{
			if(event.equalsIgnoreCase("32757-04.htm"))
			{
				st.startQuest();
			}
			else if(Util.isDigit(event) && st.getQuestItemsCount(MARK_OF_SPLENDOR) > 0)
			{
				int itemId = Integer.parseInt(event);
				st.takeItems(MARK_OF_SPLENDOR, 1);
				st.giveItems(itemId, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "32757-08.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "1");

		if(partyMember == null)
		{
			return super.onKill(npc, player, isPet);
		}

		QuestState st = partyMember.getQuestState(getClass());

		if(st != null)
		{
			st.giveItems(MARK_OF_SPLENDOR, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			st.setCond(3);
		}

		if(player.isInParty())
		{
			QuestState st2;
			for(L2PcInstance pmember : player.getParty().getMembers())
			{
				st2 = pmember.getQuestState(getClass());

				if(st2 != null && st2.getCond() == 1 && pmember.getObjectId() != partyMember.getObjectId())
				{
					st2.giveItems(MARK_OF_DARKNESS, 1);
					st2.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					st2.setCond(2);
				}
			}
		}

		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		QuestState secretMission = player.getQuestState(_10288_SecretMission.class);

		if(npc.getNpcId() == GREYMORE)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 82 && secretMission != null && secretMission.isCompleted())
					{
						return "32757-02.htm";
					}
					else
					{
						return player.getLevel() < 82 ? "32757-00.htm" : "32757-01.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "32757-04b.htm";
					}
					if(st.getCond() == 2 && st.getQuestItemsCount(MARK_OF_DARKNESS) > 0)
					{
						st.takeItems(MARK_OF_DARKNESS, 1);
						player.addExpAndSp(55983, 136500);
						st.setCond(1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "32757-05.htm";
					}
					if(st.getCond() == 3)
					{
						return "32757-06.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState secretMission = player.getQuestState(_10288_SecretMission.class);
		return secretMission != null && secretMission.isCompleted() && player.getLevel() >= 82;
	}
}