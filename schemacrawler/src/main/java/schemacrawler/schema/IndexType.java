/*
 * SchemaCrawler
 * Copyright (c) 2000-2008, Sualeh Fatehi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package schemacrawler.schema;


import java.sql.DatabaseMetaData;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An enumeration wrapper around index types.
 */
public enum IndexType
{

  /** Unknown */
  unknown(-1),
  /** Statistic. */
  statistic(DatabaseMetaData.tableIndexStatistic),
  /** Clustered. */
  clustered(DatabaseMetaData.tableIndexClustered),
  /** Hashed. */
  hashed(DatabaseMetaData.tableIndexHashed),
  /** Other. */
  other(DatabaseMetaData.tableIndexOther);

  private static final Logger LOGGER = Logger.getLogger(IndexType.class
    .getName());

  /**
   * Gets the value from the id.
   * 
   * @param id
   *        Id of the enumeration.
   * @return IndexType
   */
  public static IndexType valueOf(final int id)
  {
    for (final IndexType type: IndexType.values())
    {
      if (type.getId() == id)
      {
        return type;
      }
    }
    LOGGER.log(Level.FINE, "Unknown id " + id);
    return unknown;
  }

  private final int id;

  private IndexType(final int id)
  {
    this.id = id;
  }

  /**
   * Gets the id.
   * 
   * @return id
   */
  public int getId()
  {
    return id;
  }

}
