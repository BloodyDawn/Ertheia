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
package dwo.gameserver.model.items.base.instance;

import dwo.gameserver.model.items.base.L2Henna;
import dwo.gameserver.model.skills.base.L2Skill;

/**
 * This class represents a Non-Player-Character in the world. it can be
 * a monster or a friendly character.
 * it also uses a template to fetch some static values.
 * the templates are hardcoded in the client, so we can rely on them.
 *
 * @version $Revision$ $Date$
 */

public class L2HennaInstance
{
	//private static Logger _log = LogManager.getLogger(L2HennaInstance.class);

	private L2Henna _template;
	private int _symbolId;
	private int _itemIdDye;
	private int _price;
	private int _statINT;
	private int _statSTR;
	private int _statCON;
	private int _statMEN;
	private int _statDEX;
	private int _statWIT;
    private int _statLUC;
    private int _statCHA;
	private int _amountDyeRequire;
	private L2Skill _attrSkill;

	public L2HennaInstance(L2Henna template)
	{
		_template = template;
		_symbolId = _template.getSymbolId();
		_itemIdDye = _template.getDyeId();
		_amountDyeRequire = _template.getAmountDyeRequire();
		_price = _template.getPrice();
		_statINT = _template.getStatINT();
		_statSTR = _template.getStatSTR();
		_statCON = _template.getStatCON();
		_statMEN = _template.getStatMEN();
		_statDEX = _template.getStatDEX();
		_statWIT = _template.getStatWIT();
        _statLUC = _template.getStatLUC();
        _statCHA = _template.getStatCHA();
		_attrSkill = _template.getAttrSkill();
	}

	public String getName()
	{
		String res = "";
		if(_statINT > 0)
		{
			res = res + "INT +" + _statINT;
		}
		else if(_statSTR > 0)
		{
			res = res + "STR +" + _statSTR;
		}
		else if(_statCON > 0)
		{
			res = res + "CON +" + _statCON;
		}
		else if(_statMEN > 0)
		{
			res = res + "MEN +" + _statMEN;
		}
		else if(_statDEX > 0)
		{
			res = res + "DEX +" + _statDEX;
		}
		else if(_statWIT > 0)
		{
			res = res + "WIT +" + _statWIT;
		}
        else if(_statLUC > 0)
        {
            res = res + "LUC +" + _statLUC;
        }
        else if(_statCHA > 0)
        {
            res = res + "CHA +" + _statCHA;
        }

		if(_statINT < 0)
		{
			res = res + ", INT " + _statINT;
		}
		else if(_statSTR < 0)
		{
			res = res + ", STR " + _statSTR;
		}
		else if(_statCON < 0)
		{
			res = res + ", CON " + _statCON;
		}
		else if(_statMEN < 0)
		{
			res = res + ", MEN " + _statMEN;
		}
		else if(_statDEX < 0)
		{
			res = res + ", DEX " + _statDEX;
		}
		else if(_statWIT < 0)
		{
			res = res + ", WIT " + _statWIT;
		}
        else if(_statLUC < 0)
        {
            res = res + ", LUC " + _statLUC;
        }
        else if(_statCHA < 0)
        {
            res = res + ", CHA " + _statCHA;
        }

		return res;
	}

	public L2Henna getTemplate()
	{
		return _template;
	}

	public int getSymbolId()
	{
		return _symbolId;
	}

	public void setSymbolId(int SymbolId)
	{
		_symbolId = SymbolId;
	}

	public int getItemIdDye()
	{
		return _itemIdDye;
	}

	public void setItemIdDye(int ItemIdDye)
	{
		_itemIdDye = ItemIdDye;
	}

	public int getAmountDyeRequire()
	{
		return _amountDyeRequire;
	}

	public void setAmountDyeRequire(int AmountDyeRequire)
	{
		_amountDyeRequire = AmountDyeRequire;
	}

	public int getPrice()
	{
		return _price;
	}

	public void setPrice(int Price)
	{
		_price = Price;
	}

	public int getStatINT()
	{
		return _statINT;
	}

	public void setStatINT(int StatINT)
	{
		_statINT = StatINT;
	}

	public int getStatSTR()
	{
		return _statSTR;
	}

	public void setStatSTR(int StatSTR)
	{
		_statSTR = StatSTR;
	}

	public int getStatCON()
	{
		return _statCON;
	}

	public void setStatCON(int StatCON)
	{
		_statCON = StatCON;
	}

	public int getStatMEN()
	{
		return _statMEN;
	}

	public void setStatMEN(int StatMEN)
	{
		_statMEN = StatMEN;
	}

	public int getStatDEX()
	{
		return _statDEX;
	}

	public void setStatDEX(int StatDEX)
	{
		_statDEX = StatDEX;
	}

	public int getStatWIT()
	{
		return _statWIT;
	}

	public void setStatWIT(int StatWIT)
	{
		_statWIT = StatWIT;
	}

    public int getStatLUC()
    {
        return _statLUC;
    }

    public void setStatLUC(int StatLUC)
    {
        _statLUC = StatLUC;
    }

    public int getStatCHA()
    {
        return _statCHA;
    }

    public void setStatCHA(int StatCHA)
    {
        _statCHA = StatCHA;
    }

	public L2Skill getAttrSkill()
	{
		return _attrSkill;
	}

	public void setAttrSkill(L2Skill skill)
	{
		_attrSkill = skill;
	}
}
