package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class CustomSpawnTable
{
  private static ArrayList<CustomSpawn> customSpawns;

  public static void addCustomSpawn( final int id, final int x, final int y, final int z, final int h,
                                     final boolean save )
  {
    CustomSpawnTable.customSpawns.add( new CustomSpawn( id, x, y, z, h ) );
    if( save )
    {
      save();
    }
  }

  public static void delete( final int id, final int x, final int y, final int z )
  {
    final ArrayList<CustomSpawn> deletes = new ArrayList<CustomSpawn>();
    for( final CustomSpawn customSpawn : CustomSpawnTable.customSpawns )
    {
      if( customSpawn.getId() == id && customSpawn.getX() == x && customSpawn.getY() == y && customSpawn.getZ() == z )
      {
        deletes.add( customSpawn );
      }
    }
    for( final CustomSpawn customSpawn : deletes )
    {
      CustomSpawnTable.customSpawns.remove( customSpawn );
    }
    save();
  }

  private static void save()
  {
    try
    {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      final DocumentBuilder builder = factory.newDocumentBuilder();
      final Document doc = builder.newDocument();
      final Element ListElement = doc.createElement( "list" );
      ListElement.setAttribute( "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
      ListElement.setAttribute( "xsi:noNamespaceSchemaLocation", "../../data/xsd/locationSpawnlist.xsd" );
      final Element RootElement = doc.createElement( "spawns" );
      RootElement.setAttribute( "name", "main" );
      RootElement.setAttribute( "isCustom2", "true" );
      ListElement.appendChild( RootElement );
      for( final CustomSpawn customSpawn : CustomSpawnTable.customSpawns )
      {
        final Element NameElementTitle = doc.createElement( "npc" );
        NameElementTitle.setAttribute( "id", String.valueOf( customSpawn.getId() ) );
        RootElement.appendChild( NameElementTitle );
        final Element NameElementCompile = doc.createElement( "spawn" );
        NameElementCompile.setAttribute( "x", String.valueOf( customSpawn.getX() ) );
        NameElementCompile.setAttribute( "y", String.valueOf( customSpawn.getY() ) );
        NameElementCompile.setAttribute( "z", String.valueOf( customSpawn.getZ() ) );
        NameElementCompile.setAttribute( "heading", String.valueOf( customSpawn.getH() ) );
        NameElementCompile.setAttribute( "respawn", "60" );
        NameElementTitle.appendChild( NameElementCompile );
      }
      doc.appendChild( ListElement );
      final Transformer t = TransformerFactory.newInstance().newTransformer();
      t.setOutputProperty( "method", "xml" );
      t.setOutputProperty( "indent", "yes" );
      t.transform( new DOMSource( doc ), new StreamResult( new FileOutputStream( FilePath.CUSTOM_STATIC_SPAWN_DATA + "/main.xml" ) ) );
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
  }

  static
  {
    CustomSpawnTable.customSpawns = new ArrayList<CustomSpawn>();
  }

  static class CustomSpawn
  {
    private int id;
    private int x;
    private int y;
    private int z;
    private int h;

    CustomSpawn( final int id, final int x, final int y, final int z, final int h )
    {
      super();
      this.id = id;
      this.x = x;
      this.y = y;
      this.z = z;
      this.h = h;
    }

    int getId()
    {
      return id;
    }

    int getX()
    {
      return x;
    }

    int getY()
    {
      return y;
    }

    int getZ()
    {
      return z;
    }

    int getH()
    {
      return h;
    }
  }
}
