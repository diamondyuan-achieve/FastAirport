package ams.services;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;

public interface SessionService {

  Session openSession(String username, String host, int port, String keyPath) throws JSchException;

  void execCommand(Session session, String command) throws IOException, JSchException;

}
