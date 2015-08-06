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
import dwo.gameserver.model.items.base.proptypes.SoulshotGrade;

/**
 * sample
 * 06 8f19904b 2522d04b 00000000 80 950c0000 4af50000 08f2ffff 0000    - 0 damage (missed 0x80)
 * 06 85071048 bc0e504b 32000000 10 fc41ffff fd240200 a6f5ffff 0100 bc0e504b 33000000 10                                     3....
 * <p/>
 * format
 * dddc dddh (ddc)
 *
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class Attack extends L2GameServerPacket
{
	public static final int HITFLAG_USESS = 0x04;
	public static final int HITFLAG_CRIT = 0x20;
	public static final int HITFLAG_SHLD = 0x40;
	public static final int HITFLAG_MISS = 0x80;

	public static final int FLAG = 0x00;   //Обычный удар без надписей.
	public static final int FLAG_MISS = 0x01; //Увернулся от удара
	public static final int FLAG_BLOCK = 0x02; //Блокировал удар.
	public static final int FLAG_BLOCK_NONE = 0x03; //Блокировал удар.
	public static final int FLAG_CRIT = 0x04;    //Крит.
	public static final int FLAG_CRIT_NONE = 0x05;    //Крит.
	public static final int FLAG_SHIELD = 0x06;  //Заблокировал Крит.
	public static final int FLAG_SHIELD_NONE = 0x07;  //Заблокировал Крит.

	public static final int FLAG_SOULSHOT = 0x08; //Удар с соской.
	public static final int FLAG_SOULSHOT_MISS = 0x0b; //Удар с соской.
	public static final int SOULSHOT_FLAG_CRIT = 0x0c; //12 - Удар с соской и крит.

	//public static final int SOULSHOT_FLAG = 0x0a;
	//public static final int SOULSHOT_MISS = 0x0b;
	//public static final int SOULSHOT_CRIT = 0x0c;
	//public static final int SOULSHOT_NONE_TEXT = 0x0d;  //Похоже не используется
	//public static final int SOULSHOT_FLAG_TEXT = 0x0e;  //Похоже не используется
	public final boolean _useSoulshots;
	public final int _soulGrade;
	private final int _attackerObjId;
	private final int _targetObjId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _tx;
	private final int _ty;
	private final int _tz;
	private Hit[] _hits;

	/**
	 * @param attacker: the attacking L2Character<br>
	 * @param target:   the target L2Object<br>
	 * @param useShots: true if soulshots used
	 * @param soulGrade:  the grade of the soulshots
	 */
	public Attack(L2Character attacker, L2Object target, boolean useShots, SoulshotGrade soulGrade)
	{
		_attackerObjId = attacker.getObjectId();
		_targetObjId = target.getObjectId();
		_useSoulshots = useShots;
		_soulGrade = soulGrade.ordinal();
		_x = attacker.getX();
		_y = attacker.getY();
		_z = attacker.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
	}

	public Hit createHit(L2Object target, int damage, boolean miss, boolean crit, byte shld)
	{
		return new Hit(target, damage, miss, crit, shld);
	}

	public void hit(Hit... hits)
	{
		if(_hits == null)
		{
			_hits = hits;
			return;
		}

		// this will only happen with pole attacks
		Hit[] tmp = new Hit[hits.length + _hits.length];
		System.arraycopy(_hits, 0, tmp, 0, _hits.length);
		System.arraycopy(hits, 0, tmp, _hits.length, hits.length);
		_hits = tmp;
	}

	/**
	 * Return True if the ServerMode-Client packet Attack contains at least 1 hit.<BR><BR>
	 */
	public boolean hasHits()
	{
		return _hits != null;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_attackerObjId);
		writeD(_hits[0]._targetId);
		writeD(0x00);
		writeD(_hits[0]._damage);
		writeD(_hits[0]._flags);
		writeD(_soulGrade);
        
		writeD(_x);
		writeD(_y);
		writeD(_z);

		writeH(_hits.length - 1);
		// prevent sending useless packet while there is only one target.
		if(_hits.length > 1)
		{
			for(int i = 1; i < _hits.length; i++)
			{
				writeD(_hits[i]._targetId);
				writeD(_hits[i]._damage);
				writeD(_hits[i]._flags);
				writeD(_soulGrade);
			}
		}

		writeD(_tx);
		writeD(_ty);
		writeD(_tz);
	}

	public class Hit
	{
		protected final int _targetId;
		protected final int _damage;
		protected int _flags;

		Hit(L2Object target, int damage, boolean miss, boolean crit, byte shld)
		{
			_targetId = target.getObjectId();
			_damage = damage;

			if(!_useSoulshots)
			{
				_flags = FLAG;
			}
			if(!_useSoulshots && miss)
			{
				_flags = FLAG_MISS;
			}
			if(!_useSoulshots && crit)
			{
				_flags = FLAG_CRIT;
			}
			if(!_useSoulshots && shld > 0)
			{
				_flags = FLAG_SHIELD;
			}
			if(_useSoulshots)
			{
				_flags = FLAG_SOULSHOT;
			}
			if(_useSoulshots && miss)
			{
				_flags = FLAG_SOULSHOT_MISS;
			}
			if(_useSoulshots && crit)
			{
				_flags = SOULSHOT_FLAG_CRIT;
			}
		}
	}
}
