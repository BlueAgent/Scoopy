package scoopy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Scoopy.MOD_ID,
        name = "Scoopy",
        version = Scoopy.MOD_VERSION,
        acceptedMinecraftVersions = Scoopy.MC_VERSION,
        acceptableRemoteVersions = "*",
        clientSideOnly = false,
        serverSideOnly = false,
        dependencies = Scoopy.DEPENDENCIES
)
@Mod.EventBusSubscriber
public class Scoopy {
    public static final String MOD_ID = "scoopy";
    public static final String MOD_VERSION = "99999.999.999";
    public static final String MC_VERSION = "";
    public static final String DEPENDENCIES = "";
    public static final boolean DEV_ENVIRONMENT = MOD_VERSION.equals("99999.999.999");

    public static Logger log = LogManager.getLogger(MOD_ID);

    @Instance(MOD_ID)
    public static Scoopy INSTANCE;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println(String.format("Pre-Init %s", MOD_ID));
        System.out.println(String.format("Version %s", MOD_VERSION));
        Scoopy.log = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println(String.format("Init %s", MOD_ID));
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        System.out.println(String.format("Post-Init %s", MOD_ID));
    }
}
