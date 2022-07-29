/*
 * Copyright 2022 Douglas Hoard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devopology.common.file;

import org.devopology.common.precondition.Precondition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileMonitor {

    private File file;
    private List<FileMonitorListener> fileMonitorListenerList;
    private Thread thread;

    public FileMonitor(File file) {
        this.file = file;
        this.fileMonitorListenerList = new ArrayList<>();
    }

    public FileMonitor addListener(FileMonitorListener fileMonitorListener) {
        Precondition.notNull(fileMonitorListener, "fileMonitorListener is null");

        this.fileMonitorListenerList.add(fileMonitorListener);

        return this;
    }

    public synchronized void start() {
        if (this.thread == null) {
            this.thread = new Thread(() -> monitor());
            this.thread.setDaemon(true);
            this.thread.setName("file-monitor");
            this.thread.start();
        }
    }

    private void monitor() {
        long previousLastModified = this.file.lastModified();

        while (true) {
            try {
                Thread.currentThread().sleep(10000);
            } catch (InterruptedException e) {
                // DO NOTHING
            }

            if (this.file.exists() && this.file.isFile() && this.file.canRead()) {

                long lastModified = this.file.lastModified();

                if (lastModified != previousLastModified) {
                    for (FileMonitorListener fileMonitorListener : this.fileMonitorListenerList) {
                        try {
                            fileMonitorListener.onChange(this.file);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                previousLastModified = lastModified;
            }
        }
    }
}
