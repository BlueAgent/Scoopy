package scoopy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scoopy.common.CommonProxy;

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
    public static final String DEP_NUTRITION_ID = "nutrition";
    public static final String DEP_GREGTECH_ID = "gregtech";
    public static final boolean DEV_ENVIRONMENT = MOD_VERSION.equals("99999.999.999");

    public static Logger log = LogManager.getLogger(MOD_ID);

    @SidedProxy(clientSide = "scoopy.client.ClientProxy", serverSide = "scoopy.common.CommonProxy")
    public static CommonProxy proxy = null;

    @Instance(MOD_ID)
    public static Scoopy INSTANCE;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println(String.format("Pre-Init %s", MOD_ID));
        System.out.println(String.format("Version %s", MOD_VERSION));
        Scoopy.log = event.getModLog();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println(String.format("Init %s", MOD_ID));
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        System.out.println(String.format("Post-Init %s", MOD_ID));
        proxy.postInit(event);
    }
}
