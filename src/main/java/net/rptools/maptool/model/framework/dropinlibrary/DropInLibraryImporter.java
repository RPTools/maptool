package net.rptools.maptool.model.framework.dropinlibrary;

import com.google.protobuf.Message.Builder;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.filechooser.FileFilter;
import net.rptools.maptool.language.I18N;
import com.google.protobuf.util.JsonFormat;
import net.rptools.maptool.model.framework.proto.DropInLibraryDto;


public class DropInLibraryImporter {

  public static final String DROP_IN_LIBRARY_EXTENSION = ".mtlib";


  public static FileFilter getDropInLibraryFileFilter() {
    return new FileFilter() {

      @Override
      public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(DROP_IN_LIBRARY_EXTENSION);
      }

      @Override
      public String getDescription() {
        return I18N.getText("file.ext.dropInLib");
      }
    };
  }




  public DropInLibrary importFromFile(File file) throws IOException {
    var builder = DropInLibraryDto.newBuilder();
    JsonFormat.parser().merge(new FileReader(file), builder);
    return DropInLibrary.fromDto(builder.build());
  }
}
