package icu.cykuta.beaconshield.gui;

import java.util.List;

public abstract class PaginationGUI extends GUI {
    protected int offset = 0;
    protected final List<Integer> renderSlots = List.of(
            10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    public PaginationGUI(String titlePath) {
        super(titlePath, 45);
    }

    @Override
    public void populateInventory() {
        this.setDecorationSlots(
                0,  1,  2,  3,  4,  5,  6,  7,  8,
                9,                              17,
                18,                             26,
                27,                             35,
                37, 38,                 42, 43, 44
        );
        // Pagination arrows
        if (this.offset > 0) {
            this.addInventoryButton(39, "previous", (player) -> this.setOffset(this.offset - renderSlots.size()));
        } else {
            this.addDecorationSlot(39);
        }

        if (this.offset + renderSlots.size() < this.renderSlots.size()) {
            this.addInventoryButton(41, "next", (player) -> this.setOffset(this.offset + renderSlots.size()));
        } else {
            this.addDecorationSlot(41);
        }

        this.render();
    }

    /**
     * Pagination
     */
    private void setOffset(int offset) {
        this.offset = offset;
        this.populateInventory();
    }

    /**
     * Render player list as heads
     */
    protected abstract void render();
}
