package nitis.imageMsch;

import arc.files.Fi;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.ui.dialogs.SchematicsDialog;

import static mindustry.Vars.ui;
import static mindustry.Vars.schematics;

public class ExtSchematicsDialog extends SchematicsDialog{
    public void importFromPngAndShow(Fi file){
        try{
            // TODO: From image
            Schematic s = Schematics.read(file);
            s.removeSteamID();
            schematics.add(s);
            //checkTags(s); // inaccessible

            //setup(); // inaccessible
            showInfo(s);
        }catch(Exception e){
            ui.showException(e);
        }
    }
}
