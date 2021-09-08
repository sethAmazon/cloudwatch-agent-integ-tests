package integration.ssl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest(classes={com.cloud.watch.agent.opensource.test.OpensourceIntegTestApplication.class})
public class CABundleTest {
    private static final Logger logger = LoggerFactory.getLogger(CABundleTest.class);
    private static final String configOutputPath = "/opt/aws/amazon-cloudwatch-agent/bin/config.json";
    private static final String commonConfigOutputPath = "/opt/aws/amazon-cloudwatch-agent/etc/common-config.toml";
    private static final String configJSON = "/config.json";
    private static final String commonConfigTOML = "/common-config.toml";
    private static final String outputLog = "/opt/aws/amazon-cloudwatch-agent/logs/amazon-cloudwatch-agent.log";
    private static final String targetString = "x509";
    //Let the agent run for 2 minutes. This will give agent enough time to call server
    private static final int agentRuntime = 120000;

    private static Stream<Arguments> testCases() {
        return Stream.of(
                //arguments(false, "integration/ssl/with/bundle")
                arguments(true, "integration/ssl/without/bundle")
        );
    };

    @BeforeEach
    public void before() {
        clearLogFile();
    }

    @ParameterizedTest
    @MethodSource("testCases")
    public void bundleTest(boolean findTarget, String dataInput) throws IOException, InterruptedException {
        logger.info("Find target : {} data input : {}", findTarget, dataInput);
        String configInputPath = new ClassPathResource(dataInput + configJSON).getFile().getPath();
        String commonConfigInputPath = new ClassPathResource(dataInput + commonConfigTOML).getFile().getPath();
        logger.info("Config file path : {} common config file path : {}", configInputPath, commonConfigInputPath);
        copyFile(configInputPath, configOutputPath);
        copyFile(commonConfigInputPath, commonConfigOutputPath);
        startTheAgent();
        Thread.sleep(agentRuntime);
        logger.info("Agent has been running for : {}", agentRuntime);
        stopTheAgent();
        readTheOutputLog(findTarget);
    }

    private void clearLogFile() throws IOException {
        File logFile = new File(outputLog);
        logFile.delete();
        logFile.createNewFile();
    }

    private void copyFile(String pathIn, String pathOut) throws IOException {
        String cmd = "sudo cp " + pathIn + " " + pathOut;
        Runtime run = Runtime.getRuntime();
        run.exec(cmd);
        logger.info("File : {} copied to : {}", pathIn, pathOut);
    }

    private void startTheAgent() throws IOException, InterruptedException {
        String cmd = "sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a " +
                "fetch-config -m ec2 -s -c file:" +
                configOutputPath;
        Runtime run = Runtime.getRuntime();
        Process process = run.exec(cmd);
        process.waitFor();
        logger.info("Agent has started");
    }

    private void stopTheAgent() throws IOException, InterruptedException {
        String cmd = "sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a stop";
        Runtime run = Runtime.getRuntime();
        Process process = run.exec(cmd);
        process.waitFor();
        logger.info("Agent is stopped");
    }

    private void readTheOutputLog(boolean findTarget) throws FileNotFoundException, InterruptedException {
        Thread.sleep(5000);
        File logFile = new File(outputLog);
        Scanner scanner = new Scanner(logFile);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            logger.info("Line read from agent log file : {}", line);
            boolean containsTarget = line.contains(targetString);
            if ((findTarget && !containsTarget) || (!findTarget && containsTarget)) {
                logger.error("Contains target : {} and find target : {} do not match", containsTarget, findTarget);
                fail();
            }
        }
        scanner.close();
        logger.info("Finished reading log file");
    }

}
