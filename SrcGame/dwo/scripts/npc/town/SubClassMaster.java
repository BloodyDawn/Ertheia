package dwo.scripts.npc.town;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.instancemanager.AwakeningManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.base.PlayerClass;
import dwo.gameserver.model.player.base.SubClass;
import dwo.gameserver.model.player.base.SubClassType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.util.FStringUtil;
import dwo.gameserver.util.Util;
import dwo.scripts.quests._10385_RedThreadofFate;
import org.apache.log4j.Level;

import java.util.Iterator;
import java.util.Set;

/**
 * User: Bacek
 * Date: 04.01.13
 * Time: 20:48
 */
public class SubClassMaster extends Quest
{
	private static final int sub_class_ellenia = 33491;

	public SubClassMaster()
	{
		addAskId(sub_class_ellenia, 1);
		addAskId(sub_class_ellenia, 100);
		addAskId(sub_class_ellenia, 101);
		addAskId(sub_class_ellenia, 102);
		addAskId(sub_class_ellenia, 104);
		addAskId(sub_class_ellenia, 105);
		addAskId(sub_class_ellenia, 301);
		addAskId(sub_class_ellenia, 401);

       /*

            sub_class_master_apply                              Новый подкласс можно обрести на 40-м уровне после получения 2-ой профессии.<br>  Это правильный выбор?<br>  <?reply0?>
            sub_class_master_apply2                             Новый подкласс уничтожает умения, полученные с помощью свитков и улучшений.</font><br> Это правильный выбор?<br> <?reply0?>
            sub_class_master_change                             На какой подкласс хотите сменить?  <?reply0?> -  <?reply40?>
            sub_class_master_change_ok                          Подкласс изменен успешно.

            sub_class_master_error_change_fail                  Что-то не то.<br> Не получается сменить подкласс.<br>
            sub_class_master_error_fail                         Не получается создать подкласс.
            sub_class_master_error_invalid_slot                 Изменение подкласса невозможно.

       +ok  sub_class_master_error_inventory                    Если инвентарь по весу или объему заполнен более чем на 80%, невозможно создать или сбросить подкласс
       +ok  sub_class_master_error_level                        Второе, <font color="LEVEL">у Вас должна быть 2-я профессия, а все подклассы должны быть выше 75-го уровня</font>. То есть, если у Вас два подкласса: Рыцарь Евы и Серебряный Рейнджер, то они оба должны быть выше 75-го уровня, чтобы Вы смогли добавить 3-й подкласс.<br>
       +ok  sub_class_master_error_no_fate_item                 Для получения силы богов необходимо иметь сертификат на смену профессии. Сертификат на смену профессии выдавался героям задолго до падения Шилен. Очень жаль, но сейчас его уже не достать.<br>
       +ok  sub_class_master_error_noslot                       Вы не можете добавить подкласс. У персонажа может быть только 3 подкласса

       +ok  sub_class_master_error_nosubjob                     У Вас нет подкласса для изменения.  Сначала создайте подкласс.
       +ok  sub_class_master_error_pet                          Если у Вас есть питомец или слуга,<br> Вы не сможете создать подкласс.<br>

            sub_class_master_error_renew_dual_class             Нельзя выбирать подклассы, дублирующие основной класс персонажа.
дубль  +ok  sub_class_master_error_renew_fail                   Если инвентарь по весу или объему заполнен более чем на 80%, невозможно создать новый подкласс.
       +ok  sub_class_master_error_transform                    Нельзя создавать подкласс в трансформированном состоянии.

            sub_class_master_ok                                 Поздравляю!  Вы создали новый подкласс.
            sub_class_master_renew                              На какой подкласс хотите поменять?   <?reply0?> -  <?reply40?>

            sub_class_master_renew_list                         На какой подкласс хотите поменять?  <?reply0?> -  <?reply40?>
            sub_class_master_renew_ok                           Поздравляю!  Вы создали новый подкласс.
       +ok  sub_class_master_select                             Выберите подкласс.      <?reply0?> -  <?reply40?>

       */
	}

	private static Iterator<SubClass> iterSubClasses(L2PcInstance player)
	{
		return player.getSubClasses().values().iterator();
	}

