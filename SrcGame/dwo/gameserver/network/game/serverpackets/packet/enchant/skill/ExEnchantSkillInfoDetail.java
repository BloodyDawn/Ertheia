package dwo.gameserver.network.game.serverpackets.packet.enchant.skill;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.EnchantSkillGroupsTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.skills.L2EnchantSkillGroup.EnchantSkillDetail;
import dwo.gameserver.model.skills.L2EnchantSkillLearn;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author KenM
 */

public class ExEnchantSkillInfoDetail extends L2GameServerPacket
{
	private final int _type;
	private final int _skillid;
	private final int _skilllvl;
	private int bookId;
	private int reqCount;
	private int multi = 1;
	private int _chance;
	private int _sp;
	private int _adenacount;

	public ExEnchantSkillInfoDetail(int type, int skillid, int skilllvl, L2PcInstance ply)
	{

		L2EnchantSkillLearn enchantLearn = EnchantSkillGroupsTable.getInstance().getSkillEnchantmentBySkillId(skillid);
		EnchantSkillDetail esd = null;
		// do we have this skill?
		if(enchantLearn != null)
		{
			esd = skilllvl > 100 ? enchantLearn.getEnchantSkillDetail(skilllvl) : enchantLearn.getFirstRouteGroup().getEnchantGroupDetails().get(0);
		}

		if(esd == null)
		{
			throw new IllegalArgumentException("Skill " + skillid + " dont have enchant data for level " + skilllvl);
		}

		if(type == 0)
		{
			multi = EnchantSkillGroupsTable.NORMAL_ENCHANT_COST_MULTIPLIER;
		}
		else if(type == 1)
		{
			multi = EnchantSkillGroupsTable.SAFE_ENCHANT_COST_MULTIPLIER;
		}
		_chance = esd.getRate(ply);
		_sp = esd.getSpCost();
		if(type == EnchantSkillGroupsTable.TYPE_UNTRAIN_ENCHANT)
		{
			_sp = (int) (0.8 * _sp);
		}
		_adenacount = esd.getAdenaCost() * multi;
		_type = type;
		_skillid = skillid;
		_skilllvl = skilllvl;

		int group = enchantLearn.getGroupId();

		switch(type)
		{
			case EnchantSkillGroupsTable.TYPE_NORMAL_ENCHANT:
				bookId = group != 7 ? EnchantSkillGroupsTable.NORMAL_ENCHANT_BOOK : EnchantSkillGroupsTable.AWAKED_NORMAL_ENCHANT_BOOK;
				reqCount = _skilllvl % 100 > 1 ? 0 : 1;
				break;
			case EnchantSkillGroupsTable.TYPE_SAFE_ENCHANT:
				bookId = group != 7 ? EnchantSkillGroupsTable.SAFE_ENCHANT_BOOK : EnchantSkillGroupsTable.AWAKED_SAFE_ENCHANT_BOOK;
				reqCount = 1;
				break;
			case EnchantSkillGroupsTable.TYPE_UNTRAIN_ENCHANT:
				bookId = group != 7 ? EnchantSkillGroupsTable.UNTRAIN_ENCHANT_BOOK : EnchantSkillGroupsTable.AWAKED_UNTRAIN_ENCHANT_BOOK;
				reqCount = 1;
				break;
			case EnchantSkillGroupsTable.TYPE_CHANGE_ENCHANT:
				bookId = group != 7 ? EnchantSkillGroupsTable.CHANGE_ENCHANT_BOOK : EnchantSkillGroupsTable.AWAKED_CHANGE_ENCHANT_BOOK;
				reqCount = 1;
				break;
			case EnchantSkillGroupsTable.TYPE_PASS_TICKET:
				bookId = EnchantSkillGroupsTable.AWAKED_ENCHANT_PASS_TICKET;
				_sp = 0;
				_adenacount = 0;
				_chance = 100;
				reqCount = 1;
				break;
			default:
				return;
		}

		if(type != EnchantSkillGroupsTable.TYPE_SAFE_ENCHANT && !Config.ES_SP_BOOK_NEEDED)
		{
			reqCount = 0;
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_type);
		writeD(_skillid);
		writeD(_skilllvl);
		writeD(_sp * multi); // sp
		writeD(_chance); // exp
		writeD(2); // items count?
		writeD(PcInventory.ADENA_ID); // adena //TODO unhardcode me
		writeD(_adenacount); // adena count
		writeD(bookId); // ItemId Required
		writeD(reqCount);
	}
}
