package mindustry.ui.dialogs;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.scene.ui.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.layout.*;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.FileChooser;
import nitis.imageMsch.ImageRenderer;
import nitis.imageMsch.PixelMeta;
import nitis.imageMsch.PngMeta;

import java.io.*;

import static mindustry.Vars.*;

public class ExtSchematicsDialog extends SchematicsDialog{

    /** Calls a package-private {@link SchematicsDialog} method reflectively: same-package access is denied
      * at runtime because the mod and the game load their classes in different classloaders. */
    private void invokeSchematics(String name, Class<?>[] types, Object... args){
        try{
            java.lang.reflect.Method m = SchematicsDialog.class.getDeclaredMethod(name, types);
            m.setAccessible(true);
            m.invoke(this, args);
        }catch(Throwable e){
            throw new RuntimeException(e);
        }
    }

    /** Same as vanilla, but the import button also accepts PNG images. */
    @Override
    public void showImport(){
        BaseDialog dialog = new BaseDialog("@editor.import");
        dialog.cont.pane(p -> {
            p.margin(10f);
            p.table(Tex.button, t -> {
                TextButtonStyle style = Styles.flatt;
                t.defaults().size(280f, 60f).left();
                t.row();
                t.button("@load.clipboard", Icon.copy, style, () -> {
                    dialog.hide();
                    try{
                        Schematic s = Schematics.readBase64(Core.app.getClipboardText());
                        s.removeSteamID();
                        schematics.add(s);
                        invokeSchematics("setup", new Class<?>[0]);
                        ui.showInfoFade("@schematic.saved");
                        invokeSchematics("checkTags", new Class[]{Schematic.class}, s);
                        showInfo(s);
                    }catch(Throwable e){
                        ui.showException(e);
                    }
                }).marginLeft(12f).disabled(b -> Core.app.getClipboardText() == null || !Core.app.getClipboardText().startsWith(schematicBaseStart));
                t.row();
                t.button("@import.file", Icon.download, style, () -> FileChooser.open(schematicExtension, "png").submitMulti(files -> {
                    dialog.hide();

                    Schematic last = null;

                    for(Fi file : files){
                        try{
                            if(file.extEquals("png")){
                                importFromAnyAndShow(file);
                            }else{
                                Schematic s = Schematics.read(file);
                                s.removeSteamID();
                                schematics.add(s);
                                invokeSchematics("checkTags", new Class[]{Schematic.class}, s);
                                last = s;
                            }
                        }catch(Exception e){
                            ui.showException(e);
                        }
                    }

                    if(last != null){
                        showInfo(last);
                    }

                    invokeSchematics("setup", new Class<?>[0]);
                })).marginLeft(12f);
                t.row();
                if(steam){
                    t.button("@workshop.browse", Icon.book, style, () -> {
                        dialog.hide();
                        platform.openWorkshop();
                    }).marginLeft(12f);
                }
            });
        });

        dialog.addCloseButton();
        dialog.show();
    }

    @Override
    public void showExport(Schematic s){
        BaseDialog dialog = new BaseDialog("@editor.export");
        dialog.cont.pane(p -> {
            p.margin(10f);
            p.table(Tex.button, t -> {
                TextButtonStyle style = Styles.flatt;
                t.defaults().size(280f, 60f).left();
                if(steam && !s.hasSteamID()){
                    t.button("@workshop.share", Icon.book, style, () -> platform.publish(s)).marginLeft(12f);
                    t.row();
                }
                t.button("@copy.clipboard", Icon.copy, style, () -> {
                    dialog.hide();
                    ui.showInfoFade("@copied");
                    Core.app.setClipboardText(schematics.writeBase64(s));
                }).marginLeft(12f);
                t.row();
                t.button("@export.file", Icon.export, style, () -> {
                    dialog.hide();
                    FileChooser.export(s.name(), schematicExtension, file -> Schematics.write(s, file));
                }).marginLeft(12f);
                t.row();
                t.button("@image-msch.export.image", Icon.image, style, () -> {
                    dialog.hide();
                    FileChooser.export(s.name(), "png", file -> writeImage(s, file));
                }).marginLeft(12f);
            });
        });

        dialog.addCloseButton();
        dialog.show();
    }

    /** Imports any of the supported formats by signature: PNG image, binary schematic, or base64 text. */
    public void importFromAnyAndShow(Fi file){
        try{
            byte[] head = new byte[4];
            if(file.length() >= 4) file.readBytes(head, 0, 4);

            if(isPng(head)){
                importFromPngAndShow(file);
            }else if(isSchematic(head)){
                Schematic s = Schematics.read(file);
                s.removeSteamID();
                schematics.add(s);
                invokeSchematics("checkTags", new Class[]{Schematic.class}, s);
                showInfo(s);
            }else if(file.length() <= 1_000_000 && startsWithBase64(file)){
                Schematic s = Schematics.readBase64(file.readString());
                s.removeSteamID();
                schematics.add(s);
                invokeSchematics("checkTags", new Class[]{Schematic.class}, s);
                showInfo(s);
            }else{
                ui.showInfo(Core.bundle.format("image-msch.invalid", file.name()));
            }
        }catch(Throwable e){
            ui.showException(e);
        }
    }

    private static boolean isPng(byte[] b){
        return b.length >= 4 && (b[0] & 0xff) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
    }

    private static boolean isSchematic(byte[] b){
        return b.length >= 4 && b[0] == 'm' && b[1] == 's' && b[2] == 'c' && b[3] == 'h';
    }

    private static boolean startsWithBase64(Fi file){
        try(DataInputStream in = new DataInputStream(file.read())){
            for(int i = 0; i < schematicBaseStart.length(); i++){
                if(in.read() != schematicBaseStart.charAt(i)) return false;
            }
            return true;
        }catch(Throwable e){
            return false;
        }
    }

    public void importFromPngAndShow(Fi file){
        try{
            String base64 = PngMeta.read(file);
            if(base64 == null){
                byte[] data = PixelMeta.read(file);
                if(data != null){
                    Schematic s = Schematics.read(new ByteArrayInputStream(data));
                    s.removeSteamID();
                    schematics.add(s);
                    showInfo(s);
                    return;
                }
            }

            if(base64 == null){
                ui.showInfo(Core.bundle.format("image-msch.invalid", file.name()));
                return;
            }
            Schematic s = Schematics.readBase64(base64);
            s.removeSteamID();
            schematics.add(s);
            showInfo(s);
        }catch(Throwable e){
            ui.showException(e);
        }
    }

    private void writeImage(Schematic s, Fi file){
        try{
            Pixmap out = ImageRenderer.composite(s, file);
            try{
                PixelMeta.embed(out, schematicsBytes(s));
                PixmapIO.writePng(file, out);
            }finally{
                out.dispose();
            }
            //embed last, so the re-encoded PNG keeps the data chunk
            PngMeta.embed(file, schematics.writeBase64(s));
            ui.showInfoFade("@image-msch.export.done");
        }catch(Throwable e){
            ui.showException(e);
        }
    }

    /** Raw schematic bytes, cheaper than the base64 used in the tEXt chunk. */
    private static byte[] schematicsBytes(Schematic s) throws IOException{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Schematics.write(s, bytes);
        return bytes.toByteArray();
    }
}
