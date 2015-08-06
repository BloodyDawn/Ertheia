package dwo.gameserver.model.actor.controller.player;

import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.restriction.IRestrictionChecker;
import dwo.gameserver.model.actor.restriction.RestrictionCheck;
import dwo.gameserver.model.actor.restriction.RestrictionCheckList;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.player.PlayerOlympiadSide;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.player.formation.group.PartyExitReason;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.olympiad.OlympiadGameManager;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.SkillCoolTime;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExOlympiadMode;
import org.apache.log4j.Level;

import java.util.Map;

/**
 * Player olympiad controller.
 *
 * @author Yorie
 */
@RestrictionCheckList(RestrictionCheck.PARTICIPATING_OLYMPIAD)
public class OlympiadController extends PlayerController implements IRestrictionChecker
{
	public static final int MAX_OLYMPIAD_BUFFS = 5;
	private boolean isParticipating;
	private boolean isPlayingNow;
	private boolean isHero;
	private int gameId = -1;
	private PlayerOlympiadSide side = PlayerOlympiadSide.NONE;
	private int remainingBuffs;

	public OlympiadController(L2PcInstance player)
	{
		super(player);

		player.getRestrictionController().addChecker(this);
	}

	@Override
	public boolean checkRestriction(RestrictionCheck check, Map<RestrictionCheck, Object> params)
	{
		switch(check)
		{
			case PARTICIPATING_OLYMPIAD:
				return isParticipating;
		}
		return true;
	}

	public boolean isParticipating()
	{
		return isParticipating;
	}

	public boolean isPlayingNow()
	{
		return isPlayingNow;
	}

	public int getGameId()
	{
		return gameId;
	}

	public void setGameId(int id)
	{
		gameId = id;
	}

	/**
	 * @return Current player olly game side.
	 */
	public PlayerOlympiadSide getSide()
	{
		return side;
	}

	/**
	 * Sets up olympiad game player side.
	 * @param side Olympiad arena side.
	 */
	public void setSide(PlayerOlympiadSide side)
	{
		this.side = side;
	}

	/**
	 * Returns current olly game buff counter, that means buff count restrictions at current game.
	 * @return Count of used buffs.
	 */
	public int getRemainingGameBuffs()
	{
		return remainingBuffs;
	}

	public boolean hasGameBuffs()
	{
		return remainingBuffs > 0;
	}

	public void consumeGameBuff()
	{
		remainingBuffs = Math.max(0, remainingBuffs - 1);
	}

