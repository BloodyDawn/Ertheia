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
 * Date: 06.11.12
 * Time: 20:40
 */

public class _10283_RequestOfIceMerchant extends Quest
{
	// Квестовые персонажи
	private static final int Rafforty = 32020;
	private static final int Kier = 32022;
	private static final int Jinia = 32760;

	// ObjectID игрока, который в данный момент говорит с Kier
	private static int currentTalkingWithKier = -1;

	public _10283_RequestOfIceMerchant()
	{
		addStartNpc(Rafforty);
		addTalkId(Rafforty, Kier, Jinia);
	}

	/*@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("resetCurrentTalkingWithKier"))
		{
			currentTalkingWithKier = -1;
		}
		return null;
	}*/

	public static void main(String[] args)
	{
		new _10283_RequestOfIceMerchant();
	}

	@Override
	public int getQuestId()
	{
		return 10283;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("resetCurrentTalkingWithKier"))
		{
			currentTalkingWithKier = -1;
		}
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			qs.setMemoState(1);
			return "repre_q10283_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Rafforty)
		{
			switch(reply)
			{
				case 1:
					return "repre_q10283_04.htm";
				case 2:
					if(cond == 1)
					{
						return "repre_q10283_07.htm";
					}
				case 3:
					if(cond == 1)
					{
						return "repre_q10283_08.htm";
					}
				case 4:
					if(cond == 1)
					{
						st.setCond(2);
						st.setMemoState(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "repre_q10283_09.htm";
					}
			}
		}
		else if(npcId == Kier)
		{
			switch(reply)
			{
				case 1:
					if(cond == 2)
					{
						return "keier_q10283_01.htm";
					}
				case 2:
					if(cond == 2)
					{
						if(currentTalkingWithKier == -1)
						{
							st.setCond(3);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							currentTalkingWithKier = player.getObjectId();
							L2Npc jinia = addSpawn(Jinia, 104476, -107535, -3688, 44954, false, 60000);
							jinia.setOwner(player);
							startQuestTimer("resetCurrentTalkingWithKier", 60000, npc, player);
						}
						else
						{
							return currentTalkingWithKier == player.getObjectId() ? "keier_q10283_03.htm" : "keier_q10283_02.htm";
						}
					}
			}
		}
		else if(npcId == Jinia)
		{
			switch(reply)
			{
				case 1:
					if(st.getMemoState() == 2)
					{
						return "jinia_npc_q10283_01.htm";
					}
					break;
				case 2:
					if(st.getMemoState() == 2)
					{
						return "jinia_npc_q10283_02.htm";
					}
					break;
				case 3:
					if(st.getMemoState() == 2)
					{
						st.giveAdena(190000, true);
						st.addExpAndSp(627000, 50300);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						npc.getLocationController().delete(); // TODO: myself->AddFleeDesire(talker,1000000); бежать в рандомном направлении и deleteMe
						return "jinia_npc_q10283_03.htm";
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
		L2PcInstance player = st.getPlayer();

		if(npcId == Rafforty)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "repre_q10283_02.htm";
				case CREATED:
					QuestState prevst = player.getQuestState(_00115_TheOtherSideOfTruth.class);
					return player.getLevel() < 82 || prevst == null || !prevst.isCompleted() ? "repre_q10283_03.htm" : "repre_q10283_01.htm";
				case STARTED:
					switch(cond)
					{
						case 1:
							return "repre_q10283_06.htm";
						case 2:
							return "repre_q10283_10.htm";
					}
			}
		}
		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pst = player.getQuestState(_00115_TheOtherSideOfTruth.class);
		return !(player.getLevel() < 82 || pst == null || !pst.isCompleted());
	}
}