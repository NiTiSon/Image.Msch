package nitis.imageMsch;

import arc.util.Log;
import mindustry.ClientLauncher;
import org.lwjgl.sdl.SDL_DropEvent;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_EventFilter;
import org.lwjgl.sdl.SDLEvents;

/** Logs drag-n-dropped links (SDL_EVENT_DROP_TEXT), which arc does not surface as file drops.
 *  Reads the event through raw static accessors (no Struct wrappers) because this LWJGL fork's
 *  Struct.free() unconditionally frees the address, and SDL owns this event memory. */
public class LinkDropWatcher{
    private static SDL_EventFilter callback;

    public static void register(){
        if(callback != null) return;
        callback = SDL_EventFilter.create((userdata, event) -> {
            if(SDL_Event.ntype(event) == SDLEvents.SDL_EVENT_DROP_TEXT){
                String link = SDL_DropEvent.ndataString(event + SDL_Event.DROP);
                if(link != null){
                    ClientLauncher.runOnClientLoad(() -> {
                        // TODO?: ask user if they allow to access the internet
                        // TODO: handle image downloading
                    });
                    Log.info("Dropped link: @", link);
                }
            }
            return true;
        });
        SDLEvents.SDL_AddEventWatch(callback, 0);
    }
}
