package dwo.gameserver.network.game.serverpackets.packet.info;

import dwo.config.Config;
import dwo.gameserver.instancemanager.CursedWeaponsManager;
import dwo.gameserver.model.actor.L2Decoy;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.Colors;

public class CI extends L2GameServerPacket
{
    private final L2PcInstance _activeChar;
    private int _objId;
    private int _x, _y, _z, _heading;
    private final int _mAtkSpd, _pAtkSpd;

    private final int _runSpd, _walkSpd;
    private final double _moveMultiplier;
    private final float _attackSpeedMultiplier;
    
    private int _enchantLevel = 0;
    private int _armorEnchant = 0;
    private int _vehicleId = 0;

    private static final int[] PAPERDOLL_ORDER = new int[]
            {
                    Inventory.PAPERDOLL_UNDER,
                    Inventory.PAPERDOLL_HEAD,
                    Inventory.PAPERDOLL_RHAND,
                    Inventory.PAPERDOLL_LHAND,
                    Inventory.PAPERDOLL_GLOVES,
                    Inventory.PAPERDOLL_CHEST,
                    Inventory.PAPERDOLL_LEGS,
                    Inventory.PAPERDOLL_FEET,
                    Inventory.PAPERDOLL_CLOAK,
                    Inventory.PAPERDOLL_RHAND,
                    Inventory.PAPERDOLL_HAIR,
                    Inventory.PAPERDOLL_HAIR2
            };

    public CI(L2PcInstance cha)
    {
        _activeChar = cha;
        _objId = cha.getObjectId();
        if ((_activeChar.getVehicle() != null) && (_activeChar.getInVehiclePosition() != null))
        {
            _x = _activeChar.getInVehiclePosition().getX();
            _y = _activeChar.getInVehiclePosition().getY();
            _z = _activeChar.getInVehiclePosition().getZ();
            _vehicleId = _activeChar.getVehicle().getObjectId();
        }
        else
        {
            _x = _activeChar.getX();
            _y = _activeChar.getY();
            _z = _activeChar.getZ();
        }
        _heading = _activeChar.getHeading();
        _mAtkSpd = _activeChar.getMAtkSpd();
        _pAtkSpd = _activeChar.getPAtkSpd();
        _attackSpeedMultiplier = _activeChar.getAttackSpeedMultiplier();

        _moveMultiplier = cha.getMovementSpeedMultiplier();
        _runSpd = (int) Math.round(cha.getRunSpeed() / _moveMultiplier);
        _walkSpd = (int) Math.round(cha.getWalkSpeed() / _moveMultiplier);
        _enchantLevel = cha.getEnchantEffect();
        _armorEnchant = cha.getInventory().getFullArmorEnchant();
    }

    public CI(L2Decoy decoy)
    {
        this(decoy.getActingPlayer());
        _objId = decoy.getObjectId();
        _x = decoy.getX();
        _y = decoy.getY();
        _z = decoy.getZ();
        _heading = decoy.getHeading();
    }

