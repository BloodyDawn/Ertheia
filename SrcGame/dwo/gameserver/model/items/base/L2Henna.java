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
package dwo.gameserver.model.items.base;

import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;

public class L2Henna
{
	private final int symbolId;
	private final int dye;
	private final int price;
	private final int amount;
	private final int statINT;
	private final int statSTR;
	private final int statCON;
	private final int statMEN;
	private final int statDEX;
	private final int statWIT;
    private final int statLUC;
    private final int statCHA;

	private final L2Skill attrSkill;

	public L2Henna(StatsSet set)
	{
		symbolId = set.getInteger("symbol_id");
		dye = set.getInteger("dye");
		price = set.getInteger("price");
		amount = set.getInteger("amount");
		statINT = set.getInteger("stat_INT");
		statSTR = set.getInteger("stat_STR");
		statCON = set.getInteger("stat_CON");
		statMEN = set.getInteger("stat_MEN");
		statDEX = set.getInteger("stat_DEX");
		statWIT = set.getInteger("stat_WIT");
        statLUC = set.getInteger("stat_LUC");
        statCHA = set.getInteger("stat_CHA");

		attrSkill = set.getInteger("attributeSkillId") != 0 ? SkillTable.getInstance().getInfo(set.getInteger("attributeSkillId"), 1) : null;
	}

	public int getSymbolId()
	{
		return symbolId;
	}

	public int getDyeId()
	{
		return dye;
	}

	public int getPrice()
	{
		return price;
	}

	public int getAmountDyeRequire()
	{
		return amount;
	}

	public int getStatINT()
	{
		return statINT;
	}

	public int getStatSTR()
	{
		return statSTR;
	}

	public int getStatCON()
	{
		return statCON;
	}

	public int getStatMEN()
	{
		return statMEN;
	}

	public int getStatDEX()
	{
		return statDEX;
	}

	public int getStatWIT()
	{
		return statWIT;
	}

    public int getStatLUC()
    {
        return statLUC;
    }

    public int getStatCHA()
    {
        return statCHA;
    }

	public L2Skill getAttrSkill()
	{
		return attrSkill;
	}
}
