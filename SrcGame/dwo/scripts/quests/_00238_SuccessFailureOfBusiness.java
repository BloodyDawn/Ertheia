package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00238_SuccessFailureOfBusiness extends Quest
{
	//NPCs
	private static final int DrHelvetica = 32641;

	//Monsters
	private static final int BrazierOfPurity = 18806;
	private static final int GuardianSpiritsOfMagicForce = 22659;
	private static final int EvilSpiritsInMagicForce = 22658;

	//ITEMS
	private static final int BrokenPieveOfMagicForce = 14867;
	private static final int GuardianSpiritFragment = 14868;
	private static final int VicinityOfTheFieldOfSilenceResearchCenter = 14865;

	public _00238_SuccessFailureOfBusiness()
	{
		addStartNpc(DrHelvetica);
		addTalkId(DrHelvetica);
		addKillId(BrazierOfPurity, GuardianSpiritsOfMagicForce, EvilSpiritsInMagicForce);
		questItemIds = new int[]{BrokenPieveOfMagicForce, GuardianSpiritFragment};
	}

	public static void main(String[] args)
	{
		new _00238_SuccessFailureOfBusiness();
	}

	@Override
	public int getQuestId()
	{
		return 238;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		if(event.equals("32641-2.htm"))
		{
			st.startQuest();
		}
		if(event.equals("32641-4.htm"))
		{
			st.setCond(3);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMemberState(player, STARTED);
		if(partyMember == null)
		{
			return null;
		}
		QuestState st = partyMember.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		int cond = st.getCond();
		if(npc.getNpcId() == BrazierOfPurity)
		{
			if(cond == 1)
			{
				int count = 1;
				int chance = 5;
				while(chance > 1000)
				{
					chance -= 1000;
					count++;
				}
				if(st.getRandom(1000) <= chance)
				{
					st.giveItems(BrokenPieveOfMagicForce, count);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					if(st.getQuestItemsCount(BrokenPieveOfMagicForce) == 10)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}
		}
		else if(npc.getNpcId() == GuardianSpiritsOfMagicForce || npc.getNpcId() == EvilSpiritsInMagicForce)
		{
			if(cond == 3)
			{
				int count = 1;
				int chance = 5;
				while(chance > 1000)
				{
					chance -= 1000;
					count++;
				}
				if(st.getRandom(1000) <= chance)
				{
					st.giveItems(GuardianSpiritFragment, count);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					if(st.getQuestItemsCount(GuardianSpiritFragment) == 20)
					{
						st.setCond(4);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		int cond = st.getCond();
		if(st.getState() == COMPLETED)
		{
			return "32641-5a.htm";
		}
		if(npc.getNpcId() == DrHelvetica)
		{
			if(cond == 0)
			{
				if(!st.hasQuestItems(VicinityOfTheFieldOfSilenceResearchCenter))
				{
					return "32641-6.htm";
				}
				else if(player.getLevel() >= 82)
				{
					return "32641-0.htm";
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "32641-0a.htm";
				}
			}
			else if(cond == 1)
			{
				return "32641-2a.htm";
			}
			else if(cond == 2)
			{
				st.takeItems(BrokenPieveOfMagicForce, 10);
				return "32641-3.htm";
			}
			else if(cond == 3)
			{
				return "32641-4a.htm";
			}
			else if(cond == 4)
			{
				st.giveAdena(283346, true);
				st.takeItems(VicinityOfTheFieldOfSilenceResearchCenter, -1);
				st.takeItems(GuardianSpiritFragment, 20);
				st.addExpAndSp(1319736, 103553);
				st.unset("cond");
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "32641-5.htm";
			}
		}
		return null;
	}
}