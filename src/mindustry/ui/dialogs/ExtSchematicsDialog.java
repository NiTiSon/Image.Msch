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
import mindustry.ui.dialogs.*;
import mindustry.ui.FileChooser;
import nitis.imageMsch.ImageRenderer;
import nitis.imageMsch.PngMeta;

import java.io.*;

import static mindustry.Vars.*;

public class ExtSchematicsDialog extends SchematicsDialog{

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
                        setup();
                        ui.showInfoFade("@schematic.saved");
                        checkTags(s);
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
                                importFromPngAndShow(file);
                            }else{
                                Schematic s = Schematics.read(file);
                                s.removeSteamID();
                                schematics.add(s);
                                checkTags(s);
                                last = s;
                            }
                        }catch(Exception e){
                            ui.showException(e);
                        }
                    }

                    if(last != null){
                        showInfo(last);
                    }

                    setup();
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

    public void importFromPngAndShow(Fi file){
        try{
            String base64 = PngMeta.read(file);
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
}
