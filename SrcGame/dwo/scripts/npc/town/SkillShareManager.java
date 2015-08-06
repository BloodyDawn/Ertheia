package dwo.scripts.npc.town;

import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.AcquireSkillType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.packet.acquire.ExAcquirableSkillListByClass;
import org.apache.log4j.Level;

import java.util.List;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 04.10.12
 * Time: 17:52
 */

public class SkillShareManager extends Quest
{
	private static final int[] NPCs = {
		30022, 30030, 30032, 30036, 30067, 30068, 30116, 30117, 30118, 30119, 30144, 30145, 30188, 30293, 30330, 30375,
		30377, 30464, 30473, 30476, 30680, 30701, 30720, 30721, 30858, 30859, 30860, 30861, 30906, 30908, 30912, 31280,
		31281, 31287, 31329, 31330, 31335, 31969, 31970, 31976, 32148, 32155, 32156, 32161, 32162
	};

	private static final int feeDeleteTransferSkills = 10000000;

	private static final ItemHolder[] PORMANDERS = {
		new ItemHolder(15307, 7), // Cardinal (97)
		new ItemHolder(15308, 7), // Eva's Saint (105)
		new ItemHolder(15309, 7)  // Shillen Saint (112)
	};

	public SkillShareManager()
	{
		addEventId(HookType.ON_LEVEL_INCREASE);
		addEventId(HookType.ON_ENTER_WORLD);
		addAskId(NPCs, -707);
	}

	/***
	 * Показывает список доступных для изучения линк-умений игроку
	 * @param player игрок, которому показываем список линк-умений
	 */
	public static void showTransferSkillList(L2PcInstance player)
	{
		List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableTransferSkills(player);
		ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireSkillType.Transfer);

		for(L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getSkillId(), s.getSkillLevel());
			if(sk != null)
			{
				asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getLevelUpSp(), 0);
			}
		}

		if(asl.getSkillCount() > 0)
		{
			player.sendPacket(asl);
		}
		else
		{
			player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
		}
	}

	public static void main(String[] args)
	{
		new SkillShareManager();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(reply)
		{
			case 1: // Изучение линк-умений
				if(!npc.getTemplate().canTeach(player.getClassId()))
				{
					return npc.getServerName() + "003.htm";
				}
				if(player.getLevel() < 76 || player.getClassId().level() < 3)
				{
					return "skill_share_level_fail.htm";
				}
				if(!hasTransferSkillItems(player))
				{
					return "skill_share_healer_fail.htm";
				}
				showTransferSkillList(player);
				break;
			case 2: // Сброс выученных линк-умений
				if(player.getLevel() < 76 || player.getClassId().level() < 3)
				{
					return "skill_share_healer_fail.htm";
				}
				if(player.getAdenaCount() < feeDeleteTransferSkills)
				{
					player.sendPacket(SystemMessageId.CANNOT_RESET_SKILL_LINK_BECAUSE_NOT_ENOUGH_ADENA);
					return "skill_share_reset_fail.htm";
				}

				boolean hasSkills = false;
				Map<Integer, L2SkillLearn> skills = SkillTreesData.getInstance().getTransferSkillTree(player.getClassId());
				if(!hasTransferSkillItems(player) && skills != null)
				{
					for(L2SkillLearn s : skills.values())
					{
						L2Skill sk = player.getKnownSkill(s.getSkillId());
						if(sk != null)
						{
							player.removeSkill(sk);
							if(s.getRequiredItems().isEmpty())
							{
								_log.log(Level.ERROR, SkillShareManager.class.getSimpleName() + ": Transfer skill Id: " + s.getSkillId() + " doesn't have required items defined!");
							}
							else
							{
								player.addItem(ProcessType.NPC, s.getRequiredItems().get(0).getId(), s.getRequiredItems().get(0).getCount(), npc, true);
							}
							hasSkills = true;
						}
					}

					// Забираем плату за сброс умений
					if(hasSkills)
					{
						player.reduceAdena(ProcessType.NPC, feeDeleteTransferSkills, npc, true);
						return null;
					}
				}
				else
				{
					return "skill_share_healer_have.htm";
				}
				break;
		}
		return null;
	}

	@Override
	public void onLevelIncreased(L2PcInstance player)
	{
		givePormanders(player);
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		givePormanders(player);
	}

	/***
	 * Выдача предметов игроку, если он соответсвует условиям
	 * @param player проверяемый игрок
	 */
	private void givePormanders(L2PcInstance player)
	{
		if(player.isAwakened())
		{
			if(player.getInventory().getItemByItemId(15307) != null ||
				player.getInventory().getItemByItemId(15308) != null ||
				player.getInventory().getItemByItemId(15309) != null)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(player.getInventory().destroyItemByItemId(ProcessType.QUEST, 15307, player.getInventory().getCountOf(15307), player, null));
				iu.addRemovedItem(player.getInventory().destroyItemByItemId(ProcessType.QUEST, 15308, player.getInventory().getCountOf(15308), player, null));
				iu.addRemovedItem(player.getInventory().destroyItemByItemId(ProcessType.QUEST, 15309, player.getInventory().getCountOf(15309), player, null));
				player.sendPacket(iu);
			}
			return;
		}

		int indexInArray = getTransferClassIndex(player);

		if(indexInArray >= 0)
		{
			String classIdCheck = SkillShareManager.class.getSimpleName() + player.getClassId().getId();
			if(!player.getVariablesController().get(classIdCheck, Boolean.class, false))
			{
				player.getVariablesController().set(classIdCheck, true);
				player.addItem(ProcessType.SKILL, PORMANDERS[indexInArray].getId(), PORMANDERS[indexInArray].getCount(), null, true);
			}
		}
	}

	/***
	 * @param player проверяемый игрок
	 * @return индекс в массиве, если указанный игрок имеет право учить линк-скиллы
	 */
	private int getTransferClassIndex(L2PcInstance player)
	{
		switch(player.getClassId().getId())
		{
			case 97:  // Cardinal
				return 0;
			case 105: // Eva's Saint
				return 1;
			case 112: // Shillien Saint
				return 2;
			default:
				return -1;
		}
	}

	/***
	 * @param player проверяемый игрок
	 * @return {@code true} если у игрока есть неиспользованные предметы для скилл-линка
	 */
	private boolean hasTransferSkillItems(L2PcInstance player)
	{
		int itemId;
		switch(player.getClassId())
		{
			case cardinal:
				itemId = 15307;
				break;
			case evaSaint:
				itemId = 15308;
				break;
			case shillienSaint:
				itemId = 15309;
				break;
			default:
				itemId = -1;
		}
		return player.getInventory().getCountOf(itemId) > 0;
	}
}