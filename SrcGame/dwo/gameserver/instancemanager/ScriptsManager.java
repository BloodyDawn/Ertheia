package dwo.gameserver.instancemanager;

import dwo.gameserver.model.world.quest.Quest;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;

public class ScriptsManager
{
  private static final Logger _log = LogManager.getLogger( ScriptsManager.class );
  private static final String SCRIPT_PATH = "dwo.scripts";

  private ScriptsManager()
  {
    _log.log( Level.INFO, "Initializing Script Engine Manager" );
  }

  public static ScriptsManager getInstance()
  {
    return SingletonHolder._instance;
  }

  public void executeCoreScripts()
  {
    Collection<Class<?>> classes = ClassEnumerator.getClassesForPackage( SCRIPT_PATH );
    for( Class<?> cls : classes )
    {
      if( cls.getSimpleName().equals( "AirShipController" ) )
      {
        continue;
      }

      if( cls.isAnonymousClass() || cls.isLocalClass() || cls.isMemberClass() )
        continue;

      try
      {
        Method m = cls.getMethod( "main", String[].class);
        if (m == null)
          continue;

        if( m.getDeclaringClass().equals( cls ) ) // Check for classes like Sagas
        {
          m.invoke( cls, new Object[] { new String[] {} } );
        }
        continue;
      }
      catch( Exception e )
      {
        _log.log( Level.ERROR, e.getMessage(), e );
      }

      try
      {
        Constructor<?> c = cls.getConstructor();
        Quest q = (Quest) c.newInstance();
        q.setAltMethodCall( true );
      }
      catch( Exception e )
      {
        _log.log( Level.ERROR, e.getMessage(), e );
      }
    }
  }

  private static class SingletonHolder
  {
    protected static final ScriptsManager _instance = new ScriptsManager();
  }

  public static void main( String[] args )
  {
    ScriptsManager.getInstance().executeCoreScripts();
  }
}
