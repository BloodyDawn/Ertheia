package dwo.gameserver.network.game.serverpackets.packet.enchant.skill;

import dwo.gameserver.datatables.xml.EnchantSkillGroupsTable;
import dwo.gameserver.model.skills.L2EnchantSkillGroup.EnchantSkillDetail;
import dwo.gameserver.model.skills.L2EnchantSkillLearn;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

public class ExEnchantSkillInfo extends L2GameServerPacket
{
	private final int _id;
	private final int _lvl;
	private FastList<Integer> _routes; //skill lvls for each route
	private boolean _maxEnchanted;

	public ExEnchantSkillInfo(int id, int lvl)
	{
		_routes = new FastList<>();
		_id = id;
		_lvl = lvl;

		L2EnchantSkillLearn enchantLearn = EnchantSkillGroupsTable.getInstance().getSkillEnchantmentBySkillId(_id);
		// do we have this skill?
		if(enchantLearn != null)
		{
			// skill already enchanted?
			if(_lvl > 100)
			{
				_maxEnchanted = enchantLearn.isMaxEnchant(_lvl);

				// get detail for next level
				EnchantSkillDetail esd = enchantLearn.getEnchantSkillDetail(_lvl);

				// if it exists add it
				if(esd != null)
				{
					_routes.add(_lvl); // current enchant add firts
				}

				int skillLvL = _lvl % 100;

				for(int route : enchantLearn.getAllRoutes())
				{
					if(route * 100 + skillLvL == _lvl) // skip current
					{
						continue;
					}
					// add other levels of all routes - same lvl as enchanted
					// lvl
					_routes.add(route * 100 + skillLvL);
				}

			}
			else // not already enchanted
			{
				for(int route : enchantLearn.getAllRoutes())
				{
					// add first level (+1) of all routes
					_routes.add(route * 100 + 1);
				}
			}
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_id);
		writeD(_lvl);
		writeD(_maxEnchanted ? 0 : 1);
		writeD(_lvl > 100 ? 1 : 0); // enchanted?
		writeD(_routes.size());
		_routes.forEach(this::writeD);
	}
}