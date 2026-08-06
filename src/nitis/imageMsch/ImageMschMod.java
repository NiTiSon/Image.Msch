package nitis.imageMsch;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import mindustry.ClientLauncher;
import mindustry.Vars;
import mindustry.game.EventType.ResizeEvent;
import mindustry.gen.Icon;
import mindustry.mod.Mod;
import mindustry.ui.fragments.MenuFragment.MenuButton;

public class ImageMschMod extends Mod{
    @Override
    public void init(){
        ClientLauncher.runOnClientLoad(() -> {
            if(!(Vars.ui.schematics instanceof ExtSchematicsDialog)){
                Vars.ui.schematics = new ExtSchematicsDialog();
            }

            // the main-menu "Schematics" button is a method reference bound to the original dialog
            // at UI construction time, so swapping Vars.ui.schematics never reaches it - repoint it
            repointMenuSchematicsButton();

            Core.app.addListener(new DropdownListener());
        });
    }

    /** Rebinds the main-menu Schematics button to the swapped dialog and rebuilds the menu. */
    private void repointMenuSchematicsButton(){
        if(Vars.ui.menufrag == null || Vars.ui.menufrag.desktopButtons == null) return;

        for(MenuButton b : Vars.ui.menufrag.desktopButtons){
            if(b.submenu == null) continue;
            Seq<MenuButton> subs = b.submenu;
            for(int i = 0; i < subs.size; i++){
                if(subs.get(i).text.equals("@schematics")){
                    subs.set(i, new MenuButton("@schematics", Icon.paste, Vars.ui.schematics::show));
                    Core.app.post(() -> Events.fire(new ResizeEvent()));
                    return;
                }
            }
        }
    }
}
