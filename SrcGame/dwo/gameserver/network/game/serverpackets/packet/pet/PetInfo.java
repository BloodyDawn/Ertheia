package dwo.gameserver.network.game.serverpackets.packet.pet;

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import javolution.util.FastSet;

public class PetInfo extends L2GameServerPacket
{
	private L2Summon _summon;
	private int _state;
	private int _runSpd;
	private int _walkSpd;
	private int _swimRunSpd;
	private int _swimWalkSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private int _maxFed;
	private int _curFed;
    private FastSet<Integer> _abnormals;
    private int _statusMask;

	public PetInfo(L2Summon summon, int state)
	{
		_summon = summon;
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
		_state = _summon.getSummonState();

		if(_summon.getSummonState() == 2)
		{
			_summon.setSummonState(1);   // default
		}

		if(_summon.getSummonState() == 0)
		{
			_summon.setSummonState(1);   // default
		}

		if(_summon instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); //max fed it can be
		}
		else if(_summon instanceof L2SummonInstance)
		{
			L2SummonInstance sum = (L2SummonInstance) _summon;
			_curFed = sum.getTimeRemaining();
			_maxFed = sum.getTotalLifeTime();
		}

        _abnormals = _summon.getAbnormalEffects();

        if (summon.isRunning())
        {
            _statusMask |= 0x04;
        }
        if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(summon))
        {
            _statusMask |= 0x08;
        }
        if (summon.isDead())
        {
            _statusMask |= 0x10;
        }
    }

    @Override
    protected void writeImpl()
    {
        writeC(_summon.getSummonType());
        writeD(_summon.getObjectId());
        writeD(_summon.getTemplate().getIdTemplate() + 1000000);

        writeD(_summon.getX());
        writeD(_summon.getY());
        writeD(_summon.getZ());
        writeD(_summon.getHeading());

        writeD(_summon.getMAtkSpd());
        writeD(_summon.getPAtkSpd());

        writeH((int) _summon.getTemplate().getBaseRunSpd());
        writeH((int) _summon.getTemplate().getBaseWalkSpd());
        writeH(_swimRunSpd);
        writeH(_swimWalkSpd);
        writeH(_flRunSpd);
        writeH(_flWalkSpd);
        writeH(_flyRunSpd);
        writeH(_flyWalkSpd);

        writeF(_summon.getMovementSpeedMultiplier()); // movement multiplier
        writeF(_summon.getAttackSpeedMultiplier()); // attack speed multiplier
        writeF(_summon.getTemplate().getFCollisionRadius(_summon));
        writeF(_summon.getTemplate().getFCollisionHeight(_summon));

        writeD(_summon.getTemplate().getRightHand()); // right hand weapon
        writeD(_summon.getArmor()); // body armor
        writeD(_summon.getTemplate().getLeftHand()); // left hand weapon

        writeC(_state); //  0=teleported  1=default   2=summoned    TODO

        writeD(-1); // High Five NPCString ID
        if(_summon instanceof L2PetInstance)
        {
            writeS(_summon.getName()); // Pet name.
        }
        else
        {
            writeS(_summon.getTemplate().isServerSideName() ? _summon.getName() : ""); // Summon name.
        }

        writeD(-1); // High Five NPCString ID
        writeS(_summon.getTitle()); // owner name
        writeC(_summon.getOwner() != null ? _summon.getOwner().getPvPFlagController().getStateValue() : 0);    //0 = white,2= purpleblink, if its greater then Reputation = purple

        writeD(_summon.getReputation());  // Репутация
        writeD(_curFed); // how fed it is
        writeD(_maxFed); //max fed it can be
        writeD((int) _summon.getCurrentHp());//current hp
        writeD(_summon.getMaxVisibleHp());// max hp
        writeD((int) _summon.getCurrentMp());//current mp
        writeD(_summon.getMaxMp());//max mp

        writeQ(_summon.getStat().getSp());
        writeC(_summon.getLevel());// lvl
        writeQ(_summon.getStat().getExp());
        if(_summon.getExpForThisLevel() > _summon.getStat().getExp())
        {
            writeQ(_summon.getStat().getExp());// 0%  absolute value
        }
        else
        {
            writeQ(_summon.getExpForThisLevel());// 0%  absolute value
        }
        writeQ(_summon.getExpForNextLevel());// 100% absoulte value

        writeD(_summon instanceof L2PetInstance ? _summon.getInventory().getTotalWeight() : 0);//weight
        writeD(_summon.getMaxLoad());//max weight it can carry
        writeD(_summon.getPAtk(null));//patk
        writeD(_summon.getPDef(null));//pdef
        writeD(_summon.getPhysicalAccuracy());//accuracy
        writeD(_summon.getPhysicalEvasionRate(null));//evasion
        writeD(_summon.getCriticalHit(null, null));//critical
        writeD(_summon.getMAtk(null, null));//matk
        writeD(_summon.getMDef(null, null));//mdef
        writeD(_summon.getMagicalAccuracy()); // magic acc
        writeD(_summon.getMagicalEvasionRate(null)); // magic evasion
        writeD(_summon.getMCriticalHit(null, null)); // magic critical
        writeD((int) _summon.getStat().getMoveSpeed());//speed
        writeD(_summon.getPAtkSpd());//atkspeed
        writeD(_summon.getMAtkSpd());//casting speed

        writeC(_summon.isMountable() ? 1 : 0);//c2    ride button
        writeC(0);

		writeC(_summon.getOwner() != null ? _summon.getOwner().getTeam() : 0); // team aura (1 = blue, 2 = red)
        writeC(_summon.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit
        writeC(_summon.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit

        int form = 0;
        int npcId = _summon.getTemplate().getNpcId();
        if(npcId == 16041 || npcId == 16042)
        {
            if(_summon.getLevel() > 84)
            {
                form = 3;
            }
            else if(_summon.getLevel() > 79)
            {
                form = 2;
            }
            else if(_summon.getLevel() > 74)
            {
                form = 1;
            }
        }
        else if(npcId == 16025 || npcId == 16037)
        {
            if(_summon.getLevel() > 69)
            {
                form = 3;
            }
            else if(_summon.getLevel() > 64)
            {
                form = 2;
            }
            else if(_summon.getLevel() > 59)
            {
                form = 1;
            }
        }

        writeD(form);

        writeD(_summon.isTransformed() ? 0x01 : 0x00);

        writeC(_summon.getPointsToSummon() <= 0 ? 0x00 : _summon.getOwner().getUsedSummonPoints());
        writeC(_summon.getOwner().getMaxSummonPoints());

        writeH(_abnormals.size());
        _abnormals.forEach(this::writeH);
        writeC(_statusMask);
    }
}
