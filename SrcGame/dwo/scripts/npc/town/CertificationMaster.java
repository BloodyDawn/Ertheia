package dwo.scripts.npc.town;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.scripts.quests._00136_MoreThanMeetsTheEye;
import dwo.scripts.services.validator.DualClassSkillsValidator;
import dwo.scripts.services.validator.SubClassSkillsValidator;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 13.09.12
 * Time: 8:28
 */

public class CertificationMaster extends Quest
{
	// Для сертификации саба
	public static final String[] _questVarNames = {
		"EmergentAbility65-", "EmergentAbility70-", "EmergentAbility75-", "EmergentAbility80-"
	};
	// Для сертификации дуала
	public static final String[] _questVarNamesDual = {
		"EmergentAbility85", "EmergentAbility90", "EmergentAbility95", "EmergentAbility99"
	};
	private static final int NPC = 33490;
	private static final int feeDeleteSubClassSkills = 10000000;
	private static final int feeDeleteSubClassSkillsDual = 20000000;
	private static final int _EmergentAbilitySubItem = 10280;   // Книга сертификации саба
	private static final int _EmergentAbilitySubItemDual = 36078; // Книга сертификации дуада

	public CertificationMaster()
	{
		addAskId(NPC, -281); // Суб Класс   ( Получить сертификацию )
		addAskId(NPC, -282); // Суб Класс   ( Выучить умения 		)
		addAskId(NPC, -283); // Дуал класс  ( Получить сертификацию )
		addAskId(NPC, -284); // Дуал класс  ( Выучить умения 		)
	}

	/**
	 * @param player L2PcInstance игрока
	 * @return {@code true} если игрок может изучать трансформации
	 */
	private static boolean isAllowToLearnTransformation(L2PcInstance player)
	{
		if(Config.ALLOW_TRANSFORM_WITHOUT_QUEST)
		{
			return true;
		}

		QuestState st = player.getQuestState(_00136_MoreThanMeetsTheEye.class);
		return st != null && st.isCompleted();
	}

