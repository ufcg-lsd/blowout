package org.fogbowcloud.blowout.infrastructure;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.fogbowcloud.blowout.helpers.Constants;
import org.fogbowcloud.blowout.helpers.HoverflyRules;
import org.fogbowcloud.blowout.infrastructure.model.Token;
import org.fogbowcloud.blowout.infrastructure.token.KeystoneTokenUpdatePlugin;
import org.fogbowcloud.blowout.infrastructure.token.AbstractTokenUpdatePlugin;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

public class KeystoneTokenUpdatePluginTest {

    private AbstractTokenUpdatePlugin tokenUpdatePlugin;

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(HoverflyRules.simulationSource);

    @Before
    public void setUp() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(Constants.FILE_PATH_TESTS_CONFIG));

        this.tokenUpdatePlugin = new KeystoneTokenUpdatePlugin(properties);
    }

    @Test
    public void testGenerateTokenSuccess() {
        Token token = this.tokenUpdatePlugin.generateToken();
        assertNotNull(token);
    }

    @Test
    public void testGenerateTokenFail() {

    }
}
