package dwo.scripts.npc.town;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.scripts.quests._10338_OvercomeTheRock;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 22.10.12
 * Time: 21:07
 */

public class TestServerHelper extends Quest
{
	private static final int[] NPCs = {31756, 31757};

	public TestServerHelper()
	{
		addFirstTalkId(NPCs);
		addAskId(NPCs, -7);
		addAskId(NPCs, -2);
		addAskId(NPCs, -3);
		addAskId(NPCs, -5);
		addAskId(NPCs, -6);
		addAskId(NPCs, -8);
		addAskId(NPCs, -31);
		addAskId(NPCs, -926);
		addAskId(NPCs, -1007);
		addClassChangeRequest(NPCs);
		addPledgeLevelUpEvent(NPCs);
	}

	/***
	 * Проверка на возможность получения указанного уровня профессии
	 *
	 * @param currentClassLevel текущий уровень профессии игрока
	 * @param needClassLevel уровень профессии, на которую хочет перейти игрок
	 * @return {@code null} если персонаж может перейти на класс выше
	 */
	private static String checkCurrentClassLevel(int currentClassLevel, int needClassLevel)
	{
		if(currentClassLevel >= needClassLevel)
		{
			switch(needClassLevel)
			{
				case 1:
					return "test_server_helper028.htm";
				case 2:
					return "test_server_helper010.htm";
				case 3:
					return "test_server_helper011.htm";
			}
		}
		return null;
	}

	private static boolean validateClassId(ClassId oldCID, int val)
	{
		try
		{
			ClassId newCID = ClassId.values()[val];
			return !(newCID == null || newCID.getRace() == null) && oldCID == newCID.getParent();
		}
		catch(Exception ignored)
		{
		}
		return false;
	}

