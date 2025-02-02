package io.perfana.scheduler;

import io.perfana.event.PerfanaEventConfig;
import io.perfana.event.wiremock.WiremockEventConfig;
import io.perfana.events.commandrunner.CommandRunnerEventConfig;
import io.perfana.eventscheduler.EventScheduler;
import io.perfana.eventscheduler.EventSchedulerBuilder;
import io.perfana.eventscheduler.api.EventLogger;
import io.perfana.eventscheduler.api.SchedulerExceptionHandler;
import io.perfana.eventscheduler.api.config.EventConfig;
import io.perfana.eventscheduler.api.config.EventSchedulerConfig;
import io.perfana.eventscheduler.api.config.TestConfig;
import io.perfana.eventscheduler.log.EventLoggerStdOut;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestScheduler {

    public static void main(String[] args) {

        EventLogger eventLogger = EventLoggerStdOut.INSTANCE;
        //EventLogger eventLogger = EventLoggerStdOut.INSTANCE_DEBUG;

        final int rampupTimeInSeconds = 30;
        final int constantLoadTimeInSeconds = 300;
        final int totalRunTimeInSeconds = rampupTimeInSeconds + constantLoadTimeInSeconds;
        final int totalSleepSecondsPlusSlack = totalRunTimeInSeconds + 120;

        final String perfanaApiKey = System.getenv("PERFANA_API_KEY");
        final String perfanaUrl = getEnvOrDefault("PERFANA_URL", "http://localhost:4000");
        final String influxUrl = getEnvOrDefault("INFLUX_URL", "http://localhost:8086");
        final String influxDb = getEnvOrDefault("INFLUX_DB_K6", "k6");
        final String influxDbUsername = getEnvOrDefault("INFLUX_USER", "admin");
        final String influxDbPassword = getEnvOrDefault("INFLUX_PASSWORD", "admin");

        final String testEnvironment = "silver";
        final String workload = "load-resilience";
        final String systemUnderTest = "tiny-bank";

        final List<String> tags = List.of("jfr", "k6", "spring-boot-kubernetes");

        TestConfig testConfig = TestConfig.builder()
                .workload(workload)
                .tags(tags)
                .testEnvironment(testEnvironment)
                .systemUnderTest(systemUnderTest)
                .buildResultsUrl("http://perfana.io")
                .version("1.0.0")
                .rampupTimeInSeconds(rampupTimeInSeconds)
                .constantLoadTimeInSeconds(constantLoadTimeInSeconds)
                .annotations("First resilience test with balance service delays up to 2 seconds.")
                .build();

        List<EventConfig> eventConfigs = new ArrayList<>();

        {
            if (perfanaApiKey == null) {
                System.err.println("PERFANA_API_KEY environment variable not set, skipping Perfana event.");
            }
            else {
                PerfanaEventConfig perfanaEventConfig = new PerfanaEventConfig();
                perfanaEventConfig.setName("perfana-event");
                perfanaEventConfig.setApiKey(perfanaApiKey);
                perfanaEventConfig.setPerfanaUrl(perfanaUrl);
                eventConfigs.add(perfanaEventConfig);
            }
        }

        {
            CommandRunnerEventConfig commandConfig = new CommandRunnerEventConfig();
            commandConfig.setName("command-runner-wait-for-start");
            // wait for script to finish before starting the test
            commandConfig.setReadyForStartParticipant(true);
            commandConfig.setOnBeforeTest("./start-test-components.sh --jfr-agent");
            eventConfigs.add(commandConfig);
        }

        {
            CommandRunnerEventConfig commandConfig = new CommandRunnerEventConfig();
            commandConfig.setName("command-runner-k6-to-influx");
            commandConfig.setOnStartTest("./x2i . -i k6 -u " + influxDbUsername + " -p " + influxDbPassword + " -a " + influxUrl + " -b " + influxDb + " -t " + testEnvironment + " -y " + systemUnderTest + " -s " + totalSleepSecondsPlusSlack);
            eventConfigs.add(commandConfig);
        }

        {
            CommandRunnerEventConfig commandConfig = new CommandRunnerEventConfig();
            commandConfig.setName("command-runner-k6");
            commandConfig.setOnStartTest("export DURATION=" + totalRunTimeInSeconds + "s; k6 run --quiet --out csv=test_results.csv loadtest/k6_load_test.js");
            eventConfigs.add(commandConfig);
        }

        {
            WiremockEventConfig wiremockAccount = new WiremockEventConfig();
            wiremockAccount.setWiremockFilesDir("loadtest/mappers-account");
            wiremockAccount.setName("wiremock-account");
            wiremockAccount.setWiremockUrl("http://localhost:30123");
            eventConfigs.add(wiremockAccount);
        }

        {
            WiremockEventConfig wiremockBalance = new WiremockEventConfig();
            wiremockBalance.setWiremockFilesDir("loadtest/mappers-balance");
            wiremockBalance.setName("wiremock-balance");
            wiremockBalance.setWiremockUrl("http://localhost:30124");
            eventConfigs.add(wiremockBalance);
        }

        String scheduleScript =
                """
                    PT2S|wiremock-change-mappings(no-0ms)|delay_account=0;delay_balance=0
                    PT30S|wiremock-change-mappings(short-100ms)|delay_account=100;delay_balance=100
                    PT60S|wiremock-change-mappings(slow-500ms)|delay_account=500;delay_balance=500
                    PT120S|wiremock-change-mappings(balance-really-slow-1s)|delay_account=500;delay_balance=1000
                    PT180S|wiremock-change-mappings(balance-really-slow-2s)|delay_account=500;delay_balance=2000
                    PT220S|wiremock-change-mappings(short-delay-100ms)|delay_account=100;delay_balance=100
                """;
        {
            CommandRunnerEventConfig commandConfig = new CommandRunnerEventConfig();
            commandConfig.setName("command-runner-stop-processes");
            commandConfig.setOnAfterTest("./stop-test.sh");
            commandConfig.setOnAbort("./stop-test.sh");
            eventConfigs.add(commandConfig);
        }

        EventSchedulerConfig eventSchedulerConfig = EventSchedulerConfig.builder()
                .testConfig(testConfig)
                .eventConfigs(eventConfigs)
                .scheduleScript(scheduleScript)
                .build();

        EventScheduler scheduler = EventSchedulerBuilder.of(eventSchedulerConfig, eventLogger);
        CountDownLatch abortLatch = new CountDownLatch(1);

        addKillSwitch(scheduler);

        // the shutdown hook will count down abortLatch when finished aborting
        registerShutdownHook(scheduler, abortLatch);

        scheduler.startSession();

        try {
            waitForTestToFinish(totalSleepSecondsPlusSlack);
        } finally {
            scheduler.stopSession();
        }

        try {
            println("Waiting for abort to finish");
            abortLatch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            println("Interrupted while waiting for abort to finish");
        }
    }

    private static String getEnvOrDefault(String envVar, String defaultValue) {
        return System.getenv(envVar) != null ? System.getenv(envVar) : defaultValue;
    }

    private static void registerShutdownHook(EventScheduler scheduler, CountDownLatch abortLatch) {
        Runtime.getRuntime().addShutdownHook(
                new Thread("app-shutdown-hook") {
                    @Override
                    public void run() {
                        println("SHUTDOWN detected: waiting for abort to finish");
                        if (scheduler.isSessionStopped()) {
                            println("SHUTDOWN detected: abort already done");
                            abortLatch.countDown();
                        }
                        else {
                            scheduler.abortSession();
                            println("SHUTDOWN detected: abort session done");
                            abortLatch.countDown();
                        }
                    }
                });
    }

    private static void addKillSwitch(EventScheduler scheduler) {
        scheduler.addKillSwitch(new SchedulerExceptionHandler() {
            @Override
            public void kill(String message) {
                println("Kill switch requested: " + message);
                scheduler.abortSession();
                System.exit(3);
            }

            @Override
            public void abort(String message) {
                println("Abort requested: " + message);
                scheduler.abortSession();
                sleep(3);
                System.exit(4);
            }

            @Override
            public void stop(String message) {
                println("Stop requested: " + message);
                scheduler.stopSession();
                System.exit(5);
            }
        });
    }

    private static void println(String text) {
        System.out.println(text);
    }

    private static void waitForTestToFinish(int totalSleepSeconds) {
        sleep(totalSleepSeconds);
    }

    private static void sleep(int totalSleepSeconds) {
        try {
            Thread.sleep(Duration.ofSeconds(totalSleepSeconds).toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            println("Interrupted while waiting for test to finish");
        }
    }
}