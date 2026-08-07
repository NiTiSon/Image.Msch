package nitis.imageMsch;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import mindustry.game.Schematic;
import mindustry.graphics.Pal;

import static mindustry.Vars.schematics;

/** Shared rasterization for the exported image: tiled background over the schematic preview. */
public class ImageRenderer{

    /** Tiles the schematics gallery background texture over the pixmap. */
    public static void tileBackground(Pixmap out){
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
            out.fill(Pal.lightishGray);
        }
    }

    /** Builds the full export image for a schematic: game preview over a tiled background.
      * Writes the base preview into {@code previewFile} first (rendered via the game's own pipeline),
      * then returns a new pixmap composited on top. Caller disposes the result. */
    public static Pixmap composite(Schematic s, Fi previewFile){
        schematics.savePreview(s, previewFile);

        Pixmap preview = new Pixmap(previewFile);
        Pixmap out = new Pixmap(preview.width, preview.height);
        try{
            tileBackground(out);
            out.draw(preview, 0, 0, true);
        }finally{
            preview.dispose();
        }
        return out;
    }

}
