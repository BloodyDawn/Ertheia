package dwo.gameserver.model.world.communitybbs.Manager;

import javolution.text.TextBuilder;
import dwo.config.mods.ConfigCommunityBoardPVP;
import dwo.gameserver.datatables.xml.ClassTemplateTable;
import dwo.gameserver.instancemanager.AwakeningManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.player.base.SubClass;
import dwo.gameserver.model.player.base.SubClassType;
import dwo.gameserver.model.world.olympiad.Olympiad;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Util;

import java.util.StringTokenizer;

public class ClassBBSManager
{
    private static ClassBBSManager _instance =  new ClassBBSManager();

    public static ClassBBSManager getInstance()
    {
        return _instance;
    }

    public String parsecmd( final String command, final L2PcInstance activeChar, final String content )
    {
        if( command != null && command.startsWith( "change_class" ) )
        {
            StringTokenizer st = new StringTokenizer( command, "-" );
            st.nextToken();
            int val = Integer.parseInt( st.nextToken() );
            int jobLevel = Integer.parseInt( st.nextToken() );
            int price;

            try
            {
                price = ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_LIST[ jobLevel ];
            }
            catch( NumberFormatException e )
            {
                return null;
            }

            if( validateClassId( activeChar.getClassId(), val ) )
            {
                final L2Item item = ItemTable.getInstance().getTemplate( ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_ITEM );
                final L2ItemInstance pay = activeChar.getInventory().getItemByItemId( item.getItemId() );
                if( pay != null && pay.getCount() >= price )
                {
                    activeChar.destroyItem( ProcessType.NPC, pay, price, activeChar, true );
                    changeClass( activeChar, val );
                    return parsecmd( "", activeChar, content );
                }
                if( ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_ITEM == 57 )
                {
                    activeChar.sendPacket( SystemMessageId.YOU_NOT_ENOUGH_ADENA );
                }
                else
                {
                    activeChar.sendPacket( SystemMessageId.YOU_NOT_ENOUGH_ADENA );
                }
            }
            return null;
        }
        else if (command != null && command.startsWith("change_dual"))
        {
            if (activeChar.isDualClassActive())
            {
                activeChar.sendMessage("Вы уже взяли дуал класс");
                return null;
            }

            if (!activeChar.isSubClassActive())
            {
                activeChar.sendMessage("Вы должны быть на саб классе");
                return null;
            }
            StringTokenizer st = new StringTokenizer(command, "-");
            st.nextToken();

            L2Item item = ItemTable.getInstance().getTemplate(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_TO_CHANGE_DUAL_ITEM);
            L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());

            long price = ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_TO_CHANGE_DUAL_PRICE;

            if (pay != null && pay.getCount() >= price)
            {
                activeChar.destroyItem(ProcessType.NPC, pay, price, activeChar, true);
                activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 2122));
                activeChar.getSubclass().setClassType(SubClassType.DUAL_CLASS);
                activeChar.sendPacket(SystemMessage.getSystemMessage(3279).addClassId(activeChar.getActiveClassId()).addClassId(activeChar.getActiveClassId()));
                return parsecmd("", activeChar, content);
            }

            activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            return null;
        }

        ClassId classId = activeChar.getClassId();
        int jobLevel2 = classId.level();
        int level = activeChar.getLevel();

        TextBuilder html = new TextBuilder( "" );
        html.append( "<br>" );
        html.append( "<center>" );
        html.append( "<table width=600>" );
        html.append( "<tr><td><center>" );

        if( ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_CLASS_MASTERS_LIST.isEmpty() || !ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_CLASS_MASTERS_LIST.contains( jobLevel2 ) )
        {
            jobLevel2 = 4;
        }

        if( ((level >= 20 && jobLevel2 == 0) || (level >= 40 && jobLevel2 == 1) || (level >= 76 && jobLevel2 == 2) || (level >= 85 && jobLevel2 == 3)) && ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_CLASS_MASTERS_LIST.contains( jobLevel2 ) )
        {
            final L2Item item = ItemTable.getInstance().getTemplate( ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_ITEM );
            html.append( "Стоимость профессии: <font color=\"LEVEL\">" );
            html.append( Util.formatAdena( ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_LIST[ jobLevel2 ] ) ).append( "</font> <font color=\"LEVEL\">" ).append( item.getName() ).append( "</font><br>" );
            if (jobLevel2 == 3)
            {
                int awakenedClassId = Util.getAwakenedClassForId( activeChar.getClassId().getId() );
                html.append( "<br><center><button value=\"" ).append( ClassTemplateTable.getInstance().getClass( awakenedClassId ).getClientCode() ).append( "\" action=\"bypass -h _bbstop;classmaster:change_class-" ).append( awakenedClassId ).append( "-" ).append( jobLevel2 ).append( "\" width=250 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>" );
            }
            else
            {
                for( final ClassId cid : ClassId.values() )
                {
                    if( cid != ClassId.inspector )
                    {
                        if( cid.childOf( classId ) && cid.level() == classId.level() + 1 )
                        {
                            html.append( "<br><center><button value=\"" ).append( ClassTemplateTable.getInstance().getClass( cid ).getClientCode() ).append( "\" action=\"bypass -h _bbstop;classmaster:change_class-" ).append( cid.getId() ).append( "-" ).append( jobLevel2 ).append( "\" width=250 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>" );
                        }
                    }
                }

                if (ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_TO_CHANGE_DUAL_ITEM > 0)
                {
                    html.append("<br><center><button value=\"Dual-Class\" action=\"bypass -h _bbstop;classmaster:change_dual\" width=250 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
                }
            }
        }
        else
        {
            switch( jobLevel2 )
            {
                case 0:
                {
                    html.append( "Приветствую " ).append( activeChar.getName() ).append( "! Ваша текущая профессия <font color=F2C202>" ).append( ClassTemplateTable.getInstance().getClass( activeChar.getClassId().getId() ).getClientCode() ).append( "</font>.<br>" );
                    html.append( "Для того чтобы сменить вашу профессию вы должны достичь: <font color=F2C202>20-го уровня</font><br>" );
                    if (ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS_GIVE_NOBLESS)
                        html.append("Чтобы стать дворянином вы должны прокачать сабкласс до <font color=F2C202>76-го уровня.</font><br>");
                    break;
                }
                case 1:
                {
                    html.append( "Приветствую " ).append( activeChar.getName() ).append( "! Ваша текущая профессия <font color=F2C202>" ).append( ClassTemplateTable.getInstance().getClass( activeChar.getClassId().getId() ).getClientCode() ).append( "</font>.<br>" );
                    html.append( "Для того чтобы сменить вашу профессию вы должны достичь: <font color=F2C202>40-го уровня.</font><br>" );
                    if (ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS_GIVE_NOBLESS)
                        html.append("Чтобы стать дворянином вы должны прокачать сабкласс до <font color=F2C202>76-го уровня.</font><br>");
                    break;
                }
                case 2:
                {
                    html.append( "Приветствую " ).append( activeChar.getName() ).append( "! Ваша текущая профессия <font color=F2C202>" ).append( ClassTemplateTable.getInstance().getClass( activeChar.getClassId().getId() ).getClientCode() ).append( "</font>.<br>" );
                    html.append( "Для того чтобы сменить вашу профессию вы должны достичь: <font color=F2C202>76-го уровня.</font><br>" );
                    html.append( "Для активации сабклассов вы должны достичь <font color=F2C202>76-го уровня.</font><br>" );
                    if (ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS_GIVE_NOBLESS)
                        html.append("Чтобы стать дворянином вы должны прокачать сабкласс до <font color=F2C202>76-го уровня.</font><br>");
                    break;
                }
                case 3:
                {
                    html.append( "Приветствую " ).append( activeChar.getName() ).append( "! Ваша текущая профессия <font color=F2C202>" ).append( ClassTemplateTable.getInstance().getClass( activeChar.getClassId().getId() ).getClientCode() ).append( "</font>.<br>" );
                    html.append( "Для того чтобы сменить вашу профессию вы должны достичь: <font color=F2C202>85-го уровня.</font><br>" );
                    html.append( "Для активации сабклассов вы должны достичь <font color=F2C202>76-го уровня.</font><br>" );
                    if (ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS_GIVE_NOBLESS)
                        html.append("Чтобы стать дворянином вы должны прокачать сабкласс до <font color=F2C202>76-го уровня.</font><br>");
                    break;
                }
                case 4:
                {
                    html.append( "Приветствую " ).append( activeChar.getName() ).append( "! Ваша текущая профессия <font color=F2C202>" ).append( ClassTemplateTable.getInstance().getClass( activeChar.getClassId().getId() ).getClientCode() ).append( "</font>.<br>" );
                    html.append( "Для вас больше нет доступных профессий, либо Класс мастер в данный момент недоступен.<br>" );
                    if( !ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS_GIVE_NOBLESS )
                    {
                        break;
                    }
                    if( checkNobless( activeChar ) )
                    {
                        html.append( "<center><button value=\"Стать Дворянином\" action=\"bypass -h _bbs_carrier_nobless\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>" );
                        break;
                    }
                    html.append( "Чтобы стать дворянином вы должны прокачать сабкласс до <font color=F2C202>76-го уровня.</font><br>" );
                    break;
                }
            }
        }
        html.append( "</center></td>" );
        html.append( "</tr>" );
        html.append( "</table>" );
        html.append( "</center>" );
        return content.replace( "%classmaster%", html.toString() );
    }

    private void changeClass( final L2PcInstance activeChar, final int val )
    {
        if( activeChar.getClassId().level() == ClassLevel.THIRD.ordinal() )
        {
            activeChar.sendPacket( SystemMessageId.THIRD_CLASS_TRANSFER );
        }
        else
        {
            activeChar.sendPacket( SystemMessageId.CLASS_TRANSFER );
        }
        activeChar.setClassId( val );
        if( activeChar.getClassId().level() == ClassLevel.AWAKEN.ordinal() )
        {
            AwakeningManager.getInstance().doAwake( activeChar );
        }
        if( activeChar.isSubClassActive() )
        {
            activeChar.getSubClasses().get( activeChar.getClassIndex() ).setClassId( activeChar.getActiveClassId() );
        }
        else
        {
            activeChar.setBaseClassId( activeChar.getActiveClassId() );
            activeChar.rewardSkills();
            activeChar.broadcastUserInfo();
        }
    }

    public void enableCarrier( final L2PcInstance activeChar )
    {
        if( activeChar.isGM() )
        {
            ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS = true;
            activeChar.sendMessage( "Менеджер карьеры КБ включен." );
        }
    }

    public void disableCarrier( final L2PcInstance activeChar )
    {
        if( activeChar.isGM() )
        {
            ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS = false;
            activeChar.sendMessage( "Менеджер карьеры КБ отключен." );
        }
    }

    public boolean checkNobless( final L2PcInstance activeChar )
    {
        if( activeChar.isSubClassActive() )
        {
            return false;
        }
        if( activeChar.isNoble() )
        {
            return false;
        }
        if( activeChar.getSubClasses().size() < 1 )
        {
            return false;
        }
        boolean has75Sub = false;
        for( final SubClass sub : activeChar.getSubClasses().values() )
        {
            if( sub.getLevel() >= 76 )
            {
                has75Sub = true;
                break;
            }
        }
        return has75Sub;
    }

    public void giveNobless( final L2PcInstance activeChar )
    {
        if( !ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS_GIVE_NOBLESS )
        {
            return;
        }
        if( activeChar.isSubClassActive() )
        {
            activeChar.sendMessage( "Дворянином может стать только основной класс." );
            return;
        }
        if( activeChar.isNoble() )
        {
            activeChar.sendMessage( "Вы уже получили статус дворянина. Зачем Вам еще один? :)" );
            return;
        }
        if( activeChar.getSubClasses().size() < 1 )
        {
            activeChar.sendMessage( "Чтобы стать дворянином, нужно иметь хотя бы один саб-класс." );
            return;
        }
        boolean has75Sub = false;
        for( SubClass sub : activeChar.getSubClasses().values() )
        {
            if( sub.getLevel() >= 76 )
            {
                has75Sub = true;
                break;
            }
        }
        if( !has75Sub )
        {
            activeChar.sendMessage( "Что-то подсказывает нам, что Вашим саб-классам не хватает опыта. Нужно иметь хотя бы один подкласс 76 уровня!" );
            return;
        }
        if( activeChar.getLevel() < 76 )
        {
            activeChar.sendMessage( "Вы еще юны для получения статуса дворянина. Достигните 76 уровня!" );
            return;
        }
        if( activeChar.getClassId().level() < 3 )
        {
            activeChar.sendMessage( "Ваш стаж столяра не подходит под условия дворян. Получите квалификацию 3 или 4 профессии!" );
            return;
        }

        int price = ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS_NOBLESS_PRICE;
        L2Item item = ItemTable.getInstance().getTemplate( ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_ITEM );
        L2ItemInstance pay = activeChar.getInventory().getItemByItemId( item.getItemId() );
        if( pay == null || pay.getCount() < price )
        {
            activeChar.sendPacket( SystemMessageId.YOU_NOT_ENOUGH_ADENA );
            return;
        }
        activeChar.destroyItem( ProcessType.NPC, pay, price, activeChar, true );
        activeChar.setNoble( true );
        activeChar.sendUserInfo();
        if( Olympiad.getInstance().inCompPeriod() )
        {
            Olympiad.getInstance().generateNobleStats( activeChar );
        }
        TopBBSManager.getInstance().generateHtmlPage( "classmaster", activeChar );
        activeChar.sendMessage( "Дворяне приняли Вас как родного!" );
    }

    private static boolean validateClassId( final ClassId oldCID, final int val )
    {
        try
        {
            if (Util.getAwakenedClassForId( oldCID.getId() ) == val)
                return true;

            ClassId newCID = ClassId.values()[ val ];
            return newCID != null && newCID.getRace() != null && oldCID.equals( newCID.getParent() );
        }
        catch( Exception ignored )
        {
            return false;
        }
    }
}
