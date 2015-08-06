package dwo.scripts.quests;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.TutorialShowQuestionMark;
import dwo.gameserver.util.Util;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 20.03.12
 * Time: 21:39
 */

public class _10343_DayOfDestinyDarkElfsFate extends Quest
{
	// Quest Npc
	private static final int OLTRAN = 30862;
	private static final int QUARTERMASTER = 33407;
	private static final int ADEN_VANGUARD_CORPSE_1 = 33166;
	private static final int ADEN_VANGUARD_CORPSE_2 = 33167;
	private static final int ADEN_VANGUARD_CORPSE_3 = 33168;
	private static final int ADEN_VANGUARD_CORPSE_4 = 33169;
	private static final int ADEN_VANGUARD_MEMBER = 33165;

	// Quest Items
	private static final int VANGUARD_SOLDIER_DOG_TAGS = 17748;

	public _10343_DayOfDestinyDarkElfsFate()
	{
		addStartNpc(OLTRAN);
		addTalkId(OLTRAN, QUARTERMASTER, ADEN_VANGUARD_MEMBER, ADEN_VANGUARD_CORPSE_1, ADEN_VANGUARD_CORPSE_2, ADEN_VANGUARD_CORPSE_3, ADEN_VANGUARD_CORPSE_4);
		addEventId(HookType.ON_LEVEL_INCREASE);
		addEventId(HookType.ON_ENTER_WORLD);
		questItemIds = new int[]{VANGUARD_SOLDIER_DOG_TAGS};
	}

	public static void main(String[] args)
	{
		new _10343_DayOfDestinyDarkElfsFate();
	}

	public void cast(L2Npc npc, L2Character target, int skillId, int level)
	{
		target.broadcastPacket(new MagicSkillUse(target, target, skillId, level, 6000, 1));
		target.broadcastPacket(new MagicSkillUse(npc, npc, skillId, level, 6000, 1));
	}

	@Override
	public int getQuestId()
	{
		return 10343;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.getPlayer().getVariablesController().get("_3rd_profession_ci" + qs.getPlayer().getClassIndex(), Boolean.class, false))
		{
			qs.startQuest();
			return "grandmaster_oltlin_q10343_07.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(player.getLevel() < 76 || !Util.hasChildClassWithRace(player.getClassId(), Race.DarkElf) || player.getClassId().level() >= ClassLevel.THIRD.ordinal())
		{
			return null;
		}
		if(player.getClassId().level() < ClassLevel.SECOND.ordinal())
		{
			return null;
		}

		int thirdClass;

		int npcId = npc.getNpcId();
		int cond = st.getCond();
		switch(npcId)
		{
			case OLTRAN:
				if(reply == 1)
				{
					return "grandmaster_oltlin_q10343_04.htm";
				}
				if(reply == 2)
				{
					return "grandmaster_oltlin_q10343_05.htm";
				}
				if(reply == 3)
				{
					return "grandmaster_oltlin_q10343_06.htm";
				}
				if(reply == 11 && cond == 13)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.addExpAndSp(2050000, 0);
					st.giveAdena(5000000, true);
					st.giveItem(6622);
					st.giveItem(9570);

					thirdClass = Util.getThirdClassForId(player.getClassId().getId());
					int prevClassId = player.getClassId().getId();
					player.setClassId(thirdClass);
					if(!player.isSubClassActive() && player.getBaseClassId() == prevClassId)
					{
						player.setBaseClassId(thirdClass);
					}

					cast(npc, player, 4339, 1);

					player.broadcastUserInfo();
					player.getVariablesController().set("_3rd_profession_ci" + player.getClassIndex(), true);
					st.exitQuest(QuestType.ONE_TIME);
					return "grandmaster_oltlin_q10343_11.htm";
				}
				if(reply == 12 && cond == 13)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.addExpAndSp(2050000, 0);
					st.giveAdena(5000000, true);
					st.giveItem(6622);
					st.giveItem(9571);

					thirdClass = Util.getThirdClassForId(player.getClassId().getId());
					int prevClassId = player.getClassId().getId();
					player.setClassId(thirdClass);
					if(!player.isSubClassActive() && player.getBaseClassId() == prevClassId)
					{
						player.setBaseClassId(thirdClass);
					}

					cast(npc, player, 4339, 1);

					player.broadcastUserInfo();
					player.getVariablesController().set("_3rd_profession_ci" + player.getClassIndex(), true);
					st.exitQuest(QuestType.ONE_TIME);
					return "grandmaster_oltlin_q10343_11.htm";
				}
				if(reply == 13 && cond == 13)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.addExpAndSp(2050000, 0);
					st.giveAdena(5000000, true);
					st.giveItem(6622);
					st.giveItem(9572);

