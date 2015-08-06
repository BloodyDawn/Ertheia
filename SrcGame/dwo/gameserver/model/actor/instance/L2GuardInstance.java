package dwo.gameserver.model.actor.instance;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.knownlist.GuardKnownList;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import dwo.gameserver.util.Rnd;

import java.util.List;

public class L2GuardInstance extends L2Attackable
{
	private static final int RETURN_INTERVAL = 60000;
	/**
	 * If false, guard will not be returned home.
	 */
	private boolean _returnHome = true;
	private boolean _canAttackPlayer = true;
	private boolean _canAttackGuard = true;

	/**
	 * Constructor of L2GuardInstance (use L2Character and L2NpcInstance constructor).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2GuardInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the L2GuardInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId      Identifier of the object to initialized
	 * @param template      Template to apply to the NPC
	 */
	public L2GuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setIsMortal(true);
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ReturnTask(), RETURN_INTERVAL, RETURN_INTERVAL + Rnd.get(60000));
	}

	public void setReturnHome(boolean value)
	{
		_returnHome = value;
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if(!_canAttackPlayer && attacker instanceof L2PcInstance)
		{
			return;
		}

		if(!_canAttackGuard && attacker instanceof L2GuardInstance)
		{
			return;
		}

		super.addDamageHate(attacker, damage, aggro);
	}

	@Override
	public GuardKnownList getKnownList()
	{
		return (GuardKnownList) super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new GuardKnownList(this));
	}

	/**
	 * Set the home location of its L2GuardInstance.
	 */
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();

		// check the region where this mob is, do not activate the AI if region is inactive.
		L2WorldRegion region = WorldManager.getInstance().getRegion(getX(), getY());
		if(region != null && !region.isActive())
		{
			getAI().stopAITask();
		}
	}

	/**
	 * Notify the L2GuardInstance to return to its home location (AI_INTENTION_MOVE_TO) and clear its _aggroList.
	 */
	@Override
	public void returnHome()
	{
		if(_returnHome && !isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), 150, false))
		{
			clearAggroList();
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, getSpawn().getLoc());
		}
	}

	/**
	 * @return {@code true} if the attacker is a L2MonsterInstance.
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return attacker instanceof L2MonsterInstance;
	}

	/**
	 * Manage actions when a player click on the L2GuardInstance.<BR><BR>
	 * <p/>
	 * <B><U> Actions on first click on the L2GuardInstance (Select it)</U> :</B><BR><BR>
	 * <li>Set the L2GuardInstance as target of the L2PcInstance player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
	 * <li>Set the L2PcInstance Intention to AI_INTENTION_IDLE </li>
	 * <li>Send a Server->Client packet ValidateLocation to correct the L2GuardInstance position and heading on the client </li><BR><BR>
	 * <p/>
	 * <B><U> Actions on second click on the L2GuardInstance (Attack it/Interact with it)</U> :</B><BR><BR>
	 * <li>If L2PcInstance is in the _aggroList of the L2GuardInstance, set the L2PcInstance Intention to AI_INTENTION_ATTACK</li>
	 * <li>If L2PcInstance is NOT in the _aggroList of the L2GuardInstance, set the L2PcInstance Intention to AI_INTENTION_INTERACT (after a distance verification) and show message</li><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action, AttackRequest</li><BR><BR>
	 *
	 * @param player The L2PcInstance that start an action on the L2GuardInstance
	 */
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if(!canTarget(player))
		{
			return;
		}

		player.setLastFolkNPC(this);

		// Check if the L2PcInstance already target the L2GuardInstance
		if(getObjectId() != player.getTargetId())
		{
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			// The color to display in the select window is White
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if(interact)
		{
			// Check if the L2PcInstance is in the _aggroList of the L2GuardInstance
			if(containsTarget(player))
			{
				// Set the L2PcInstance Intention to AI_INTENTION_ATTACK
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				// Calculate the distance between the L2PcInstance and the L2NpcInstance
				if(canInteract(player))
				{
					// Send a Server->Client packet SocialAction to the all L2PcInstance on the _knownPlayer of the L2NpcInstance
					// to display a social action of the L2GuardInstance on their client
					// Если НПЦ не разговаривает, то слать социалку приветствия собственно и не имеет смысла
					if(!Config.NON_TALKING_NPCS.contains(getNpcId()))
					{
						broadcastPacket(new SocialAction(getObjectId(), Rnd.get(8)));
					}

					List<Quest> qlsa = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
					List<Quest> qlst = getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);

					if(qlsa != null && !qlsa.isEmpty())
					{
						player.setLastQuestNpcObject(getObjectId());
					}

					if(qlst != null && qlst.size() == 1)
					{
						qlst.get(0).notifyFirstTalk(this, player);
					}
					else
					{
						showChatWindow(player, 0);
					}
				}
				else
				{
					// Set the L2PcInstance Intention to AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
			}
		}
		// Send a Server->Client ActionFail to the L2PcInstance in order to avoid that the client wait another packet
		player.sendActionFailed();
	}

	public void setCanAttackPlayer(boolean value)
	{
		_canAttackPlayer = value;
	}

	public void setCanAttackGuard(boolean value)
	{
		_canAttackGuard = value;
	}

	public class ReturnTask implements Runnable
	{
		@Override
		public void run()
		{
			if(getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			{
				returnHome();
			}
		}
	}
}
