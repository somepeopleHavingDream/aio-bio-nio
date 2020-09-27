package org.yangxin.nio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author yangxin
 * 2020/09/27 15:02
 */
public class FileCopyDemo {

    public static void main(String[] args) {
        FileCopyRunner noBufferStreamCopy = (File source, File target)  -> {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = new FileInputStream(source);
                outputStream = new FileOutputStream(target);

                int result;
                while ((result = inputStream.read()) != -1) {
                    outputStream.write(result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(outputStream);
                close(inputStream);
            }
        };

        FileCopyRunner bufferedStreamCopy = (File source, File target) -> {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = new BufferedInputStream(new FileInputStream(source));
                outputStream = new BufferedOutputStream(new FileOutputStream(target));

                byte[] buffer = new byte[1024];

                int result;
                while ((result = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(outputStream);
                close(inputStream);
            }
        };

        FileCopyRunner nioBufferCopy = (File source, File target) -> {
            FileChannel inputChannel = null;
            FileChannel outputChannel = null;

            try {
                inputChannel = new FileInputStream(source).getChannel();
                outputChannel = new FileOutputStream(target).getChannel();

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while (inputChannel.read(buffer) != -1) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        outputChannel.write(buffer);
                    }
                    buffer.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(outputChannel);
                close(inputChannel);
            }
        };

        FileCopyRunner nioTransferCopy = (File source, File target) -> {
            FileChannel inputChannel = null;
            FileChannel outputChannel = null;
            try {
                inputChannel = new FileInputStream(source).getChannel();
                outputChannel = new FileOutputStream(target).getChannel();

                long transferred = 0L;
                long size = inputChannel.size();
                while (transferred != size) {
                    transferred += inputChannel.transferTo(transferred, size - transferred, outputChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(outputChannel);
                close(inputChannel);
            }
        };
    }

    private static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * @author yangxin
 * 2020/09/27 15:03
 */
interface FileCopyRunner {

    void copyFile(File source, File target);
}
