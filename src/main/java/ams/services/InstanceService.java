package ams.services;


import ams.domain.GenericException;
import com.aliyuncs.exceptions.ClientException;

import java.io.IOException;

public interface InstanceService {

  void serviceInit() throws IOException,GenericException,ClientException,InterruptedException;

  String createPrivateKey() throws ClientException, IOException;

  String getInstanceId(String scalingGroupId, String activeScalingConfigurationId) throws ClientException;

}
