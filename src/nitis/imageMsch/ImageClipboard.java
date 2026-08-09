package nitis.imageMsch;

import arc.graphics.Pixmap;
import arc.util.Log;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLClipboard;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDL_ClipboardCleanupCallback;
import org.lwjgl.sdl.SDL_ClipboardDataCallback;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.system.MemoryUtil.*;

public class ImageClipboard{
    static SDL_ClipboardDataCallback dataCb;
    static SDL_ClipboardCleanupCallback cleanupCb;

    /** Copies a pixmap to the OS clipboard as BMP (maps to CF_DIB on Windows) via SDL3. Returns false on failure. */
    public static boolean copy(Pixmap pm){
        ByteBuffer data = memAlloc(pm.width * pm.height * 4 + 54);
        bmp(pm, data);

        PointerBuffer mimeTypes = memAllocPointer(1);
        ByteBuffer mimeType = memUTF8("image/bmp");
        mimeTypes.put(memAddress(mimeType));
        mimeTypes.flip();

        // Each copy gets its own callbacks capturing this generation's buffer. SDL synchronously
        // calls the PREVIOUS cleanup during SDL_SetClipboardData, so a shared static would clobber
        // the new buffer before SDL renders the image ("invalid bmp data").
        SDL_ClipboardDataCallback newDataCb = SDL_ClipboardDataCallback.create((userdata, mime, size) -> {
            memPutLong(size, data.remaining());
            return memAddress(data);
        });
        SDL_ClipboardCleanupCallback newCleanupCb = SDL_ClipboardCleanupCallback.create(userdata -> memFree(data));

        boolean ok = SDLClipboard.SDL_SetClipboardData(newDataCb, newCleanupCb, NULL, mimeTypes);
        memFree(mimeType);   // SDL copies the mime list, safe to free after the call
        memFree(mimeTypes);

        if(!ok){
            Log.err("SDL clipboard error: " + SDLError.SDL_GetError());
            memFree(data);
            newDataCb.free();
            newCleanupCb.free();
            return false;
        }

        // The previous generation's cleanup already ran inside SDL_SetClipboardData, so its
        // callbacks are unreferenced by SDL and safe to free.
        if(dataCb != null) dataCb.free();
        if(cleanupCb != null) cleanupCb.free();
        dataCb = newDataCb;
        cleanupCb = newCleanupCb;
        return true;
    }

    /** Encodes a pixmap as a 32bpp BMP file (BGRA, bottom-up) into the given buffer. SDL converts this to CF_DIB on Windows. */
    static void bmp(Pixmap pm, ByteBuffer out){
        int w = pm.width, h = pm.height, row = w * 4;
        out.order(ByteOrder.LITTLE_ENDIAN);
        out.put((byte)'B').put((byte)'M');
        out.putInt(54 + row * h); // file size
        out.putInt(0);            // reserved
        out.putInt(54);           // pixel data offset
        out.putInt(40);           // header size
        out.putInt(w);
        out.putInt(h);
        out.putShort((short)1);   // planes
        out.putShort((short)32);  // bpp
        out.putInt(0);            // BI_RGB
        out.putInt(row * h);      // image size
        out.putInt(0);            // x ppm
        out.putInt(0);            // y ppm
        out.putInt(0);            // colors used
        out.putInt(0);            // important colors

        pm.pixels.clear();
        for(int y = h - 1; y >= 0; y--){
            for(int x = 0; x < w; x++){
                int i = (y * w + x) * 4;
                out.put(pm.pixels.get(i + 2)); // B
                out.put(pm.pixels.get(i + 1)); // G
                out.put(pm.pixels.get(i));     // R
                out.put(pm.pixels.get(i + 3)); // A
            }
        }
        pm.pixels.clear();
        out.flip();
    }
}
