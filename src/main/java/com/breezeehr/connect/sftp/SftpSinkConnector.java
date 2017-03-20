package com.breezeehr.connect.sftp;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.*;

/**
 * Created by favila on 3/20/17.
 */
public class SftpSinkConnector extends SinkConnector {
    public static final String USERNAME = "sftp.username";
    public static final String HOSTNAME = "sftp.hostname";
    public static final String PORT = "sftp.port";
    public static final String PASSWORD = "sftp.password";
    public static final String PRIVATE_KEY = "sftp.private-key";
    private static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(USERNAME, Type.STRING, Importance.HIGH, "Username used to connect to the sftp.hostname")
            .define(HOSTNAME, Type.STRING, Importance.HIGH, "The sftp host to connect to")
            .define(PORT, Type.INT, 22, Importance.LOW, "The port number to connect to")
            .define(PASSWORD, Type.PASSWORD, Importance.HIGH, "Password used to authenticate with sftp.hostname")
            .define(PRIVATE_KEY, Type.STRING, NO_DEFAULT_VALUE, Importance.HIGH, "Base64-encoded private key used to authenticate with sftp.hostname",
                    null, -1, Width.LONG, "Base64-encoded private key used to authenticate with sftp.hostname");
    private Map<String, String> config;

    public String version() {
        return getClass().getPackage().getImplementationVersion();
    }

    public void start(Map<String, String> map) {
        config = map;
    }

    public Class<? extends Task> taskClass() {
        return SftpSinkTask.class;
    }

    public List<Map<String, String>> taskConfigs(int maxTasks) {
        final ArrayList<Map<String, String>> cfgs = new ArrayList<>(1);
        cfgs.add(config);
        return cfgs;
    }

    public void stop() {

    }

    public ConfigDef config() {
        return CONFIG_DEF;
    }
}
