/*
 * $Header: MultiSellList.java, 2/08/2005 14:21:01 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 2/08/2005 14:21:01 $
 * $Revision: 1 $
 * $Log: MultiSellList.java,v $
 * Revision 1  2/08/2005 14:21:01  luisantonioa
 * Added copyright notice
 *
 *
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
package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.items.multisell.Entry;
import dwo.gameserver.model.items.multisell.Ingredient;
import dwo.gameserver.model.items.multisell.ListContainer;

import static dwo.gameserver.datatables.xml.MultiSellData.PAGE_SIZE;

public class MultiSellList extends L2GameServerPacket {
    private final ListContainer _list;
    private final boolean _finished;
    private int _size;
    private int _index;

    public MultiSellList(ListContainer list, int index) {
        _list = list;
        _index = index;

        _size = list.getEntries().size() - index;
        if (_size > PAGE_SIZE) {
            _finished = false;
            _size = PAGE_SIZE;
        } else {
            _finished = true;
        }
    }

    @Override
    protected void writeImpl()
    {
        writeD(_list.getListId());    // list id
        writeC(_list.isShowAll() ? 1 : 0);  // showType
        writeD(1 + _index / PAGE_SIZE); // page started from 1
        writeD(_finished ? 1 : 0);    // finished
        writeD(PAGE_SIZE);    // size of pages
        writeD(_size); //list length
        writeC(_list.isChanceBuy() ? 1 : 0); // Тип мультиселла
        Entry ent;
        while (_size-- > 0)
        {
            ent = _list.getEntries().get(_index++);
            writeD(ent.getEntryId());
            writeC(ent.isStackable() ? 1 : 0);
            writeH(0x00);
            writeD(0x00);
            writeD(0x00);
            writeH(0x00);
            writeH(0x00);
            writeH(0x00);
            writeH(0x00);
            writeH(0x00);
            writeH(0x00);
            writeH(0x00);
            writeH(0x00);

            writeH(ent.getProducts().size());
            writeH(ent.getIngredients().size());

            for (Ingredient ing : ent.getProducts())
            {
                writeD(ing.getItemId());
                if (ing.getTemplate() != null)
                {
                    writeQ(ing.getTemplate().getBodyPart());
                    writeH(ing.getTemplate().getType2());
                }
                else
                {
                    writeQ(0);
                    writeH(65535);
                }
                writeQ(ing.getItemCount());
                if (ing.getItemInfo() != null)
                {
                    writeH(ing.getItemInfo().getEnchantLevel()); // enchant level
                    writeD(_list.isChanceBuy() ? ing.getChance() : ing.getItemInfo().getAugmentId()); // augment id
                    writeD(0x00); // mana
                    writeD(0x00); // time ?
                    writeH(ing.getItemInfo().getElementId()); // attack element
                    writeH(ing.getItemInfo().getElementPower()); // element power
                    writeH(ing.getItemInfo().getElementals()[0]); // fire
                    writeH(ing.getItemInfo().getElementals()[1]); // water
                    writeH(ing.getItemInfo().getElementals()[2]); // wind
                    writeH(ing.getItemInfo().getElementals()[3]); // earth
                    writeH(ing.getItemInfo().getElementals()[4]); // holy
                    writeH(ing.getItemInfo().getElementals()[5]); // dark
                }
                else
                {
                    writeH(0x00); // enchant level
                    writeD(ing.getChance()); // augment id
                    writeD(0x00); // mana
                    writeD(0x00); // time ?
                    writeH(0x00); // attack element
                    writeH(0x00); // element power
                    writeH(0x00); // fire
                    writeH(0x00); // water
                    writeH(0x00); // wind
                    writeH(0x00); // earth
                    writeH(0x00); // holy
                    writeH(0x00); // dark
                }
            }

            for (Ingredient ing : ent.getIngredients())
            {
                writeD(ing.getItemId());
                writeH(ing.getTemplate() != null ? ing.getTemplate().getType2() : 65535);
                writeQ(ing.getItemCount());
                if (ing.getItemInfo() != null)
                {
                    writeH(ing.getItemInfo().getEnchantLevel()); // enchant level
                    writeD(_list.isChanceBuy() ? ing.getChance() : ing.getItemInfo().getAugmentId()); // augment id
                    writeD(0x00); // mana
                    writeH(ing.getItemInfo().getElementId()); // attack element
                    writeH(ing.getItemInfo().getElementPower()); // element power
                    writeH(ing.getItemInfo().getElementals()[0]); // fire
                    writeH(ing.getItemInfo().getElementals()[1]); // water
                    writeH(ing.getItemInfo().getElementals()[2]); // wind
                    writeH(ing.getItemInfo().getElementals()[3]); // earth
                    writeH(ing.getItemInfo().getElementals()[4]); // holy
                    writeH(ing.getItemInfo().getElementals()[5]); // dark
                }
                else
                {
                    writeH(0x00); // enchant level
                    writeD(ing.getChance()); // augment id
                    writeD(0x00); // mana
                    writeH(0x00); // attack element
                    writeH(0x00); // element power
                    writeH(0x00); // fire
                    writeH(0x00); // water
                    writeH(0x00); // wind
                    writeH(0x00); // earth
                    writeH(0x00); // holy
                    writeH(0x00); // dark
                }
            }
        }
    }
}

