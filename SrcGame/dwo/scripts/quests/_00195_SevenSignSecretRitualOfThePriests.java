package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00195_SevenSignSecretRitualOfThePriests extends Quest
{
	// Квестовые персонажи
	private static final int ClaudiaAthebalt = 31001;
	private static final int John = 32576;
	private static final int Raymond = 30289;
	private static final int IasonHeine = 30969;
	private static final int Black = 32579;

	// Квестовые предметы
	private static final int EmperorShunaimanContract = 13823;
	private static final int IdentityCard = 13822;

	// Скилы трансформаций
	private static final int GuardofDawn = 6204;
	private static final SkillHolder transform = new SkillHolder(GuardofDawn, 1);

	public _00195_SevenSignSecretRitualOfThePriests()
	{
		addStartNpc(ClaudiaAthebalt);
		addTalkId(ClaudiaAthebalt, John, Raymond, Black, IasonHeine);
		questItemIds = new int[]{IdentityCard, EmperorShunaimanContract};
	}

	public static void main(String[] args)
	{
		new _00195_SevenSignSecretRitualOfThePriests();
	}

	@Override
	public int getQuestId()
	{
		return 195;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		switch(event)
		{
			case "31001-02.htm":
				st.setState(STARTED);
				break;
			case "31001-05.htm":
				st.setCond(1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32576-02.htm":
				st.giveItems(IdentityCard, 1);
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30289-04.htm":
				player.stopAllEffects();
				npc.setTarget(player);
				npc.doCast(transform.getSkill());
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30289-09.htm":
				player.stopAllEffects();
				npc.setTarget(player);
				npc.doCast(transform.getSkill());
				break;
			case "30289-07.htm":
				if(player.getFirstEffect(GuardofDawn) != null)
				{
					player.stopAllEffects();
				}
				else
				{
					return "30289-07a.htm";
				}
				break;
			case "30969-03.htm":
				if(player.isSubClassActive())
				{
					return "subclass_forbidden.htm";
				}
				st.addExpAndSp(10000000, 2500000);
				st.unset("cond");
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				break;
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		QuestState st1 = player.getQuestState(_00194_SevenSignContractOfMammon.class);
		String htmltext = getNoQuestMsg(player);
		if(st == null || player.getLevel() < 79)
		{
			return htmltext;
		}
		int cond = st.getCond();
		if(st.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
		}
		else if(npc.getNpcId() == ClaudiaAthebalt)
		{
			if(st1 != null && st1.isCompleted())
			{
				if(cond == 0)
				{
					if(player.getLevel() >= 79)
					{
						htmltext = "31001-01.htm";
					}
					else
					{
						htmltext = "31001-0a.htm";
						st.exitQuest(QuestType.REPEATABLE);
					}
				}
				else if(cond == 1)
				{
					htmltext = "31001-06.htm";
				}
			}
			else
			{
				return "31001-0a.htm";
			}
		}
		else if(npc.getNpcId() == John)
		{
			if(cond == 1)
			{
				htmltext = "32576-01.htm";
			}
			else if(cond == 2)
			{
				htmltext = "32576-03.htm";
			}
		}
		else if(npc.getNpcId() == Raymond)
		{
			if(cond == 2)
			{
				htmltext = "30289-01.htm";
			}
			else if(cond == 3)
			{
				if(player.getItemsCount(EmperorShunaimanContract) > 0)
				{
					htmltext = "30289-08.htm";
					player.stopAllEffects();
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(4);
				}
				else
				{
					htmltext = "30289-06.htm";
				}
			}
			else if(cond == 4)
			{
				htmltext = "30289-12.htm";
			}
		}
		else if(npc.getNpcId() == IasonHeine && cond == 4)
		{
			htmltext = "30969-01.htm";
		}
		return htmltext;
	}
}