	public static void main(String[] args)
	{
		new SubClassMaster();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case 100:
			{
				NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile(player.getLang(), "sub_class_master_apply.htm");
				html.replace("<\\?reply0\\?>", FStringUtil.makeFString(11170000 + reply, "104", String.valueOf(reply)));
				return html.toString();
			}
			case 101:
			{
				NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				Set<PlayerClass> subsAvailable = getAvailableSubClasses(player);
				if(subsAvailable != null && !subsAvailable.isEmpty())
				{
					html.setFile(player.getLang(), "sub_class_master_renew.htm");
					int number = 0;
					for(PlayerClass subClass : subsAvailable)
					{
						html.replace("<\\?reply" + number + "\\?>", FStringUtil.makeFString(11170000 + subClass.ordinal(), "102", subClass.ordinal() + " " + reply));
						number++;
					}
					html.replace("\\<\\?reply.*\\>", "");
					return html.toString();
				}
				return "sub_class_master_error_change_fail.htm";
			}
			case 102:
			{
				String link = String.valueOf(reply);
				// Получение профы  331   - 33 профа  1 номер саба
				int classId = Integer.parseInt(link.substring(0, link.length() - 1));

				NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile(player.getLang(), "sub_class_master_apply2.htm");
				html.replace("<\\?reply0\\?>", FStringUtil.makeFString(11170000 + classId, "105", String.valueOf(reply)));
				return html.toString();
			}
			case 104:   // Добавление саба
			case 105:   // Изменение саба
				if(Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS && !checkSubClassQuest(player))
				{
					return "sub_class_master_error_quest.htm";
				}

				// Нельзя создавать подкласс в трансформированном состоянии.
				if(player.isTransformed())
				{
					return "sub_class_master_error_transform.htm";
				}

				// Если у Вас есть питомец или слуга, Вы не сможете создать подкласс.
				if(!player.getPets().isEmpty())
				{
					return "sub_class_master_error_pet.htm";
				}

				// Если инвентарь по весу или объему заполнен более чем на 80%, невозможно создать или сбросить подкласс
				if(!player.isInventoryUnder(0.8, true) || player.getWeightPenalty() >= 2)
				{
					return "sub_class_master_error_inventory.htm";
				}

				// Если саб получает 4 профу основного то выводим ошибку
				if(Util.getAwakenRelativeClass(player.getBaseClassId()) == Util.getAwakenRelativeClass(Util.getThirdClassForId(reply)))
				{
					return "sub_class_master_error_renew_dual_class.htm";
				}

				// Добовление саба
				if(ask == 104)
				{
					// Проверяем количество сабов
					if(player.getTotalSubClasses() >= Config.MAX_SUBCLASS)
					{
						return "sub_class_master_error_noslot.htm";
					}

					if(!isValidNewSubClass(player, reply))
					{
						return "sub_class_master_error_fail.htm";
					}

					// Проверяем уровень текущего персанажа
					if(player.getLevel() < 75)
					{
						return "sub_class_master_error_level.htm";
					}

					// Проверяем уровни сабов
					if(!player.getSubClasses().isEmpty())
					{
						for(Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext(); )
						{
							SubClass subClass = subList.next();
							if(subClass.getLevel() < 75)
							{
								return "sub_class_master_error_level.htm";
							}
						}
					}

					if(!player.getFloodProtectors().getSubclass().tryPerformAction(FloodAction.CLASS_ADD))
					{
						_log.log(Level.WARN, SubClassMaster.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
						return null;
					}

					// Выдаем саб класс
					if(!player.addSubClass(reply, player.getTotalSubClasses() + 1, SubClassType.SUB_CLASS))
					{
						return "sub_class_master_error_fail.htm";
					}

					player.setActiveClass(player.getTotalSubClasses());
					player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS); // Subclass added.
					return "sub_class_master_ok.htm";
				}
				else  // Изменение саба
				{
					String link = String.valueOf(reply);
					// Получение профы  331   - 33 профа  1 номер саба
					int index = Integer.parseInt(link.substring(link.length() - 1)) + 1;
					int classId = Integer.parseInt(link.substring(0, link.length() - 1));

					if(!isValidNewSubClass(player, classId) || player.isDualClassActive() || classId > 138)
					{
						return "sub_class_master_error_fail.htm";
					}

					if(player.modifySubClass(index, classId, SubClassType.SUB_CLASS))
					{
						if(!player.getFloodProtectors().getSubclass().tryPerformAction(FloodAction.CLASS_ADD))
						{
							_log.log(Level.WARN, SubClassMaster.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
							return null;
						}

						player.abortCast();
						player.stopAllEffectsExceptThoseThatLastThroughDeath(); // all effects from old subclass stopped!
						player.stopAllEffectsNotStayOnSubclassChange();
						player.stopCubics();
						player.setActiveClass(index);
						player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS); // Subclass added.
						return "sub_class_master_change_ok.htm";
					}
					else
					{
						/*
					   * This isn't good! modifySubClass() removed subclass from memory
					   * we must update _classIndex! Else IndexOutOfBoundsException can turn
					   * up some place down the line along with other seemingly unrelated
					   * problems.
					   */
						player.setActiveClass(0); // Also updates _classIndex plus switching _classid to baseclass.
						return "sub_class_master_error_change_fail.htm";
					}
				}
			case 1:
				switch(reply)
				{
					case 1:   // Добавить подкласс (создать заново)
					case 2:   // Добавить подкласс после удаление (сброс)
						// Подклассы не может быть изменен во время использования умении.
						if(player.isCastingNow() || player.isImmobilized())
						{
							player.sendPacket(SystemMessageId.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE);
							break;
						}

						if(Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS && !checkSubClassQuest(player))
						{
							return "sub_class_master_error_quest.htm";
						}

						// Нельзя создавать подкласс в трансформированном состоянии.
						if(player.isTransformed())
						{
							return "sub_class_master_error_transform.htm";
						}

						// Если у Вас есть питомец или слуга, Вы не сможете создать подкласс.
						if(!player.getPets().isEmpty())
						{
							return "sub_class_master_error_pet.htm";
						}

						// Если инвентарь по весу или объему заполнен более чем на 80%, невозможно создать или сбросить подкласс
						if(!player.isInventoryUnder(0.8, true) || player.getWeightPenalty() >= 2)
						{
							return "sub_class_master_error_inventory.htm";
						}

						NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());

						// Добавить подкласс (создать заново)
						if(reply == 1)
						{
							// Вы не можете добавить подкласс. У персонажа может быть только 3 подкласса
							if(player.getTotalSubClasses() >= Config.MAX_SUBCLASS)
							{
								return "sub_class_master_error_noslot.htm";
							}

							// Проверяем уровень текущего персанажа
							if(player.getLevel() < 75)
							{
								return "sub_class_master_error_level.htm";
							}

							// Проверяем уровни сабов
							if(!player.getSubClasses().isEmpty())
							{
								for(Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext(); )
								{
									SubClass subClass = subList.next();
									if(subClass.getLevel() < 75)
									{
										return "sub_class_master_error_level.htm";
									}
								}
							}

							Set<PlayerClass> subsAvailable = getAvailableSubClasses(player);
							if(subsAvailable != null && !subsAvailable.isEmpty())
							{
								html.setFile(player.getLang(), "sub_class_master_select.htm");
								int number = 0;
								for(PlayerClass subClass : subsAvailable)
								{
									html.replace("<\\?reply" + number + "\\?>", FStringUtil.makeFString(11170000 + subClass.ordinal(), "100", String.valueOf(subClass.ordinal())));
									number++;
								}
								html.replace("\\<\\?reply.*\\>", "");
								return html.toString();
							}
							break;
						}
						else // Добавить подкласс после удаление (сброс)
						{
							// У Вас нет подкласса для изменения.  Сначала создайте подкласс.
							if(player.getTotalSubClasses() < 1)
							{
								return "sub_class_master_error_nosubjob.htm";
							}

							int number = 0;
							html.setFile(player.getLang(), "sub_class_master_renew.htm");
							for(SubClass subClass : player.getSubClasses().values())
							{
								if(!subClass.isDualClass())
								{
									html.replace("<\\?reply" + number + "\\?>", FStringUtil.makeFString(11170000 + subClass.getClassId(), "101", String.valueOf(number)));
								}
								number++;
							}
							html.replace("\\<\\?reply.*\\>", "");
							return html.toString();
						}
					case 4:
				    /*
		                    Для получения силы богов необходимо иметь сертификат на смену профессии.
		                    Сертификат на смену профессии выдавался героям задолго до падения Шилен.
		                    Очень жаль, но сейчас его уже не достать.
		             */
						return "sub_class_master_error_no_fate_item.htm";
				}
			case 301:  //  Добавить двойного подкласс после удаление (сброс)
				// Если не на дуале
				if(!player.isDualClassActive() || player.getLevel() < 85)
				{
					return "dual_class_master_error_no_dual.htm";
				}

