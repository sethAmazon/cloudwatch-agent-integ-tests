All integration tests must be written in the "integration" directory

All tests must specify spring config by adding @SpringBootTest(classes={com.cloud.watch.agent.opensource.test.OpensourceIntegTestApplication.class}) to the class

To run a test use ./gradlew test -Dtest.profile=integration --tests {put your test class name here}

Test must have sudo privileges because they change files that are only editable by the super user

What is required to run

<ul>
    <li>
        Isengard 506463145083 cloudwatch-orangezest+opensourcecwagentintegtest@amazon.com
    </li>
    <li>
        Ubuntu
    </li>
    <li>
        Java 11
    </li>
    <li>
        Forked local stack https://github.com/sethAmazon/localstack
    </li>
    <li>
        Docker that does not need sudo
    </li>
    <li>
        Openssl that does not need sudo
    </li>
    <li>
        git
    </li>
    <li>
        This repo https://github.com/sethAmazon/cloudwatch-agent-integ-tests
    </li>
    <li>
        Agent repo https://github.com/aws/amazon-cloudwatch-agent
    </li>
    <li>
        CloudWatchAgentServerRole attached to ec2
    </li>
    <li>
        If you do not want to set up ec2 instance use ami ami-0e76c450140211023
    </li>
    <li>
        Github secrets (AWS access key and password TERRAFORM_AWS_ACCESS_KEY_ID secrets.TERRAFORM_AWS_SECRET_ACCESS_KEY, ssh key AWS_PRIVATE_KEY, and user login USER_NAME)
    </li>
</ul>