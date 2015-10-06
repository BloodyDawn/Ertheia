package dwo.gameserver.network.game.serverpackets.packet.info;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.ExperienceTable;
import dwo.gameserver.instancemanager.RaidBossPointsManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.Elementals;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.network.game.masktypes.UserInfoType;
import dwo.gameserver.network.game.serverpackets.AbstractMaskPacket;
import dwo.gameserver.util.Colors;

public class UI extends AbstractMaskPacket<UserInfoType>
{
    private final L2PcInstance _activeChar;

    private final int _relation;
    private final int _runSpd;
    private final int _walkSpd;
    private final int _swimRunSpd;
    private final int _swimWalkSpd;
    private final int _flWalkSpd;
    private final int _flRunSpd;
    private final int _flyRunSpd;
    private final int _flyWalkSpd;
    private final double _moveMultiplier;
    private String _title;
    private int _enchantLevel = 0;

    private final byte[] _masks = new byte[]
            {
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00
            };

    private int _initSize = 5;

    public UI(L2PcInstance cha)
    {
        this(cha, true);
    }

    public UI(L2PcInstance cha, boolean addAll)
    {
        _activeChar = cha;
        
        _title = (cha.isGM() && cha.getAppearance().getInvisible()) ? "[Невидимый]" : cha.getTitle();
        
        _relation = calculateRelation(cha);
        _moveMultiplier = cha.getMovementSpeedMultiplier();
        _runSpd = (int) Math.round(cha.getRunSpeed() / _moveMultiplier);
        _walkSpd = (int) Math.round(cha.getWalkSpeed() / _moveMultiplier);
        _swimRunSpd = _runSpd;
        _swimWalkSpd = _walkSpd;
        _flRunSpd = _runSpd;
        _flWalkSpd = _walkSpd;
        _flyRunSpd = cha.isFlying() ? _runSpd : 0;
        _flyWalkSpd = cha.isFlying() ? _walkSpd : 0;
        int _airShipHelm = cha.isInAirShip() && cha.getAirShip().isCaptain(cha) ? cha.getAirShip().getHelmItemId() : 0;
        _enchantLevel = cha.isMounted() || (_airShipHelm != 0) ? 0x00 : cha.getEnchantEffect();

        if (addAll)
        {
            addComponentType(UserInfoType.values());
        }

        cha.sendPacket(new ExUserInfoEquipSlot(cha, true));
        cha.sendPacket(new ExUserInfoCubic(cha));
        cha.sendPacket(new ExUserInfoAbnormalVisualEffect(cha));
    }

    @Override
    protected byte[] getMasks()
    {
        return _masks;
    }

    @Override
    protected void onNewMaskAdded(UserInfoType component)
    {
        calcBlockSize(component);
    }

    private void calcBlockSize(UserInfoType type)
    {
        switch (type)
        {
            case BASIC_INFO:
            {
                _initSize += type.getBlockLength() + (_activeChar.getAppearance().getVisibleName().length() * 2);
                break;
            }
            case CLAN:
            {
                _initSize += type.getBlockLength() + (_activeChar.getTitle().length() * 2);
                break;
            }
            default:
            {
                _initSize += type.getBlockLength();
                break;
            }
        }
    }

