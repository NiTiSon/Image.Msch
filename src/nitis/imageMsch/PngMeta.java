package nitis.imageMsch;

import arc.files.Fi;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

/** Reads and writes a text (tEXt) chunk inside a PNG file.
  * This is the main way to transfer schematic, QR codes used for messengers, that compact images. */
public class PngMeta{
    private static final String KEY = "imsch";
    private static final int typeText = 0x745874; // "tEXt"
    private static final int typeEnd = 0x49454E44; // "IEND"

    /** Writes the file with the value stored in a tEXt chunk. */
    public static void embed(Fi file, String value) throws IOException{
        embed(file, KEY, value);
    }

    /** Writes the file with a tEXt chunk (keyword + value) inserted before the IEND chunk. */
    public static void embed(Fi file, String keyword, String value) throws IOException{
        byte[] png = file.readBytes();
        ByteArrayOutputStream out = new ByteArrayOutputStream(png.length + value.length() + 256);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(png));

        byte[] signature = new byte[8];
        in.readFully(signature);
        out.write(signature);

        while(true){
            int length = in.readInt();
            byte[] type = new byte[4];
            in.readFully(type);
            byte[] data = new byte[length];
            in.readFully(data);
            int crc = in.readInt();

            if(readInt(type) == typeEnd){
                writeChunk(out, "tEXt", (keyword + "\0" + value).getBytes(StandardCharsets.UTF_8));
            }

            out.write(intBytes(length));
            out.write(type);
            out.write(data);
            out.write(intBytes(crc));

            if(readInt(type) == typeEnd) break;
        }

        file.writeBytes(out.toByteArray());
    }

    /** @return the stored value, or null if the PNG has no such chunk. */
    public static String read(Fi file){
        return read(file, KEY);
    }

    /** @return the stored value for the keyword, or null if the PNG has no such chunk. */
    public static String read(Fi file, String keyword){
        try{
            byte[] png = file.readBytes();
            if(png.length < 8) return null;

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(png));
            byte[] signature = new byte[8];
            in.readFully(signature);
            if(readInt(signature) != 0x89504E47) return null; // not a PNG

            while(in.available() > 0){
                int length = in.readInt();
                byte[] type = new byte[4];
                in.readFully(type);
                byte[] data = new byte[length];
                in.readFully(data);
                in.readInt(); // crc

                int t = readInt(type);
                if(t == typeEnd) break;
                if(t == typeText){
                    String text = new String(data, StandardCharsets.ISO_8859_1);
                    int split = text.indexOf('\0');
                    if(split > 0 && text.substring(0, split).equals(keyword)){
                        return text.substring(split + 1);
                    }
                }
            }
        }catch(Throwable ignore){
        }
        return null;
    }

    private static void writeChunk(OutputStream out, String type, byte[] data) throws IOException{
        byte[] t = type.getBytes(StandardCharsets.US_ASCII);
        CRC32 crc = new CRC32();
        crc.update(t);
        crc.update(data);
        out.write(intBytes(data.length));
        out.write(t);
        out.write(data);
        out.write(intBytes((int)crc.getValue()));
    }

    private static byte[] intBytes(int i){
        return new byte[]{(byte)(i >>> 24), (byte)(i >>> 16), (byte)(i >>> 8), (byte)i};
    }

    private static int readInt(byte[] b){
        return (b[0] & 0xff) << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | (b[3] & 0xff);
    }
}
