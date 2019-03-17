package org.fogbowcloud.blowout.helpers;

public class Constants {

    public static final String FAKE_UUID = "1234";
    public static final String FAKE_CLOUD_NAME = "fake-cloud-name";
    public static final String FAKE_IMAGE_FLAVOR_NAME = "fake-image-flavor-name";
    public static final String FAKE_IMAGE_FLAVOR_ID = "fake-image-flavor-id";
    public static final String FAKE_FOGBOW_USER_NAME = "fake-fogbow-user-name";
    public static final String FAKE_PUBLIC_KEY = "fake-public-key";
    public static final String FAKE_PRIVATE_KEY_FILE_PATH = "fake-private-key-file-path";
    public static final String FAKE_RESOURCE_ID = "fake-resource-id";
    public static final String FAKE_TASK_ID = "fake-task-id";
    public static final String FAKE_ORDER_ID = "fake-order-id";

    public static final String POSTFIX_B = "-b";
    public static final String POSTFIX_C = "-c";
    public static final String POSTFIX_D = "-d";

    public static final String USER_DATA_FILE = "scripts/lvl-user-data.sh";
    public static final String USER_DATA_TYPE = "text/x-shellscript";

    public static final String EXAMPLE_FOGBOW_REQUIREMENT = "Glue2vCPU >= 1 && Glue2RAM >= 1024 ";
    public static final String EXAMPLE_CORE_SIZE = "1";
    public static final String EXAMPLE_MEM_SIZE = "1024";
    public static final String EXAMPLE_DISK_SIZE = "20";
    public static final String EXAMPLE_LOCATION = "edu.ufcg.lsd.cloud_1s";

    public static final String FILE_PATH_USER_DATA_MOCK = "src/test/java/org/fogbowcloud/blowout/infrastructure/provider/fogbow/userDataMock";
    public static final String FILE_PATH_RESPONSE_NO_INSTANCE_ID = "src/test/resources/requestInfoWithoutInstanceId";
    public static final String FILE_PATH_RESPONSE_INSTANCE_ID = "src/test/resources/requestInfoWithInstanceId";
}
