package dwo.gameserver.network.game.clientpackets.packet.enchant.skill;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.EnchantSkillGroupsTable;
import dwo.gameserver.engine.logengine.formatters.EnchantLogFormatter;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.L2ShortCut;
import dwo.gameserver.model.skills.L2EnchantSkillGroup.EnchantSkillDetail;
import dwo.gameserver.model.skills.L2EnchantSkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.enchant.skill.ExEnchantSkillInfo;
import dwo.gameserver.network.game.serverpackets.packet.enchant.skill.ExEnchantSkillInfoDetail;
import dwo.gameserver.network.game.serverpackets.packet.enchant.skill.ExEnchantSkillResult;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class RequestExEnchantSkill extends L2GameClientPacket
{
	private static final Logger _logEnchant = LogManager.getLogger("enchantSkill");

	private int _type;
	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_type = readD();
		_skillId = readD();
		_skillLvl = readD();
	}

	@Override
	protected void runImpl()
	{
		if(_skillId <= 0 || _skillLvl <= 0) // minimal sanity check
		{
			return;
		}

		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(player.getClassId().level() < 3) // requires to have 3rd class quest completed
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_IN_THIS_CLASS);
			return;
		}

		if(player.getLevel() < 76)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ON_THIS_LEVEL);
			return;
		}

		if(!player.isAllowedToEnchantSkills())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ATTACKING_TRANSFORMED_BOAT);
			return;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		if(skill == null)
		{
			return;
		}

		L2EnchantSkillLearn s = EnchantSkillGroupsTable.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if(s == null)
		{
			return;
		}

		boolean bless = false;
		boolean safe = false;
		int costMultiplier = EnchantSkillGroupsTable.NORMAL_ENCHANT_COST_MULTIPLIER;
		int bookId;

		switch(_type)
		{
			case EnchantSkillGroupsTable.TYPE_NORMAL_ENCHANT:
				bookId = s.getGroupId() != 7 ? EnchantSkillGroupsTable.NORMAL_ENCHANT_BOOK : EnchantSkillGroupsTable.AWAKED_NORMAL_ENCHANT_BOOK;
				break;
			case EnchantSkillGroupsTable.TYPE_SAFE_ENCHANT:
				safe = true;
				bookId = s.getGroupId() != 7 ? EnchantSkillGroupsTable.SAFE_ENCHANT_BOOK : EnchantSkillGroupsTable.AWAKED_SAFE_ENCHANT_BOOK;
				break;
			case EnchantSkillGroupsTable.TYPE_PASS_TICKET:
				safe = true;
				bless = true;
				bookId = EnchantSkillGroupsTable.AWAKED_ENCHANT_PASS_TICKET;
				break;
			case EnchantSkillGroupsTable.TYPE_CHANGE_ENCHANT:
				bookId = s.getGroupId() != 7 ? EnchantSkillGroupsTable.CHANGE_ENCHANT_BOOK : EnchantSkillGroupsTable.AWAKED_CHANGE_ENCHANT_BOOK;
				break;
			case EnchantSkillGroupsTable.TYPE_UNTRAIN_ENCHANT:
				bookId = s.getGroupId() != 7 ? EnchantSkillGroupsTable.UNTRAIN_ENCHANT_BOOK : EnchantSkillGroupsTable.AWAKED_UNTRAIN_ENCHANT_BOOK;
				break;
			default:
				return;
		}

		if(_type == EnchantSkillGroupsTable.TYPE_UNTRAIN_ENCHANT)
		{
			int beforeUntrainSkillLevel = player.getSkillLevel(_skillId);
			if(beforeUntrainSkillLevel - 1 != _skillLvl && (beforeUntrainSkillLevel % 100 != 1 || _skillLvl != s.getBaseLevel()))
			{
				return;
			}

			EnchantSkillDetail esd = s.getEnchantSkillDetail(beforeUntrainSkillLevel);

			int requiredSp = esd.getSpCost();
			int requireditems = esd.getAdenaCost();

			L2ItemInstance spb = player.getInventory().getItemByItemId(bookId);
			if(Config.ES_SP_BOOK_NEEDED)
			{
				if(spb == null) // Haven't spellbook
				{
					player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}
			}

			if(player.getInventory().getAdenaCount() < requireditems)
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}

			boolean check = true;
			if(Config.ES_SP_BOOK_NEEDED)
			{
				check &= player.destroyItem(ProcessType.CONSUME, spb.getObjectId(), 1, player, true);
			}

			check &= player.destroyItemByItemId(ProcessType.CONSUME, PcInventory.ADENA_ID, requireditems, player, true);

			if(!check)
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}

			player.getStat().addSp((int) (requiredSp * 0.8));

			if(Config.LOG_SKILL_ENCHANTS)
			{
				_logEnchant.log(Level.INFO, EnchantLogFormatter.format("Untrain", new Object[]{player, skill, spb}));
			}

			player.addSkill(skill, true);
			player.sendPacket(ExEnchantSkillResult.valueOf(true));

			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requireditems + " Adena.");
			}

			player.sendUserInfo();

			if(_skillLvl > 100)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_DECREASED_BY_ONE).addSkillName(_skillId));
			}
			else
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_RESETED).addSkillName(_skillId));
			}
			player.sendSkillList();
			int afterUntrainSkillLevel = player.getSkillLevel(_skillId);
			player.sendPacket(new ExEnchantSkillInfo(_skillId, afterUntrainSkillLevel));
			player.sendPacket(new ExEnchantSkillInfoDetail(2, _skillId, afterUntrainSkillLevel - 1, player));
			player.getShortcutController().updateShortcuts(_skillId, afterUntrainSkillLevel, L2ShortCut.ShortcutType.SKILL);
		}
		else if(_type == EnchantSkillGroupsTable.TYPE_CHANGE_ENCHANT)
		{
			int beforeEnchantSkillLevel = player.getSkillLevel(_skillId);
			// do u have this skill enchanted?
			if(beforeEnchantSkillLevel <= 100)
			{
				return;
			}

			int currentEnchantLevel = beforeEnchantSkillLevel % 100;
			// is the requested level valid?
			if(currentEnchantLevel != _skillLvl % 100)
			{
				return;
			}
			EnchantSkillDetail esd = s.getEnchantSkillDetail(_skillLvl);

			int requiredSp = esd.getSpCost();
			int requireditems = esd.getAdenaCost();

			if(player.getSp() >= requiredSp)
			{
				// only first lvl requires book
				L2ItemInstance spb = player.getInventory().getItemByItemId(bookId);
				if(Config.ES_SP_BOOK_NEEDED)
				{
					if(spb == null)// Haven't spellbook
					{
						player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_ITENS_NEEDED_TO_CHANGE_SKILL_ENCHANT_ROUTE);
						return;
					}
				}

				if(player.getInventory().getAdenaCount() < requireditems)
				{
					player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}

				boolean check;
				check = player.getStat().removeExpAndSp(0, requiredSp, false);
				if(Config.ES_SP_BOOK_NEEDED)
				{
					check &= player.destroyItem(ProcessType.CONSUME, spb.getObjectId(), 1, player, true);
				}

				check &= player.destroyItemByItemId(ProcessType.CONSUME, PcInventory.ADENA_ID, requireditems, player, true);

				if(!check)
				{
					player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}

				int levelPenalty = Rnd.get(Math.min(4, currentEnchantLevel));
				_skillLvl -= levelPenalty;
				if(_skillLvl % 100 == 0)
				{
					_skillLvl = s.getBaseLevel();
				}

				skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);

				if(skill != null)
				{
					if(Config.LOG_SKILL_ENCHANTS)
					{
						_logEnchant.log(Level.INFO, EnchantLogFormatter.format("Route Change", new Object[]{
							player, skill, spb
						}));
					}

					player.addSkill(skill, true);
					player.sendPacket(ExEnchantSkillResult.valueOf(true));
				}

				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requireditems + " Adena.");
				}

				player.sendUserInfo();

				if(levelPenalty == 0)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WILL_REMAIN).addSkillName(_skillId));
				}
				else
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WAS_DECREASED_BY_S2).addSkillName(_skillId).addNumber(levelPenalty));
				}

				player.sendSkillList();
				int afterEnchantSkillLevel = player.getSkillLevel(_skillId);
				player.sendPacket(new ExEnchantSkillInfo(_skillId, afterEnchantSkillLevel));
				player.sendPacket(new ExEnchantSkillInfoDetail(3, _skillId, afterEnchantSkillLevel, player));
				player.getShortcutController().updateShortcuts(_skillId, afterEnchantSkillLevel, L2ShortCut.ShortcutType.SKILL);
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			}

		}
		else   // Обычная точка
		{
			EnchantSkillDetail esd = s.getEnchantSkillDetail(_skillLvl);
			int beforeEnchantSkillLevel = player.getSkillLevel(_skillId);
			if(beforeEnchantSkillLevel != s.getMinSkillLevel(_skillLvl))
			{
				return;
			}

			int requiredSp = esd.getSpCost() * costMultiplier;
			int requireditems = esd.getAdenaCost() * costMultiplier;

			if(bless)
			{
				requiredSp = 0;
				requireditems = 0;
			}

			if(player.getSp() >= requiredSp)
			{
				// Если точим обычными забираем книгу при 1 заточке
				// Если же благ точками то забираем при каждом
				boolean usesBook = _skillLvl % 100 == 1 || safe;
				L2ItemInstance spb = player.getInventory().getItemByItemId(bookId);

				if(Config.ES_SP_BOOK_NEEDED && usesBook)
				{
					if(spb == null)// Haven't spellbook
					{
						player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
						return;
					}
				}

				if(player.getInventory().getAdenaCount() < requireditems)
				{
					player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}

				boolean check = bless || player.getStat().removeExpAndSp(0, requiredSp, false);
				if(Config.ES_SP_BOOK_NEEDED && usesBook)
				{
					check &= player.destroyItem(ProcessType.CONSUME, spb.getObjectId(), 1, player, true);
				}

				check &= bless || player.destroyItemByItemId(ProcessType.CONSUME, PcInventory.ADENA_ID, requireditems, player, true);

				if(!check)
				{
					player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}
				// Заточка скила
				int rate = bless ? 100 : esd.getRate(player);
				if(Rnd.getChance(rate))
				{
					if(Config.LOG_SKILL_ENCHANTS)
					{
						_logEnchant.log(Level.INFO, EnchantLogFormatter.format("Success", new Object[]{
							player, skill, spb, rate
						}));
					}

					player.addSkill(skill, true);
					player.sendPacket(ExEnchantSkillResult.valueOf(true));
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1).addSkillName(_skillId));
				}
				else
				{
					if(Config.LOG_SKILL_ENCHANTS)
					{
						_logEnchant.log(Level.INFO, EnchantLogFormatter.format("Fail", new Object[]{
							player, skill, spb, rate
						}));
					}

					if(safe)
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_FAILED_S1_LEVEL_WILL_REMAIN).addSkillName(_skillId));
					}
					else
					{
						player.addSkill(SkillTable.getInstance().getInfo(_skillId, s.getBaseLevel()), true);
						player.sendPacket(SystemMessageId.YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1);
					}

					player.sendPacket(ExEnchantSkillResult.valueOf(false));
				}

				player.sendUserInfo();
				player.sendSkillList();
				int afterEnchantSkillLevel = player.getSkillLevel(_skillId);
				player.sendPacket(new ExEnchantSkillInfo(_skillId, afterEnchantSkillLevel));
				player.sendPacket(new ExEnchantSkillInfoDetail(0, _skillId, afterEnchantSkillLevel + 1, player));
				player.getShortcutController().updateShortcuts(_skillId, afterEnchantSkillLevel, L2ShortCut.ShortcutType.SKILL);
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:07 RequestExEnchantSkill";
	}
}