    @Override
    protected final void writeImpl()
    {
        boolean gmSeeInvis = false;

        if (isInvisible())
        {
            L2PcInstance activeChar = getClient().getActiveChar();
            if ((activeChar != null) && activeChar.isGM())
            {
                gmSeeInvis = true;
            }
        }

        writeD(_x);
        writeD(_y);
        writeD(_z);
        writeD(_vehicleId);
        writeD(_objId);
        writeS(_activeChar.getAppearance().getVisibleName());
        writeH(_activeChar.getRace().ordinal());
        writeC(_activeChar.getAppearance().getSex() ? 0x01 : 0x00);
        writeD(_activeChar.getBaseClassId());

        for (int slot : getPaperdollOrder())
        {
            writeD(_activeChar.getInventory().getPaperdollItemSkinByItemId(slot));
        }

        for (int slot : getPaperdollOrderAugument())
        {
            writeD(_activeChar.getInventory().getPaperdollAugmentationId(slot));
        }

        writeC(_armorEnchant);

        for (int slot : getPaperdollOrderVisualId())
        {
            writeD(_activeChar.getInventory().getPaperdollItemSkinByItemId(slot));
        }

        writeC(_activeChar.getPvPFlagController().getStateValue());
        writeD(_activeChar.getReputation());

        writeD(_mAtkSpd);
        writeD(_pAtkSpd);

        writeH(_runSpd);
        writeH(_walkSpd);
        writeH(_runSpd);//swim
        writeH(_walkSpd);
        writeH(_runSpd);//fly
        writeH(_walkSpd);
        writeH(_runSpd);//fly
        writeH(_walkSpd);
        writeF(_moveMultiplier);
        writeF(_attackSpeedMultiplier);

        writeF(_activeChar.getCollisionRadius());
        writeF(_activeChar.getCollisionHeight());

        writeD(_activeChar.getAppearance().getHairStyle());
        writeD(_activeChar.getAppearance().getHairColor());
        writeD(_activeChar.getAppearance().getFace());

        writeS(gmSeeInvis ? "[Невидимый]" : _activeChar.getAppearance().getVisibleTitle());

        if (!_activeChar.isCursedWeaponEquipped())
        {
            writeD(_activeChar.getClanId());
            writeD(_activeChar.getClanCrestId());
            writeD(_activeChar.getAllyId());
            writeD(_activeChar.getAllyCrestId());
        }
        else
        {
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
        }

        writeC(_activeChar.isSitting() ? 0x00 : 0x01);
        writeC(_activeChar.isRunning() ? 0x01 : 0x00);
        writeC(_activeChar.isInCombat() ? 0x01 : 0x00);

        writeC(!_activeChar.getOlympiadController().isParticipating() && _activeChar.isAlikeDead() ? 1 : 0);

        writeC(!gmSeeInvis && isInvisible() ? 0x01 : 0x00);

        writeC(_activeChar.getMountType());
        writeC(_activeChar.getPrivateStoreType().ordinal());

        writeH(_activeChar.getCubics().size());
        _activeChar.getCubics().stream().forEach(cubics -> writeH(cubics.getId()));
//        for(L2CubicInstance cubic : _activeChar.getCubics())
//        {
//            writeH(cubic.getId());
//        }

        writeC(_activeChar.isInPartyMatchRoom() ? 0x01 : 0x00);

        writeC(_activeChar.isInWater() ? 1 : _activeChar.isFlyingMounted() ? 2 : 0);
        writeH(_activeChar.getRecommendations());
        writeD(_activeChar.getMountNpcId() == 0 ? 0 : _activeChar.getMountNpcId() + 1000000);

        writeD(_activeChar.getClassId().getId());
        writeD(0x00); // TODO
        writeC(_activeChar.isMounted() ? 0 : _enchantLevel);

        writeC(_activeChar.getTeam());

        writeD(_activeChar.getClanCrestLargeId());
        writeC(_activeChar.isNoble() ? 1 : 0);
        writeC(_activeChar.getOlympiadController().isHero() || _activeChar.isGM() && Config.GM_HERO_AURA ? 1 : 0);

        writeC(_activeChar.isFishing() ? 1 : 0);
        writeD(_activeChar.getFishx());
        writeD(_activeChar.getFishy());
        writeD(_activeChar.getFishz());

        writeD(_activeChar.getAppearance().getNameColor());

        writeD(_heading);

        writeC(_activeChar.getPledgeClass());
        writeH(_activeChar.getPledgeType());

        if (_activeChar.getUseTitlePvpMod()) {
            writeD(Config.TITLE_PVP_MODE ? Colors.getColor(_activeChar.getPvpKills()) : _activeChar.getAppearance().getTitleColor());
        }
        else {
            writeD(_activeChar.getAppearance().getTitleColor());
        }

        writeC(_activeChar.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquippedId()) : 0);

        writeD(_activeChar.getClanId() > 0 ? _activeChar.getClan().getReputationScore() : 0);
        writeD(_activeChar.getTransformationId());
        writeD(_activeChar.getAgathionId());

        writeC(0x01); // TODO: Find me!

        writeD((int) Math.round(_activeChar.getCurrentCp()));
        writeD(_activeChar.getMaxHp());
        writeD((int) Math.round(_activeChar.getCurrentHp()));
        writeD(_activeChar.getMaxMp());
        writeD((int) Math.round(_activeChar.getCurrentMp()));

        writeC(0x00); // TODO: Find me!
        
        writeD(_activeChar.getAbnormalEffects().size());
        _activeChar.getAbnormalEffects().forEach(this::writeH);

        writeC(0x00); //Unk    если 1 убираются все эффекты таликов / заточки пух ( походул нужно для фестиваля )
        writeC(_activeChar.isHairAccessoryEnabled() ? 0x01 : 0x00); // похоже на хайд урашений когда есть прическа из шопа   1 - показывать 0 - нет
        writeC(_activeChar.getAbilityPointsUsed());
    }

    @Override
    protected int[] getPaperdollOrder()
    {
        return PAPERDOLL_ORDER;
    }
}
