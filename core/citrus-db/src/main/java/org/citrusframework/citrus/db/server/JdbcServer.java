/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.citrus.db.server;

import org.citrusframework.citrus.db.server.builder.RuleBasedControllerBuilder;
import org.citrusframework.citrus.db.server.controller.JdbcController;
import org.citrusframework.citrus.db.server.controller.RuleBasedController;
import org.citrusframework.citrus.db.server.controller.SimpleJdbcController;
import org.citrusframework.citrus.db.server.exceptionhandler.JdbcServerExceptionHandler;
import org.citrusframework.citrus.db.server.handler.connection.*;
import org.citrusframework.citrus.db.server.handler.statement.*;
import org.citrusframework.citrus.db.server.transformer.JsonResponseTransformer;
import org.citrusframework.citrus.db.server.util.DeamonThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import java.util.concurrent.*;

/**
 * @author Christoph Deppisch
 */
public class JdbcServer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JdbcServer.class);

    /** Endpoint configuration */
    private final JdbcServerConfiguration configuration;

    /** Controller handling requests */
    private JdbcController controller;

    /** The spark service */
    private Service service;

    /** Transforms response data to JSON */
    private JsonResponseTransformer responseTransformer = new JsonResponseTransformer();

    /**
     * Default constructor initializing controller and configuration.
     */
    public JdbcServer() {
        this(new JdbcServerConfiguration());
    }

    /**
     * Default constructor using a given configuration.
     * @param configuration The configuration of the jdbc server
     */
    public JdbcServer(final JdbcServerConfiguration configuration) {
        this(new SimpleJdbcController(), configuration);
    }

    /**
     * Default constructor using controller and configuration.
     * @param controller The controller to use for request handling
     * @param configuration The configuration of the jdbc server
     */
    public JdbcServer(final JdbcController controller, final JdbcServerConfiguration configuration) {
        this.controller = controller;
        this.configuration = configuration;
    }

    public JdbcServer(final String[] args) throws JdbcServerException {
        this();
        new JdbcServerOptions().apply(configuration, args);
    }

    public RuleBasedControllerBuilder when() {
        if (!RuleBasedController.class.isAssignableFrom(controller.getClass())) {
            controller = new RuleBasedController();
        }

        return new RuleBasedControllerBuilder((RuleBasedController) controller);
    }

    /**
     * Main method
     * @param args The command line arguments of the java call
     */
    public static void main(final String[] args) throws JdbcServerException {
        final JdbcServer server = new JdbcServer(args);

        if (server.configuration.getTimeToLive() > 0) {
            CompletableFuture.runAsync(() -> {
                try {
                    new CompletableFuture<Void>().get(server.configuration.getTimeToLive(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    server.stop();
                }
            });
        }

        server.start();
    }

    /**
     * Start server instance and listen for incoming requests.
     */
    public void start() {
        if (configuration.isDeamon()) {
            Executors.newSingleThreadExecutor(DeamonThread::new).submit(this::initService);
        } else {
            initService();
        }
    }

    private void initService() {
        service = Service.ignite();
        service.port(configuration.getPort());
        service.before((request, response) -> log.info(request.requestMethod() + " " + request.url()));
        registerEndpoints();
        service.exception(JdbcServerException.class, new JdbcServerExceptionHandler());
    }

    private void registerEndpoints() {
        registerConnectionEndpoint();
        registerStatementEndpoint();
        registerPreparedStatementEndpoint();
        registerCallableStatementEndpoint();
    }

    /**
     * Handles all operations concerning connection operations
     */
    private void registerConnectionEndpoint() {
        service.path("/connection", () -> {
            service.get("", new OpenConnectionHandler(controller));
            service.delete("", new CloseConnectionHandler(controller));
        });

        service.path("/connection/transaction", () -> {
            service.get("", new GetTransactionStateHandler(controller));
            service.post("", new SetTransactionStateHandler(controller));
            service.put("", new CommitTransactionStatementsHandler(controller));
            service.delete("", new RollbackTransactionStatementsHandler(controller));
        });
    }

    /**
     * Handles all operations that are valid for all kinds of statements
     */
    private void registerStatementEndpoint() {
        service.path("/statement", () -> {
            service.get("", new CreateStatementHandler(controller));
            service.delete("", new CloseStatementHandler(controller));
        });

        service.post("/query",
                new ExecuteQueryHandler(controller),
                responseTransformer);

        service.post("/execute",
                new ExecuteStatementHandler(controller),
                responseTransformer);

        service.post("/update", new ExecuteUpdateHandler(controller));
    }

    /**
     * Handles all operations that are prepared statement specific
     */
    private void registerPreparedStatementEndpoint(){
        service.post("/preparedStatement", new CreatePreparedStatementHandler(controller));
    }

    /**
     * Handles all operations that are callable statement specific
     */
    private void registerCallableStatementEndpoint() {
        service.post("/callableStatement", new CreateCallableStatementHandler(controller));
    }

    /**
     * Stops the server instance.
     */
    public void stop() {
        if(service != null){
            service.stop();
        }
    }

    /**
     * Starts the server and awaits its initialization
     */
    public void startAndAwaitInitialization(){
        start();
        service.awaitInitialization();
    }
}