				// Если есть пет или в трансформе
				if(player.isTransformed() || !player.getPets().isEmpty())
				{
					return "dual_class_master_error_pet_transform.htm";
				}

				int adena = getPriceChangeDualClass(player.getLevel());
				int cloak = getCloakIdForClass(player.getClassId().getGeneralIdForAwaken());

				if(player.getAdenaCount() < adena || cloak == 0 || !player.getInventory().hasItems(cloak))    //   TODO проверить проверку!!!
				{
					NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
					html.setFile(player.getLang(), "dual_class_master_error_no_adena.htm");
					html.replace("<\\?price\\?>", String.valueOf(adena));
					return html.toString();
				}

				switch(reply)
				{
					case 1:
						NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
						html.setFile(player.getLang(), "dual_class_master_change1.htm");
						html.replace("<\\?price\\?>", String.valueOf(adena));
						return html.toString();
					case 2:
						return "dual_class_master_change_cancel.htm";
					case 3:
						return "dual_class_master_change2.htm";
					case 139:
						return "dual_class_master_change_139.htm";
					case 140:
						return "dual_class_master_change_140.htm";
					case 141:
						return "dual_class_master_change_141.htm";
					case 142:
						return "dual_class_master_change_142.htm";
					case 143:
						return "dual_class_master_change_143.htm";
					case 144:
						return "dual_class_master_change_144.htm";
					case 145:
						return "dual_class_master_change_145.htm";
					case 146:
						return "dual_class_master_change_146.htm";
					// сигели
					case 148:
					case 149:
					case 150:
					case 151:
						// тиры
					case 152:
					case 153:
					case 154:
					case 155:
						// 156 нельзя брать
					case 157:
						// дагеры
					case 158:
					case 159:
					case 160:
					case 161:
						// луки
					case 162:
					case 163:
					case 164:
					case 165:
						// маги
					case 166:
					case 167:
					case 168:
					case 169:
					case 170:
						// исы
					case 171:
					case 172:
					case 173:
						// 174 нельзя брать
					case 175:
						// суммы
					case 176:
					case 177:
					case 178:
						// хилы
					case 179:
					case 180:
					case 181:
						// TODO Поправить нпе =((
						if(player.modifySubClass(player.getClassIndex(), reply, SubClassType.DUAL_CLASS))
						{
							player.abortCast();
							player.stopAllEffectsExceptThoseThatLastThroughDeath(); // all effects from old subclass stopped!
							player.stopAllEffectsNotStayOnSubclassChange();
							player.stopCubics();
							player.setActiveClass(player.getClassIndex());
							// Удаляем левые скилы
							AwakeningManager.getInstance().deleteNonAwakedSkills(player);
							// Забираем адену
							player.destroyOneItemByItemIds(ProcessType.QUEST, adena, npc, true, 57);
							// Забираем плащ
							player.destroyOneItemByItemIds(ProcessType.QUEST, 1, npc, true, cloak);
							// Выдаем новый плащ
							player.getInventory().addItem(ProcessType.QUEST, getCloakIdForClass(Util.getGeneralIdForAwaken(reply)), 1, player, null, true);
							player.getInventory().addItem(ProcessType.QUEST, 37375, 2, player, null, true);
						}
						return "dual_class_master_change_ok.htm";
				}
			case 401:
				switch(reply)
				{
					case 1:
						// Позвольте мне взглянуть на предметы двойного класса.
						MultiSellData.getInstance().separateAndSend(2027, player, npc);
						break;
				}
		}
		return null;
	}

	/*
		* Возвращает список доступных подклассов
		*/
	private Set<PlayerClass> getAvailableSubClasses(L2PcInstance player)
	{
		// get player base class
		int currentBaseId = player.getBaseClassId();
		ClassId baseCID = ClassId.getClassId(currentBaseId);

		// we need 2nd occupation ID
		int baseClassId;
		baseClassId = baseCID.level() > 2 ? baseCID.getParent().ordinal() : currentBaseId;

		/**
		 * If the race of your main class is Elf or Dark Elf,
		 * you may not select each class as a subclass to the other class.
		 *
		 * If the race of your main class is Kamael, you may not subclass any other race
		 * If the race of your main class is NOT Kamael, you may not subclass any Kamael class
		 *
		 * You may not select Overlord and Warsmith class as a subclass.
		 *
		 * You may not select a similar class as the subclass.
		 * The occupations classified as similar classes are as follows:
		 *
		 * Treasure Hunter, Plainswalker and Abyss Walker
		 * Hawkeye, Silver Ranger and Phantom Ranger
		 * Paladin, Dark Avenger, Temple Knight and Shillien Knight
		 * Warlocks, Elemental Summoner and Phantom Summoner
		 * Elder and Shillien Elder
		 * Swordsinger and Bladedancer
		 * Sorcerer, Spellsinger and Spellhowler
		 *
		 * Also, Kamael have a special, hidden 4 subclass, the inspector, which can
		 * only be taken if you have already completed the other two Kamael subclasses
		 *
		 */
		Set<PlayerClass> availSubs = PlayerClass.values()[baseClassId].getAvailableSubclasses(player);

		if(availSubs != null && !availSubs.isEmpty())
		{
			for(Iterator<PlayerClass> availSub = availSubs.iterator(); availSub.hasNext(); )
			{
				PlayerClass pclass = availSub.next();

				// scan for already used subclasses
				int availClassId = pclass.ordinal();
				ClassId cid = ClassId.getClassId(availClassId);
				SubClass prevSubClass;
				ClassId subClassId;
				for(SubClass subClass : player.getSubClasses().values())
				{
					prevSubClass = subClass;
					subClassId = ClassId.getClassId(prevSubClass.getClassId());

					if(subClassId.equalsOrChildOf(cid))
					{
						availSub.remove();
						break;
					}
				}
			}
		}
		return availSubs;
	}

	/*
	* Check new subclass classId for validity
	* (villagemaster race/type, is not contains in previous subclasses,
	* is contains in allowed subclasses)
	* Base class not added into allowed subclasses.
	*/
	private boolean isValidNewSubClass(L2PcInstance player, int classId)
	{
		if(classId != ClassId.inspector.getId())
		{
			// - В случае с третьепрофным основным классом
			if(Util.getAwakenRelativeClass(player.getBaseClassId()) == Util.getAwakenRelativeClass(Util.getThirdClassForId(classId)))
			{
				return false;
			}
			// - В случае с второпрофным основным классом
			if(Util.getAwakenedClassForId(Util.getThirdClassForId(player.getBaseClassId())) == Util.getAwakenRelativeClass(Util.getThirdClassForId(classId)))
			{
				return false;
			}
		}

		ClassId cid = ClassId.values()[classId];
		SubClass sub;
		ClassId subClassId;
		for(Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext(); )
		{
			sub = subList.next();
			subClassId = ClassId.values()[sub.getClassId()];

			if(subClassId.equalsOrChildOf(cid))
			{
				return false;
			}
		}

		// get player base class
		int currentBaseId = player.getBaseClassId();
		ClassId baseCID = ClassId.getClassId(currentBaseId);

		// we need 2nd occupation ID
		int baseClassId;
		baseClassId = baseCID.level() > 2 ? baseCID.getParent().ordinal() : currentBaseId;

		Set<PlayerClass> availSubs = PlayerClass.values()[baseClassId].getAvailableSubclasses(player);
		if(availSubs == null || availSubs.isEmpty())
		{
			return false;
		}

		boolean found = false;

		for(PlayerClass pclass : availSubs)
		{
			if(pclass.ordinal() == classId)
			{
				found = true;
				break;
			}
		}

		return found;
	}

	protected boolean checkSubClassQuest(L2PcInstance player)
	{
		// Проверяем на завершенный сабкласс-квест
		QuestState st = player.getQuestState(_10385_RedThreadofFate.class);
		return st != null && st.isCompleted();

	}

	private int getPriceChangeDualClass(int level)
	{
		switch(level)
		{
			case 85:
				return 100000000;
			case 86:
				return 90000000;
			case 87:
				return 80000000;
			case 88:
				return 70000000;
			case 89:
				return 60000000;
			case 90:
				return 50000000;
			case 91:
				return 40000000;
			case 92:
				return 30000000;
			case 93:
				return 20000000;
		}
		return 10000000;
	}

	private int getCloakIdForClass(int classId)
	{
		switch(classId)
		{
			case 139:
				return 30310; // Мантия Авелиуса - Рыцарь Сигеля
			case 140:
				return 30311; // Мантия Сафироса - Воин Тира
			case 141:
				return 30312; // Мантия Кайшунаги - Разбойник Одала
			case 142:
				return 30313; // Мантия Кронвиста - Лучник Эура
			case 143:
				return 30314; // Мантия Сольткрига - Волшебник Фео
			case 144:
				return 30316; // Мантия Райстера - Заклинатель Иса
			case 145:
				return 30315; // Мантия Набиаропа - Призыватель Веньо
			case 146:
				return 30317; // Мантия Лакисиса - Целитель Альгиза
		}
		return 0;
	}
}
