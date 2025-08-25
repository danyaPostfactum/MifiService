package com.mifiservice.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.fileupload.FileUploadBase;

/* loaded from: classes.dex */

// TODO try-catch mess
/*
public class CompressUtil {
    public static byte[] gzip(byte[] b) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            GZIPOutputStream gzip2 = new GZIPOutputStream(out);
            try {
                gzip2.write(b);
                if (gzip2 != null) {
                    try {
                        gzip2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return out.toByteArray();
            } catch (Throwable th) {
                th = th;
                gzip = gzip2;
                if (gzip != null) {
                    try {
                        gzip.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    public static byte[] ungzip(byte[] b) throws IOException {
        ByteArrayInputStream in;
        GZIPInputStream ginzip;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in2 = null;
        GZIPInputStream ginzip2 = null;
        try {
            in = new ByteArrayInputStream(b);
            try {
                ginzip = new GZIPInputStream(in);
            } catch (Throwable th) {
                th = th;
                in2 = in;
            }
        } catch (Throwable th2) {
            th = th2;
        }
        try {
            byte[] buffer = new byte[FileUploadBase.MAX_HEADER_SIZE];
            while (true) {
                int offset = ginzip.read(buffer);
                if (offset == -1) {
                    break;
                }
                out.write(buffer, 0, offset);
            }
            if (ginzip != null) {
                try {
                    ginzip.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e2) {
                }
            }
            return out.toByteArray();
        } catch (Throwable th3) {
            th = th3;
            ginzip2 = ginzip;
            in2 = in;
            if (ginzip2 != null) {
                try {
                    ginzip2.close();
                } catch (IOException e3) {
                }
            }
            if (in2 == null) {
                throw th;
            }
            try {
                in2.close();
                throw th;
            } catch (IOException e4) {
                throw th;
            }
        }
    }

    public static byte[] zip(byte[] b) throws IOException {
        ZipOutputStream zout;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zout2 = null;
        try {
            zout = new ZipOutputStream(out);
        } catch (Throwable th) {
            th = th;
        }
        try {
            zout.putNextEntry(new ZipEntry("0"));
            zout.write(b);
            zout.closeEntry();
            if (zout != null) {
                try {
                    zout.close();
                } catch (IOException e) {
                }
            }
            return out.toByteArray();
        } catch (Throwable th2) {
            th = th2;
            zout2 = zout;
            if (zout2 != null) {
                try {
                    zout2.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
    }

    public static byte[] unzip(byte[] b) throws IOException {
        ByteArrayInputStream in;
        ZipInputStream zin;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in2 = null;
        ZipInputStream zin2 = null;
        try {
            in = new ByteArrayInputStream(b);
            try {
                zin = new ZipInputStream(in);
            } catch (Throwable th) {
                th = th;
                in2 = in;
            }
        } catch (Throwable th2) {
            th = th2;
        }
        try {
            zin.getNextEntry();
            byte[] buffer = new byte[FileUploadBase.MAX_HEADER_SIZE];
            while (true) {
                int offset = zin.read(buffer);
                if (offset == -1) {
                    break;
                }
                out.write(buffer, 0, offset);
            }
            if (zin != null) {
                try {
                    zin.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e2) {
                }
            }
            return out.toByteArray();
        } catch (Throwable th3) {
            th = th3;
            zin2 = zin;
            in2 = in;
            if (zin2 != null) {
                try {
                    zin2.close();
                } catch (IOException e3) {
                }
            }
            if (in2 == null) {
                throw th;
            }
            try {
                in2.close();
                throw th;
            } catch (IOException e4) {
                throw th;
            }
        }
    }
}*/