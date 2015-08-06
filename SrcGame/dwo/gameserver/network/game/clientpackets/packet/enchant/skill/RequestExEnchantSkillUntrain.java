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
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class RequestExEnchantSkillUntrain extends L2GameClientPacket
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

		L2EnchantSkillLearn s = EnchantSkillGroupsTable.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if(s == null)
		{
			return;
		}

		if(_skillLvl % 100 == 0)
		{
			_skillLvl = s.getBaseLevel();
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		if(skill == null)
		{
			return;
		}

		int beforeUntrainSkillLevel = player.getSkillLevel(_skillId);
		if(beforeUntrainSkillLevel - 1 != _skillLvl && (beforeUntrainSkillLevel % 100 != 1 || _skillLvl != s.getBaseLevel()))
		{
			return;
		}

		EnchantSkillDetail esd = s.getEnchantSkillDetail(beforeUntrainSkillLevel);

		int requiredSp = esd.getSpCost();
		int requireditems = esd.getAdenaCost();

		int reqItemId;
		reqItemId = s.getGroupId() != 7 ? EnchantSkillGroupsTable.UNTRAIN_ENCHANT_BOOK : EnchantSkillGroupsTable.AWAKED_UNTRAIN_ENCHANT_BOOK;

		L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
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

	@Override
	public String getType()
	{
		return "[C] D0:33 RequestExEnchantSkillUntrain";
	}
}
