package com.breezeehr.connect.sftp;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.*;

public class SftpSinkConnector extends SinkConnector {
    static final String USERNAME = "sftp.username";
    static final String HOSTNAME = "sftp.hostname";
    static final String PORT = "sftp.port";
    static final String PASSWORD = "sftp.password";
    static final String PRIVATE_KEY = "sftp.private-key";
    static final String FILE_PREFIX = "sftp.file-prefix";
    private static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(USERNAME, Type.STRING, Importance.HIGH, "Username used to connect to the sftp.hostname")
            .define(HOSTNAME, Type.STRING, Importance.HIGH, "The sftp host to connect to")
            .define(PORT, Type.INT, 22, Importance.LOW, "The port number to connect to")
            .define(PASSWORD, Type.PASSWORD, null, Importance.HIGH, "Password used to authenticate with sftp.hostname")
            .define(PRIVATE_KEY, Type.STRING, null, Importance.HIGH, "Base64-encoded private key used to authenticate with sftp.hostname",
                    null, -1, Width.LONG, "Base64-encoded private key used to authenticate with sftp.hostname")
            .define(FILE_PREFIX, Type.STRING, "", Importance.MEDIUM, "String prepended to filenames before PUTing to remote sftp server. Allows you to, e.g., put all files in a particular directory.");
    private Map<String, String> config;

    public String version() {
        return getClass().getPackage().getImplementationVersion();
    }

    public void start(Map<String, String> map) {
        final String[] setting_keys = {USERNAME, HOSTNAME, PORT, PASSWORD, PRIVATE_KEY, FILE_PREFIX};
        config = new HashMap<>(setting_keys.length);
        for (String k : setting_keys) {
            config.put(k, map.get(k));
        }
    }

    public Class<? extends Task> taskClass() {
        return SftpSinkTask.class;
    }

    public List<Map<String, String>> taskConfigs(int maxTasks) {
        final ArrayList<Map<String, String>> cfgs = new ArrayList<>(maxTasks);
        for (int i = 0; i < maxTasks; i++) {
            cfgs.add(new HashMap<>(config));
        }
        return cfgs;
    }

    public void stop() {
        config = null;
    }

    public ConfigDef config() {
        return CONFIG_DEF;
    }
}
