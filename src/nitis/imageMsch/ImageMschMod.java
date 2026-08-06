package nitis.imageMsch;

import mindustry.ClientLauncher;
import mindustry.Vars;
import mindustry.mod.Mod;

public class ImageMschMod extends Mod{
    @Override
    public void init() {
        ClientLauncher.runOnClientLoad(() -> {
            if(!(Vars.ui.schematics instanceof ExtSchematicsDialog)){
                // Probably ain't best way of doing that, but at least stable and does not require bytecode modification.
                Vars.ui.schematics = new ExtSchematicsDialog();
            }

            // Register dropdown launcher.
            if(Vars.platform instanceof ClientLauncher launcher){
                launcher.add(new DropdownListener());
            }
        });
    }
}
