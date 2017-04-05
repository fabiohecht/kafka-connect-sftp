package com.breezeehr.connect.sftp;

import com.jcraft.jsch.*;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

public class SftpSinkTask extends SinkTask {
    private Session session;
    private ChannelSftp channel;
    private String file_prefix;

    public String version() {
        return getClass().getPackage().getImplementationVersion();
    }

    public void start(Map<String, String> props) {
        final JSch ssh = new JSch();
        final String private_key = props.get(SftpSinkConnector.PRIVATE_KEY);
        if (private_key != null) {
            try {
                ssh.addIdentity("sftp-connector-identity",
                        private_key.getBytes(StandardCharsets.UTF_8),
                        null, null);
            } catch (JSchException e) {
                throw new ConnectException("Could not add sftp.private-key", e);
            }
        }
        try {
            session = ssh.getSession(props.get(SftpSinkConnector.USERNAME),
                    props.get(SftpSinkConnector.HOSTNAME),
                    Integer.parseInt(props.get(SftpSinkConnector.PORT)));
        } catch (JSchException e) {
            throw new ConnectException("Could not create ssh session", e);
        }
        if (props.get(SftpSinkConnector.PASSWORD) != null)
            session.setPassword(props.get(SftpSinkConnector.PASSWORD));
        try {
            session.connect(5000);
        } catch (JSchException e) {
            session = null;
            throw new ConnectException("Could not establish ssh connection", e);
        }
        try {
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(5000);
        } catch (JSchException e) {
            channel = null;
            session.disconnect();
            session = null;
            throw new ConnectException("Could not open sftp channel", e);
        }
        file_prefix = props.get(SftpSinkConnector.FILE_PREFIX);
    }

    public void put(Collection<SinkRecord> records) {
        for (SinkRecord sr : records) {
            final Object key = sr.key();
            final Object value = sr.value();
            final String filename;
            if (key instanceof String) {
                filename = file_prefix + sr.key();
            } else {
                throw new ConnectException("SFTP connector requires key (the desired filename) be a string");
            }
            final InputStream file_bytes;
            if (value instanceof byte[]) {
                file_bytes = new ByteArrayInputStream((byte[]) value);
            } else if (value instanceof CharSequence) {
                file_bytes = new ByteArrayInputStream(value.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                throw new ConnectException("SFTP connector requires value (the desired file contents) be a string or bytes");
            }
            try {
                channel.put(file_bytes, filename);
            } catch (SftpException e) {
                throw new RetriableException("Could not upload file " + filename, e);
            }
        }
    }

    public void stop() {
        file_prefix = null;
        try {
            if (channel != null)
                channel.disconnect();
        } finally {
            channel = null;
            try {
                if (session != null)
                    session.disconnect();
            } finally {
                session = null;
            }
        }
    }
}
