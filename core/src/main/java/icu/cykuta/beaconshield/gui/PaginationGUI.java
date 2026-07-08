package icu.cykuta.beaconshield.gui;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;

import java.util.List;

/**
 * A 45-slot GUI that paginates a list of entries inside a decorated frame.
 *
 * @param <T> type of the entries being listed.
 */
public abstract class PaginationGUI<T> extends GUI {
    protected static final List<Integer> CONTENT_SLOTS = List.of(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34);

    private static final int PREVIOUS_SLOT = 39;
    private static final int NEXT_SLOT = 41;

    private int page = 0;

    protected PaginationGUI(BeaconShieldBlock beacon, String titleKey) {
        super(beacon, titleKey, 45);
    }

    @Override
    protected void populate() {
        this.addDecoration(
                0,  1,  2,  3,  4,  5,  6,  7,  8,
                9,                              17,
                18,                             26,
                27,                             35,
                    37, 38,     40,     42, 43, 44
        );
        this.addControls();

        List<T> items = this.getItems();
        int pageSize = CONTENT_SLOTS.size();
        int maxPage = items.isEmpty() ? 0 : (items.size() - 1) / pageSize;
        this.page = Math.min(this.page, maxPage);

        if (this.page > 0) {
            this.addButton(PREVIOUS_SLOT, "global.previous", click -> this.showPage(this.page - 1));
        } else {
            this.addDecoration(PREVIOUS_SLOT);
        }

        if (this.page < maxPage) {
            this.addButton(NEXT_SLOT, "global.next", click -> this.showPage(this.page + 1));
        } else {
            this.addDecoration(NEXT_SLOT);
        }

        int start = this.page * pageSize;
        for (int i = 0; i < pageSize && start + i < items.size(); i++) {
            this.renderItem(CONTENT_SLOTS.get(i), items.get(start + i));
        }
    }

    private void showPage(int page) {
        this.page = page;
        this.refresh();
    }

    /**
     * Add the fixed buttons of this menu (back, add member, ...).
     * Slots 36 and 40 are free for subclasses to use.
     */
    protected void addControls() {
    }

    /**
     * Get the full list of entries to paginate.
     */
    protected abstract List<T> getItems();

    /**
     * Render one entry in the given slot.
     */
    protected abstract void renderItem(int slot, T item);
}
