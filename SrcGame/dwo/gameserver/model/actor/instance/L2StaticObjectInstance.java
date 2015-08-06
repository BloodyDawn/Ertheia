/*
 * $Header: /cvsroot/l2j/L2_Gameserver/java/net/sf/l2j/gameserver/model/L2StaticObjectInstance.java,v 1.3.2.2.2.2 2005/02/04 13:05:27 maximas Exp $
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
package dwo.gameserver.model.actor.instance;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.ai.L2CharacterAI;
import dwo.gameserver.model.actor.knownlist.StaticObjectKnownList;
import dwo.gameserver.model.actor.stat.StaticObjStat;
import dwo.gameserver.model.actor.status.StaticObjStatus;
import dwo.gameserver.model.actor.templates.L2CharTemplate;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.StaticObject;
import dwo.gameserver.network.game.serverpackets.packet.show.ShowTownMap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * GODSON ROX!
 */
public class L2StaticObjectInstance extends L2Character
{
	/**
	 * The interaction distance of the L2StaticObjectInstance
	 */
	public static final int INTERACTION_DISTANCE = 150;
	protected static final Logger log = LogManager.getLogger(L2StaticObjectInstance.class);
	private int _staticObjectId;
	private int _meshIndex;     // 0 - static objects, alternate static objects
	private int _type = -1;         // 0 - map signs, 1 - throne , 2 - arena signs
	private ShowTownMap _map;

	/**
	 * @param objectId
	 * @param template
	 * @param staticId
	 */
	public L2StaticObjectInstance(int objectId, L2CharTemplate template, int staticId)
	{
		super(objectId, template);
		_staticObjectId = staticId;
	}

	@Override
	public L2CharacterAI getAI()
	{
		return null;
	}

	@Override
	public StaticObjStat getStat()
	{
		return (StaticObjStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		setStat(new StaticObjStat(this));
	}

	@Override
	public StaticObjStatus getStatus()
	{
		return (StaticObjStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new StaticObjStatus(this));
	}

	@Override
	public void updateAbnormalEffect()
	{
	}

	/**
	 * Return null.<BR><BR>
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public StaticObjectKnownList getKnownList()
	{
		return (StaticObjectKnownList) super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new StaticObjectKnownList(this));
	}

	@Override
	public int getLevel()
	{
		return 1;
	}

	/**
	 * @return Returns the StaticObjectId.
	 */
	public int getStaticObjectId()
	{
		return _staticObjectId;
	}

	public int getType()
	{
		return _type;
	}

	public void setType(int type)
	{
		_type = type;
	}

	public void setMap(String texture, int x, int y)
	{
		_map = new ShowTownMap("town_map." + texture, x, y);
	}

	public ShowTownMap getMap()
	{
		return _map;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new StaticObject(this));
	}

	/**
	 * Return the meshIndex of the object.<BR><BR>
	 * <p/>
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li> default textures : 0</li>
	 * <li> alternate textures : 1 </li><BR><BR>
	 */
	public int getMeshIndex()
	{
		return _meshIndex;
	}

	/**
	 * Set the meshIndex of the object<BR><BR>
	 * <p/>
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li> default textures : 0</li>
	 * <li> alternate textures : 1 </li><BR><BR>
	 *
	 * @param meshIndex
	 */
	public void setMeshIndex(int meshIndex)
	{
		_meshIndex = meshIndex;
		broadcastPacket(new StaticObject(this));
	}

	/**
	 * This class may be created only by L2Character and only for AI
	 */
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}

		@Override
		public L2StaticObjectInstance getActor()
		{
			return L2StaticObjectInstance.this;
		}

		@Override
		public void moveTo(int x, int y, int z, int offset)
		{
		}

		@Override
		public void moveTo(int x, int y, int z)
		{
		}

		@Override
		public void stopMove(Location pos)
		{
		}

		@Override
		public void doAttack(L2Character target)
		{
		}

		@Override
		public void doCast(L2Skill skill)
		{
		}
	}
}
