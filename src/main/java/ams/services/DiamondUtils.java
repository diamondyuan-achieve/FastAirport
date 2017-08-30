package ams.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DiamondUtils {


  public static void saveFile(String content,String filePath) throws IOException{
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





}
