/*
 * Copyright 2016 liufanping@iveely.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iveely.computing.api;

import com.iveely.computing.io.IReader;
import com.iveely.computing.task.ReaderTask;

import java.util.HashMap;
import java.util.List;

/**
 * IInput with reader.
 *
 * IInputReader is an input with the function of file read, any IReader
 * implementation can be used in IInputReader, including Windows file system,
 * Unix file system, Hadoop file system, etc.
 *
 * more @see IInput.
 */
public abstract class IInputReader extends IInput {

  /**
   * The reader for current input.
   */
  private IReader reader;

  /**
   * The set of all read the file of the current Input.
   */
  private List<String> files;

  /**
   * Build input reader instance.
   */
  public IInputReader() {
    super();
  }

  /**
   * @param conf The user's custom configuration information.
   * @see IInput#start(java.util.HashMap)
   */
  @Override
  public void start(HashMap<String, Object> conf) {
    ReaderTask readerTask = (ReaderTask) conf.get(this.getClass().getName());
    this.reader = readerTask.getReader();
    this.files = readerTask.getFiles();
  }

  /**
   * @param channel Stream channel.
   * @see IInput#nextTuple(com.iveely.computing.api.StreamChannel)
   */
  @Override
  public void nextTuple(StreamChannel channel) {
    if (this.reader != null && this.reader.hasNext()) {
      nextTuple(channel, this.reader.onRead());
    } else {
      if (this.files != null && this.files.size() > 0) {
        this.reader.onClose();
        if (this.reader.onOpen(this.files.get(0))) {
          this.files.remove(0);
          return;
        }
      }
      if (this.reader != null) {
        this.reader.onClose();
      }
      nextTuple(channel, null);
    }
  }

  /**
   * Call next tuple with data.
   *
   * Usually, nextTuple data generated by the users themselves, but if use
   * IInputReader, iveely.computing need to read the file data in rows returned
   * to the user.
   *
   * @param channel Stream channel.
   * @param line    The data by line.
   */
  public abstract void nextTuple(StreamChannel channel, String line);
}
