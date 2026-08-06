package nitis.imageMsch;

import arc.ApplicationListener;
import arc.Core;
import arc.files.Fi;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.Schematic;

import static mindustry.Vars.ui;

public class DropdownListener implements ApplicationListener {
    @Override
    public void fileDropped(Fi file) {
        if(file.extEquals("png")){
            handleImageMschImport(file);
        }
    }

    private void handleImageMschImport(Fi file){
        Core.app.post(() -> {
            try{
                if (ui.schematics instanceof ExtSchematicsDialog dialog){
                    Schematic schematic = dialog.importSchematic(file);
                    Vars.schematics.add(schematic);
                    dialog.showInfo(schematic);
                } else {
                    Log.err("For some reason image schematics dialog was not embed. This probably because of other mods.");
                }
            }catch(Throwable e){
                Log.err("Failed to import image schematic", e);
                ui.showException("@save.import.invalid", e);
            }
        });
    }
}
