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
import dwo.gameserver.model.actor.L2Object;

/**
 * @author KenM
 */

public class FlyToLocation extends L2GameServerPacket
{
	private final int _destX;
	private final int _destY;
	private final int _destZ;
	private final int _chaObjId;
	private final int _chaX;
	private final int _chaY;
	private final int _chaZ;
	private final int _flySpeed;
	private final int _flyDelay;
	private final int _animationSpeed;
	private final FlyType _type;

	public FlyToLocation(L2Character cha, int destX, int destY, int destZ, FlyType type, int flySpeed, int flyDelay, int animationSpeed)
	{
		_chaObjId = cha.getObjectId();
		_chaX = cha.getX();
		_chaY = cha.getY();
		_chaZ = cha.getZ();
		_destX = destX;
		_destY = destY;
		_destZ = destZ;
		_type = type;
		_flySpeed = flySpeed;
		_flyDelay = flyDelay;
		_animationSpeed = animationSpeed;
	}

	public FlyToLocation(L2Character cha, L2Object dest, FlyType type, int flySpeed, int flyDelay, int animationSpeed)
	{
		this(cha, dest.getX(), dest.getY(), dest.getZ(), type, flySpeed, flyDelay, animationSpeed);
	}

	@Override
	protected void writeImpl()
	{
		writeD(_chaObjId);
		writeD(_destX);
		writeD(_destY);
		writeD(_destZ);
		writeD(_chaX);
		writeD(_chaY);
		writeD(_chaZ);
		writeD(_type.ordinal());
		writeD(_flySpeed); // Скорость движения
		writeD(_flyDelay); // Задержка перед началом движения чем меньше число тем дольше задержка.
		writeD(_animationSpeed); // speed animation ?? - на оффе при тесте нескольких скиллов все время приходило 333!  У цепи сигеля приходит 0! А у скиллов лука и птицы у лука 333!
	}

	public enum FlyType
	{
		THROW_UP, // Траектория полета по дуге.  ID = 0
		THROW_HORIZONTAL,  // Притягивание цели к себе. ID = 1
		DUMMY, // Блинк  ID = 2
		CHARGE, // Движение от точки А к точке В, с разворотом в сторону точки В  ID = 3
		PUSH_HORIZONTAL, // Отталкивает от цели. ID = 4
		JUMP_EFFECTED, // Прыжок от точки к точке с эффектом. ID = 5
		NOT_USED, // Крит клиента. ID = 6
		PUSH_DOWN_HORIZONTAL, // Сбивает с ног и отбрасывает назад. ID = 7
		WARP_BACK, // Движение от точки А к точке В, без разворота. Похоже на отталкивание. ID = 8
		WARP_FORWARD // Движение от точки А к точке В, без разворота. Похоже на отталкивание. ID = 9
	}
}
