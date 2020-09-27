package org.yangxin.nio;

import java.io.*;

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

        FileCopyRunner nioBufferCopy;
        FileCopyRunner nioTransferCopy;
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
