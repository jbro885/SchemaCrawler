/* 
 *
 * SchemaCrawler
 * http://sourceforge.net/projects/schemacrawler
 * Copyright (c) 2000-2011, Sualeh Fatehi.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */

package schemacrawler.tools.executable;


import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import schemacrawler.schemacrawler.SchemaCrawlerException;

/**
 * Parses the command line.
 * 
 * @author Sualeh Fatehi
 */
public final class CommandRegistry
{

  private static final Logger LOGGER = Logger.getLogger(CommandRegistry.class
    .getName());

  private static Map<String, String> loadCommandRegistry()
    throws SchemaCrawlerException
  {
    final Map<String, String> commandRegistry = new HashMap<String, String>();
    final Set<URL> commandRegistryUrls = new HashSet<URL>();
    try
    {
      final ClassLoader classLoader = CommandRegistry.class.getClassLoader();
      Enumeration<URL> resources;

      resources = classLoader.getResources("tools.command.properties");
      commandRegistryUrls.addAll(Collections.list(resources));
      //
      resources = classLoader.getResources("command.properties");
      commandRegistryUrls.addAll(Collections.list(resources));
    }
    catch (final IOException e)
    {
      throw new SchemaCrawlerException("Could not load command registry", e);
    }
    for (final URL commandRegistryUrl: commandRegistryUrls)
    {
      try
      {
        final Properties commandRegistryProperties = new Properties();
        commandRegistryProperties.load(commandRegistryUrl.openStream());
        final List<String> propertyNames = (List<String>) Collections
          .list(commandRegistryProperties.propertyNames());
        for (final String commandName: propertyNames)
        {
          final String executableClassName = commandRegistryProperties
            .getProperty(commandName);
          commandRegistry.put(commandName, executableClassName);
        }
      }
      catch (final IOException e)
      {
        LOGGER.log(Level.WARNING, "Could not load command registry, "
                                  + commandRegistryUrl, e);
      }
    }
    if (commandRegistry.isEmpty())
    {
      throw new SchemaCrawlerException("Could not load any command registry");
    }
    return commandRegistry;
  }

  private final Map<String, String> commandRegistry;

  public CommandRegistry()
    throws SchemaCrawlerException
  {
    commandRegistry = loadCommandRegistry();
  }

  public Executable instantiateExecutableForCommand(final String command)
    throws SchemaCrawlerException
  {
    final String commandExecutableClassName = lookupExecutableClassName(command);
    if (commandExecutableClassName == null)
    {
      throw new SchemaCrawlerException("No executable found for command '"
                                       + command + "'");
    }

    Class<? extends Executable> commandExecutableClass;
    try
    {
      commandExecutableClass = (Class<? extends Executable>) Class
        .forName(commandExecutableClassName);
    }
    catch (final ClassNotFoundException e)
    {
      throw new SchemaCrawlerException("Could not load class "
                                       + commandExecutableClassName, e);
    }

    Executable executable;
    try
    {
      executable = commandExecutableClass.newInstance();
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.FINE, "Could not instantiate "
                             + commandExecutableClassName
                             + " using the default constructor");
      try
      {
        final Constructor<? extends Executable> constructor = commandExecutableClass
          .getConstructor(new Class[] {
            String.class
          });
        executable = constructor.newInstance(command);
      }
      catch (final Exception e1)
      {
        throw new SchemaCrawlerException("Could not instantiate executable for command '"
                                             + command + "'",
                                         e1);
      }
    }

    return executable;
  }

  public String[] lookupAvailableCommands()
  {
    final Set<String> availableCommandsList = commandRegistry.keySet();
    availableCommandsList.remove("default");
    final String[] availableCommands = availableCommandsList
      .toArray(new String[availableCommandsList.size()]);
    Arrays.sort(availableCommands);
    return availableCommands;
  }

  public String lookupExecutableClassName(final String command)
  {
    final String commandExecutableClassName;
    if (commandRegistry.containsKey(command))
    {
      commandExecutableClassName = commandRegistry.get(command);
    }
    else
    {
      commandExecutableClassName = commandRegistry.get("default");
    }
    return commandExecutableClassName;
  }

}
