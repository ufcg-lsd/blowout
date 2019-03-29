package org.fogbowcloud.blowout.helpers;

import org.fogbowcloud.blowout.core.model.Command;

public class Constants {
    public class FakeData {
        public static final String CLOUD_NAME = "fake-cloud-name";
        public static final String COMPUTE_IMAGE_FLAVOR_NAME = "fake-compute-flavor-name";
        public static final String COMPUTE_IMAGE_ID = "fake-compute-image-id";
        public static final String FOGBOW_USER_NAME = "fake-fogbow-user-name";
        public static final String PUBLIC_KEY = "fake-public-key";
        public static final String PASSWORD = "fake-fogbow-password";
        public static final String PRIVATE_KEY_FILE_PATH = "fake-private-key-file-path";
        public static final String RESOURCE_ID = "fake-resource-id";
        public static final String TASK_ID = "fake-task-id";
        public static final String ORDER_ID = "fake-order-id";
        public static final String METADATA_VALUE = "fake-metadata-value";
        public static final String METADATA = "fake-metadata";

        public static final String COMPUTE_ORDER_ID = "fake-compute-order-id";
        public static final String PUBLIC_IP_ORDER_ID = "fake-public-ip-order-id";
        public static final String RAS_BASE_URL_PROP = "fake-ras-base-url";

        public static final String FOGBOW_USER_TOKEN = "fake-fogbow-user-token";
        public static final String RAS_PUBLIC_KEY = "fake-ras-public-key";
        public static final String RAS_BASE_URL = "fake-ras.fogbow.cloud";
        public static final String AS_BASE_URL = "fake-as.fogbow.cloud";

        public static final String RAS_MEMBER_ID = "fake-ras-member-id";
        public static final String USER_DATA_FILE = "scripts/lvl-user-data.sh";
        public static final String USER_DATA_TYPE = "text/x-shellscript";
        public static final String CORE_SIZE = "1";
        public static final String MEM_SIZE = "1024";
        public static final String DISK_SIZE = "20";
        public static final String LOCATION = "edu.ufcg.lsd.cloud_1s";
        public static final String COMMAND = "echo fake-echo";
        public static final String UUID = "1234";
    }

    public class JSON {
        public static final String CONTENT_TYPE = "application/json";

        public class Header {
            public class Key {
                public static final String FOGBOW_USER_TOKEN = "Fogbow-User-Token";
            }
        }

        public class Key {
            public static final String PRIVATE_KEY_PATH = "privateKeyPath";
            public static final String CLOUD_NAME = "cloudName";
            public static final String PUBLIC_KEY = "publicKey";
            public static final String USERNAME = "username";
            public static final String IMAGE_ID = "image";
        }

        public class Body {
            public static final String PUBLIC_IP_ORDER_ID = "{" +
                    "\"id\": \"fake-public-ip-order-id\"" +
                    "}";

            public static final String COMPUTE_ORDER_ID = "{" +
                    "\"id\": \"fake-compute-order-id\"" +
                    "}";

            public static final String PUBLIC_IP_ORDER = "{" +
                    "\"cloudName\": \"fake-cloud-name\"," +
                    "\"computeId\": \"fake-compute-image-id\"," +
                    "\"provider\": \"fake-provider\"" +
                    "}";

            public static final String COMPUTE = "{" +
                    "\"cloudName\": \"fake-cloud-name\"," +
                    " \"disk\": 0, " +
                    "\"imageId\": \"fake-compute-image-id\"," +
                    "\"memory\": 0," +
                    "\"name\": \"fake-compute-name\"," +
                    "\"provider\": \"fake-provider\", " +
                    "\"publicKey\": \"fake-public-key\", " +
                    "\"vCPU\": 0" +
                    "}";

            public static final String COMMAND = "{" +
                    "\"command\": \"echo fake-echo\", " +
                    "\"state\": \"QUEUED\", " +
                    "\"type\": \"REMOTE\"" +
                    "}";

            public static final String COMMAND_RUNNING = "{" +
                    "\"command\": \"echo fake-echo\", " +
                    "\"state\": \"RUNNING\", " +
                    "\"type\": \"REMOTE\"" +
                    "}";

            public static final String PUBLIC_IP_INSTANCE_RESPONSE = "{" +
                    "\"cloudName\": \"string\"," +
                    "\"computeId\": \"string\"," +
                    "\"computeName\": \"string\"," +
                    "\"id\": \"string\"," +
                    "\"ip\": \"string\"," +
                    "\"provider\": \"string\"," +
                    "\"state\": \"DISPATCHED\"" +
                    "}";

            public static final String IMAGES_RESPONSE = "{" +
                    "\"fake-compute-image-id\": \"fake-compute-flavor-name\"," +
                    " \"31bada4e-0a19-41a1-a552-73d6bbd3ada9\": \"fedora-atomic-magnum\"," +
                    " \"f943ca3f-d9f3-407a-af26-e4c85f649af2\": \"debian\"}";

            public static final String AUTHENTICATE_RESPONSE = "{" +
                    "\"id\": \"fake-public-ip-order-id\"" +
                    "}";

            public static final String AUTHENTICATE = "{" +
                    "\"credentials\": {" +
                        "\"username\": \"fake-fogbow-user-name\"," +
                        "\"password\": \"fake-fogbow-password\"," +
                        "\"projectname\": \"fake-project-name\"," +
                        "\"domain\": \"fake-domain\"," +
                        "}," +
                    "\"publicKey\": \"fake-ras-public-key\""
                    ;
        }
    }

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

    public static final String FOGBOW_REQUIREMENT_A = "Glue2vCPU >= 1 && Glue2RAM >= 1024 ";
    public final static String FOGBOW_REQUIREMENT_B = "Glue2vCPU >= 1 &&  Glue2RAM >= 1024 && Glue2disk <= 20 ";
    public final static String FOGBOW_REQUIREMENT_C = "Glue2vCPU <= 1 && Glue2RAM >= 1024 && Glue2disk >= 20 && " +
            "Glue2CloudComputeManagerID ==\"servers.your.domain\"";
    public final static String FOGBOW_REQUIREMENT_D = "Glue2vCPU >= 1  && Glue2RAM == 1024 || Glue2RAM == 2048 " +
            "&& Glue2disk == 20 && Glue2CloudComputeManagerID ==\"servers.your.domain\"";
    public final static String FOGBOW_REQUIREMENT_E = "";
    public final static String FOGBOW_REQUIREMENT_F = null;

    public static final String FILE_PATH_TESTS_CONFIG = "src/test/resources/blowout-test.conf";
    public static final String FILE_PATH_USER_DATA_MOCK = "src/test/java/org/fogbowcloud/blowout/infrastructure/provider/fogbow/userDataMock";
    public static final String FILE_PATH_RESPONSE_NO_INSTANCE_ID = "src/test/resources/requestInfoWithoutInstanceId";
    public static final String FILE_PATH_RESPONSE_INSTANCE_ID = "src/test/resources/requestInfoWithInstanceId";
}
