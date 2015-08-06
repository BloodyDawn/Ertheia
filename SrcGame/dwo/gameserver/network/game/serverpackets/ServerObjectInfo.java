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
package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.holders.WorldStatisticStatueHolder;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.world.zone.Location;

public class ServerObjectInfo extends L2GameServerPacket
{
	private L2Npc _activeChar;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _idTemplate;
	private boolean _isAttackable;
	private boolean _isWorldStatStatue;
	private double _collisionHeight;
	private double _collisionRadius;
	private String _name;

	public ServerObjectInfo(L2Npc activeChar, L2Character actor)
	{
		if(activeChar.getTemplate() instanceof WorldStatisticStatueHolder)
		{
			ServerObjectInfo(activeChar, activeChar.getLoc());
			return;
		}

		_activeChar = activeChar;
		_idTemplate = _activeChar.getTemplate().getIdTemplate();
		_isAttackable = _activeChar.isAutoAttackable(actor);
		_collisionHeight = _activeChar.getCollisionHeight();
		_collisionRadius = _activeChar.getCollisionRadius();
		_x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_name = _activeChar.getTemplate().isServerSideName() ? _activeChar.getTemplate().getName() : "";
		_isWorldStatStatue = false;
	}

	/**
	 * Спаун статуй мировой статистики
	 * @param loc
	 */
	public void ServerObjectInfo(L2Npc activeChar, Location loc)
	{
		_activeChar = activeChar;
		_idTemplate = 0;
		_isAttackable = false;
		_isWorldStatStatue = true;
		_collisionHeight = 0;
		_collisionRadius = 0;
		_x = activeChar.getX();
		_y = activeChar.getY();
		_z = activeChar.getZ();
		_heading = activeChar.getHeading();
		_name = activeChar.getName();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_activeChar.getObjectId());  //objId для статуй свой.
		writeD(_isWorldStatStatue ? 0 : _idTemplate + 1000000); //Для статуй приходит 0
		writeS(_name); // name
		writeD(_isAttackable ? 1 : 0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeF(1.0); // movement multiplier
		writeF(1.0); // attack speed multiplier

		if(_isWorldStatStatue)
		{
			// Как не странно но для всех статуй 1 и теже колизии
			writeF(30.0);
			writeF(40.0);
		}
		else
		{
			writeF(_collisionRadius);
			writeF(_collisionHeight);
		}

		writeD((int) (_isAttackable ? _activeChar.getCurrentHp() : 0));
		writeD(_isAttackable ? _activeChar.getMaxVisibleHp() : 0);
		writeD(_isWorldStatStatue ? 0x07 : 0x01); // Для статуй приходит 0x07
		writeD(0x00); // special effects

		if(_isWorldStatStatue)
		{
			WorldStatisticStatueHolder template = (WorldStatisticStatueHolder) _activeChar.getTemplate();
			// New For GoD
			writeD(template.getCategory().getClientId()); // категория, в которой занял первое место
			writeD(0x00); // unk во всех статуях 0
			writeD(template.getSocialId()); // socialID
			writeD(0x00); // socialFrame видел 0 8 9 14 16 19 20 30 39
			writeD(ClassId.getClassId(template.getClassId()).getId()); // classId
			writeD(template.getRaceId()); // раса
			writeD(Integer.parseInt(template.getSex())); // пол
			writeD(template.getHairStyle()); // прическа
			writeD(template.getHairColor()); // цвет прически
			writeD(template.getFace()); // лицо
			writeD(template.getNecklace()); // ожерелье
			writeD(template.getHead()); // шлем
			writeD(template.getRightHand()); // оружие правая рука
			writeD(template.getLeftHand()); // щит\сигель
			writeD(template.getGloves()); // перчатки
			writeD(template.getArmor()); // туника\броня
			writeD(template.getLegs()); // штаны
			writeD(template.getFeet()); // боты
			writeD(0x00); // TODO плащ
			// Украшение на голову
			writeD(template.getAccessory(1));
			writeD(template.getAccessory(2));
		}
	}
}
