package nitis.imageMsch;

import arc.ApplicationListener;
import arc.Core;
import arc.files.Fi;
import arc.util.Log;
import mindustry.ui.dialogs.ExtSchematicsDialog;

import static mindustry.Vars.ui;

public class FileDropListener implements ApplicationListener{
    @Override
    public void fileDropped(Fi file){
        if(!file.extEquals("png")) return;

        Core.app.post(() -> {
            try{
                ImageMschMod.schematics.importFromAnyAndShow(file);
            }catch(Throwable e){
                Log.err("Failed to import image schematic", e);
                ui.showException("@save.import.invalid", e);
            }
        });
    }
}
