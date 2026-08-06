package nitis.imageMsch;

// TODO: Need to add configuration
// Best way, if we can add additional button for mod page
public class ModConfiguration {
    /** When enabled, allow user to share schematics to any messengers
      * @apiNote This isn't required for Discord, as it does not compress user images. */
    public boolean addQRCode = false;
    /** Normalized transparency */
    public float opacity = 0.4f;
}
