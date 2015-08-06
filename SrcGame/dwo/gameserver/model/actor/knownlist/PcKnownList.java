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
package dwo.gameserver.model.actor.knownlist;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2AirShipInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.DeleteObject;
import dwo.gameserver.network.game.serverpackets.SpawnItem;

public class PcKnownList extends PlayableKnownList
{
	public PcKnownList(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	/**
	 * Add a visible L2Object to L2PcInstance _knownObjects and _knownPlayer (if necessary) and send ServerMode-Client Packets needed to inform the L2PcInstance of its state and actions in progress.<BR><BR>
	 * <p/>
	 * <B><U> object is a L2ItemInstance </U> :</B><BR><BR>
	 * <li> Send ServerMode-Client Packet DropItem/SpawnItem to the L2PcInstance </li><BR><BR>
	 * <p/>
	 * <B><U> object is a L2DoorInstance </U> :</B><BR><BR>
	 * <li> Send ServerMode-Client Packets DoorInfo and DoorStatusUpdate to the L2PcInstance </li>
	 * <li> Send ServerMode->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
	 * <p/>
	 * <B><U> object is a L2NpcInstance </U> :</B><BR><BR>
	 * <li> Send ServerMode-Client Packet NpcInfo to the L2PcInstance </li>
	 * <li> Send ServerMode->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
	 * <p/>
	 * <B><U> object is a L2Summon </U> :</B><BR><BR>
	 * <li> Send ServerMode-Client Packet NpcInfo/PetItemList (if the L2PcInstance is the owner) to the L2PcInstance </li>
	 * <li> Send ServerMode->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
	 * <p/>
	 * <B><U> object is a L2PcInstance </U> :</B><BR><BR>
	 * <li> Send ServerMode-Client Packet CharInfo to the L2PcInstance </li>
	 * <li> If the object has a private store, Send ServerMode-Client Packet PrivateStoreMsgSell to the L2PcInstance </li>
	 * <li> Send ServerMode->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
	 *
	 * @param object  The L2Object to add to _knownObjects and _knownPlayer
	 */
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if(!super.addKnownObject(object))
		{
			return false;
		}

		if(object.getPolyController().isMorphed() && object.getPolyController().getPolyType().equals("item"))
		{
			getActiveChar().sendPacket(new SpawnItem(object));
		}
		else
		{
			if(object.isVisible())
            {
                object.sendInfo(getActiveChar());

                if (object instanceof L2Character)
                {
                    // Update the state of the L2Character object client side by sending ServerMode->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance
                    L2Character obj = (L2Character) object;
                    if (obj.hasAI())
                    {
                        obj.getAI().describeStateToPlayer(getActiveChar());
                    }
                }
            }
		}

		return true;
	}

	/**
	 * Remove a L2Object from L2PcInstance _knownObjects and _knownPlayer (if necessary) and send ServerMode-Client Packet DeleteObject to the L2PcInstance.<BR><BR>
	 *
	 * @param object The L2Object to remove from _knownObjects and _knownPlayer
	 */
	@Override
	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if(!super.removeKnownObject(object, forget))
		{
			return false;
		}

		if(object instanceof L2AirShipInstance)
		{
			if(((L2AirShipInstance) object).getCaptainId() != 0 && ((L2AirShipInstance) object).getCaptainId() != getActiveChar().getObjectId())
			{
				getActiveChar().sendPacket(new DeleteObject(((L2AirShipInstance) object).getCaptainId()));
			}
			if(((L2AirShipInstance) object).getHelmObjectId() != 0)
			{
				getActiveChar().sendPacket(new DeleteObject(((L2AirShipInstance) object).getHelmObjectId()));
			}
		}

		// Send ServerMode-Client Packet DeleteObject to the L2PcInstance
		getActiveChar().sendPacket(new DeleteObject(object));

		if(Config.CHECK_KNOWN && object instanceof L2Npc && getActiveChar().isGM())
		{
			getActiveChar().sendMessage("Removed NPC: " + object.getName());
		}

		return true;
	}

	@Override
	public L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}

	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if(object.isCharacter() && object.getCharacter().isWalker())
		{
			return 10000;
		}

		// when knownlist grows, the distance to forget should be at least
		// the same as the previous watch range, or it becomes possible that
		// extra charinfo packets are being sent (watch-forget-watch-forget)
		int knownlistSize = getKnownObjects().size();
		if(knownlistSize <= 25)
		{
			return 4000;
		}
		if(knownlistSize <= 35)
		{
			return 3500;
		}
		return knownlistSize <= 70 ? 2910 : 2310;
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if(object.isCharacter() && object.getCharacter().isWalker())
		{
			return 9000;
		}

		int knownlistSize = getKnownObjects().size();
		if(knownlistSize <= 25)
		{
			return 3400; // empty field
		}
		if(knownlistSize <= 35)
		{
			return 2900;
		}
		return knownlistSize <= 70 ? 2300 : 1700;
	}
}