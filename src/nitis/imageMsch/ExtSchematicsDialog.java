package nitis.imageMsch;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.scene.ui.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.layout.*;
import io.nayuki.qrcodegen.DataTooLongException;
import io.nayuki.qrcodegen.QrCode;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import java.io.*;

import static mindustry.Vars.*;

public class ExtSchematicsDialog extends SchematicsDialog{
    private static final int qrBackingAlpha = (int)(0xFF * 0.50f); // ~50% white panel behind the QR
    private static final int qrModuleAlpha = (int)(0xFF * 0.30f); // ~30% black squares

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
            schematics.savePreview(s, file);

            Pixmap preview = new Pixmap(file);
            Pixmap out = null;
            try{
                out = new Pixmap(preview.width, preview.height);
                tileBackground(out);
                out.draw(preview, 0, 0, true);
                drawQr(out, s);
                PixmapIO.writePng(file, out);
            }finally{
                preview.dispose();
                if(out != null) out.dispose();
            }

            //embed last, so the re-encoded PNG keeps the data chunk
            PngMeta.embed(file, schematics.writeBase64(s));
            ui.showInfoFade("@image-msch.export.done");
        }catch(Throwable e){
            ui.showException(e);
        }
    }

    /** Tiles the schematics gallery background texture over the pixmap. */
    private void tileBackground(Pixmap out){
        try{
            Pixmap bg = new Pixmap(Core.files.internal("sprites/schematic-background.png"));
            try{
                for(int x = 0; x < out.width; x += bg.width){
                    for(int y = 0; y < out.height; y += bg.height){
                        out.draw(bg, x, y);
                    }
                }
            }finally{
                bg.dispose();
            }
        }catch(Throwable ignore){
            //ponytail: no texture file (e.g. stripped assets), solid dark fallback
            out.fill(0x0A0A0AFF);
        }
    }

    /** Draws a semi-transparent QR code of the schematic data in the bottom-right corner. */
    private void drawQr(Pixmap out, Schematic s){
        QrCode qr;
        try{
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Schematics.write(s, bytes);
            qr = QrCode.encodeBinary(bytes.toByteArray(), QrCode.Ecc.LOW);
        }catch(DataTooLongException e){
            //ponytail: too big for one QR, use the name so there's still a decorative code
            qr = QrCode.encodeText(s.name(), QrCode.Ecc.LOW);
        }catch(IOException e){
            throw new RuntimeException(e);
        }

        int scale = Math.max(1, Math.min(out.width, out.height) / 4 / qr.size);
        int size = qr.size * scale;
        int margin = 10;

        Pixmap qp = new Pixmap(size, size);
        try{
            // translucent white panel first, so the QR reads as black-on-white over any art (no teal tint)
            qp.fill(0xFFFFFF00 | qrBackingAlpha);
            for(int y = 0; y < qr.size; y++){
                for(int x = 0; x < qr.size; x++){
                    if(qr.getModule(x, y)){
                        qp.fillRect(x * scale, y * scale, scale, scale, qrModuleAlpha);
                    }
                }
            }
            out.draw(qp, 0, 0, qp.width, qp.height, out.width - size - margin, out.height - size - margin, size, size, false, true);
        }finally{
            qp.dispose();
        }
    }
}
