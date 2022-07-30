package org.devopology.common.file;

import java.io.File;

public interface FileMonitorListener {

    void onChange(File file) throws Exception;
}
