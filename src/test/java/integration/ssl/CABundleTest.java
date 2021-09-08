package integration.ssl;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes={com.cloud.watch.agent.opensource.test.OpensourceIntegTestApplication.class})
@RunWith(Parameterized.class)
public class CABundleTest {
    private static final Logger logger = LoggerFactory.getLogger(CABundleTest.class);
    private static final String configOutputPath = "/opt/aws/amazon-cloudwatch-agent/bin/config.json";
    private static final String commonConfigOutputPath = "/opt/aws/amazon-cloudwatch-agent/etc/common-config.toml";
    private static final String configJSON = "/config.json";
    private static final String commonConfigTOML = "/common-config.toml";
    private static final String outputLog = "/opt/aws/amazon-cloudwatch-agent/logs/amazon-cloudwatch-agent.log";
    private static final String targetString = "x509";
    //Let the agent run for 5 minutes. This will give us at least 4 cycles of data
    private static final int agentRuntime = 300000;
    private final boolean findTarget;
    private String dataInput = "integration/ssl";

    public CABundleTest(boolean findTarget, String dataInput) {
        this.findTarget = findTarget;
        this.dataInput = this.dataInput + dataInput;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> bundle() {
        return Arrays.asList(new Object[][] {
                { false, "/with/bundle" },
                { true, "without/bundle"}
        });
    }

    @Test
    public void withBundleTest() throws IOException, InterruptedException {
        String configInputPath = new ClassPathResource(dataInput + configJSON).getFile().getPath();
        String commonConfigInputPath = new ClassPathResource(dataInput + commonConfigTOML).getFile().getPath();
        logger.info("Config file path : {} common config file path : {}", configInputPath, commonConfigInputPath);
        copyFile(configInputPath, configOutputPath);
        copyFile(commonConfigInputPath, commonConfigOutputPath);
        startTheAgent();
        Thread.sleep(agentRuntime);
        stopTheAgent();
        readTheOutputLog();
    }

    private void copyFile(String pathIn, String pathOut) throws IOException {
        String cmd = "sudo cp " + pathIn + " " + pathOut;
        Runtime run = Runtime.getRuntime();
        run.exec(cmd);
    }

    private void startTheAgent() throws IOException, InterruptedException {
        String cmd = "sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a " +
                "fetch-config -m ec2 -s -c file:" +
                configOutputPath;
        Runtime run = Runtime.getRuntime();
        Process process = run.exec(cmd);
        process.waitFor();
    }

    private void stopTheAgent() throws IOException, InterruptedException {
        String cmd = "sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a stop";
        Runtime run = Runtime.getRuntime();
        Process process = run.exec(cmd);
        process.waitFor();
    }

    private void readTheOutputLog() throws FileNotFoundException {
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
    }

}