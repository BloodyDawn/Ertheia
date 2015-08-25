package dwo.gameserver.model.skills.stats;

import dwo.config.FilePath;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.util.crypt.datapack.CryptUtil;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * @author DS
 */
public enum BaseStats
{
  STR( new STR() ),
  INT( new INT() ),
  DEX( new DEX() ),
  WIT( new WIT() ),
  CON( new CON() ),
  MEN( new MEN() ),
  // TODO: Implement luck
  LUC( new LUC()
  {
    @Override
    public double calcBonus( L2Character actor )
    {
      return 1;
    }
  } ),
  // TODO: Implement charisma
  CHA( new CHA()
  {
    @Override
    public double calcBonus( L2Character actor )
    {
      return 1;
    }
  } ),
  NULL( new NULL() );

  public static final int MAX_STAT_VALUE = 201;
  private static final double[] STRbonus = new double[ MAX_STAT_VALUE ];
  private static final double[] INTbonus = new double[ MAX_STAT_VALUE ];
  private static final double[] DEXbonus = new double[ MAX_STAT_VALUE ];
  private static final double[] WITbonus = new double[ MAX_STAT_VALUE ];
  private static final double[] CONbonus = new double[ MAX_STAT_VALUE ];
  private static final double[] MENbonus = new double[ MAX_STAT_VALUE ];
  private static final double[] LUCbonus = new double[ MAX_STAT_VALUE ];
  private static final double[] CHAbonus = new double[ MAX_STAT_VALUE ];
  private static final Logger _log = LogManager.getLogger( BaseStats.class );
  private final BaseStat _stat;

  private BaseStats( BaseStat s )
  {
    _stat = s;
  }

  public String getValue()
  {
    return _stat.getClass().getSimpleName();
  }

  public double calcBonus( L2Character actor )
  {
    if( actor != null )
    {
      return _stat.calcBonus( actor );
    }

    return 1;
  }

  private interface BaseStat
  {
    double calcBonus( L2Character actor );
  }

  private static class STR implements BaseStat
  {
    @Override
    public double calcBonus( L2Character actor )
    {
      return STRbonus[ actor.getSTR() ];
    }
  }

  private static class INT implements BaseStat
  {
    @Override
    public double calcBonus( L2Character actor )
    {
      return INTbonus[ actor.getINT() ];
    }
  }

  private static class DEX implements BaseStat
  {
    @Override
    public double calcBonus( L2Character actor )
    {
      return DEXbonus[ actor.getDEX() ];
    }
  }

  private static class WIT implements BaseStat
  {
    @Override
    public double calcBonus( L2Character actor )
    {
      return WITbonus[ actor.getWIT() ];
    }
  }

  private static class CON implements BaseStat
  {
    @Override
    public double calcBonus( L2Character actor )
    {
      return CONbonus[ actor.getCON() ];
    }
  }

  private static class MEN implements BaseStat
  {
    @Override
    public double calcBonus( L2Character actor )
    {
      return MENbonus[ actor.getMEN() ];
    }
  }

  private static class LUC implements BaseStat
  {
    @Override
    public double calcBonus( L2Character actor )
    {
      return LUCbonus[ actor.getLUC() ];
    }
  }

  private static class CHA implements BaseStat
  {
    @Override
    public double calcBonus( L2Character actor )
    {
      return CHAbonus[ actor.getCHA() ];
    }
  }

  private static class NULL implements BaseStat
  {
    @Override
    public double calcBonus( L2Character actor )
    {
      return 1.0f;
    }
  }

  static
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating( false );
    factory.setIgnoringComments( true );
    File file = FilePath.BASE_STAT_BONUS_DATA;
    Document doc = null;

    if( file.exists() )
    {
      try
      {
        doc = factory.newDocumentBuilder().parse( CryptUtil.decryptOnDemand( file ), file.getParentFile().getAbsolutePath() + File.separator );
      }
      catch( Exception e )
      {
        _log.log( Level.ERROR, "[BaseStats] Could not parse file: " + e.getMessage(), e );
      }

      String statName;
      int val;
      double bonus;

      NamedNodeMap attrs;
      for( Node list = doc.getFirstChild(); list != null; list = list.getNextSibling() )
      {
        if( "list".equalsIgnoreCase( list.getNodeName() ) )
        {
          for( Node stat = list.getFirstChild(); stat != null; stat = stat.getNextSibling() )
          {
            statName = stat.getNodeName();
            for( Node value = stat.getFirstChild(); value != null; value = value.getNextSibling() )
            {
              if( "stat".equalsIgnoreCase( value.getNodeName() ) )
              {
                attrs = value.getAttributes();
                try
                {
                  val = Integer.parseInt( attrs.getNamedItem( "value" ).getNodeValue() );
                  bonus = Double.parseDouble( attrs.getNamedItem( "bonus" ).getNodeValue() );
                }
                catch( Exception e )
                {
                  _log.log( Level.ERROR, "[BaseStats] Invalid stats value: " + value.getNodeValue() + ", skipping" );
                  continue;
                }

                if( "STR".equalsIgnoreCase( statName ) )
                {
                  STRbonus[ val ] = (1000 + bonus) / 1000;
                }
                else if( "INT".equalsIgnoreCase( statName ) )
                {
                  INTbonus[ val ] = (1000 + bonus) / 1000;
                }
                else if( "DEX".equalsIgnoreCase( statName ) )
                {
                  DEXbonus[ val ] = (1000 + bonus) / 1000;
                }
                else if( "WIT".equalsIgnoreCase( statName ) )
                {
                  WITbonus[ val ] = (1000 + bonus) / 1000;
                }
                else if( "CON".equalsIgnoreCase( statName ) )
                {
                  CONbonus[ val ] = (1000 + bonus) / 1000;
                }
                else if( "MEN".equalsIgnoreCase( statName ) )
                {
                  MENbonus[ val ] = (1000 + bonus) / 1000;
                }
                else if( "LUC".equalsIgnoreCase( statName ) )
                {
                  LUCbonus[ val ] = (1000 + bonus) / 1000;
                }
                else if( "CHA".equalsIgnoreCase( statName ) )
                {
                  CHAbonus[ val ] = (1000 + bonus) / 1000;
                }
                else
                {
                  _log.log( Level.ERROR, "[BaseStats] Invalid stats name: " + statName + ", skipping" );
                }
              }
            }
          }
        }
      }
    }
    else
    {
      throw new Error( "[BaseStats] File not found: " + file.getName() );
    }
  }
}