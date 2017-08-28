package ams.services.impl;

import ams.services.SessionService;
import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DefaultSessionService implements SessionService {

  public Session openSession(String username, String host, int port, String keyPath) throws JSchException {
    JSch jsch = new JSch();
    Session session;
    jsch.addIdentity(keyPath);
    session = jsch.getSession(username, host, port);
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect(3000);
    return session;
  }



  public void execCommand(Session session, String command) throws IOException, JSchException {
    Channel channel = session.openChannel("exec");
    ChannelExec channelExec = (ChannelExec) channel;
    channelExec.setCommand(command);
    channelExec.setInputStream(null);
    BufferedReader input = new BufferedReader(new InputStreamReader
      (channelExec.getInputStream()));
    channelExec.connect();
    String line;
    while ((line = input.readLine()) != null) {
      System.out.println(line);
    }
    input.close();
    if (channelExec.isClosed()) {
      System.out.println(channelExec.getExitStatus());
    }
    channelExec.disconnect();
  }


}
