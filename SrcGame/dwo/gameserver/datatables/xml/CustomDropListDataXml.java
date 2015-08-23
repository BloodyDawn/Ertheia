package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.npc.drop.L2DropData;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomDropListDataXml extends XmlDocumentParser
{
  private List<DropList> dropLists = new ArrayList<>();

  private CustomDropListDataXml()
  {
    try
    {
      load();
    }
    catch( JDOMException | IOException e )
    {
      e.printStackTrace();
    }
  }

  @Override
  public void load() throws JDOMException, IOException
  {
    dropLists.clear();
    int dropDataCount = 0;
    List<File> fileList = Util.getAllFileList( FilePath.CUSTOM_DROPLIST_DIR, "xml" );

    for( File f : fileList )
    {
      parseFile( f );
    }

    for( DropList dl : dropLists )
    {
      L2NpcTemplate template = NpcTable.getInstance().getTemplate( dl.getId() );
      if( template == null )
      {
        _log.log( Level.WARN, ("Omitted NPC ID: " + dl.getId() + " - NPC template does not exists!") );
        return;
      }

      template.clearAllDropData();

      for( DropCategory cat : dl.getCategories() )
      {
        template.setDropCategoryChance( cat.getId(), (int) cat.getChance() );
        for( L2DropData item : cat.getItems() )
        {
          item.setChance( item.getChance() * 10000.0 );
          template.addDropData( item, cat.getId() );
        }
      }
      ++dropDataCount;
    }
    _log.log( Level.INFO, (this.getClass().getSimpleName() + ": Loaded " + dropDataCount + " drops.") );
  }

  @Override
  protected void parseDocument( Element rootElement )
  {
    try
    {
      for( Element l : rootElement.getChildren() )
      {
        for( Element n : l.getChildren() )
        {
          if( "npc".equalsIgnoreCase( n.getName() ) )
          {
            int npcId = Integer.parseInt( n.getAttributeValue( "id" ) );
            L2NpcTemplate template = NpcTable.getInstance().getTemplate( npcId );
            if( template == null )
            {
              _log.log( Level.WARN, ("Omitted NPC ID: " + npcId + " - NPC template does not exists!") );
            }
            else
            {
              template.clearAllDropData();
              DropList dropList = new DropList();
              dropList.categories = new ArrayList<>();
              dropList.id = npcId;
              dropLists.add( dropList );
              for( Element d : n.getChildren() )
              {
                if( d.getName().equals( "category" ) )
                {
                  int categoryId = Integer.parseInt( d.getAttributeValue( "id" ) );
                  double cache = Double.parseDouble( d.getAttributeValue( "chance" ) );
                  DropCategory dropCategory = new DropCategory();
                  dropCategory.id = categoryId;
                  dropCategory.setChance( cache );
                  dropList.categories.add( dropCategory );

                  List<L2DropData> items = new ArrayList<>();
                  dropCategory.items = items;
                  for( Element r : d.getChildren() )
                  {
                    if( r.getName().equals( "items" ) )
                    {
                      for( Element i : r.getChildren() )
                      {
                        if( i.getName().equals( "item" ) )
                        {
                          int itemId = Integer.parseInt( i.getAttributeValue( "id" ) );
                          int min = Integer.parseInt( i.getAttributeValue( "min" ) );
                          int max = Integer.parseInt( i.getAttributeValue( "max" ) );
                          double chance = Double.parseDouble( i.getAttributeValue( "chance" ) );

                          L2DropData dropData = new L2DropData();
                          dropData.setItemId( itemId );
                          dropData.setMinDrop( min );
                          dropData.setMaxDrop( max );
                          dropData.setChance( chance );
                          items.add( dropData );
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    catch( Exception e )
    {
      _log.error( ("Failed to read data from file [" + getCurrentFile() + "]."), e );
    }
  }

  public static CustomDropListDataXml getInstance()
  {
    return SingletonHolder._instance;
  }

  private static class SingletonHolder
  {
    protected static CustomDropListDataXml _instance;

    static
    {
      _instance = new CustomDropListDataXml();
    }
  }

  public static class DropCategory
  {
    private int id;
    private double chance;
    private List<L2DropData> items;

    public DropCategory()
    {
      super();
      items = Collections.emptyList();
    }

    public int getId()
    {
      return id;
    }

    public double getChance()
    {
      return chance;
    }

    public void setChance( double chance )
    {
      this.chance = chance * 10000.0;
    }

    public List<L2DropData> getItems()
    {
      return items;
    }
  }

  public static class DropList
  {
    private int id;
    private List<DropCategory> categories;

    public DropList()
    {
      super();
      categories = Collections.emptyList();
    }

    public int getId()
    {
      return id;
    }

    public List<DropCategory> getCategories()
    {
      return categories;
    }
  }
}
