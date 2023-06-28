package gregtech.common.tileentities.casings.upgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import gregtech.api.gui.modularui.GT_UITextures;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import gregtech.api.enums.GT_Values.NBT;
import gregtech.api.multitileentity.interfaces.IMultiBlockController;
import gregtech.api.multitileentity.multiblock.casing.UpgradeCasing;
import gregtech.api.net.GT_Packet_MultiTileEntity;

public class Inventory extends UpgradeCasing {

    public UUID inventoryID;
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int BOTH = 2;

    private String inventoryName = "inventory";
    private int inventorySize;
    private int type = INPUT;

    public String getCustomInventoryName() {
        return inventoryName;
    }

    public String getInventoryID() {
        return inventoryID.toString();
    }

    public void setInventoryName(String aInventoryName) {
        inventoryName = aInventoryName;
    }

    public int getType() {
        return type;
    }

    @Override
    protected void customWork(IMultiBlockController aTarget) {
        int tInvSize = inventorySize;
        if (type == BOTH) {
            tInvSize /= 2;
        }
        aTarget.registerInventory(inventoryName, inventoryID.toString(), tInvSize, type);
        if (isServerSide()) {
            issueClientUpdate();
        }
    }

    @Override
    public String getTileEntityName() {
        return "gt.multitileentity.multiblock.inventory";
    }

    @Override
    public void readMultiTileNBT(NBTTagCompound aNBT) {
        super.readMultiTileNBT(aNBT);
        if (aNBT.hasKey(NBT.UPGRADE_INVENTORY_UUID)) {
            inventoryID = UUID.fromString(aNBT.getString(NBT.UPGRADE_INVENTORY_UUID));
        } else {
            inventoryID = UUID.randomUUID();
        }
        if (aNBT.hasKey(NBT.UPGRADE_INVENTORY_NAME)) {
            inventoryName = aNBT.getString(NBT.UPGRADE_INVENTORY_NAME);
        } else {
            inventoryName = "inventory";
        }
        inventorySize = aNBT.getInteger(NBT.UPGRADE_INVENTORY_SIZE);
    }

    @Override
    public void writeMultiTileNBT(NBTTagCompound aNBT) {
        super.writeMultiTileNBT(aNBT);
        aNBT.setString(NBT.UPGRADE_INVENTORY_UUID, inventoryID.toString());
        aNBT.setString(NBT.UPGRADE_INVENTORY_NAME, inventoryName);
    }

    @Override
    public boolean breakBlock() {
        final IMultiBlockController controller = getTarget(false);
        if (controller != null) {
            controller.unregisterInventory(inventoryName, inventoryID.toString(), type);
        }
        return super.breakBlock();
    }

    @Override
    public boolean hasGui(ForgeDirection side) {
        return true;
    }

    @Override
    public String getLocalName() {
        return "Inventory Upgrade";
    }

    @Override
    public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
        builder.widget(
            new TextFieldWidget().setGetter(() -> inventoryName)
                .setSetter((val) -> {
                    inventoryName = val;
                    final IMultiBlockController controller = getTarget(false);
                    if (controller != null) {
                        controller.changeInventoryName(inventoryName, inventoryID.toString(), type);
                    }
                })
                .setTextColor(Color.WHITE.normal)
                .setTextAlignment(Alignment.CenterLeft)
                .setBackground(GT_UITextures.BACKGROUND_TEXT_FIELD)
                .addTooltip("Name")
                .setSize(100, 18)
                .setPos(25, 25));
        builder.widget(
            new ButtonWidget().setPlayClickSound(true)
                .setOnClick(
                    (clickData, widget) -> {
                        setInventoryMode(type == INPUT ? OUTPUT : INPUT);
                        widget.notifyTooltipChange();
                    })
                .setBackground(() -> {
                    List<UITexture> ret = new ArrayList<>();
                    if (type == INPUT) {
                        ret.add(GT_UITextures.BUTTON_STANDARD);

                    } else {
                        ret.add(GT_UITextures.BUTTON_STANDARD_PRESSED);
                    }
                    return ret.toArray(new IDrawable[0]);
                })
                .setSize(18, 18)
                .dynamicTooltip(() ->
                    Arrays.asList(
                        "Toggle Inventory Mode",
                        (type == INPUT ? "Input Mode" : "Output Mode")))
                .attachSyncer(new FakeSyncWidget.IntegerSyncer(() -> type, val -> {
                    setInventoryMode(val);
                }), builder)
                .setPos(140, 25));
    }

    private void setInventoryMode(int newType){
        int oldType = type;
        type = newType;

        final IMultiBlockController controller = getTarget(false);
        if (controller != null) {
            controller.unregisterInventory(inventoryName, inventoryID.toString(), oldType);
            controller.registerInventory(inventoryName, inventoryID.toString(), inventorySize, type);
        }
    }

    @Override
    protected boolean canOpenControllerGui() {
        return false;
    }

    @Override
    public GT_Packet_MultiTileEntity getClientDataPacket() {
        final GT_Packet_MultiTileEntity packet = super.getClientDataPacket();
        String name = getCustomInventoryName();
        packet.setInventoryName(name, inventoryID.toString());
        return packet;
    }

    @Override
    public void addToolTips(List<String> list, ItemStack stack, boolean f3_h) {
        super.addToolTips(list, stack, f3_h);
        list.add("Adds another item inventory");
        list.add("Inventory size: " + inventorySize);
    }

    public void setInventoryId(String inventoryID) {
        this.inventoryID = UUID.fromString(inventoryID);
    }
}