	public boolean teleport(PlayerOlympiadSide side, Location location, int arenaId)
	{
		if(!player.isOnline())
		{
			return false;
		}

		try
		{
			player.getLocationController().rememberLocation();

			if(player.isSitting())
			{
				player.standUp();
			}

			gameId = arenaId;
			isParticipating = true;
			isPlayingNow = false;
			this.side = side;
			remainingBuffs = MAX_OLYMPIAD_BUFFS;

			player.teleToInstance(location, OlympiadGameManager.getInstance().getOlympiadTask(arenaId).getZone().getInstanceId(), false);
			player.sendPacket(new ExOlympiadMode(2));
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "Error occurs while teleporting player to olly arena.", e);
			return false;
		}
		return true;
	}

	public void startGame()
	{
		isPlayingNow = true;
	}

	public void stopGame()
	{
		try
		{
			if(!isPlayingNow)
			{
				return;
			}

			// prevent players kill each other
			isPlayingNow = false;

			player.setTarget(null);
			player.abortAttack();
			player.abortCast();
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

			if(player.isDead())
			{
				player.setIsDead(false);
			}

			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			player.clearSouls();
			player.clearCharges();

			if(player.getAgathionId() > 0)
			{
				player.setAgathionId(0);
			}

			if(!player.getPets().isEmpty())
			{
				player.getPets().stream().filter(pet -> !pet.isDead()).forEach(pet -> {
					pet.setTarget(null);
					pet.abortAttack();
					pet.abortCast();
					pet.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					pet.stopAllEffectsExceptThoseThatLastThroughDeath();
				});
			}
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.getStatus().startHpMpRegeneration();
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, e.getMessage(), e);
		}
	}

	public void restoreStatus()
	{
		try
		{
			if(!isParticipating)
			{
				return;
			}

			if(player.isTransformed())
			{
				player.untransform(true);
			}

			isParticipating = false;
			isPlayingNow = false;
			side = PlayerOlympiadSide.NONE;
			gameId = -1;

			player.sendPacket(new ExOlympiadMode(0));

			// Add Clan Skills
			if(player.getClan() != null)
			{
				player.getClan().addSkillEffects(player);
				if(player.getClan().getCastleId() > 0)
				{
					CastleManager.getInstance().getCastleByOwner(player.getClan()).giveResidentialSkills(player);
				}
				if(player.getClan().getFortId() > 0)
				{
					FortManager.getInstance().getFortByOwner(player.getClan()).giveResidentialSkills(player);
				}
			}

			// Add Hero Skills
			if(isHero)
			{
				giveHeroSkills();
			}
			player.sendSkillList();

			// heal again after adding clan skills
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.getStatus().startHpMpRegeneration();
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "portPlayersToArena()", e);
		}
	}

	public void preparePlayer(boolean leaveParty)
	{
		try
		{
			if(player == null)
			{
				return;
			}

			// Remove Buffs
			//player.stopAllEffectsExceptThoseThatLastThroughDeath();
			player.stopAllEffects();

			// Remove Clan Skills
			if(player.getClan() != null)
			{
				player.getClan().removeSkillEffects(player);
				if(player.getClan().getCastleId() > 0)
				{
					CastleManager.getInstance().getCastleByOwner(player.getClan()).removeResidentialSkills(player);
				}
				if(player.getClan().getFortId() > 0)
				{
					FortManager.getInstance().getFortByOwner(player.getClan()).removeResidentialSkills(player);
				}
			}
			// Abort casting if player casting
			player.abortAttack();
			player.abortCast();

			// Force the character to be visible
			player.getAppearance().setVisible();

			// Remove Hero Skills
			if(isHero)
			{
				for(L2Skill skill : SkillTreesData.getInstance().getHeroSkillTree().values())
				{
					player.removeSkill(skill, false);
				}
			}

			// Heal Player fully
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());

			// Remove Summon's Buffs
			if(!player.getPets().isEmpty())
			{
				for(L2Summon pet : player.getPets())
				{
					pet.stopAllEffectsExceptThoseThatLastThroughDeath();
					pet.abortAttack();
					pet.abortCast();

					if(pet instanceof L2PetInstance)
					{
						if(pet.isDead())
						{
							pet.doRevive();
						}

						pet.getLocationController().decay();
					}
				}
			}

			// stop any cubic that has been given by other player.
			player.stopCubicsByOthers();

			// Remove player from his party
			if(leaveParty)
			{
				L2Party party = player.getParty();
				if(party != null)
				{
					party.removePartyMember(player, PartyExitReason.EXPELLED);
				}
			}
			// Remove Agathion
			if(player.getAgathionId() > 0)
			{
				player.setAgathionId(0);
				player.broadcastUserInfo();
			}

			player.checkItemRestriction();

			// Remove shot automation
			player.disableAutoShotsAll();

			// Discharge any active shots
			if(player.getActiveWeaponInstance() != null)
			{
				player.getActiveWeaponInstance().setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
				player.getActiveWeaponInstance().setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}

			// enable skills with cool time <= 15 minutes
			player.getAllSkills().stream().filter(skill -> skill.getReuseDelay() <= 900000).forEach(player::enableSkill);

			player.sendSkillList();
			player.sendPacket(new SkillCoolTime(player));
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, e.getMessage(), e);
		}
	}

	public void returnPlayer()
	{
		if(player.getLocationController().getMemorizedLocation() == null)
		{
			return;
		}

		player.getInstanceController().setInstanceId(0);
		player.teleToLocation(player.getLocationController().getMemorizedLocation());
		player.getLocationController().forgetLocation();
	}

	/**
	 * @return True if current player status is hero.
	 */
	public boolean isHero()
	{
		return isHero;
	}

	/**
	 * Gives all hero skills to current player.
	 */
	public void giveHeroSkills()
	{
		SkillTreesData.getInstance().giveHeroSkills(player);
	}

	/**
	 * Takes all hero skills from player.
	 */
	public void takeHeroSkills()
	{
		SkillTreesData.getInstance().removeHeroSkills(player);
	}

	/**
	 * Grants hero status to player without storing status and skills to DB.
	 */
	public void giveHero()
	{
		if(!isHero)
		{
			isHero = true;
			if(player.getBaseClassId() == player.getActiveClassId())
			{
				giveHeroSkills();
			}
		}
	}

	/**
	 * Takes hero status from player without storing status and skill changes to DB.
	 */
	public void takeHero()
	{
		if(isHero)
		{
			isHero = false;
			takeHeroSkills();
		}
	}

	public boolean onSameArena(L2Character target)
	{
		return target != null && target.isPlayer() && target.getActingPlayer().getOlympiadController().gameId == gameId;
	}

	/**
	 * Ensures that opponent player @target is in the same olympiad game as current player. Also ensures that current player is taking olly match.
	 * @param target Opponent character.
	 * @return True if current player and target are opponents.
	 */
	public boolean isOpponent(L2Character target)
	{
		return isPlayingNow && onSameArena(target) && target.getActingPlayer().getOlympiadController().isPlayingNow && target.getActingPlayer().getOlympiadController().side != side;
	}

	/**
	 * Ensures that current player and selected player are on same side in olympiad game.
	 * @param target Target.
	 * @return True if current player and target are comrades.
	 */
	public boolean isComrade(L2Character target)
	{
		return isPlayingNow && onSameArena(target) && target.getActingPlayer().getOlympiadController().isPlayingNow && side == target.getActingPlayer().getOlympiadController().side;
	}
}
