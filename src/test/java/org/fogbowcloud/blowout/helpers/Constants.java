package org.fogbowcloud.blowout.helpers;

import org.fogbowcloud.blowout.core.model.Command;

public class Constants {

    public static final String FAKE_UUID = "1234";
    public static final String FAKE_CLOUD_NAME = "fake-cloud-name";
    public static final String FAKE_COMPUTE_IMAGE_FLAVOR_NAME = "fake-compute-flavor-name";
    public static final String FAKE_COMPUTE_IMAGE_FLAVOR_ID = "fake-image-flavor-id";
    public static final String FAKE_FOGBOW_USER_NAME = "fake-fogbow-user-name";
    public static final String FAKE_PUBLIC_KEY = "fake-public-key";
    public static final String FAKE_PRIVATE_KEY_FILE_PATH = "fake-private-key-file-path";
    public static final String FAKE_RESOURCE_ID = "fake-resource-id";
    public static final String FAKE_TASK_ID = "fake-task-id";
    public static final String FAKE_ORDER_ID = "fake-order-id";
    public static final String FAKE_METADATA_VALUE = "fake-metadata-value";
    public static final String FAKE_METADATA = "fake-metadata";
    public static final String FAKE_COMMAND = "echo fake-echo";
    public static final String FAKE_COMPUTE_ORDER_ID = "fake-compute-order-id";
    public static final String FAKE_PUBLIC_IP_ORDER_ID = "fake-public-ip-order-id";
    public static final String FAKE_RAS_BASE_URL = "fake-ras-base-url";
    public static final String FAKE_RAS_MEMBER_ID = "fake-ras-member-id";

    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String JSON_KEY_PRIVATE_KEY_PATH = "privateKeyPath";
    public static final String JSON_KEY_CLOUD_NAME = "cloudName";
    public static final String JSON_KEY_PUBLIC_KEY = "publicKey";
    public static final String JSON_KEY_USERNAME = "username";
    public static final String JSON_KEY_IMAGE_ID = "image";

    public static final String STR_OTHER_VALUE = "otherValue";
    public static final String STR_SECOND_REQUIREMENT = "secondRequirement";
    public static final String STR_THIS_VALUE = "thisValue";
    public static final String STR_FIRST_REQUIREMENT = "firstRequirement";
    public static final Integer WANTED_NUMBER_OF_INVOCATIONS = 1;

    public static final Command.Type COMMAND_TYPE_DEFAULT = Command.Type.REMOTE;

    public static final String TIME_OUT_VALUE_EMPTY = "";
    public static final String TIME_OUT_VALUE_GIBBERISH = "fbajsmnfsakl";
    public static final String TIME_OUT_VALUE_SMALL = "1";
    public static final String TIME_OUT_VALUE_BIG = "50000000000";

    public static final String POSTFIX_B = "-b";
    public static final String POSTFIX_C = "-c";
    public static final String POSTFIX_D = "-d";

    public static final String USER_DATA_FILE = "scripts/lvl-user-data.sh";
    public static final String USER_DATA_TYPE = "text/x-shellscript";

    public static final String FOGBOW_REQUIREMENT_A = "Glue2vCPU >= 1 && Glue2RAM >= 1024 ";
    public final static String FOGBOW_REQUIREMENT_B = "Glue2vCPU >= 1 &&  Glue2RAM >= 1024 && Glue2disk <= 20 ";
    public final static String FOGBOW_REQUIREMENT_C = "Glue2vCPU <= 1 && Glue2RAM >= 1024 && Glue2disk >= 20 && " +
            "Glue2CloudComputeManagerID ==\"servers.your.domain\"";
    public final static String FOGBOW_REQUIREMENT_D = "Glue2vCPU >= 1  && Glue2RAM == 1024 || Glue2RAM == 2048 " +
            "&& Glue2disk == 20 && Glue2CloudComputeManagerID ==\"servers.your.domain\"";
    public final static String FOGBOW_REQUIREMENT_E = "";
    public final static String FOGBOW_REQUIREMENT_F = null;


    public static final String EXAMPLE_CORE_SIZE = "1";
    public static final String EXAMPLE_MEM_SIZE = "1024";
    public static final String EXAMPLE_DISK_SIZE = "20";
    public static final String EXAMPLE_LOCATION = "edu.ufcg.lsd.cloud_1s";

    public static final String FILE_PATH_USER_DATA_MOCK = "src/test/java/org/fogbowcloud/blowout/infrastructure/provider/fogbow/userDataMock";
    public static final String FILE_PATH_RESPONSE_NO_INSTANCE_ID = "src/test/resources/requestInfoWithoutInstanceId";
    public static final String FILE_PATH_RESPONSE_INSTANCE_ID = "src/test/resources/requestInfoWithInstanceId";

    public static final String JSON_BODY_COMMAND = "{" +
            "\"command\": \"echo fake-echo\", " +
            "\"state\": \"QUEUED\", " +
            "\"type\": \"REMOTE\"" +
            "}";
    public static final String JSON_BODY_COMMAND_RUNNING = "{" +
            "\"command\": \"echo fake-echo\", " +
            "\"state\": \"RUNNING\", " +
            "\"type\": \"REMOTE\"" +
            "}";

    public static final String JSON_BODY_RAS_PUBLIC_IP_INSTANCE_RESPONSE = "{\r\n  \"cloudName\": \"string\",\r\n " +
            " \"computeId\": \"string\",\r\n  \"computeName\": \"string\",\r\n  \"id\": \"string\",\r\n  \"ip\": " +
            "\"string\",\r\n  \"provider\": \"string\",\r\n  \"state\": \"DISPATCHED\"\r\n}";


    public static final String TEST_CONFIG_FILE_PATH = "src/test/resources/blowout-test.conf";
}
