package nitis.imageMsch;

import arc.ApplicationListener;
import arc.Core;
import arc.files.Fi;
import arc.util.Log;
import mindustry.ui.dialogs.ExtSchematicsDialog;

import static mindustry.Vars.ui;

public class DropdownListener implements ApplicationListener{
    @Override
    public void fileDropped(Fi file){
        // GitHub#2 related:
        // looks like the links isn't treated like files
        // so we probably need to workaround with SDL directly to access drag-n-drop links
        // Log.info(file.absolutePath());
        if(!file.extEquals("png")) return;

        Core.app.post(() -> {
            try{
                ExtSchematicsDialog dialog;
                if(ui.schematics instanceof ExtSchematicsDialog d){
                    dialog = d;
                }else{
                    // self-heal if the swap didn't happen (e.g. another mod replaced the dialog)
                    dialog = new ExtSchematicsDialog();
                    ui.schematics = dialog;
                }
                dialog.importFromPngAndShow(file);
            }catch(Throwable e){
                Log.err("Failed to import image schematic", e);
                ui.showException("@save.import.invalid", e);
            }
        });
    }
}
