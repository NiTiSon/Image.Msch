package nitis.imageMsch;

import arc.Core;
import arc.files.Fi;
import arc.util.Http;
import arc.util.Http.HttpStatus;
import arc.util.Log;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.ExtSchematicsDialog;

import static mindustry.Vars.ui;

/** Downloads drag-n-dropped image links (SDL_EVENT_DROP_TEXT), which arc does not surface as file drops.
 *  Reads the event through raw static accessors (no Struct wrappers) because this LWJGL fork's
 *  Struct.free() unconditionally frees the address, and SDL owns this event memory. */
public class SDL3LinkDropWatcher {
    private static org.lwjgl.sdl.SDL_EventFilter callback;

    public static void register(){
        if(callback != null) return;
        callback = org.lwjgl.sdl.SDL_EventFilter.create((userdata, event) -> {
            if(org.lwjgl.sdl.SDL_Event.ntype(event) == org.lwjgl.sdl.SDLEvents.SDL_EVENT_DROP_TEXT){
                String text = org.lwjgl.sdl.SDL_DropEvent.ndataString(event + org.lwjgl.sdl.SDL_Event.DROP);
                if(text == null){
                    return true;
                }

                if(text.startsWith("http://") || text.startsWith("https://")){
                    onLink(text);
                }
                if(text.startsWith("bXNj")){
                    onBase64(text);
                }
                if(text.startsWith("file:///")) {
                    onFileLink(text);
                }
            }
            return true;
        });
        org.lwjgl.sdl.SDLEvents.SDL_AddEventWatch(callback, 0);
    }

    private static void onBase64(String schematic){
        Core.app.post(() -> ImageMschMod.schematics.importFromBase64(schematic));
    }

    private static void onFileLink(String link){
        Core.app.post(() -> {
            try{
                Fi file = Core.files.absolute(link.substring("file:///".length()));
                importFrom(file);
            }catch(Throwable e){
                Log.err(e);
                ui.showException(e);
            }
        });
    }

    private static void onLink(String link){
        Core.app.post(() -> {
            BaseDialog dialog = new BaseDialog("@image-msch.link-drop-download.title");
            dialog.cont.add(Core.bundle.format("image-msch.link-drop-download", link)).width(500f).wrap().pad(4f);
            dialog.buttons.defaults().size(200f, 54f).pad(2f);
            dialog.buttons.button("@image-msch.download", Icon.download, () -> {
                dialog.hide();
                downloadAndImport(link);
            });
            dialog.buttons.button("@cancel", Icon.cancel, dialog::hide);
            dialog.show();
        });
    }

    private static void downloadAndImport(String link){
        Http.get(link, response -> {
            if(response.getStatus() != HttpStatus.OK){
                Core.app.post(() -> ui.showInfoFade(Core.bundle.format("image-msch.link-drop-download.error", link)));
                return;
            }
            byte[] bytes = response.getResult();

            Core.app.post(() -> {
                try{
                    Fi file = Core.files.local("saves/tmp/" + nameFrom(link, isPng(bytes) ? ".png" : ".msch"));
                    file.writeBytes(bytes);
                    importFrom(file);
                }catch(Throwable e){
                    Log.err("Failed to import dropped link: @", e, link);
                    ui.showException(e);
                }
            });
        }, error -> {
            Log.err("Failed to download dropped link", error, link);
            Core.app.post(() -> ui.showInfoFade(Core.bundle.get("image-msch.link-drop-download.error")));
        });
    }

    /** Looks up (or self-heals) the extended dialog and imports the file like a regular drop. */
    private static void importFrom(Fi file){
        ImageMschMod.schematics.importFromAnyAndShow(file);
    }

    private static boolean isPng(byte[] b){
        return b.length >= 4 && (b[0] & 0xff) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
    }

    private static String nameFrom(String link, String ext){
        String name = link.substring(link.lastIndexOf('/') + 1);
        return name.contains("?") || !name.endsWith(ext) ? "drop" + ext : name;
    }
}
