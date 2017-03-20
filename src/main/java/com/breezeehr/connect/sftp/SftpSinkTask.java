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

/**
 * Created by favila on 3/20/17.
 */
public class SftpSinkTask extends SinkTask {
    private Session session;
    private ChannelSftp channel;

    public String version() {
        return getClass().getPackage().getImplementationVersion();
    }

    public void start(Map<String, String> props) {
        final JSch ssh = new JSch();
        try {
            session = ssh.getSession(props.get(SftpSinkConnector.USERNAME),
                    props.get(SftpSinkConnector.HOSTNAME),
                    Integer.parseInt(props.get(SftpSinkConnector.PORT)));
        } catch (JSchException e) {
            throw new ConnectException("Could not create ssh session", e);
        }
        try {
            session.connect(5000);
        } catch (JSchException e) {
            throw new ConnectException("Could not establish ssh connection", e);
        }
        try {
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(5000);
        } catch (JSchException e) {
            session.disconnect();
            throw new ConnectException("Could not open sftp channel", e);
        }
    }

    public void put(Collection<SinkRecord> records) {
        for (SinkRecord sr : records) {
            final InputStream src = new ByteArrayInputStream(((String) sr.value())
                    .getBytes(StandardCharsets.UTF_8));
            try {
                channel.put(src, (String) sr.key());
            } catch (SftpException e) {
                throw new RetriableException("Could not upload file " + (String) sr.key(), e);
            }
        }
    }

    public void stop() {
        channel.disconnect();
        session.disconnect();
        channel = null;
        session = null;
    }
}
