package diamondyuan.services;

import com.jcraft.jsch.*;

import java.io.*;

/**
 * @author DiamondYuan
 */
public class DiamondUtils {


  public static void saveFile(String content, String filePath) throws IOException {
    File myFile = new File(filePath);
    if (!myFile.exists()) {
      if (myFile.createNewFile()) {
        FileWriter writer;
        writer = new FileWriter(filePath);
        writer.write(content);
        writer.flush();
        writer.close();
      }
    }
  }


  public static Session openSession(String username, String host, int port, String keyPath) throws JSchException {
    JSch jsch = new JSch();
    Session session;
    jsch.addIdentity(keyPath);
    session = jsch.getSession(username, host, port);
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect(3000);
    return session;
  }


  public static int execCommand(Session session, String command) throws JSchException, IOException {
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
    while (!channelExec.isClosed()) {

    }

    if (channelExec.isClosed()) {
      channelExec.disconnect();
      return channelExec.getExitStatus();
    }
    return -1;
  }


}
