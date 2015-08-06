package dwo.gameserver.instancemanager;

import dwo.gameserver.datatables.xml.EnchantSkillGroupsTable;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.L2EnchantSkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO, Bacek
 * Date: 05.11.11
 * Time: 14:23
 */

public class AwakeningManager
{
	private static final Logger _log = LogManager.getLogger(AwakeningManager.class);

	private AwakeningManager()
	{
		_log.log(Level.INFO, "Awakening Manager: Dummy Mode");
	}

	public static AwakeningManager getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Главный метод, отвечающий за пробуждение
	 * @param player инстанс пробуждающегося игрока
	 */
	public void doAwake(L2PcInstance player)
	{
		if(!checkRestrictions(player))
		{
			return;
		}

		player.rewardSkills();
		player.broadcastUserInfo();

		deleteNonAwakedSkills(player);
		deleteRestrictedItems(player);
		giveItems(player);
		sendSocialAction(player);
		//player.showUsmVideo(player.getClassId().getGeneralIdForAwaken());  Убрали в LV
	}

	/**
	 * Удаление "лишних" скилов у игрока при перерождении ( только профные. клан, саб и тд не затрагиваем ).
	 * @param player игрок
	 */
	public void deleteNonAwakedSkills(L2PcInstance player)
	{
		if(!player.isAwakened())
		{
			return;
		}

		List<Integer> transferSkills = SkillTreesData.getInstance().getAvailableTransferSkillsList(player);
		List<Integer> ignored = SkillTreesData.getInstance().getAwakenDeleteSkills(player);
		player.getAllSkills().stream().filter(actual -> ignored.contains(actual.getId()) || transferSkills.contains(actual.getId())).forEach(player::removeSkill);
	}

	/**
	 * Удаляем предметы, которые должны быть удалены при перерождении
	 * @param player инстанс перерождающегося игрока
	 */
	public void deleteRestrictedItems(L2PcInstance player)
	{
		InventoryUpdate iu = new InventoryUpdate();
		iu.addRemovedItem(player.getInventory().destroyItemByItemId(ProcessType.QUEST, 17600, 1, player, null));

		// Скилл линк
		iu.addRemovedItem(player.getInventory().destroyItemByItemId(ProcessType.QUEST, 15307, player.getInventory().getCountOf(15307), player, null));
		iu.addRemovedItem(player.getInventory().destroyItemByItemId(ProcessType.QUEST, 15308, player.getInventory().getCountOf(15308), player, null));
		iu.addRemovedItem(player.getInventory().destroyItemByItemId(ProcessType.QUEST, 15309, player.getInventory().getCountOf(15309), player, null));

		player.sendPacket(iu);
	}

	/**
	 * Отправляем анимацию перерождения.
	 * @param player игрок
	 */
	public void sendSocialAction(L2PcInstance player)
	{
		if(player.isAwakened())
		{
			player.broadcastPacket(new SocialAction(player.getObjectId(), player.getClassId().getGeneralIdForAwaken() - 119));
		}
	}