	public static void main(String[] args)
	{
		new CertificationMaster();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == NPC)
		{
			if(ask == -281)
			{
				if(!player.isSubClassActive())
				{
					return "subclass_comp_03.htm";
				}

				if(player.getLevel() < 65)
				{
					return "subclass_comp_06.htm";
				}

				if(isAllLevelsCertified(player))
				{
					return "subclass_comp_05.htm";
				}

				switch(reply)
				{
					case 0:
						return "subclass_comp_07.htm";
					case 1:
						return checkCertified(player, 65) ? "subclass_comp_701.htm" : "subclass_comp_08.htm";
					case 2:
						if(player.getLevel() >= 70)
						{
							return checkCertified(player, 70) ? "subclass_comp_701.htm" : "subclass_comp_08.htm";
						}
						else
						{
							return "subclass_comp_09.htm";
						}
					case 3:
						if(player.getLevel() >= 75)
						{
							return checkCertified(player, 75) ? "subclass_comp_701.htm" : "subclass_comp_08.htm";
						}
						else
						{
							return "subclass_comp_10.htm";
						}
					case 4:
						if(player.getLevel() >= 80)
						{
							return checkCertified(player, 80) ? "subclass_comp_701.htm" : "subclass_comp_08.htm";
						}
						else
						{
							return "subclass_comp_11.htm";
						}
					case 65:
						return "subclass_comp_65.htm";
					case 70:
						return "subclass_comp_70.htm";
					case 75:
						return "subclass_comp_75.htm";
					case 80:
						return "subclass_comp_80.htm";
				}
			}
			else if(ask == -282)
			{
				if(!player.isInventoryUnder90(true))
				{
					return "sub_class_trandoom011no.htm";
				}
				if(player.isSubClassActive())
				{
					return "sub_class_trandoom007.htm"; // игрок на саб классе
				}

				//	else if (!isAllowToLearnTransformation(player))
				//{
				//	return "sub_class_trandoom007.htm"; // нету квеста на трансформацию   TODO
				//}

				switch(reply)
				{
					case 0:
						SkillTreesData.showSubClassSkillList(player, false);
						break;
					case 1:
						if(player.getAdenaCount() < feeDeleteSubClassSkills)
						{
							return "sub_class_trandoom008no.htm";  // не хватает денег
						}
						else
						{
							return сancelCertification(npc, player) ? "sub_class_trandoom009no.htm" : "sub_class_trandoom010no.htm";
						}
				}
			}
			else if(ask == -283)
			{
				// не на дуал классе
				if(!player.isDualClassActive())
				{
					return "dual_class_comp_03.htm";
				}

				if(!player.isInventoryUnder90(true))
				{
					return "sub_class_trandoom011no.htm";
				}

				if(player.isTransformed() || !player.getPets().isEmpty())
				{
					return "dual_class_comp_pet_transform.htm";
				}

				switch(reply)
				{
					case 0:
						return "dual_class_comp_select_book.htm";
					case 85:
						if(player.getLevel() >= 85)
						{
							return checkCertifiedDual(player, 85) ? "dual_class_comp_ok_85.htm" : "dual_class_comp_received_85.htm";
						}
						else
						{
							return "dual_class_comp_no_level_85.htm";
						}
					case 90:
						if(player.getLevel() >= 90)
						{
							return checkCertifiedDual(player, 90) ? "dual_class_comp_ok_90.htm" : "dual_class_comp_received_90.htm";
						}
						else
						{
							return "dual_class_comp_no_level_90.htm";
						}
					case 95:
						if(player.getLevel() >= 95)
						{
							return checkCertifiedDual(player, 95) ? "dual_class_comp_ok_95.htm" : "dual_class_comp_received_95.htm";
						}
						else
						{
							return "dual_class_comp_no_level_95.htm";
						}
					case 99:
						if(player.getLevel() >= 99)
						{
							return checkCertifiedDual(player, 99) ? "dual_class_comp_ok_99.htm" : "dual_class_comp_received_99.htm";
						}
						else
						{
							return "dual_class_comp_no_level_99.htm";
						}
				}
			}
			else if(ask == -284)
			{
				if(!player.isInventoryUnder90(true))
				{
					return "sub_class_trandoom011no.htm";
				}
				if(player.isSubClassActive())
				{
					return "dual_class_comp_error_no_main.htm"; // игрок на саб классе
				}

				switch(reply)
				{
					case 0:
						SkillTreesData.showSubClassSkillList(player.getActingPlayer(), true);
						break;
					case 1:
						if(player.getAdenaCount() < feeDeleteSubClassSkillsDual)
						{
							return "dual_class_comp_error_no_adena.htm";  // не хватает денег
						}
						else
						{
							return сancelCertificationDual(npc, player) ? "dual_class_comp_cancel_ok.htm" : "sub_class_trandoom010no.htm";
						}
				}
			}
		}
		return null;
	}

	/***
	 * @param player проверяемый игрок
	 * @return {@code true} если все сертификаты на текущем саб-классе получены
	 */
	private boolean isAllLevelsCertified(L2PcInstance player)
	{
		QuestState st = player.getQuestState(SubClassSkillsValidator.class);
		if(st == null)
		{
			Quest subClassSkilllsQuest = QuestManager.getInstance().getQuest(SubClassSkillsValidator.class);
			if(subClassSkilllsQuest != null)
			{
				st = subClassSkilllsQuest.newQuestState(player);
			}
			else
			{
				return false;
			}
		}

		int count = 0;
		for(String str : _questVarNames)
		{
			boolean isCertified = !st.getGlobalQuestVar(str + player.getClassIndex()).isEmpty();
			if(isCertified)
			{
				count++;
			}
		}
		return count == 4;
	}

	/***
	 * @param player проверяемый игрок
	 * @return {@code true} если все сертификаты на текущем дуал-классе получены
	 */
	private boolean isAllLevelsCertifiedDual(L2PcInstance player)
	{
		QuestState st = player.getQuestState(DualClassSkillsValidator.class);
		if(st == null)
		{
			Quest subClassSkilllsQuest = QuestManager.getInstance().getQuest(DualClassSkillsValidator.class);
			if(subClassSkilllsQuest != null)
			{
				st = subClassSkilllsQuest.newQuestState(player);
			}
			else
			{
				return false;
			}
		}

		int count = 0;
		for(String str : _questVarNamesDual)
		{
			boolean isCertified = !st.getGlobalQuestVar(str).isEmpty();
			if(isCertified)
			{
				count++;
			}
		}
		return count == 4;
	}

	/***
	 * @param player проверяемый игрок
	 * @param level сертифицируемый уровень саб-класса
	 * @return {@code true} если уровень успешно прошел сертификацию, {@code false} если уровень саб-класса уже был сертифицирован
	 */
	private boolean checkCertified(L2PcInstance player, int level)
	{
		QuestState st = player.getQuestState(SubClassSkillsValidator.class);
		if(st == null)
		{
			Quest subClassSkilllsQuest = QuestManager.getInstance().getQuest(SubClassSkillsValidator.class);
			if(subClassSkilllsQuest != null)
			{
				st = subClassSkilllsQuest.newQuestState(player);
			}
			else
			{
				return false;
			}
		}

		String var = "EmergentAbility" + level + '-' + player.getClassIndex();
		boolean isCertified = !st.getGlobalQuestVar(var).isEmpty();
		if(!isCertified)
		{
			L2ItemInstance item = player.addItem(ProcessType.QUEST, _EmergentAbilitySubItem, 1, null, true);
			st.saveGlobalQuestVar(var, String.valueOf(item.getObjectId()));
			return true;
		}
		return false;
	}

	/***
	 * @param player проверяемый игрок
	 * @param level сертифицируемый уровень дуал-класса
	 * @return {@code true} если уровень успешно прошел сертификацию, {@code false} если уровень дуал-класса уже был сертифицирован
	 */
	private boolean checkCertifiedDual(L2PcInstance player, int level)
	{
		QuestState st = player.getQuestState(DualClassSkillsValidator.class);
		if(st == null)
		{
			Quest subClassSkilllsQuest = QuestManager.getInstance().getQuest(DualClassSkillsValidator.class);
			if(subClassSkilllsQuest != null)
			{
				st = subClassSkilllsQuest.newQuestState(player);
			}
			else
			{
				return false;
			}
		}

		String var = "EmergentAbility" + level;
		boolean isCertified = !st.getGlobalQuestVar(var).isEmpty();
		if(!isCertified)
		{
			L2ItemInstance item = player.addItem(ProcessType.QUEST, _EmergentAbilitySubItemDual, 1, null, true);
			st.saveGlobalQuestVar(var, String.valueOf(item.getObjectId()));
			return true;
		}
		return false;
	}

	/***
	 * Отмена сертификации и удаление полученных умений у указанного игрока
	 * @param npc меннеджер сертификации
	 * @param player игрок, который отменяет сертификацию
	 * @return {@code true} если все прошло успешно
	 */
	private boolean сancelCertification(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(SubClassSkillsValidator.class);
		if(st == null)
		{
			Quest subClassSkilllsQuest = QuestManager.getInstance().getQuest(SubClassSkillsValidator.class);
			if(subClassSkilllsQuest != null)
			{
				st = subClassSkilllsQuest.newQuestState(player);
			}
			else
			{
				return false;
			}
		}

		int activeCertifications = 0;
		for(String varName : _questVarNames)
		{
			for(int i = 1; i <= Config.MAX_SUBCLASS; i++)
			{
				boolean isCertified = !st.getGlobalQuestVar(varName + i).isEmpty();
				if(isCertified)
				{
					activeCertifications++;
				}
			}
		}
		if(activeCertifications == 0)
		{
			return false;
		}
		for(String varName : _questVarNames)
		{
			for(int i = 1; i <= Config.MAX_SUBCLASS; i++)
			{
				st.deleteGlobalQuestVar(varName + i);
			}
		}
		for(L2SkillLearn skillLearn : SkillTreesData.getInstance().getAllSubClassSkills())
		{
			L2Skill skill = player.getKnownSkill(skillLearn.getSkillId());
			if(skill != null)
			{
				player.removeSkill(skill);
			}
		}
		player.reduceAdena(ProcessType.NPC, feeDeleteSubClassSkills, npc, true);
		player.sendSkillList();

		//Let's consume all certification books, even those not present in database.
		L2ItemInstance itemInstance = player.getInventory().getItemByItemId(_EmergentAbilitySubItem);
		if(itemInstance != null)
		{
			// _log.log(Level.WARN, CertificationMaster.class.getName() + ": player " + player + " had 'extra' certification skill books while cancelling sub-class certifications!");
			player.destroyItem(ProcessType.NPC, itemInstance, npc, true);
		}
		return true;
	}

	/***
	 * Отмена сертификации и удаление полученных умений у указанного игрока
	 * @param npc меннеджер сертификации
	 * @param player игрок, который отменяет сертификацию
	 * @return {@code true} если все прошло успешно
	 */
	private boolean сancelCertificationDual(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(DualClassSkillsValidator.class);
		if(st == null)
		{
			Quest subClassSkilllsQuest = QuestManager.getInstance().getQuest(DualClassSkillsValidator.class);
			if(subClassSkilllsQuest != null)
			{
				st = subClassSkilllsQuest.newQuestState(player);
			}
			else
			{
				return false;
			}
		}

		int activeCertifications = 0;
		for(String varName : _questVarNamesDual)
		{
			boolean isCertified = !st.getGlobalQuestVar(varName).isEmpty();
			if(isCertified)
			{
				activeCertifications++;
			}
		}
		if(activeCertifications == 0)
		{
			return false;
		}
		for(String varName : _questVarNamesDual)
		{
			st.deleteGlobalQuestVar(varName);
		}
		for(L2SkillLearn skillLearn : SkillTreesData.getInstance().getAllDualClassSkills())
		{
			L2Skill skill = player.getKnownSkill(skillLearn.getSkillId());
			if(skill != null)
			{
				player.removeSkill(skill);
			}
		}
		player.reduceAdena(ProcessType.NPC, feeDeleteSubClassSkillsDual, npc, true);
		player.sendSkillList();

		//Let's consume all certification books, even those not present in database.
		L2ItemInstance itemInstance = player.getInventory().getItemByItemId(_EmergentAbilitySubItemDual);
		if(itemInstance != null)
		{
			// _log.log(Level.WARN, CertificationMaster.class.getName() + ": player " + player + " had 'extra' certification skill books while cancelling dual-class certifications!");
			player.destroyItem(ProcessType.NPC, itemInstance, npc, true);
		}
		return true;
	}
}