    @Override
    protected void writeImpl()
    {
        writeD(_activeChar.getObjectId());
        writeD(_initSize);
        writeH(23);
        writeB(_masks);

        if (containsMask(UserInfoType.RELATION))
        {
            writeD(_relation);
        }

        if (containsMask(UserInfoType.BASIC_INFO))
        {
            writeH(0x10 + (_activeChar.getName().length() * 2));
            writeString(_activeChar.getName());
            writeC(_activeChar.isGM() ? 0x01 : 0x00);
            writeC(_activeChar.getRace().ordinal());
            writeC(_activeChar.getAppearance().getSex() ? 0x01 : 0x00);
            writeD(_activeChar.getBaseClassId());
            writeD(_activeChar.getClassId().getId());
            writeC(_activeChar.getLevel());
        }

        if (containsMask(UserInfoType.BASE_STATS))
        {
            writeH(18);
            writeH(_activeChar.getSTR());
            writeH(_activeChar.getDEX());
            writeH(_activeChar.getCON());
            writeH(_activeChar.getINT());
            writeH(_activeChar.getWIT());
            writeH(_activeChar.getMEN());
            writeH(_activeChar.getLUC());
            writeH(_activeChar.getCHA());
        }

        if (containsMask(UserInfoType.MAX_HPCPMP))
        {
            writeH(14);
            writeD(_activeChar.getMaxHp());
            writeD(_activeChar.getMaxMp());
            writeD(_activeChar.getMaxCp());
        }

        if (containsMask(UserInfoType.CURRENT_HPMPCP_EXP_SP))
        {
            writeH(38);
            writeD((int) Math.round(_activeChar.getCurrentHp()));
            writeD((int) Math.round(_activeChar.getCurrentMp()));
            writeD((int) Math.round(_activeChar.getCurrentCp()));
            writeQ(_activeChar.getSp());
            writeQ(_activeChar.getExp());
            writeF((float) (_activeChar.getExp() - ExperienceTable.getInstance().getExpForLevel(_activeChar.getLevel())) / (ExperienceTable.getInstance().getExpForLevel(_activeChar.getLevel() + 1) - ExperienceTable.getInstance().getExpForLevel(_activeChar.getLevel())));
        }

        if (containsMask(UserInfoType.ENCHANTLEVEL))
        {
            writeH(4);
            writeC(_enchantLevel);
            writeC(0x00);
        }

        if (containsMask(UserInfoType.APPAREANCE))
        {
            writeH(15);
            writeD(_activeChar.getAppearance().getHairStyle());
            writeD(_activeChar.getAppearance().getHairColor());
            writeD(_activeChar.getAppearance().getFace());
            writeC(_activeChar.isHairAccessoryEnabled() ? 0x01 : 0x00);
        }

        if (containsMask(UserInfoType.STATUS))
        {
            writeH(6);
            writeC(_activeChar.getMountType());
            writeC(_activeChar.getPrivateStoreType().ordinal());
            writeC(_activeChar.hasDwarvenCraft() ? 1 : 0);
            writeC(_activeChar.getAbilityPointsUsed());
        }

        if (containsMask(UserInfoType.STATS))
        {
            writeH(56);
            writeH(_activeChar.getActiveWeaponItem() != null ? 40 : 20);
            writeD(_activeChar.getPAtk(null));
            writeD(_activeChar.getPAtkSpd());
            writeD(_activeChar.getPDef(null));
            writeD(_activeChar.getPhysicalEvasionRate(null));
            writeD(_activeChar.getPhysicalAccuracy());
            writeD(_activeChar.getCriticalHit(null, null));
            writeD(_activeChar.getMAtk(null, null));
            writeD(_activeChar.getMAtkSpd());
            writeD(_activeChar.getPAtkSpd()); // Seems like atk speed - 1
            writeD(_activeChar.getMagicalEvasionRate(null));
            writeD(_activeChar.getMDef(null, null));
            writeD(_activeChar.getMagicalAccuracy());
            writeD(_activeChar.getMCriticalHit(null, null));
        }

        if (containsMask(UserInfoType.ELEMENTALS))
        {
            writeH(14);
            writeH(_activeChar.getDefenseElementValue(Elementals.FIRE));
            writeH(_activeChar.getDefenseElementValue(Elementals.WATER));
            writeH(_activeChar.getDefenseElementValue(Elementals.WIND));
            writeH(_activeChar.getDefenseElementValue(Elementals.EARTH));
            writeH(_activeChar.getDefenseElementValue(Elementals.HOLY));
            writeH(_activeChar.getDefenseElementValue(Elementals.DARK));
        }

        if (containsMask(UserInfoType.POSITION))
        {
            writeH(18);
            writeD(_activeChar.getX());
            writeD(_activeChar.getY());
            writeD(_activeChar.getZ());
            writeD(_activeChar.getHeading());
        }

        if (containsMask(UserInfoType.SPEED))
        {
            writeH(18);
            writeH(_runSpd);
            writeH(_walkSpd);
            writeH(_swimRunSpd);
            writeH(_swimWalkSpd);
            writeH(_flRunSpd);
            writeH(_flWalkSpd);
            writeH(_flyRunSpd);
            writeH(_flyWalkSpd);
        }

        if (containsMask(UserInfoType.MULTIPLIER))
        {
            writeH(18);
            writeF(_moveMultiplier);
            writeF(_activeChar.getAttackSpeedMultiplier());
        }

        if (containsMask(UserInfoType.COL_RADIUS_HEIGHT))
        {
            writeH(18);
            writeF(_activeChar.getCollisionRadius());
            writeF(_activeChar.getCollisionHeight());
        }

        if (containsMask(UserInfoType.ATK_ELEMENTAL))
        {
            writeH(5);
            byte attackAttribute = _activeChar.getAttackElement();
            writeC(attackAttribute);
            writeH(_activeChar.getAttackElementValue(attackAttribute));
        }

        if (containsMask(UserInfoType.CLAN))
        {
            writeH(32 + _title.length() * 2);
            writeH(_title.length());
            writeNS(_title);
            writeH(_activeChar.getPledgeType());
            writeD(_activeChar.getClanId());
            writeD(_activeChar.getClanCrestLargeId());
            writeD(_activeChar.getClanCrestId());
            writeD(_activeChar.getClanPrivileges());
            writeC(_activeChar.isClanLeader() ? -1 : 0x00);
            writeD(_activeChar.getAllyId());
            writeD(_activeChar.getAllyCrestId());
            writeC(_activeChar.isInPartyMatchRoom() ? 0x01 : 0x00);
        }

        if (containsMask(UserInfoType.SOCIAL))
        {
            writeH(22);
            writeC(_activeChar.getPvPFlagController().getStateValue());
            writeD(_activeChar.getReputation()); // Reputation
            writeC(_activeChar.isNoble() ? 0x01 : 0x00);
            writeC(_activeChar.getOlympiadController().isHero() || _activeChar.isGM() && Config.GM_HERO_AURA ? 1 : 0);
            writeC(_activeChar.getPledgeClass());
            writeD(_activeChar.getPkKills());
            writeD(_activeChar.getPvpKills());
            writeH(_activeChar.getRecommendationsLeft());
            writeH(_activeChar.getRecommendations());
        }

        if (containsMask(UserInfoType.VITA_FAME))
        {
            writeH(15);
            writeD(_activeChar.getVitalityDataForCurrentClassIndex().getVitalityPoints());
            writeC(0x00); // Vita Bonus
            writeD(_activeChar.getFame());
            writeD(RaidBossPointsManager.getInstance().getPointsByOwnerId(_activeChar.getObjectId()));
        }

        if (containsMask(UserInfoType.SLOTS))
        {
            writeH(9);
            writeC(_activeChar.getInventory().getMaxTalismanCount());
            writeC(_activeChar.getInventory().getMaxStoneCount());
            writeC(_activeChar.getTeam());
            writeD(0x00);
        }

        if (containsMask(UserInfoType.MOVEMENTS))
        {
            writeH(4);
            writeC(_activeChar.isInsideZone(L2Character.ZONE_WATER) ? 1 : _activeChar.isFlyingMounted() ? 2 : 0);
            writeC(_activeChar.isRunning() ? 0x01 : 0x00);
        }

        if (containsMask(UserInfoType.COLOR))
        {
            writeH(10);
            writeD(_activeChar.getAppearance().getNameColor());
            if (_activeChar.getUseTitlePvpMod()) {
                writeD(Config.TITLE_PVP_MODE && Config.TITLE_PVP_MODE_FOR_SELF ? Colors.getColor(_activeChar.getPvpKills()) : _activeChar.getAppearance().getTitleColor());
            }
            else {
                writeD(_activeChar.getAppearance().getTitleColor());
            }
        }

        if (containsMask( UserInfoType.INVENTORY_LIMIT))
        {
            writeH( 9 );
            writeD( 0x00 );
            writeH( _activeChar.getInventoryLimit() );
            writeC( 0x00 );
//            writeC(_activeChar.isTransformed() ? _activeChar.getTransformationId() : 0x00);
//            writeC(_activeChar.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquippedId()) : 0);
        }

        if (containsMask(UserInfoType.UNK_3))
        {
            writeH(9);
            writeD(0x01);
            writeH(0x00);
            writeC(0x00);
        }
    }

    private int calculateRelation(L2PcInstance activeChar)
    {
        int relation = 0;
        final L2Party party = activeChar.getParty();
        final L2Clan clan = activeChar.getClan();

        if (party != null)
        {
            relation |= 0x08; // Party member
            if (party.getLeader() == _activeChar)
            {
                relation |= 0x10; // Party leader
            }
        }

        if (clan != null)
        {
            relation |= 0x20; // Clan member
            if (clan.getLeaderId() == activeChar.getObjectId())
            {
                relation |= 0x40; // Clan leader
            }
        }

        if (activeChar.isInSiege())
        {
            relation |= 0x80; // In siege
        }

        return relation;
    }
}