/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

public class RequestExEnchantSkillSafe extends L2GameClientPacket
{
	private static final Logger _logEnchant = LogManager.getLogger("enchantSkill");

	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
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
		int costMultiplier = EnchantSkillGroupsTable.SAFE_ENCHANT_COST_MULTIPLIER;
		int reqItemId;
		reqItemId = s.getGroupId() != 7 ? EnchantSkillGroupsTable.SAFE_ENCHANT_BOOK : EnchantSkillGroupsTable.AWAKED_SAFE_ENCHANT_BOOK;

		EnchantSkillDetail esd = s.getEnchantSkillDetail(_skillLvl);
		int beforeEnchantSkillLevel = player.getSkillLevel(_skillId);
		if(beforeEnchantSkillLevel != s.getMinSkillLevel(_skillLvl))
		{
			return;
		}

		int requiredSp = esd.getSpCost() * costMultiplier;
		int requireditems = esd.getAdenaCost() * costMultiplier;
		int rate = esd.getRate(player);

		if(player.getSp() >= requiredSp)
		{
			// No config option for safe enchant book consume
			L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
			if(spb == null)// Haven't spellbook
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}

			if(player.getInventory().getAdenaCount() < requireditems)
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}

			boolean check = player.getStat().removeExpAndSp(0, requiredSp, false);
			check &= player.destroyItem(ProcessType.CONSUME, spb.getObjectId(), 1, player, true);

			check &= player.destroyItemByItemId(ProcessType.CONSUME, PcInventory.ADENA_ID, requireditems, player, true);

			if(!check)
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}

			// ok. Destroy ONE copy of the book
			if(Rnd.getChance(rate))
			{
				if(Config.LOG_SKILL_ENCHANTS)
				{
					_logEnchant.log(Level.INFO, EnchantLogFormatter.format("Safe Success", new Object[]{
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
					_logEnchant.log(Level.INFO, EnchantLogFormatter.format("Safe Fail", new Object[]{
						player, skill, spb, rate
					}));
				}

				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_FAILED_S1_LEVEL_WILL_REMAIN).addSkillName(_skillId));
				player.sendPacket(ExEnchantSkillResult.valueOf(false));
			}

			player.sendUserInfo();
			player.sendSkillList();
			int afterEnchantSkillLevel = player.getSkillLevel(_skillId);
			player.sendPacket(new ExEnchantSkillInfo(_skillId, afterEnchantSkillLevel));
			player.sendPacket(new ExEnchantSkillInfoDetail(1, _skillId, afterEnchantSkillLevel + 1, player));
			player.getShortcutController().updateShortcuts(_skillId, afterEnchantSkillLevel, L2ShortCut.ShortcutType.SKILL);
		}
		else
		{
			player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:32 RequestExEnchantSkillSafe";
	}
}
