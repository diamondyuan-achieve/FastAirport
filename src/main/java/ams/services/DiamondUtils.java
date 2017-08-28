package ams.services;

import ams.domain.GenericException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DiamondUtils {


  public static void saveFile(String content,String filePath) throws GenericException{
    File myFile = new File(filePath);
    if (!myFile.exists()) {
      try {
        if (myFile.createNewFile()) {
          FileWriter writer;
          writer = new FileWriter(filePath);
          writer.write(content);
          writer.flush();
          writer.close();
        }
      } catch (IOException e) {
        throw new GenericException("20001", "创建文件失败");
      }
    }
  }





}
