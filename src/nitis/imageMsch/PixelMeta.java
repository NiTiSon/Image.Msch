package nitis.imageMsch;

import arc.files.Fi;
import arc.graphics.Pixmap;

/** Stores the raw schematic bytes in the pixels of the top-left corner of the image,
  * for messengers (Discord) that strip PNG metadata chunks like tEXt.
  * Format: row-major from (0,0), 3 bytes per pixel (R, G, B), alpha preserved.
  * Header is 8 bytes: 4-byte magic "imsc" + 4-byte big-endian data length. */
public class PixelMeta{
    private static final byte[] magic = {'i', 'm', 's', 'c'};
    private static final int headerLength = 8;

    /** Encodes data into the top-left corner of the pixmap. No-op if it doesn't fit. */
    public static void embed(Pixmap pm, byte[] data){
        int pixels = pm.getWidth() * pm.getHeight();

        // This very unlikely
        // Probably long processor instructions will take a large space, but still...
        if(headerLength + data.length > pixels * 3) return;

        for(int i = 0; i < 4; i++) setByte(pm, i, magic[i]);
        setByte(pm, 4, (byte)(data.length >>> 24));
        setByte(pm, 5, (byte)(data.length >>> 16));
        setByte(pm, 6, (byte)(data.length >>> 8));
        setByte(pm, 7, (byte)data.length);
        for(int i = 0; i < data.length; i++) setByte(pm, headerLength + i, data[i]);
    }

    /** @return the stored data, or null if the corner has no valid header. */
    public static byte[] read(Pixmap pm){
        int pixels = pm.getWidth() * pm.getHeight();
        if(pixels * 3 < headerLength) return null;
        for(int i = 0; i < 4; i++){
            if(getByte(pm, i) != magic[i]) return null;
        }
        int len = (getByte(pm, 4) & 0xff) << 24 | (getByte(pm, 5) & 0xff) << 16
                | (getByte(pm, 6) & 0xff) << 8 | getByte(pm, 7) & 0xff;
        if(len < 0 || headerLength + len > pixels * 3) return null;

        byte[] data = new byte[len];
        for(int i = 0; i < len; i++) data[i] = getByte(pm, headerLength + i);
        return data;
    }

    /** @return the stored data, or null if the PNG's top-left corner has no valid header. */
    public static byte[] read(Fi file){
        try{
            Pixmap pm = new Pixmap(file);
            try{
                return read(pm);
            }finally{
                pm.dispose();
            }
        }catch(Throwable ignore){
            return null;
        }
    }

    private static void setByte(Pixmap pm, int byteIndex, byte value){
        int width = pm.getWidth();
        int x = byteIndex / 3 % width;
        int y = byteIndex / 3 / width;
        int channel = byteIndex % 3;
        int shift = channel == 0 ? 24 : channel == 1 ? 16 : 8;
        //clear the channel, keep the others, force alpha opaque so re-encoders don't drop the data
        pm.set(x, y, (pm.get(x, y) & ~(0xFF << shift) & 0xFFFFFF00) | 0xFF | (value & 0xff) << shift);
    }

    private static byte getByte(Pixmap pm, int byteIndex){
        int width = pm.getWidth();
        int x = byteIndex / 3 % width;
        int y = byteIndex / 3 / width;
        int channel = byteIndex % 3;
        int shift = channel == 0 ? 24 : channel == 1 ? 16 : 8;
        return (byte)(pm.get(x, y) >>> shift);
    }
}
