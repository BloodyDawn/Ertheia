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
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 20.03.12
 * Time: 21:41
 */

public class _10345_DayOfDestinyDwarfsFate extends Quest
{
	// Квестовые персонажи
	private static final int ИнтендантАдена = 33407;
	private static final int Феррис = 30847;
	private static final int[] Трупы = {33166, 33167, 33168, 33169};
	private static final int ЧленАвангарда = 33165;

	// Квестовые предметы
	private static final int РеликвияВоиновАдена = 17748;

	public _10345_DayOfDestinyDwarfsFate()
	{
		addStartNpc(Феррис);
		addTalkId(Феррис, ИнтендантАдена, ЧленАвангарда);
		addTalkId(Трупы);
		addEventId(HookType.ON_LEVEL_INCREASE);
		addEventId(HookType.ON_ENTER_WORLD);
		questItemIds = new int[]{РеликвияВоиновАдена};
	}

	public static void main(String[] args)
	{
		new _10345_DayOfDestinyDwarfsFate();
	}

	public void Cast(L2Npc npc, L2Character target, int skillId, int level)
	{
		target.broadcastPacket(new MagicSkillUse(target, target, skillId, level, 6000, 1));
		target.broadcastPacket(new MagicSkillUse(npc, npc, skillId, level, 6000, 1));
	}

	@Override
	public int getQuestId()
	{
		return 10345;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(player.getLevel() < 76 || !Util.hasChildClassWithRace(player.getClassId(), Race.Dwarf) || player.getClassId().level() >= ClassLevel.THIRD.ordinal())
		{
			return null;
		}
		if(player.getClassId().level() < ClassLevel.SECOND.ordinal())
		{
			return null;
		}

		int Class;
		int prevClass;
		switch(event)
		{
			case "30847-07.htm":
				st.startQuest();
				break;
			case "33407-01.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "corpse_search":
				if(st.getBool(String.valueOf(npc.getNpcId())))
				{
					event = "corpse-02.htm";
				}
				else
				{
					st.set(String.valueOf(npc.getNpcId()), "1");
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					st.giveItem(РеликвияВоиновАдена);
					if(st.getQuestItemsCount(РеликвияВоиновАдена) == 4)
					{
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					event = "corpse-01.htm";
				}
				break;
			case "33407-04.htm":
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "33165-01.htm":
				//st.unset("1");
				//st.unset("2");
				//st.setCond(5);
				//st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				//st.takeItems(Util.getThirdClassForId(player.getActiveClassId() + 17396),-1);
				//enterInstance(player);
				st.setCond(13);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "start_instance":
				st.setCond(13);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				event = null;
				break;
			case "red":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(2050000, 0);
				st.giveAdena(5000000, true);
				st.giveItem(6622);
				st.giveItem(9570);

				Class = Util.getThirdClassForId(player.getClassId().getId());
				prevClass = player.getClassId().getId();
				player.setClassId(Class);
				if(!player.isSubClassActive() && player.getBaseClassId() == prevClass)
				{
					player.setBaseClassId(Class);
				}

				Cast(npc, player, 4339, 1);

				player.broadcastUserInfo();
				player.getVariablesController().set("_3rd_profession_ci" + player.getClassIndex(), true);
				st.exitQuest(QuestType.ONE_TIME);
				return "30847-11.htm";
			case "blue":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(2050000, 0);
				st.giveAdena(5000000, true);
				st.giveItem(6622);
				st.giveItem(9571);

				Class = Util.getThirdClassForId(player.getClassId().getId());
				prevClass = player.getClassId().getId();
				player.setClassId(Class);
				if(!player.isSubClassActive() && player.getBaseClassId() == prevClass)
				{
					player.setBaseClassId(Class);
				}

				Cast(npc, player, 4339, 1);

				player.broadcastUserInfo();
				player.getVariablesController().set("_3rd_profession_ci" + player.getClassIndex(), true);
				st.exitQuest(QuestType.ONE_TIME);
				return "30847-11.htm";
			case "green":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(2050000, 0);
				st.giveAdena(5000000, true);
				st.giveItem(6622);
				st.giveItem(9572);

				Class = Util.getThirdClassForId(player.getClassId().getId());
				prevClass = player.getClassId().getId();
				player.setClassId(Class);
				if(!player.isSubClassActive() && player.getBaseClassId() == prevClass)
				{
					player.setBaseClassId(Class);
				}

				Cast(npc, player, 4339, 1);

				player.broadcastUserInfo();
				player.getVariablesController().set("_3rd_profession_ci" + player.getClassIndex(), true);
				st.exitQuest(QuestType.ONE_TIME);
				return "30847-11.htm";
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Феррис)
		{
			switch(st.getState())
			{
				case COMPLETED:
					if(player.getVariablesController().get("_3rd_profession_ci" + player.getClassIndex(), Boolean.class, false))
					{
						return "30847-02.htm";
					}
					else
					{
						if(player.getLevel() < 76 || !Util.hasChildClassWithRace(player.getClassId(), Race.Dwarf) || player.getClassId().level() >= ClassLevel.THIRD.ordinal())
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "30847-02.htm";
						}
						else if(player.getClassId().level() < ClassLevel.SECOND.ordinal())
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "30847-10.htm";
						}
						else
						{
							return "30847-01.htm";
						}
					}
				case CREATED:
					if(player.getVariablesController().get("_3rd_profession_ci" + player.getClassIndex(), Boolean.class, false) || player.getLevel() < 76 || !Util.hasChildClassWithRace(player.getClassId(), Race.Dwarf) || player.getClassId().level() >= ClassLevel.THIRD.ordinal())
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30847-02.htm";
					}
					else if(player.getClassId().level() < ClassLevel.SECOND.ordinal())
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30847-10.htm";
					}
					else
					{
						return "30847-01.htm";
					}
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "30847-08.htm";
						case 13:
							return "30847-09.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == ИнтендантАдена)
		{
			if(st.isStarted())
			{
				switch(st.getCond())
				{
					case 1:
						return "33407-00.htm";
					case 2:
						return "33407-02.htm";
					case 3:
						return "33407-03.htm";
					case 4:
						return "33407-04.htm";
				}
			}
		}
		else if(ArrayUtils.contains(Трупы, npc.getNpcId()))
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					return "corpse.htm";
				}
			}
		}
		else if(npc.getNpcId() == ЧленАвангарда)
		{
			if(st.isStarted())
			{
				if(st.getCond() >= 4 && st.getCond() < 13)
				{
					return "33165-00.htm";
				}
			}
		}
		return null;
	}

	@Override
	public void onLevelIncreased(L2PcInstance player)
	{
		if(player.getLevel() >= 76 && Util.hasChildClassWithRace(player.getClassId(), Race.Dwarf) && player.getClassId().level() < ClassLevel.THIRD.ordinal())
		{
			QuestState st = player.getQuestState(getClass());
			if(st == null)
			{
				player.sendPacket(new TutorialShowQuestionMark(105));
			}
		}
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		if(player.getLevel() >= 76 && Util.hasChildClassWithRace(player.getClassId(), Race.Dwarf) && player.getClassId().level() < ClassLevel.THIRD.ordinal())
		{
			QuestState st = player.getQuestState(getClass());
			if(st == null)
			{
				player.sendPacket(new TutorialShowQuestionMark(105));
			}
		}
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 76 && Util.hasChildClassWithRace(player.getClassId(), Race.Dwarf) && player.getClassId().level() < ClassLevel.THIRD.ordinal();

	}
}