	public static void main(String[] args)
	{
		new TestServerHelper();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		ClassId currentClassId = player.getClassId();
		switch(ask)
		{
			case -31: // Пока непонятно что (new Lindvior)
				break;
			case -926: // Получить Сундук Поддержки Линдвиора
				if(player.getVariablesController().get("lindvior_chest_ex", Boolean.class, false))
				{
					return "test_server_helper043.htm";
				}
				player.addItem(ProcessType.NPC, 37128, 1, npc, true);
				player.getVariablesController().set("lindvior_chest_ex", true);
				break;
			case -7: // Первая профессия
				if(player.getLevel() < 20)
				{
					return "test_server_helper027.htm";
				}
				if(checkCurrentClassLevel(currentClassId.level(), ClassLevel.FIRST.ordinal()) != null)
				{
					return checkCurrentClassLevel(currentClassId.level(), ClassLevel.FIRST.ordinal());
				}
				// Так как текущая система не позволяет задать классу родителем самого себя, то юзаем текущий
				switch(currentClassId.getId())
				{
					// Люди
					case 0:
						return "test_server_helper026a.htm";
					case 10:
						return "test_server_helper026b.htm";
					// Светлые эльфы
					case 18:
						return "test_server_helper026c.htm";
					case 25:
						return "test_server_helper026d.htm";
					// Темные эльфы
					case 31:
						return "test_server_helper026e.htm";
					case 38:
						return "test_server_helper026f.htm";
					// Орки
					case 44:
						return "test_server_helper026g.htm";
					case 49:
						return "test_server_helper026h.htm";
					// Гномы
					case 53:
						return "test_server_helper026i.htm";
					// Камаели
					case 123:
						onClassChangeRequest(npc, player, 125);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 124:
						onClassChangeRequest(npc, player, 126);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
				}
				break;
			case -2: // Вторая профессия
				if(player.getLevel() < 40)
				{
					return "test_server_helper023.htm";
				}
				if(checkCurrentClassLevel(currentClassId.level(), ClassLevel.SECOND.ordinal()) != null)
				{
					return checkCurrentClassLevel(currentClassId.level(), ClassLevel.SECOND.ordinal());
				}
				// Используем родительский класс для проверок "каГнаофе"
				switch(currentClassId.getId())
				{
					// Люди
					case 0:
						return "test_server_helper012.htm";
					case 1:
						return "test_server_helper012a.htm";
					case 4:
						return "test_server_helper012b.htm";
					case 7:
						return "test_server_helper012c.htm";
					case 10:
						return "test_server_helper013.htm";
					case 11:
						return "test_server_helper013a.htm";
					case 15:
						return "test_server_helper013b.htm";
					// Светлые эльфы
					case 18:
						return "test_server_helper014.htm";
					case 19:
						return "test_server_helper014a.htm";
					case 22:
						return "test_server_helper014b.htm";
					case 25:
						return "test_server_helper015.htm";
					case 26:
						return "test_server_helper015a.htm";
					case 29:
						return "test_server_helper015b.htm";
					// Темные эльфы
					case 31:
						return "test_server_helper016.htm";
					case 32:
						return "test_server_helper016a.htm";
					case 35:
						return "test_server_helper016b.htm";
					case 38:
						return "test_server_helper017.htm";
					case 39:
						return "test_server_helper017a.htm";
					case 42:
						return "test_server_helper017b.htm";
					// Орки
					case 44:
						return "test_server_helper018.htm";
					case 45:
						return "test_server_helper018a.htm";
					case 47:
						return "test_server_helper018b.htm";
					case 49:
					case 50:
						return "test_server_helper019.htm";
					// Гномы
					case 53:
						return "test_server_helper020.htm";
					case 56:
						return "test_server_helper020b.htm";
					case 54:
						return "test_server_helper020a.htm";
					// Камаель
					case 125:
						return "test_server_helper020c.htm";
					case 126:
						return "test_server_helper020d.htm";
				}
				break;
			case -5: // Третья профессия
				if(player.getLevel() < 75)
				{
					return "test_server_helper023.htm";
				}
				if(checkCurrentClassLevel(currentClassId.level(), ClassLevel.THIRD.ordinal()) != null)
				{
					return checkCurrentClassLevel(currentClassId.level(), ClassLevel.THIRD.ordinal());
				}
				// Используем родительский класс для проверок "каГнаофе"
				switch(currentClassId.getId())
				{
					case 2:
						onClassChangeRequest(npc, player, 88);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 3:
						onClassChangeRequest(npc, player, 89);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 5:
						onClassChangeRequest(npc, player, 90);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 6:
						onClassChangeRequest(npc, player, 91);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 8:
						onClassChangeRequest(npc, player, 93);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 9:
						onClassChangeRequest(npc, player, 92);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 12:
						onClassChangeRequest(npc, player, 94);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 13:
						onClassChangeRequest(npc, player, 95);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 14:
						onClassChangeRequest(npc, player, 96);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 16:
						onClassChangeRequest(npc, player, 97);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						// myself->GiveItem1(talker,15307,1);
						return "test_server_helper021.htm";
					case 17:
						onClassChangeRequest(npc, player, 98);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 20:
						onClassChangeRequest(npc, player, 99);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 21:
						onClassChangeRequest(npc, player, 100);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 23:
						onClassChangeRequest(npc, player, 101);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 24:
						onClassChangeRequest(npc, player, 102);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 27:
						onClassChangeRequest(npc, player, 103);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 28:
						onClassChangeRequest(npc, player, 104);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 30:
						onClassChangeRequest(npc, player, 105);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						// myself->GiveItem1(talker,15308,1);
						return "test_server_helper021.htm";
					case 33:
						onClassChangeRequest(npc, player, 106);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 34:
						onClassChangeRequest(npc, player, 107);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 36:
						onClassChangeRequest(npc, player, 108);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 37:
						onClassChangeRequest(npc, player, 109);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 40:
						onClassChangeRequest(npc, player, 110);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 41:
						onClassChangeRequest(npc, player, 111);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 43:
						onClassChangeRequest(npc, player, 112);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						// myself->GiveItem1(talker,15309,4);
						return "test_server_helper021.htm";
					case 46:
						onClassChangeRequest(npc, player, 113);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 48:
						onClassChangeRequest(npc, player, 114);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 51:
						onClassChangeRequest(npc, player, 115);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 52:
						onClassChangeRequest(npc, player, 116);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 55:
						onClassChangeRequest(npc, player, 117);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 57:
						onClassChangeRequest(npc, player, 118);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 127:
						onClassChangeRequest(npc, player, 131);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 128:
						onClassChangeRequest(npc, player, 132);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 129:
						onClassChangeRequest(npc, player, 133);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 130:
						onClassChangeRequest(npc, player, 134);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
					case 135:
						onClassChangeRequest(npc, player, 136);
						player.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						return "test_server_helper021.htm";
				}
				break;
			case -3:
				if(reply == 1) // Поднять уровень клана
				{
					return !player.isClanLeader() ? "pl014.htm" : "test_server_helper022.htm";
				}
				break;
			case -6: // Стать дворянином
				if(reply == 1)
				{
					if(player.isNoble())
					{
						return "test_server_helper025b.htm";
					}
					else if(player.getLevel() < 75)
					{
						return "test_server_helper025a.htm";
					}
					else
					{
						player.setNoble(true);
						player.sendUserInfo();
						return "test_server_helper025.htm";
					}
				}
				break;
			case -8: // Выучить все навыки до 3-ей профессии
				player.giveAvailableSkills(Config.AUTO_LEARN_FS_SKILLS, true);
				break;
			case -1007: // Пробуждение
				if(reply == 1)
				{
					if(!player.isAwakened() && player.getLevel() >= 85 && (!player.isSubClassActive() || player.isSubClassActive() && player.getSubclass().isDualClass()))
					{
						if(player.getItemsCount(17600) == 0)
						{
							player.addItem(ProcessType.QUEST, 17600, 1, npc, true);
						}
						player.teleToLocation(-114962, 226564, -2864);
					}
					else
					{
						return "test_server_helper001_failed.htm";
					}
				}
				break;
		}
		return null;
	}

	@Override
	public void onClassChangeRequest(L2Npc npc, L2PcInstance player, int classId)
	{
		if(!validateClassId(player.getClassId(), classId))
		{
			return;
		}

		// Если игрок перерождается и до этого был взят квест на перерождение
		if(classId >= 148 && player.getQuestState(_10338_OvercomeTheRock.class) != null)
		{
			player.getQuestState(_10338_OvercomeTheRock.class).exitQuest(QuestType.ONE_TIME);
		}

		player.setClassId(classId);

		if(player.isSubClassActive())
		{
			player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClassId());
		}
		else
		{
			player.setBaseClassId(player.getActiveClassId());
		}

		player.rewardSkills();
		player.broadcastUserInfo();
	}

	@Override
	public String onPledgeLevelUp(L2Npc npc, L2PcInstance player, int currentPledgeLevel)
	{
		switch(currentPledgeLevel)
		{
			case 1:
			case 2:
			case 3:
			case 4:
				player.getClan().setLevel(currentPledgeLevel + 1);
				break;
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "test_server_helper001.htm";
	}
}