package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class _00111_ElrokianHuntersProof extends Quest
{
	// НПЦ: MARQUEZ,MUSHIKA,ASHAMAH,KIRIKASHIN
	private static final int[] QUEST_NPC = {32113, 32114, 32115, 32116, 32117};

	// Фрагменты
	private static final int Fragment = 8768;

	private static final int[] QUEST_MONSTERS1 = {22196, 22197, 22198, 22218};
	private static final int[] QUEST_MONSTERS2 = {22200, 22201, 22202, 22219};
	private static final int[] QUEST_MONSTERS3 = {22208, 22209, 22210, 22221};
	private static final int[] QUEST_MONSTERS4 = {22203, 22204, 22205, 22220};

	public _00111_ElrokianHuntersProof()
	{
		addStartNpc(QUEST_NPC[0]);
		addTalkId(QUEST_NPC);
		addKillId(QUEST_MONSTERS1);
		addKillId(QUEST_MONSTERS2);
		addKillId(QUEST_MONSTERS3);
		addKillId(QUEST_MONSTERS4);
		questItemIds = new int[]{Fragment};
	}

	public static void main(String[] args)
	{
		new _00111_ElrokianHuntersProof();
	}

	@Override
	public int getQuestId()
	{
		return 111;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(player == null || player.getParty() == null)
		{
			return super.onKill(npc, player, isPet);
		}

		QuestState st = player.getParty().getLeader().getQuestState(getClass());
		if(st == null || st.getState() != STARTED)
		{
			return super.onKill(npc, player, isPet);
		}

		int cond = st.getCond();
		int npcId = npc.getNpcId();

		switch(cond)
		{
			case 4:
				if(ArrayUtils.contains(QUEST_MONSTERS1, npcId))
				{
					if(Rnd.getChance(25))
					{
						st.giveItems(Fragment, 1);
						if(st.getQuestItemsCount(Fragment) <= 49)
						{
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
						else
						{
							st.setCond(5);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
					}
				}
				break;
			case 10:
				if(ArrayUtils.contains(QUEST_MONSTERS2, npcId))
				{
					if(Rnd.getChance(75))
					{
						st.giveItems(8770, 1);
						if(st.getQuestItemsCount(8770) <= 9)
						{
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
				}
				else if(ArrayUtils.contains(QUEST_MONSTERS3, npcId))
				{
					if(Rnd.getChance(75))
					{
						st.giveItems(8772, 1);
						if(st.getQuestItemsCount(8771) <= 9)
						{
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
				}
				else if(ArrayUtils.contains(QUEST_MONSTERS4, npcId))
				{
					if(Rnd.getChance(75))
					{
						st.giveItems(8771, 1);
						if(st.getQuestItemsCount(8772) <= 9)
						{
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
				}

				if(st.getQuestItemsCount(8770) >= 10 && st.getQuestItemsCount(8771) >= 10 && st.getQuestItemsCount(8772) >= 10)
				{
					st.setCond(11);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				break;
		}

		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		int cond = st.getCond();
		int npcId = npc.getNpcId();

		switch(st.getState())
		{
			case COMPLETED:
				return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
			case CREATED:
				if(npcId == QUEST_NPC[0])
				{
					if(checkPartyCondition(player))
					{
						st.startQuest();
						return "32113-1.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "<html><body>Этот квест может быть взять только партией игроков 75 уровня и выше. Только лидер партии должен говорить со мной.</body></html>";
					}
				}
				break;
			case STARTED:
				if(npcId == QUEST_NPC[0])
				{
					switch(cond)
					{
						case 3:
							st.setCond(4);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "32113-2.htm";
						case 5:
							if(st.getQuestItemsCount(Fragment) >= 50)
							{
								st.takeItems(Fragment, -1);
								st.setCond(6);
								st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
								return "32113-3.htm";
							}
							break;
					}
				}
				else if(npcId == QUEST_NPC[1])
				{
					if(cond == 1)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "32114-1.htm";
					}
				}
				else if(npcId == QUEST_NPC[2])
				{
					switch(cond)
					{
						case 2:
							st.setCond(3);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "32115-1.htm";
						case 8:
							st.setCond(9);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "32115-2.htm";
						case 9:
							st.setCond(10);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "32115-3.htm";
						case 11:
							st.setCond(12);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.giveItems(8773, 1);
							return "32115-5.htm";
					}
				}
				else if(npcId == QUEST_NPC[3])
				{
					switch(cond)
					{
						case 6:
							st.setCond(8);
							st.playSound(QuestSound.ETCSOUND_ELROKI_SOUND_FULL);
							return "32116-1.htm";
						case 12:
							if(st.hasQuestItems(8773))
							{
								st.takeItems(8773, 1);
								st.giveItems(8763, 1);
								st.giveItems(8764, 100);
								st.giveAdena(1022636, true);
								st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
								st.exitQuest(QuestType.ONE_TIME);
								return "32116-2.htm";
							}
							break;
					}
				}
				break;
		}
		return null;
	}

	private boolean checkPartyCondition(L2PcInstance leader)
	{
		if(leader == null)
		{
			return false;
		}

		L2Party party = leader.getParty();
		if(party == null)
		{
			return false;
		}

		if(!party.getLeader().equals(leader))
		{
			return false;
		}

		for(L2PcInstance player : party.getMembers())
		{
			if(player.getLevel() < 75)
			{
				return false;
			}
		}

		return true;
	}
}