package ams.domain;

import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Test {


  private Session openSession(String username, String host, int port) throws JSchException {
    JSch jsch = new JSch();
    Session session = null;
    jsch.addIdentity("path of prvkey");
    session = jsch.getSession(username, host, port);
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect(3000);
    return session;
  }

  private void execCommand(Session session, String command) throws IOException, JSchException {

    Channel channel = session.openChannel("exec");
    ChannelExec channelExec = (ChannelExec) channel;
    channelExec.setCommand(command);
    channelExec.setInputStream(null);
    BufferedReader input = new BufferedReader(new InputStreamReader
      (channelExec.getInputStream()));
    channelExec.connect();
    System.out.println(String.format("The remote command is : %s", command));
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


  public static void main(String[] args) throws IOException, JSchException {
    Test test = new Test();
    Session session = test.openSession("username", "host", 22);
    test.execCommand(session, "apt-get update");
    session.disconnect();
  }
}
