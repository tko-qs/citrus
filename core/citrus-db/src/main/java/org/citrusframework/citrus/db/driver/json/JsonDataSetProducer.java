/*
 * Copyright 2006-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.citrus.db.driver.json;

import org.citrusframework.citrus.db.driver.JdbcDriverException;
import org.citrusframework.citrus.db.driver.data.Row;
import org.citrusframework.citrus.db.driver.dataset.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.citrus.db.driver.dataset.DataSet;
import org.citrusframework.citrus.db.driver.dataset.DataSetBuilder;
import org.citrusframework.citrus.db.driver.dataset.DataSetProducer;

import java.io.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Christoph Deppisch
 */
public class JsonDataSetProducer implements DataSetProducer {

    /** Json data used as table source */
    private final InputStream input;

    private DataSetBuilder builder;

    public JsonDataSetProducer(File file) {
        this(file.toPath());
    }

    public JsonDataSetProducer(Path path) {
        try {
            this.input = new FileInputStream(path.toFile());
        } catch (FileNotFoundException e) {
            throw new JdbcDriverException("Failed to access json input file content", e);
        }
    }

    public JsonDataSetProducer(String jsonInput) {
        this.input = new ByteArrayInputStream(jsonInput.getBytes());
    }

    public JsonDataSetProducer(InputStream inputStream) {
        this.input = inputStream;
    }

    @Override
    public DataSet produce() throws SQLException {
        if (builder != null) {
            return builder.build();
        }

        builder = new DataSetBuilder();

        try {
            List<Map<String, Object>> rawDataSet = new ObjectMapper().readValue(input, List.class);

            rawDataSet.forEach(rowData -> {
                Row row = new Row();
                row.getValues().putAll(rowData);
                builder.add(row);
            });
        } catch (IOException e) {
            throw new JdbcDriverException("Unable to read table data set from Json input", e);
        }

        return builder.build();
    }
}