	/**
	 * Выдаем итемы за перерождение
	 * @param player игрок
	 */
	public void giveItems(L2PcInstance player)
	{
		switch(player.getClassId().getGeneralIdForAwaken())
		{
			case 139:
				player.getInventory().addItem(ProcessType.QUEST, 32264, 1, player, null, true);  //Сундук Силы Авелиуса Рыцарь Сигеля
				//iu.addItem(player.getInventory().addItem(ProcessType.QUEST, 33735, 1, player, null));  //Меч Силы Храмовника Шилен
				break;
			case 140:
				player.getInventory().addItem(ProcessType.QUEST, 32265, 1, player, null, true); //Сундук Силы Сафироса Воин Тира
				//iu.addItem(player.getInventory().addItem(ProcessType.QUEST, 33742, 1, player, null)); //Двуручный Меч Силы Титана
				break;
			case 141:
				player.getInventory().addItem(ProcessType.QUEST, 32266, 1, player, null, true); //Сундук Силы Кайшунаги Разбойник Одала
				//iu.addItem(player.getInventory().addItem(ProcessType.QUEST, 33722, 1, player, null)); //Парные Кинжалы Силы Авантюриста
				break;
			case 142:
				player.getInventory().addItem(ProcessType.QUEST, 32267, 1, player, null, true);  //Сундук Силы Кронвиста Лучник Эура
				//iu.addItem(player.getInventory().addItem(ProcessType.QUEST, 33763, 1, player, null));  //Арбалет Силы Диверсанта
				break;
			case 143:
				player.getInventory().addItem(ProcessType.QUEST, 32268, 1, player, null, true);  // Сундук Силы Сольткрига Волшебник Фео
				//iu.addItem(player.getInventory().addItem(ProcessType.QUEST, 33732, 1, player, null));  //Меч Силы Магистра Магии
				break;
			case 144:
				player.getInventory().addItem(ProcessType.QUEST, 32270, 1, player, null, true);  //Сундук Силы Райстера Заклинатель Иса
				//iu.addItem(player.getInventory().addItem(ProcessType.QUEST, 33727, 1, player, null));  //Посох Силы Апостола
				break;
			case 145:
				player.getInventory().addItem(ProcessType.QUEST, 32269, 1, player, null, true); //Сундук Силы Набиаропа Призыватель Веньо
				//iu.addItem(player.getInventory().addItem(ProcessType.QUEST, 33740, 1, player, null)); //Меч Силы Владыки Теней
				break;
			case 146:
				player.getInventory().addItem(ProcessType.QUEST, 32271, 1, player, null, true); //Сундук Силы Лакисиса Целитель Альгиза
				//iu.addItem(player.getInventory().addItem(ProcessType.QUEST, 33726, 1, player, null)); //Посох Силы Кардинала
				break;
		}

		if(player.isSubClassActive())
		{
			player.getInventory().addItem(ProcessType.QUEST, 37375, 2, player, null, true);
		}
		else
		{
			player.getInventory().addItem(ProcessType.QUEST, 37374, 2, player, null, true);
		}
	}

	/**
	 * Выдаем персонажу Эссенции Гигантов в зависимости от заточки умения
	 * и сбрасываем его заточку
	 *
	 * @param player инстанс персонажа
	 */
	public int giveGiantEssences(L2PcInstance player, boolean onlyCalculateCount)
	{
		List<Integer> enchantedSkills = new FastList<>();
		int[] for2ndClass = {
			0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 6, 6, 7, 8, 9, 9, 10, 11, 13, 14, 15, 19, 21, 25
		};  // + 30
		int[] for3ndClass = {0, 0, 0, 1, 1, 1, 1, 2, 2, 3, 3, 3, 6, 8, 11};  // + 15
		int count = 0;
		for(L2Skill skill : player.getAllSkills())
		{
			if(skill.getLevel() > 99)
			{
				L2EnchantSkillLearn s = EnchantSkillGroupsTable.getInstance().getSkillEnchantmentBySkillId(skill.getId());
				if(s.getGroupId() == 1 || s.getGroupId() == 2 || s.getGroupId() == 30)
				{
					count += for2ndClass[skill.getLevel() % 100 - 1];
				}
				else if(s.getGroupId() == 5 || s.getGroupId() == 6)
				{
					count += for3ndClass[skill.getLevel() % 100 - 1];
				}
				enchantedSkills.add(skill.getId());
			}
		}
		if(!onlyCalculateCount)
		{
			if(count > 0)
			{
				for(int skillId : enchantedSkills)
				{
					player.removeSkill(skillId);
					player.addSkill(SkillTable.getInstance().getInfo(skillId, SkillTable.getInstance().getMaxLevel(skillId)));
				}
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(player.getInventory().addItem(ProcessType.QUEST, 30306, count, player, null)); //Эссенция Гигантов
				player.sendPacket(iu);
			}
		}
		return count;
	}

	/**
	 * @param player проверяемый игрок
	 * @return {@code true} если персонаж соответствует условиям для перерождения
	 */
	private boolean checkRestrictions(L2PcInstance player)
	{
		return !(player.isSubClassActive() && !player.getSubclass().isDualClass());
	}

	private static class SingletonHolder
	{
		protected static final AwakeningManager _instance = new AwakeningManager();
	}
}
