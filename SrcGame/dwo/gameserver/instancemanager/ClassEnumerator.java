package dwo.gameserver.instancemanager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created with IntelliJ IDEA.
 * User: Eugene Chipachenko
 * Date: 05.08.2015
 * Time: 20:19
 */
class ClassEnumerator
{
  private static final Logger _log = LogManager.getLogger( ClassEnumerator.class );
  private static void log( String msg )
  {
    _log.info( "ClassDiscovery: " + msg );
  }

  private static Class<?> loadClass( String className )
  {
    try
    {
      return Class.forName( className );
    }
    catch( ClassNotFoundException e )
    {
      throw new RuntimeException( "Unexpected ClassNotFoundException loading class '" + className + "'" );
    }
  }

  private static void processDirectory( File directory, String pkgname, ArrayList<Class<?>> classes )
  {
    log( "Reading Directory '" + directory + "'" );
    // Get the list of the files contained in the package
    String[] files = directory.list();
    for( int i = 0; i < files.length; i++ )
    {
      String fileName = files[ i ];
      String className = null;
      // we are only interested in .class files
      if( fileName.endsWith( ".class" ) )
      {
        // removes the .class extension
        className = pkgname + '.' + fileName.substring( 0, fileName.length() - 6 );
      }
      log( "FileName '" + fileName + "'  =>  class '" + className + "'" );
      if( className != null )
      {
        classes.add( loadClass( className ) );
      }
      File subdir = new File( directory, fileName );
      if( subdir.isDirectory() )
      {
        processDirectory( subdir, pkgname + '.' + fileName, classes );
      }
    }
  }

  private static void processJarfile( URL resource, String pkgname, ArrayList<Class<?>> classes )
  {
    String relPath = pkgname.replace( '.', '/' );
    String resPath = resource.getPath();
    String jarPath = resPath.replaceFirst( "[.]jar[!].*", ".jar" ).replaceFirst( "file:", "" );
    log( "Reading JAR file: '" + jarPath + "'" );
    JarFile jarFile;
    try
    {
      jarFile = new JarFile( jarPath );
    }
    catch( IOException e )
    {
      throw new RuntimeException( "Unexpected IOException reading JAR File '" + jarPath + "'", e );
    }
    Enumeration<JarEntry> entries = jarFile.entries();
    while( entries.hasMoreElements() )
    {
      JarEntry entry = entries.nextElement();
      String entryName = entry.getName();
      String className = null;
      if( entryName.endsWith( ".class" ) && entryName.startsWith( relPath ) && entryName.length() > (relPath.length() + "/".length()) )
      {
        className = entryName.replace( '/', '.' ).replace( '\\', '.' ).replace( ".class", "" );
      }
      log( "JarEntry '" + entryName + "'  =>  class '" + className + "'" );
      if( className != null )
      {
        classes.add( loadClass( className ) );
      }
    }
  }

  public static ArrayList<Class<?>> getClassesForPackage( String pkg )
  {
    ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

    String relPath = pkg.replace( '.', '/' );

    // Get a File object for the package
    URL resource = ClassLoader.getSystemClassLoader().getResource( relPath );
    if( resource == null )
    {
      throw new RuntimeException( "Unexpected problem: No resource for " + relPath );
    }
    log( "Package: '" + pkg + "' becomes Resource: '" + resource.toString() + "'" );

    resource.getPath();
    if( resource.toString().startsWith( "jar:" ) )
    {
      processJarfile( resource, pkg, classes );
    }
    else
    {
      processDirectory( new File( resource.getPath() ), pkg, classes );
    }

    return classes;
  }
}