					thirdClass = Util.getThirdClassForId(player.getClassId().getId());
					int prevClassId = player.getClassId().getId();
					player.setClassId(thirdClass);
					if(!player.isSubClassActive() && player.getBaseClassId() == prevClassId)
					{
						player.setBaseClassId(thirdClass);
					}

					cast(npc, player, 4339, 1);

					player.broadcastUserInfo();
					player.getVariablesController().set("_3rd_profession_ci" + player.getClassIndex(), true);
					st.exitQuest(QuestType.ONE_TIME);
					return "grandmaster_oltlin_q10343_11.htm";
				}
				break;
			case QUARTERMASTER:
				if(reply == 1 && cond == 1)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "aden_assult_01_3rd_q10343_02.htm";
				}
				if(reply == 2 && cond == 3)
				{
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "aden_assult_01_3rd_q10343_05.htm";
				}
				break;
			case ADEN_VANGUARD_CORPSE_1:
				if(st.getBool(String.valueOf(npc.getNpcId())))
				{
					return "sol_corpse1_3rd_q10343_03.htm";
				}
				else
				{
					st.set(String.valueOf(npc.getNpcId()), "1");
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					st.giveItem(VANGUARD_SOLDIER_DOG_TAGS);
					if(st.getQuestItemsCount(VANGUARD_SOLDIER_DOG_TAGS) == 4)
					{
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					return "sol_corpse1_3rd_q10343_02.htm";
				}
			case ADEN_VANGUARD_CORPSE_2:
				if(st.getBool(String.valueOf(npc.getNpcId())))
				{
					return "sol_corpse2_3rd_q10343_03.htm";
				}
				else
				{
					st.set(String.valueOf(npc.getNpcId()), "1");
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					st.giveItem(VANGUARD_SOLDIER_DOG_TAGS);
					if(st.getQuestItemsCount(VANGUARD_SOLDIER_DOG_TAGS) == 4)
					{
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					return "sol_corpse2_3rd_q10343_02.htm";
				}
			case ADEN_VANGUARD_CORPSE_3:
				if(st.getBool(String.valueOf(npc.getNpcId())))
				{
					return "sol_corpse3_3rd_q10343_03.htm";
				}
				else
				{
					st.set(String.valueOf(npc.getNpcId()), "1");
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					st.giveItem(VANGUARD_SOLDIER_DOG_TAGS);
					if(st.getQuestItemsCount(VANGUARD_SOLDIER_DOG_TAGS) == 4)
					{
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					return "sol_corpse3_3rd_q10343_02.htm";
				}
			case ADEN_VANGUARD_CORPSE_4:
				if(st.getBool(String.valueOf(npc.getNpcId())))
				{
					return "sol_corpse4_3rd_q10343_03.htm";
				}
				else
				{
					st.set(String.valueOf(npc.getNpcId()), "1");
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					st.giveItem(VANGUARD_SOLDIER_DOG_TAGS);
					if(st.getQuestItemsCount(VANGUARD_SOLDIER_DOG_TAGS) == 4)
					{
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					return "sol_corpse4_3rd_q10343_02.htm";
				}
			case ADEN_VANGUARD_MEMBER:
				if(reply == 1)
				{
					// TODO: Тут идет инстанс, временно затычка (сразу 13 конд).
					st.setCond(13);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "sol_3rd_enter_q10343_02.htm";
				}
				break;
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == OLTRAN)
		{
			switch(st.getState())
			{
				case COMPLETED:
					if(player.getVariablesController().get("_3rd_profession_ci" + player.getClassIndex(), Boolean.class, false))
					{
						return "grandmaster_oltlin_q10343_02.htm";
					}
					else
					{
						if(player.getLevel() < 76 || !Util.hasChildClassWithRace(player.getClassId(), Race.DarkElf) || player.getClassId().level() >= ClassLevel.THIRD.ordinal())
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "grandmaster_oltlin_q10343_02.htm";
						}
						else if(player.getClassId().level() < ClassLevel.SECOND.ordinal())
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "grandmaster_oltlin_q10343_10.htm";
						}
						else
						{
							return "grandmaster_oltlin_q10343_01.htm";
						}
					}
				case CREATED:
					if(player.getVariablesController().get("_3rd_profession_ci" + player.getClassIndex(), Boolean.class, false) || player.getLevel() < 76 || !Util.hasChildClassWithRace(player.getClassId(), Race.DarkElf) || player.getClassId().level() >= ClassLevel.THIRD.ordinal())
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "grandmaster_oltlin_q10343_02.htm";
					}
					else if(player.getClassId().level() < ClassLevel.SECOND.ordinal())
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "grandmaster_oltlin_q10343_10.htm";
					}
					else
					{
						return "grandmaster_oltlin_q10343_01.htm";
					}
				case STARTED:
					switch(st.getCond())
					{
						case 1:
						case 2:
						case 3:
						case 4:
							return "grandmaster_oltlin_q10343_08.htm";
						case 13:
							return "grandmaster_oltlin_q10343_09.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == QUARTERMASTER)
		{
			if(st.isStarted())
			{
				switch(st.getCond())
				{
					case 1:
						return "aden_assult_01_3rd_q10343_01.htm";
					case 2:
						return "aden_assult_01_3rd_q10343_03.htm";
					case 3:
						return "aden_assult_01_3rd_q10343_04.htm";
					case 4:
						return "aden_assult_01_3rd_q10343_06.htm";
				}
			}
		}
		else if(npc.getNpcId() == ADEN_VANGUARD_MEMBER)
		{
			if(st.isStarted())
			{
				if(st.getCond() >= 4 && st.getCond() < 13)
				{
					return "sol_3rd_enter_q10343_01.htm";
				}
			}
		}

		if(st.isStarted() && st.getCond() >= 2)
		{
			switch(npc.getNpcId())
			{
				case ADEN_VANGUARD_CORPSE_1:
					return "sol_corpse1_3rd_q10343_01.htm";
				case ADEN_VANGUARD_CORPSE_2:
					return "sol_corpse2_3rd_q10343_01.htm";
				case ADEN_VANGUARD_CORPSE_3:
					return "sol_corpse3_3rd_q10343_01.htm";
				case ADEN_VANGUARD_CORPSE_4:
					return "sol_corpse4_3rd_q10343_01.htm";
			}
		}
		return null;
	}

	@Override
	public void onLevelIncreased(L2PcInstance player)
	{
		if(player.getLevel() >= 76 && Util.hasChildClassWithRace(player.getClassId(), Race.DarkElf) && player.getClassId().level() < ClassLevel.THIRD.ordinal())
		{
			QuestState st = player.getQuestState(getClass());
			if(st == null)
			{
				player.sendPacket(new TutorialShowQuestionMark(103));
			}
		}
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		if(player.getLevel() >= 76 && Util.hasChildClassWithRace(player.getClassId(), Race.DarkElf) && player.getClassId().level() < ClassLevel.THIRD.ordinal())
		{
			QuestState st = player.getQuestState(getClass());
			if(st == null)
			{
				player.sendPacket(new TutorialShowQuestionMark(103));
			}
		}
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 76 && Util.hasChildClassWithRace(player.getClassId(), Race.DarkElf) && player.getClassId().level() < ClassLevel.THIRD.ordinal() && !player.getVariablesController().get("_3rd_profession_ci" + player.getClassIndex(), Boolean.class, false);
	}
}