/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2020, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/

package schemacrawler.test.script;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;
import static schemacrawler.test.utility.FileHasContent.classpathResource;
import static schemacrawler.test.utility.FileHasContent.hasSameContentAs;
import static schemacrawler.test.utility.FileHasContent.outputOf;
import static schemacrawler.test.utility.ScriptTestUtility.commandLineScriptExecution;
import static schemacrawler.test.utility.TestUtility.compareOutput;
import static schemacrawler.test.utility.TestUtility.validateDiagram;
import static us.fatehi.utility.IOUtility.readFully;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.test.utility.DatabaseConnectionInfo;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;
import schemacrawler.tools.options.TextOutputFormat;
import schemacrawler.tools.text.schema.SchemaTextOptions;
import schemacrawler.tools.text.schema.SchemaTextOptionsBuilder;
import us.fatehi.utility.IOUtility;

@ExtendWith(TestDatabaseConnectionParameterResolver.class)
public class SchemaCrawlerExecutableChainTest {

  @Test
  public void commandlineChain(final DatabaseConnectionInfo connectionInfo) throws Exception {
    assertThat(
        outputOf(commandLineScriptExecution(connectionInfo, "/chain_script.js")),
        hasSameContentAs(classpathResource("chain_output.txt")));

    final Path schemaFile = Paths.get("chain_schema.txt");
    final List<String> failures =
        compareOutput("chain_schema.txt", schemaFile, TextOutputFormat.text.name());
    if (failures.size() > 0) {
      fail(failures.toString());
    }
    Files.deleteIfExists(schemaFile);

    final Path diagramFile = Paths.get("chain_schema.png");
    validateDiagram(diagramFile);
    Files.deleteIfExists(diagramFile);
  }

  @Test
  public void executableChain(final Connection connection) throws Exception {
    final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable("script");
    final Path testOutputFile = IOUtility.createTempFilePath("sc", "data");

    final LimitOptionsBuilder limitOptionsBuilder =
        LimitOptionsBuilder.builder().includeAllRoutines();
    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withLimitOptions(limitOptionsBuilder.toOptions());

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder.builder();
    textOptionsBuilder.noSchemaCrawlerInfo(false).showDatabaseInfo().showJdbcDriverInfo();
    final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

    final Config additionalConfiguration = SchemaTextOptionsBuilder.builder(textOptions).toConfig();
    additionalConfiguration.put("script", "/chain.js");

    final OutputOptions outputOptions =
        OutputOptionsBuilder.newOutputOptions("text", testOutputFile);

    executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
    executable.setOutputOptions(outputOptions);
    executable.setAdditionalConfiguration(additionalConfiguration);
    executable.setConnection(connection);
    executable.execute();

    assertThat(
        "Created files \"schema.txt\" and \"schema.png\"" + System.lineSeparator(),
        equalTo(readFully(new FileReader(testOutputFile.toFile()))));

    final Path schemaFile = Paths.get("schema.txt");
    final List<String> failures =
        compareOutput("schema.txt", schemaFile, TextOutputFormat.text.name());
    if (failures.size() > 0) {
      fail(failures.toString());
    }
    Files.deleteIfExists(schemaFile);

    final Path diagramFile = Paths.get("schema.png");
    validateDiagram(diagramFile);
    Files.deleteIfExists(diagramFile);
  }
}
